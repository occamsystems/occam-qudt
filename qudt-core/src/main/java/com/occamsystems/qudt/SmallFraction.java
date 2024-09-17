package com.occamsystems.qudt;

import java.text.NumberFormat;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Copyright (c) 2024 Occam Systems, Inc.
 *
 * <p>This class handles rational numbers with short numerator and denominator values. This is
 * intended to be used by dimension vectors, whose denominators are normally 1, sometimes 2, and may
 * rarely go as high as 4. Similarly, numerators above 16 essentially never happen.
 */
public class SmallFraction extends Number {

  public static SmallFraction ZERO = new SmallFraction(0, 1);
  public static SmallFraction ONE = new SmallFraction(1);
  public static SmallFraction NEG_ONE = new SmallFraction(-1);
  private int num;
  private int denom;

  public static final NumberFormat NF;

  static {
    NF = NumberFormat.getNumberInstance();
    NF.setMaximumFractionDigits(3);
    NF.setMinimumFractionDigits(0);
  }

  public SmallFraction(int value) {
    this(value, 1);
  }

  public SmallFraction(int num, int denom) {
    this.num = num;
    this.denom = denom;
  }

  /**
   * Rounds the value to a small fraction if it is approximately equal to such a value. Denominators
   * of approximate fractions will always be 8 or less. If no nearby small fraction is found, the
   * value will be rounded to the nearest integer.
   */
  public static SmallFraction approximate(double val) {
    for (int i = 1; i < 9; i++) {
      double vi = val * i;
      if (Math.abs(vi - Math.round(vi)) < 0.01) {
        return new SmallFraction((int) Math.round(vi), i);
      }
    }

    return new SmallFraction((int) val);
  }

  public int intValue() {
    return num / denom;
  }

  public long longValue() {
    return intValue();
  }

  public float floatValue() {
    return ((float) (num)) / ((float) (denom));
  }

  public double doubleValue() {
    return floatValue();
  }

  /**
   * Reduces this fraction, then returns itself.
   *
   * @return This fraction, reduced.
   */
  public SmallFraction reduce() {
    int gcd = gcd(Math.abs(this.num), Math.abs(this.denom));
    this.num = this.num / gcd;
    this.denom = this.denom / gcd;

    if (this.denom < 0) {
      this.num *= -1;
      this.denom *= -1;
    }

    return this;
  }

  private static int gcd(int a, int b) {
    return b == 0 ? a : gcd(b, a % b);
  }

  public static SmallFraction plus(SmallFraction f1, int f2) {
    if (f1.denom == 1) {
      return new SmallFraction(f1.num + f2);
    } else {
      return new SmallFraction(f1.num + f2 * f1.denom, f1.denom);
    }
  }

  public static SmallFraction plus(SmallFraction f1, SmallFraction f2) {
    if (f1.denom == f2.denom) {
      return new SmallFraction(f1.num + f2.num, f1.denom);
    } else {
      return new SmallFraction(f1.num * f2.denom + f2.num * f1.denom, f1.denom * f2.denom);
    }
  }

  public static SmallFraction minus(SmallFraction f1, SmallFraction f2) {
    if (f1.denom == f2.denom) {
      return new SmallFraction(f1.num - f2.num, f1.denom);
    } else {
      return new SmallFraction(f1.num * f2.denom - f2.num * f1.denom, f1.denom * f2.denom);
    }
  }

  public static SmallFraction times(SmallFraction f, SmallFraction mult) {
    return new SmallFraction(f.num * mult.num, f.denom * mult.denom);
  }

  public static SmallFraction divided(SmallFraction f, SmallFraction div) {
    return new SmallFraction(f.num * div.denom, f.denom * div.num);
  }

  public static SmallFraction times(SmallFraction f, int mult) {
    return new SmallFraction(f.num * mult, f.denom);
  }

  public static SmallFraction divided(SmallFraction f, int div) {
    return new SmallFraction(f.num, f.denom * div);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SmallFraction that = (SmallFraction) o;
    return doubleValue() == that.doubleValue();
  }

  @Override
  public int hashCode() {
    return Objects.hash(doubleValue());
  }

  @Override
  public String toString() {
    if (denom == 1) {
      return String.valueOf(num);
    }

    return num + "/" + denom;
  }

  public String toDecimalString() {
    return NF.format(this.floatValue());
  }

  public boolean isZero() {
    return num == 0;
  }

  public boolean isOne() {
    return num == denom;
  }

  public String encodeReduced() {
    this.reduce();
    int i = Math.abs(num) + 8 * denom + (this.num < 0 ? 128 : 0);

    if (i > 255) {
      Logger.getLogger(this.getClass().getName()).warning("BAD ENCODING for " + this);
    }

    return Integer.toHexString(i);
  }
}
