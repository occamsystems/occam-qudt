package com.occamsystems.qudt.jena;

import com.occamsystems.qudt.QuantityKind;
import com.occamsystems.qudt.predefined.DimensionVectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
class QuantityKindIOTest {
  @Test
  void io() {
    QuantityKindIO quantityKindIO = new QuantityKindIO();
    Model defaultModel = ModelFactory.createDefaultModel();

    QuantityKind kindA =
        new QuantityKind("A", "http://occamsystems.com/test#qkA", DimensionVectors.L2M1);
    String bUri = "http://occamsystems.com/test#qkB";
    QuantityKind kindB = new QuantityKind("B", bUri, DimensionVectors.L2M1, kindA);

    quantityKindIO.write(defaultModel, kindA);
    Resource write = quantityKindIO.write(defaultModel, kindB);

    QuantityKind read = quantityKindIO.read(defaultModel, bUri);

    Assertions.assertEquals(write.getURI(), read.uri());
    Assertions.assertEquals(1, read.broaderKinds().length);
    Assertions.assertTrue(DimensionVectors.L2M1.equivalent(read.dimensionVector()));
    Assertions.assertEquals("B", read.label());
  }
}
