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

package org.metal.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FormSchemaMethods {

  public static String of(Class<?> metalClz) {
    Method[] methods = metalClz.getMethods();
    Method formSchemaMethod = null;
    for (Method method : methods) {
      if (!Modifier.isStatic(method.getModifiers())) {
        continue;
      }

      if (method.getParameterCount() != 0) {
        continue;
      }

      if (method.getReturnType() != String.class) {
        continue;
      }

      Annotation[] annotations = method.getAnnotationsByType(FormSchemaMethod.class);
      if (annotations == null || annotations.length == 0) {
        continue;
      }
      formSchemaMethod = method;
      break;
    }

    if (formSchemaMethod == null) {
      return null;
    }
    String schema = null;
    try {
      schema = (String) formSchemaMethod.invoke(null);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } finally {
      return schema;
    }
  }
}
