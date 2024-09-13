package com.occamsystems.qudt;

import java.util.Objects;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public abstract class Unit {
  abstract String label();
  abstract String symbol();
  abstract String ucumCode();
  abstract DimensionVector dv();

  abstract double conversionMultiplier();

  abstract double conversionOffset();

  boolean isConvertable(Unit other) {
    return this == other || this.dv().equals(other.dv());
  }

  /**
   * This converts a raw value into a scaled value.
   * For example, degF.scale(273.15) gives 32.
   */
  double scale(double value) {
    return value * this.conversionMultiplier() - conversionOffset();
  }

  /**
   * This converts a scaled value into a raw value.
   * For example, degF.unscale(32) gives 273.15.
   */
  double unscale(double value) {
    return (value + this.conversionOffset()) * this.conversionMultiplier();
  }

  public boolean equivalent(Unit other) {
    return other != null && this.dv().equals(other.dv())
        && this.conversionMultiplier() == other.conversionMultiplier()
        && this.conversionOffset() == other.conversionOffset();
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbol(), dv(), conversionMultiplier(), conversionOffset());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Unit && obj.hashCode() == this.hashCode();
  }
}
