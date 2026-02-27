package com.occamsystems.qudt.jena;

import com.occamsystems.qudt.SmallFraction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public abstract class ModelUtils {

  public static final String SCHEMA_QUDT = "http://qudt.org/schema/qudt/";
  public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";

  private ModelUtils() {}

  static String orElse(Resource resource, Property t, String defaultValue) {
    Statement statement = resource.getProperty(t);

    if (statement != null) {
      return statement.getString();
    }

    return defaultValue;
  }

  static SmallFraction orElse(Resource resource, Property t, SmallFraction defaultValue) {
    Statement statement = resource.getProperty(t);

    if (statement != null) {
      return SmallFraction.approximate(statement.getDouble());
    }

    return defaultValue;
  }

  static double orElse(Resource resource, Property t, double defaultValue) {
    Statement statement = resource.getProperty(t);

    if (statement != null) {
      return statement.getDouble();
    }

    return defaultValue;
  }

  static Property rdfsProperty(Model model, String localName) {
    return property(model, RDFS, localName);
  }

  static Property qudtProperty(Model model, String localName) {
    return property(model, SCHEMA_QUDT, localName);
  }

  public static Property property(Model model, String schema, String localName) {
    Property property = model.getProperty(schema + localName);
    if (property == null) {
      return model.createProperty(schema, localName);
    } else {
      return property;
    }
  }

  public static String localName(String uri) {
    String[] split = uri.split("[/#]");
    String localName = split[split.length - 1];
    return localName;
  }

  public static void setLiteral(Resource r, Property p, Object o) {
    r.removeAll(p);
    r.addLiteral(p, o);
  }

  public static void setResources(Model m, Resource r, Property p, Iterable<String> uris) {
    r.removeAll(p);
    uris.forEach(uri -> r.addProperty(p, m.getResource(uri)));
  }

  public static void setResource(Model m, Resource r, Property p, String uri) {
    r.removeAll(p);
    r.addProperty(p, m.getResource(uri));
  }
}
