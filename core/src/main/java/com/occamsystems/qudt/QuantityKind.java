package com.occamsystems.qudt;

import java.util.List;

/**
 * Copyright (c)  2024 Occam Systems, Inc.
 */
public class QuantityKind {
  String uri;
  String label;
  DimensionVector dimensionVector;

  QuantityKind[] broaderKinds;
  public QuantityKind(String label, String uri, DimensionVector dimensionVector,
      QuantityKind... broaderKinds) {
    this.uri = uri;
    this.label = label;
    this.dimensionVector = dimensionVector;
    this.broaderKinds = broaderKinds;
  }

  public String uri() {
    if (this.uri.startsWith("http")) {
      return this.uri;
    } else {
      return "http://qudt.org/vocab/unit/" + this.uri;
    }
  }
}
