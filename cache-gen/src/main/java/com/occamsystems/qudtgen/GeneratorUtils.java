package com.occamsystems.qudtgen;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class GeneratorUtils {

  public static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  static final String HAS_VECTOR = "http://qudt.org/schema/qudt/hasDimensionVector";

  static double doubleOrElse(Resource res, Property conversionOffset, double v) {
    Statement stm = res.getProperty(conversionOffset);
    return stm == null ? v : stm.getDouble();
  }

  static String bestString(Resource res, Property prop) {
    Statement en = res.getProperty(prop, "en");
    if (en != null) {
      return en.getString().replace("\"","\\\"");
    }

    Statement any = res.getProperty(prop);
    if (any != null) {
      return any.getString().replace("\"","\\\"");
    }

    return "";
  }

  public static String toConstName(String base) {
    return base.replaceAll("([a-z\\d])([A-Z])", "$1_$2").replace("-","_").toUpperCase();
  }
}
