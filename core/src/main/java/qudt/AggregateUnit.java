package qudt;

import static qudt.DimensionVector.DIMENSIONLESS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class AggregateUnit implements Unit{
  Map<LiteralUnit, SmallFraction> components;

  public AggregateUnit(Unit unit, int i) {
    this(unit, new SmallFraction(i));
  }

  public AggregateUnit(Unit unit, SmallFraction i) {
    if (unit instanceof LiteralUnit lu) {
      components = Map.of(lu, i);
    } else if (unit instanceof AggregateUnit agg) {
      components = new HashMap<>(agg.components.size());

      agg.components.forEach((lu, lui) -> components.put(lu, SmallFraction.times(lui, i).reduce()));
    }
  }

  public AggregateUnit(Unit unit, int i, Unit unit1, int i1) {
    components = new HashMap<>(5);

    if (unit instanceof LiteralUnit lu) {
      components.put(lu, new SmallFraction(i));
    } else if (unit instanceof AggregateUnit agg) {
      agg.components.forEach((lu, lui) -> components.put(lu, SmallFraction.times(lui, i).reduce()));
    }

    if (unit1 instanceof LiteralUnit lu) {
      components.computeIfPresent(lu, (u, prev) -> SmallFraction.plus(prev, i1));
      components.computeIfAbsent(lu, u -> new SmallFraction(i1));
    } else if (unit instanceof AggregateUnit agg) {
      agg.components.forEach((lu, lui) -> {
        SmallFraction sf = SmallFraction.times(lui, i1).reduce();
        components.computeIfPresent(lu, (u, prev) -> SmallFraction.plus(prev, sf));
        components.putIfAbsent(lu, sf);
      });
    }

    List<LiteralUnit> zeroes = this.components.entrySet().stream()
        .filter(e -> e.getValue().isZero())
        .map(Entry::getKey)
        .toList();

    zeroes.forEach(this.components::remove);
  }

  @Override
  public String label() {
    return null;
  }

  @Override
  public String symbol() {
    return null;
  }

  @Override
  public String ucumCode() {
    return null;
  }

  @Override
  public DimensionVector dv() {
    return this.components.entrySet()
        .stream()
        .map(entry -> entry.getKey().dv().scaledBy(entry.getValue()))
        .reduce(DIMENSIONLESS, DimensionVector::add);
  }

  @Override
  public double conversionMultiplier() {
    return this.components.entrySet()
        .stream()
        .mapToDouble(e -> Math.pow(e.getKey().conversionMultiplier(), e.getValue().doubleValue()))
        .reduce(1.0, (a, b) -> a * b);
  }

  @Override
  public double conversionOffset() {
    if (this.dv().unary()) {
      this.components.entrySet()
          .stream()
          .mapToDouble(e -> e.getKey().conversionMultiplier())
          .findAny().orElse(0.);
    }

    return 0;
  }
}
