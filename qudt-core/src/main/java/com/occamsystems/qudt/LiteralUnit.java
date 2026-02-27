package com.occamsystems.qudt;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public class LiteralUnit extends Unit {

  private String label;
  private String uri;
  private String symbol;
  private DimensionVector dv;
  private double conversionOffset;
  private double conversionMultiplier;
  private QuantityKind[] quantityKinds;

  public LiteralUnit(
      String label,
      String uri,
      String symbol,
      DimensionVector dv,
      double conversionOffset,
      double conversionMultiplier,
      QuantityKind... quantityKinds) {
    this.label = label;
    this.uri = uri;
    this.symbol = symbol;
    this.dv = dv;
    this.conversionOffset = conversionOffset;
    this.conversionMultiplier = conversionMultiplier;
    this.quantityKinds = quantityKinds;
  }

  public String uri() {
    if (this.uri.startsWith("http")) {
      return this.uri;
    } else {
      return "http://qudt.org/vocab/unit/" + this.uri;
    }
  }

  @Override
  public String label() {
    return this.label;
  }

  @Override
  public String symbol() {
    return this.symbol;
  }

  @Override
  public DimensionVector dv() {
    return this.dv;
  }

  @Override
  public double conversionMultiplier() {
    return this.conversionMultiplier;
  }

  @Override
  public double conversionOffset() {
    return this.conversionOffset;
  }

  public QuantityKind[] quantityKinds() {
    return quantityKinds;
  }
}
