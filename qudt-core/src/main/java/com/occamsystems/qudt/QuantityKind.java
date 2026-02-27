package com.occamsystems.qudt;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public class QuantityKind {
  String uri;
  String label;
  DimensionVector dimensionVector;

  QuantityKind[] broaderKinds;

  public String label() {
    return label;
  }

  public DimensionVector dimensionVector() {
    return dimensionVector;
  }

  public QuantityKind[] broaderKinds() {
    return broaderKinds;
  }

  public QuantityKind(
      String label, String uri, DimensionVector dimensionVector, QuantityKind... broaderKinds) {
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
