package com.occamsystems;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class CacheDimensionVectors {
  public static final String VECTOR_VOCAB = "https://qudt.org/2.1/vocab/dimensionvector";
  public static final String DEFINED_BY = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";

  public void run() {
    Model model = ModelFactory.createDefaultModel();
    model.read(VECTOR_VOCAB);

    Property definedBy = model.createProperty(DEFINED_BY);

    model.listSubjectsWithProperty(definedBy,"http://qudt.org/2.1/vocab/dimensionvector");
  }
}
