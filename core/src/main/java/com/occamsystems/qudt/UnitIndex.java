package com.occamsystems.qudt;

import com.occamsystems.qudt.predefined.Units;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class UnitIndex {

  public static final Predicate<String> UCUM_COMPOSITE = Pattern.compile(".*[0-9³⁴√·\\./_-].*").asMatchPredicate();
  private List<LiteralUnit> simpleUnits = null;

  /**
   * Returns a list of units that are neither powers nor explicit compositions of other units.
   * For example, N and km are simple units, while m/s and m2 are not.
   */
  List<LiteralUnit> simpleUnits() {
    if (simpleUnits == null) {
      simpleUnits = Units.byDV.values().stream().flatMap(Arrays::stream)
          .filter(unit -> !UCUM_COMPOSITE.test(unit.ucumCode()) &&
              !UCUM_COMPOSITE.test(unit.symbol()))
          .toList();
    }

    return simpleUnits;
  }

  /**
   * If the unit is not already a LiteralUnit, returns the best matched unit from among predefined
   * units.
   * Match quality is defined as similarity of conversion multiplier, then symbolic concision.
   * (e.g., L is preferred to dm3.)
   * This will be null if and only if the dimension vector of the supplied unit has no
   * predefined units.
   */
  LiteralUnit bestPredefinedMatch(Unit base) {
    if (base instanceof LiteralUnit lu) {
      return lu;
    }

    LiteralUnit[] matches = Units.byDV.getOrDefault(base.dv().indexCode(), new LiteralUnit[]{});

    double baseConvLog = Math.log(base.conversionMultiplier());
    return Arrays.stream(matches)
        .sorted(Comparator.comparing(u -> Math.abs(Math.log(((Unit) u).conversionMultiplier()) - baseConvLog))
        .thenComparing(u -> ((Unit) u).symbol().length()))
        .findFirst().orElse(null);
  }
}
