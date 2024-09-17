package com.occamsystems.qudt;

/** Copyright (c) 2024 Occam Systems, Inc. */
public class LiteralUnit extends Unit {

  private String label;
  private String uri;
  private String symbol;
  private String ucumCode;
  private DimensionVector dv;
  private double conversionOffset;
  private double conversionMultiplier;

  public LiteralUnit(
      String label,
      String uri,
      String symbol,
      String ucumCode,
      DimensionVector dv,
      double conversionOffset,
      double conversionMultiplier) {
    this.label = label;
    this.uri = uri;
    this.symbol = symbol;
    this.ucumCode = ucumCode;
    this.dv = dv;
    this.conversionOffset = conversionOffset;
    this.conversionMultiplier = conversionMultiplier;
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
  public String ucumCode() {
    return this.ucumCode;
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
}
