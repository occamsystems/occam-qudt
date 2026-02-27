package com.occamsystems.qudtgen;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public class GeneratorUtils {

  public static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  static final String HAS_VECTOR = "http://qudt.org/schema/qudt/hasDimensionVector";
  static final String REPLACED_BY = "http://purl.org/dc/terms/isReplacedBy";

  static double doubleOrThrow(Resource res, Property conversionOffset) {
    Statement stm = res.getProperty(conversionOffset);
    assert stm != null;
    return stm.getDouble();
  }

  static double doubleOrElse(Resource res, Property conversionOffset, double v) {
    Statement stm = res.getProperty(conversionOffset);
    return stm == null ? v : stm.getDouble();
  }

  static String bestString(Resource res, Property prop) {
    Statement en = res.getProperty(prop, "en");
    if (en != null) {
      return en.getString().replace("\"", "\\\"");
    }

    Statement any = res.getProperty(prop);
    if (any != null) {
      return any.getString().replace("\"", "\\\"");
    }

    return "";
  }

  public static String toConstName(String base) {
    return base.replaceAll("([a-z\\d])([A-Z])", "$1_$2").replace("-", "_").toUpperCase();
  }

  public static String shortenVectorName(String longName) {
    String regex = "[AELIMHTD]";
    String[] split = longName.split(regex);

    if (split.length != 9) {
      return longName;
    }

    StringBuilder b = new StringBuilder();
    for (int i = 0; i < split.length; i++) {
      if (!split[i].isBlank() && !"0".equals(split[i])) {
        b.append(regex.charAt(i)).append(split[i].replace("-", "_").replace("pt", "dot"));
      }
    }

    if (b.isEmpty()) {
      return longName;
    } else {
      return b.toString();
    }
  }
}
