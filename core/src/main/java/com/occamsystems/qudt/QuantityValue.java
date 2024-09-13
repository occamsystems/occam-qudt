package com.occamsystems.qudt;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class QuantityValue {
  double unscaled;
  Unit unit;

  public static QuantityValue ofScaled(double value, Unit unit) {
    return new QuantityValue(unit.unscale(value), unit);
  }

  public static QuantityValue ofUnscaled(double value, Unit unit) {
    return new QuantityValue(value, unit);
  }

   private QuantityValue(double unscaled, Unit unit) {
    this.unscaled = unscaled;
    this.unit = unit;
  }

  public double value() {
    return this.unit.scale(unscaled);
  }

  public static QuantityValue divide(QuantityValue qv1, QuantityValue qv2) {
    return new QuantityValue(qv1.unscaled / qv2.unscaled,
        new AggregateUnit(qv1.unit, 1, qv2.unit, -1));
  }

  public static QuantityValue add(QuantityValue qv1, QuantityValue qv2) {
    assert qv1.unit.isConvertable(qv2.unit);

    return new QuantityValue(qv1.unscaled + qv2.unscaled,
        qv1.unit);
  }


  public static QuantityValue subtract(QuantityValue qv1, QuantityValue qv2) {
    assert qv1.unit.isConvertable(qv2.unit);

    return new QuantityValue(qv1.unscaled - qv2.unscaled,
        qv1.unit);
  }

  public static QuantityValue pow(QuantityValue qv1, int pow) {
    return new QuantityValue(Math.pow(qv1.unscaled, pow),
        new AggregateUnit(qv1.unit, pow));
  }

  public static QuantityValue pow(QuantityValue qv1, SmallFraction pow) {
    return new QuantityValue(Math.pow(qv1.unscaled, pow.doubleValue()),
        new AggregateUnit(qv1.unit, pow));
  }

  public static QuantityValue pow(QuantityValue qv1, int powNum, int powDenom) {
    return QuantityValue.pow(qv1, new SmallFraction(powNum, powDenom));
  }

  public static QuantityValue multiply(QuantityValue qv1, QuantityValue qv2) {
    return new QuantityValue(qv1.unscaled * qv2.unscaled,
        new AggregateUnit(qv1.unit, 1, qv2.unit, 1));
  }

}
