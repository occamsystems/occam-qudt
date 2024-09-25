package com.occamsystems.qudt;

import java.util.Arrays;

/** Copyright (c) 2024 Occam Systems, Inc. */
public class DimensionVector {

  public static final DimensionVector DIMENSIONLESS = DimensionVector.builder().build();
  public static final String DIMENSIONLESS_NAME = "A0E0L0I0M0H0T0D1";
  public static final String DIMENSIONLESS_CODE = "D1";
  public static final String QKDV = "http://qudt.org/vocab/dimensionvector/";
  public static final String LOCAL_NAME_REGEX = "[AELIMHTD]";
  private final SmallFraction[] vector = new SmallFraction[7];
  public static final int amountOfSubstance = 0;
  public static final int electricCurrent = 1;
  public static final int length = 2;
  public static final int luminousIntensity = 3;
  public static final int mass = 4;
  public static final int thermodynamicTemperature = 5;
  public static final int time = 6;

  private static final char[] dimesionChars = "AELIMHTD".toCharArray();

  public static Builder builder() {
    return new Builder();
  }

  public DimensionVector(String uri) {
    String[] names = uri.split(QKDV);
    String localName = names[names.length - 1];
    String regex = LOCAL_NAME_REGEX;
    String[] split = localName.split(regex);
    int[] array = new int[14];
    if (split.length >= 9) {
      for (int i = 0; i < 7; i++) {
        String expStr = split[i + 1];
        String numStr = expStr.replaceAll("[(dot)(pt)]", ".");
        this.vector[i] = SmallFraction.approximate(Double.parseDouble(numStr));
      }
    } else {
      for (int i = 0; i < 7; i++) {
        this.vector[i] = SmallFraction.ZERO;
      }
    }
  }

  public DimensionVector(int[] numDenomArray) {
    if (numDenomArray.length != 7 && numDenomArray.length != 14) {
      throw new IllegalArgumentException(
          "Dimension vector int[] constructor must have length "
              + "7 for simple int values or "
              + "14 for fractional values.");
    }

    boolean hasDenoms = numDenomArray.length == 14;

    for (int i = 0; i < 7; i++) {
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

  public boolean dimensionless() {
    return this.isEmpty();
  }

  public boolean isEmpty() {
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
    SmallFraction[] scaled = new SmallFraction[7];
    for (int i = 0; i < this.vector.length; i++) {
      scaled[i] = SmallFraction.times(this.vector[i], value);
    }
    return new DimensionVector(scaled);
  }

  public String uri() {
    return QKDV + localName();
  }

  public String localName() {
    if (this.isEmpty()) {
      return DIMENSIONLESS_NAME;
    }

    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 7; i++) {
      b.append(dimesionChars[i]);
      b.append(vector[i].toDecimalString());
    }

    return b.append("D0").toString().replace(".", "dot");
  }

  public String indexCode() {
    StringBuilder b = new StringBuilder();

    for (int i = 0; i < 7; i++) {
      if (!vector[i].isZero()) {
        b.append(dimesionChars[i]);
        b.append(vector[i].toDecimalString());
      }
    }

    return b.isEmpty() ? DIMENSIONLESS_CODE : b.toString().replace(".", "dot").replace("-", "_");
  }

  public SmallFraction amount() {
    return this.vector[amountOfSubstance];
  }

  public SmallFraction current() {
    return this.vector[electricCurrent];
  }

  public SmallFraction length() {
    return this.vector[length];
  }

  public SmallFraction luminous() {
    return this.vector[luminousIntensity];
  }

  public SmallFraction mass() {
    return this.vector[mass];
  }

  public SmallFraction temperature() {
    return this.vector[thermodynamicTemperature];
  }

  public SmallFraction time() {
    return this.vector[time];
  }

  public SmallFraction dimensionlessExponent() {
    return this.dimensionless() ? SmallFraction.ONE : SmallFraction.ZERO;
  }

  public SmallFraction[] vector() {
    return this.vector;
  }

  public static boolean isSemanticUri(String uri) {
    String[] split = uri.split("[/#]");
    String localName = split[split.length - 1];
    return localName.split(LOCAL_NAME_REGEX).length == 9;
  }

  public static DimensionVector add(DimensionVector a, DimensionVector b) {
    SmallFraction[] scaled = new SmallFraction[7];
    for (int i = 0; i < 7; i++) {
      scaled[i] = SmallFraction.plus(a.vector[i], b.vector[i]);
    }
    return new DimensionVector(scaled);
  }

  @Override
  public String toString() {
    return "DimensionVector:" + this.indexCode();
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

  public boolean equivalent(DimensionVector other) {
    return other != null && Arrays.equals(vector, other.vector);
  }

  public static class Builder {
    private SmallFraction amountOfSubstance = SmallFraction.ZERO;
    private SmallFraction electricCurrent = SmallFraction.ZERO;
    private SmallFraction length = SmallFraction.ZERO;
    private SmallFraction luminousIntensity = SmallFraction.ZERO;
    private SmallFraction mass = SmallFraction.ZERO;
    private SmallFraction thermodynamicTemperature = SmallFraction.ZERO;
    private SmallFraction time = SmallFraction.ZERO;

    public Builder withAmountOfSubstance(int i) {
      return withAmountOfSubstance(new SmallFraction(i));
    }

    public Builder withAmountOfSubstance(SmallFraction amountOfSubstance) {
      this.amountOfSubstance = amountOfSubstance;
      return this;
    }

    public Builder withElectricCurrent(int i) {
      return withElectricCurrent(new SmallFraction(i));
    }

    public Builder withElectricCurrent(SmallFraction electricCurrent) {
      this.electricCurrent = electricCurrent;
      return this;
    }

    public Builder withLength(int i) {
      return withLength(new SmallFraction(i));
    }

    public Builder withLength(SmallFraction length) {
      this.length = length;
      return this;
    }

    public Builder withLuminousIntensity(int i) {
      return withLuminousIntensity(new SmallFraction(i));
    }

    public Builder withLuminousIntensity(SmallFraction luminousIntensity) {
      this.luminousIntensity = luminousIntensity;
      return this;
    }

    public Builder withMass(int i) {
      return withMass(new SmallFraction(i));
    }

    public Builder withMass(SmallFraction mass) {
      this.mass = mass;
      return this;
    }

    public Builder withThermodynamicTemperature(int i) {
      return withThermodynamicTemperature(new SmallFraction(i));
    }

    public Builder withThermodynamicTemperature(SmallFraction thermodynamicTemperature) {
      this.thermodynamicTemperature = thermodynamicTemperature;
      return this;
    }

    public Builder withTime(int i) {
      return withTime(new SmallFraction(i));
    }

    public Builder withTime(SmallFraction time) {
      this.time = time;
      return this;
    }

    public DimensionVector build() {
      return new DimensionVector(
          new SmallFraction[] {
            this.amountOfSubstance,
            this.electricCurrent,
            this.length,
            this.luminousIntensity,
            this.mass,
            this.thermodynamicTemperature,
            this.time
          });
    }
  }
}
