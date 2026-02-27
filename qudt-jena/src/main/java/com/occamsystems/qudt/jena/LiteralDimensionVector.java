package com.occamsystems.qudt.jena;

import com.occamsystems.qudt.DimensionVector;
import com.occamsystems.qudt.SmallFraction;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public class LiteralDimensionVector extends DimensionVector {

  private String uri;
  private String label;

  /** This constructor assumes a semantic URI. Use the other constructor if this is not the case. */
  public LiteralDimensionVector(String uri) {
    super(uri);
    this.uri(uri);
  }

  public LiteralDimensionVector(SmallFraction[] smallFractions) {
    super(smallFractions);
  }

  @Override
  public String uri() {
    if (this.uri == null) {
      return super.uri();
    } else {
      return uri;
    }
  }

  public LiteralDimensionVector uri(String uri) {
    this.uri = uri;
    return this;
  }

  public String label() {
    if (this.label == null) {
      return localName();
    }

    return label;
  }

  public LiteralDimensionVector label(String label) {
    this.label = label;
    return this;
  }
}
