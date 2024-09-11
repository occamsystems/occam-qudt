package com.occamsystems.qudt;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class LiteralUnit extends Unit {

  private String label;
  private String uri;
  private String symbol;
  private String ucumCode;
  private DimensionVector dv;
  private double conversionOffset;
  private double conversionMultiplier;

  public String uri() {
    return this.uri;
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

  public static final LiteralUnit UNITLESS = new LiteralUnit() {
    @Override
    public String label() {
      return "Unitless";
    }

    @Override
    public String symbol() {
      return "";
    }

    @Override
    public String ucumCode() {
      return "";
    }

    @Override
    public DimensionVector dv() {
      return DimensionVector.DIMENSIONLESS;
    }

    @Override
    public double conversionMultiplier() {
      return 1;
    }

    @Override
    public double conversionOffset() {
      return 0;
    }
  };
}
