package com.occamsystems.qudt;

import static org.junit.jupiter.api.Assertions.*;

import com.occamsystems.qudt.predefined.units.L1Units;
import com.occamsystems.qudt.predefined.units.L3Units;
import com.occamsystems.qudt.predefined.units.T_1Units;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
class UnitIndexTest {

  @Test
  void simpleUnits() {
    UnitIndex unitIndex = new UnitIndex();
    List<LiteralUnit> literalUnits = unitIndex.simpleUnits();

    Assertions.assertTrue(literalUnits.contains(L1Units.M.u));
    Assertions.assertTrue(literalUnits.contains(L1Units.KiloM.u));
    Assertions.assertTrue(literalUnits.contains(L3Units.L.u));
    Assertions.assertFalse(literalUnits.contains(L3Units.CentiM3.u));
    Assertions.assertTrue(literalUnits.contains(T_1Units.HZ.u));
    Assertions.assertFalse(literalUnits.contains(T_1Units.PER_SEC.u));

    System.out.println(literalUnits.stream().map(u -> u.symbol() + "\t:\t" + u.ucumCode()).distinct().collect(Collectors.joining("\n")));
  }
}