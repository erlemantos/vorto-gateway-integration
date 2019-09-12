/**
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.vorto.codegen.fim;

import java.util.Arrays;
import java.util.List;
import org.eclipse.vorto.core.api.model.datatype.ObjectPropertyType;
import org.eclipse.vorto.core.api.model.datatype.PrimitivePropertyType;
import org.eclipse.vorto.core.api.model.datatype.PrimitiveType;
import org.eclipse.vorto.core.api.model.datatype.Property;
import org.eclipse.vorto.core.api.model.datatype.PropertyType;
import org.eclipse.vorto.core.api.model.informationmodel.FunctionblockProperty;

public class Utils {

  private static final List<String> keywords =
      Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
          "class", "const", "continue", "default", "do", "double", "else", "extends", "false",
          "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
          "int", "interface", "long", "native", "new", "null", "package", "private", "protected",
          "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
          "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while");

  public static String mapSimpleDatatype(PropertyType type) {
    if (type instanceof ObjectPropertyType) {
      return "String";
    } else {
      PrimitivePropertyType primitiveType = (PrimitivePropertyType) type;
      if (primitiveType.getType().compareTo(PrimitiveType.STRING) == 0) {
        return "String";
      } else if (primitiveType.getType().compareTo(PrimitiveType.DATETIME) == 0) {
        return "java.util.Date";
      } else {
        return primitiveType.getType().getLiteral();
      }
    }
  }
  
  public static String getPropertyName(Property property) {
    return checkKeyword(property.getName());
  }
  
  public static String getFbPropertyName(FunctionblockProperty property) {
    return checkKeyword(property.getName());
  }

  public static String checkKeyword(String name) {
    return keywords.contains(name) ? name + "_" : name;
  }
}
