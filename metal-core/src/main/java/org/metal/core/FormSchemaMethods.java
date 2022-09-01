package org.metal.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FormSchemaMethods {
  public static String of(Class<?> metalClz) {
    Method[] methods = metalClz.getMethods();
    Method formSchemaMethod = null;
    for (Method method: methods) {
      if(!Modifier.isStatic(method.getModifiers())) {
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
