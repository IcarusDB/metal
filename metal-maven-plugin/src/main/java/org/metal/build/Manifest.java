package org.metal.build;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javassist.tools.reflect.Reflection;
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
    for(String compiled : compiledElements) {
      try {
        urls.add(new File(compiled).toURI().toURL());
      } catch (MalformedURLException e) {
        getLog().error(e);
        throw new MojoExecutionException(e);
      }
    }

    URL[] urlArray  = urls.toArray(URL[]::new);
    URLClassLoader classLoader = new URLClassLoader(urlArray);

    List<Scanner> scanners = new ArrayList<>();
    for (String pkg: this.packages) {
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

    String sourceClz = "org.metal.core.MSource";
    extractSchema(classLoader, reflections, sourceClz);
    String mapperClz = "org.metal.core.MMapper";
    extractSchema(classLoader, reflections, mapperClz);
  }

  private void extractSchema(URLClassLoader classLoader, Reflections reflections, String clz) {
    Set<Class<?>> subClzs = reflections.get(
        Scanners.SubTypes.of(clz).asClass(classLoader)
    );

    List<String> clzs = new ArrayList<>();
    for (Class<?> subClz : subClzs) {
      if(excludedPkgs(subClz)) {
        continue;
      }
      clzs.add(subClz.getName());
      getLog().info(subClz.getName());
      ObjectMapper mapper = new ObjectMapper();
      JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
      try {
        JsonSchema schema = generator.generateSchema(subClz);
        getLog().info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
      } catch (JsonMappingException e) {
        e.printStackTrace();
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

    }
  }

  private boolean excludedPkgs(Class<?> clz) {
    String pkg = clz.getPackageName();
    for(String excludedPkg: this.excludedPackages) {
      if (pkg.equals(excludedPkg) || pkg.startsWith(excludedPkg + ".")) {
        return true;
      }
    }
    return false;
  }
}
