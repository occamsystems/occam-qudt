package com.occamsystems.qudt;

import com.occamsystems.qudt.predefined.units.D1Units;
import java.util.Objects;

/** Copyright (c) 2024 Occam Systems, Inc. */
public class QuantityValue implements Comparable<QuantityValue> {

  private static double EPSILON = 0.00001;
  double unscaled;
  Unit unit;

  /**
   * Sets the default precision for equivalence checks. The same default is applied for both ratio
   * and absolute equivalence functions. Has a default value of 0.00001.
   */
  public static void epsilon(double epsilon) {
    assert epsilon > 0 && epsilon < 1;

    EPSILON = epsilon;
  }

  public static QuantityValue ofNumber(double value) {
    return new QuantityValue(value, D1Units.NUM.u);
  }

  public static QuantityValue ofScaled(double value, Unit unit) {
    return new QuantityValue(unit.unscale(value), unit);
  }

  public static QuantityValue ofUnscaled(double value, Unit unit) {
    return new QuantityValue(value, unit);
  }

  protected QuantityValue(double unscaled, Unit unit) {
    this.unscaled = unscaled;
    this.unit = unit;
  }

  public double unscaled() {
    return unscaled;
  }

  public Unit unit() {
    return unit;
  }

  public double value() {
    return this.unit.scale(unscaled);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue plus(QuantityValue other) {
    return QuantityValue.add(this, other);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue minus(QuantityValue other) {
    return QuantityValue.subtract(this, other);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue times(QuantityValue other) {
    return QuantityValue.multiply(this, other);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue times(double other) {
    return QuantityValue.multiply(this, other);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue dividedBy(QuantityValue other) {
    return QuantityValue.divide(this, other);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue dividedBy(double other) {
    return QuantityValue.divide(this, other);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue toPower(int pow) {
    return QuantityValue.pow(this, pow);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue toPower(SmallFraction pow) {
    return QuantityValue.pow(this, pow);
  }

  /**
   * Creates a new QuantityValue by applying the specified operation.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue toPower(int powNum, int powDenom) {
    return QuantityValue.pow(this, new SmallFraction(powNum, powDenom));
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue plusMut(QuantityValue other) {
    assert this.unit.isConvertible(other.unit);
    this.unscaled += other.unscaled;
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue minusMut(QuantityValue other) {
    assert this.unit.isConvertible(other.unit);
    this.unscaled -= other.unscaled;
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue timesMut(QuantityValue other) {
    this.unscaled *= other.unscaled;
    this.unit = new AggregateUnit(this.unit, 1, other.unit, 1);
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue timesMut(double other) {
    this.unscaled *= other;
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue dividedByMut(QuantityValue other) {
    this.unscaled /= other.unscaled;
    this.unit = new AggregateUnit(this.unit, 1, other.unit, -1);
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue dividedByMut(double other) {
    this.unscaled /= other;
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue toPowerMut(int pow) {
    this.unscaled = Math.pow(this.unscaled, pow);
    this.unit = new AggregateUnit(this.unit, pow);
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue toPowerMut(SmallFraction pow) {
    this.unscaled = Math.pow(this.unscaled, pow.doubleValue());
    this.unit = new AggregateUnit(this.unit, pow);
    return this;
  }

  /**
   * Mutates this QuantityValue by applying the specified operation.
   *
   * @return This QuantityValue with an updated value.
   */
  public QuantityValue toPowerMut(int powNum, int powDenom) {
    return this.toPowerMut(new SmallFraction(powNum, powDenom));
  }

  /**
   * Creates a new QuantityValue equivalent to this, but in the specified unit.
   *
   * @return A new QuantityValue.
   */
  public QuantityValue inUnit(Unit unit) {
    return QuantityValue.converted(this, unit);
  }

  /**
   * Mutates this QuantityValue by setting its unit.
   *
   * @return This QuantityValue with an updated unit.
   */
  public QuantityValue inUnitMut(Unit unit) {
    assert this.unit().isConvertible(unit);
    this.unit = unit;
    return this;
  }

  /**
   * Checks whether the absolute difference of quantities is within the default epsilon. Note that
   * this is not strict equality and may not be transitive if epsilon is large relative to the
   * distribution of values.
   */
  public static boolean equivalentAbsolute(QuantityValue qv, QuantityValue qv2) {
    return equivalentAbsolute(qv, qv2, EPSILON);
  }

  /**
   * Checks whether the absolute difference of quantities is within a given epsilon. Note that this
   * is not strict equality and may not be transitive if epsilon is large relative to the
   * distribution of values.
   */
  public static boolean equivalentAbsolute(QuantityValue qv, QuantityValue qv2, double epsilon) {
    return qv == qv2
        || (qv.unit.isConvertible(qv2.unit) && Math.abs(qv.unscaled - qv2.unscaled) < epsilon);
  }

  /**
   * Checks whether the ratio difference of quantities is within the default epsilon. Note that this
   * is not strict equality and may not be transitive if epsilon is large relative to the
   * distribution of values.
   */
  public static boolean equivalentRatio(QuantityValue qv, QuantityValue qv2) {
    return equivalentRatio(qv, qv2, EPSILON);
  }

  /**
   * Checks whether the ratio difference of quantities is within a given epsilon. Note that this is
   * not strict equality and may not be transitive if epsilon is large relative to the distribution
   * of values.
   */
  public static boolean equivalentRatio(QuantityValue qv, QuantityValue qv2, double epsilon) {
    return qv == qv2
        || (qv.unit.isConvertible(qv2.unit)
            && Math.abs(1 - (qv.unscaled / qv2.unscaled)) < epsilon);
  }

  public static QuantityValue converted(QuantityValue qv, Unit u) {
    assert qv.unit().isConvertible(u);

    return QuantityValue.ofUnscaled(qv.unscaled, u);
  }

  public static QuantityValue divide(QuantityValue qv1, QuantityValue qv2) {
    return new QuantityValue(
        qv1.unscaled / qv2.unscaled, new AggregateUnit(qv1.unit, 1, qv2.unit, -1));
  }

  public static QuantityValue divide(QuantityValue qv1, double d) {
    return new QuantityValue(qv1.unscaled / d, qv1.unit);
  }

  public static QuantityValue add(QuantityValue qv1, QuantityValue qv2) {
    if (qv1.unscaled == 0.) {
      return qv2;
    }

    if (qv2.unscaled == 0.) {
      return qv1;
    }

    assert qv1.unit.isConvertible(qv2.unit);

    return new QuantityValue(qv1.unscaled + qv2.unscaled, qv1.unit);
  }

  public static QuantityValue subtract(QuantityValue qv1, QuantityValue qv2) {
    if (qv1.unscaled == 0.) {
      return QuantityValue.ofUnscaled(-qv2.value(), qv2.unit());
    }

    if (qv2.unscaled == 0.) {
      return qv1;
    }

    assert qv1.unit.isConvertible(qv2.unit);

    return new QuantityValue(qv1.unscaled - qv2.unscaled, qv1.unit);
  }

  public static QuantityValue pow(QuantityValue qv1, int pow) {
    return new QuantityValue(Math.pow(qv1.unscaled, pow), new AggregateUnit(qv1.unit, pow));
  }

  public static QuantityValue pow(QuantityValue qv1, SmallFraction pow) {
    return new QuantityValue(
        Math.pow(qv1.unscaled, pow.doubleValue()), new AggregateUnit(qv1.unit, pow));
  }

  public static QuantityValue pow(QuantityValue qv1, int powNum, int powDenom) {
    return QuantityValue.pow(qv1, new SmallFraction(powNum, powDenom));
  }

  public static QuantityValue multiply(QuantityValue qv1, double d) {
    return new QuantityValue(qv1.unscaled * d, qv1.unit);
  }

  public static QuantityValue multiply(QuantityValue qv1, QuantityValue qv2) {
    return new QuantityValue(
        qv1.unscaled * qv2.unscaled, new AggregateUnit(qv1.unit, 1, qv2.unit, 1));
  }

  public static double compareRatio(QuantityValue qv1, QuantityValue qv2) {
    return QuantityValue.equivalentRatio(qv1, qv2) ? 0 : qv1.compareTo(qv2);
  }

  public static double compareAbsolute(QuantityValue qv1, QuantityValue qv2) {
    return QuantityValue.equivalentAbsolute(qv1, qv2) ? 0 : qv1.compareTo(qv2);
  }

  @Override
  public String toString() {
    return "%s %s".formatted(value(), unit);
  }

  @Override
  public int compareTo(QuantityValue o) {
    if (this.unit.isConvertible(o.unit)) {
      return Double.compare(this.unscaled, o.unscaled);
    }

    throw new IllegalArgumentException("Cannot compare " + this + " to " + o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QuantityValue that = (QuantityValue) o;
    return that.unscaled == unscaled && unit.isConvertible(that.unit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unscaled, unit.dv().hashCode());
  }
}
