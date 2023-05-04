/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.build;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

@Mojo(name = "manifest", requiresDependencyResolution = ResolutionScope.COMPILE)
public class Manifest extends AbstractMojo {

  @Component
  private MavenProject project;

  @Parameter(property = "manifest.packages")
  private Set<String> packages;

  @Parameter(property = "manifest.excluded.packages")
  private Set<String> excludedPackages;

  @Parameter(property = "manifest.file", defaultValue = "manifest.json")
  private String manifestPath;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String artifactId = project.getArtifactId();
    String groupId = project.getGroupId();
    String version = project.getVersion();

    List<String> compiledElements;
    try {
      compiledElements = project.getCompileClasspathElements();
    } catch (DependencyResolutionRequiredException e) {
      getLog().error(e);
      throw new MojoExecutionException(e);
    }

    List<URL> urls = new ArrayList<>();
    for (String compiled : compiledElements) {
      try {
        urls.add(new File(compiled).toURI().toURL());
      } catch (MalformedURLException e) {
        getLog().error(e);
        throw new MojoExecutionException(e);
      }
    }

    URL[] urlArray = urls.toArray(URL[]::new);
    URLClassLoader classLoader = new URLClassLoader(urlArray);

    List<Scanner> scanners = new ArrayList<>();
    for (String pkg : this.packages) {
      Scanner scanner = Scanners.SubTypes.filterResultsBy(
          new FilterBuilder().includePackage(pkg)
      );
      scanners.add(scanner);
    }

    Reflections reflections = new Reflections(
        new ConfigurationBuilder()
            .addUrls(urls)
            .forPackages(this.packages.toArray(String[]::new))
            .addScanners(scanners.toArray(Scanner[]::new))
    );

    Class<?> formOfClz = null;
    try {
      formOfClz = classLoader.loadClass("org.metal.core.FormSchemaMethods");
    } catch (ClassNotFoundException e) {
      getLog().error(e);
      throw new MojoFailureException(e);
    }
    Map<String, String> metalTypes = new HashMap<>();
    metalTypes.put("sources", "org.metal.core.MSource");
    metalTypes.put("mappers", "org.metal.core.MMapper");
    metalTypes.put("fusions", "org.metal.core.MFusion");
    metalTypes.put("sinks", "org.metal.core.MSink");
    metalTypes.put("setups", "org.metal.backend.ISetup");

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode manifest = objectMapper.createObjectNode();

    for (Map.Entry<String, String> entry : metalTypes.entrySet()) {
      String type = entry.getKey();
      String clz = entry.getValue();
      ArrayNode pkgs = objectMapper.createArrayNode();
      extractSchema(
          groupId, artifactId, version,
          pkgs,
          classLoader, reflections, clz, formOfClz
      );
      manifest.putIfAbsent(type, pkgs);
    }

    String manifestPretty = manifest.toPrettyString();
    getLog().info(manifestPretty);
    try {
      dump(manifestPretty);
    } catch (IOException e) {
      getLog().error("Fail to dump mainfest.");
      getLog().error(e);
    }
  }

  private void extractSchema(
      String groupId, String artifactId, String version,
      ArrayNode metalPkgs,
      URLClassLoader classLoader,
      Reflections reflections,
      String clz,
      Class<?> formOfClz)
      throws MojoFailureException {
    Set<Class<?>> subClzs = reflections.get(
        Scanners.SubTypes.of(clz).asClass(classLoader)
    );
    for (Class<?> subClz : subClzs) {
      Method formOf = null;
      for (Method method : formOfClz.getMethods()) {
        if (!method.getName().equals("of")) {
          continue;
        }
        if (!Modifier.isStatic(method.getModifiers())) {
          continue;
        }
        if (method.getParameterCount() != 1) {
          continue;
        }
        if (method.getReturnType() != String.class) {
          continue;
        }

        try {
          getLog().info(subClz.getName());
          String schema = (String) method.invoke(null, subClz);
          if (schema == null) {
            getLog().warn("[PASS] " + subClz.getName());
            continue;
          }
          getLog().info(schema);
          ObjectMapper mapper = new ObjectMapper();
          JsonNode schemaNode = mapper.readTree(schema);
          ObjectNode metalPkg = mapper.createObjectNode();
          metalPkg.put("pkg", groupId + ":" + artifactId + ":" + version);
          metalPkg.put("class", subClz.getName());

          JsonNode formSchema = schemaNode.get("formSchema");
          JsonNode uiSchema = schemaNode.get("uiSchema");
          JsonNode description = schemaNode.get("description");

          if (formSchema == null) {
            getLog().error("formSchema is lost in " + schema);
            getLog().warn("[PASS] " + subClz.getName());
            continue;
          }
          metalPkg.putIfAbsent("formSchema", formSchema);
          if (uiSchema != null) {
            metalPkg.putIfAbsent("uiSchema", uiSchema);
          }
          if (description != null) {
            metalPkg.putIfAbsent("description", description);
          }
          metalPkgs.add(metalPkg);
        } catch (IllegalAccessException e) {
          getLog().error(e);
          throw new MojoFailureException(e);
        } catch (InvocationTargetException e) {
          getLog().error(e);
          throw new MojoFailureException(e);
        } catch (JsonMappingException e) {
          getLog().error(e);
          throw new MojoFailureException(e);
        } catch (JsonProcessingException e) {
          getLog().error(e);
          throw new MojoFailureException(e);
        }
      }
    }
  }

  private boolean excludedPkgs(Class<?> clz) {
    String pkg = clz.getPackageName();
    for (String excludedPkg : this.excludedPackages) {
      if (pkg.equals(excludedPkg) || pkg.startsWith(excludedPkg + ".")) {
        return true;
      }
    }
    return false;
  }

  private void dump(String manifest) throws IOException {
    File baseDir = project.getBasedir();
    Path path = Path.of(baseDir.getPath() + File.separator + manifestPath);
    Files.createDirectories(path.getParent());
    path.toFile().createNewFile();

    FileWriter writer = new FileWriter(path.toFile());
    writer.write(manifest);
    writer.close();
    getLog().info("Manifest dump into " + path.toString());
  }
}
