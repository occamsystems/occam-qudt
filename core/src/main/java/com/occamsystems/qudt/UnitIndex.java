package com.occamsystems.qudt;

import com.occamsystems.qudt.predefined.Units;
import java.util.Arrays;
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
}
