package com.occamsystems.qudt;

import java.util.Arrays;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class DimensionVector {

  public static final DimensionVector DIMENSIONLESS = DimensionVector.builder().build();
  private final SmallFraction[] vector = new SmallFraction[8];
  public static final int amountOfSubstance = 0;
  public static final int electricCurrent = 1;
  public static final int length = 2;
  public static final int luminousIntensity = 3;
  public static final int mass = 4;
  public static final int thermodynamicTemperature = 5;
  public static final int time = 6;
  public static final int dimensionless = 7;

  public static Builder builder() {
    return new Builder();
  }

  public DimensionVector(int[] numDenomArray) {
    if (numDenomArray.length != 8 && numDenomArray.length != 16) {
      throw new IllegalArgumentException("Dimension vector int[] constructor must have length "
          + "8 for simple int values or "
          + "16 for fractional values.");
    }

    boolean hasDenoms = numDenomArray.length == 16;

    for (int i = 0; i < 8; i++) {
      if (hasDenoms) {
        this.vector[i] = new SmallFraction(numDenomArray[2 * i], numDenomArray[2 * i + 1]);
      } else {
        this.vector[i] = new SmallFraction(numDenomArray[i]);
      }
    }
  }

  public DimensionVector(SmallFraction[] smallFractions) {
    for (int i = 0; i < smallFractions.length; i++) {
      this.vector[i] = smallFractions[i];
    }
  }

  public boolean empty() {
    for (int i = 0; i < vector.length; i++) {
      if (!vector[i].isZero()) {
        return false;
      }
    }
    return true;
  }

  public boolean unary() {
    boolean foundOne = false;
    for (int i = 0; i < vector.length; i++) {
      if (vector[i].isOne()) {
        if (foundOne) {
          return false;
        } else {
          foundOne = true;
        }
      } else if (!vector[i].isZero()) {
        return false;
      }
    }
    return false;
  }

  public DimensionVector scaledBy(SmallFraction value) {
    SmallFraction[] scaled = new SmallFraction[8];
    for (int i = 0; i < this.vector.length; i++) {
        scaled[i] = SmallFraction.times(this.vector[i], value);
    }
    return new DimensionVector(scaled);
  }

  public static DimensionVector add(DimensionVector a, DimensionVector b) {
    SmallFraction[] scaled = new SmallFraction[7];
    for (int i = 0; i < 7; i++) {
      scaled[i] = SmallFraction.plus(a.vector[i], b.vector[i]);
    }
    return new DimensionVector(scaled);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DimensionVector that = (DimensionVector) o;
    return Arrays.equals(vector, that.vector);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(vector);
  }

  public static class Builder {
    private SmallFraction amountOfSubstance = SmallFraction.ZERO;
    private SmallFraction electricCurrent = SmallFraction.ZERO;
    private SmallFraction length = SmallFraction.ZERO;
    private SmallFraction luminousIntensity = SmallFraction.ZERO;
    private SmallFraction mass = SmallFraction.ZERO;
    private SmallFraction thermodynamicTemperature = SmallFraction.ZERO;
    private SmallFraction time = SmallFraction.ZERO;
    private SmallFraction dimensionless = SmallFraction.ZERO;

    public Builder withAmountOfSubstance(SmallFraction amountOfSubstance) {
      this.amountOfSubstance = amountOfSubstance;
      return this;
    }

    public Builder withElectricCurrent(SmallFraction electricCurrent) {
      this.electricCurrent = electricCurrent;
      return this;
    }

    public Builder withLength(SmallFraction length) {
      this.length = length;
      return this;
    }

    public Builder withLuminousIntensity(SmallFraction luminousIntensity) {
      this.luminousIntensity = luminousIntensity;
      return this;
    }

    public Builder withMass(SmallFraction mass) {
      this.mass = mass;
      return this;
    }

    public Builder withThermodynamicTemperature(SmallFraction thermodynamicTemperature) {
      this.thermodynamicTemperature = thermodynamicTemperature;
      return this;
    }

    public Builder withTime(SmallFraction time) {
      this.time = time;
      return this;
    }

    public Builder withDimensionless(SmallFraction dimensionless) {
      this.dimensionless = dimensionless;
      return this;
    }

    public DimensionVector build() {
      return new DimensionVector(new SmallFraction[]{this.amountOfSubstance,
      this.electricCurrent,
      this.length,
      this.luminousIntensity,
      this.mass,
      this.thermodynamicTemperature,
      this.time,
      this.dimensionless});
    }
  }
}
