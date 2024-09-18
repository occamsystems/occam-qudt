package com.occamsystems.qudt;

import java.util.Objects;

/** Copyright (c) 2024 Occam Systems, Inc. */
public abstract class Unit {
  public abstract String label();

  public abstract String symbol();

  public abstract String ucumCode();

  public abstract DimensionVector dv();

  public abstract double conversionMultiplier();

  public abstract double conversionOffset();

  public boolean isBasicNumber() {
    return this.conversionMultiplier() == 1. && this.dv().dimensionless();
  }

  public boolean isNumber() {
    return this.dv().dimensionless();
  }

  public boolean isConvertible(Unit other) {
    return this == other || this.dv().equals(other.dv());
  }

  /** This converts a raw value into a scaled value. For example, degF.scale(273.15) gives 32. */
  public double scale(double value) {
    return value / this.conversionMultiplier() - conversionOffset();
  }

  /** This converts a scaled value into a raw value. For example, degF.unscale(32) gives 273.15. */
  public double unscale(double value) {
    return (value + this.conversionOffset()) * this.conversionMultiplier();
  }

  public boolean equivalent(Unit other) {
    return other != null
        && this.dv().equals(other.dv())
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

  @Override
  public String toString() {
    return symbol();
  }
}
