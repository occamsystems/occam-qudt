package com.occamsystems.qudt;

import com.occamsystems.qudt.predefined.units.A1Units;
import com.occamsystems.qudt.predefined.units.H1Units;
import com.occamsystems.qudt.predefined.units.L1M1T_2Units;
import com.occamsystems.qudt.predefined.units.L1M1Units;
import com.occamsystems.qudt.predefined.units.L1Units;
import com.occamsystems.qudt.predefined.units.L3Units;
import com.occamsystems.qudt.predefined.units.T_1Units;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Copyright (c) 2024 Occam Systems, Inc. */
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
  }

  @Test
  void bestPredefinedMatch() {
    UnitIndex unitIndex = new UnitIndex();
    Assertions.assertEquals(L3Units.DeciM3.u, unitIndex.bestPredefinedMatch(L3Units.DeciM3.u));
    Assertions.assertEquals(
        L3Units.L.u, unitIndex.bestPredefinedMatch(new AggregateUnit(L1Units.DeciM.u, 3)));
    Assertions.assertEquals(
        L3Units.L.u,
        unitIndex.bestPredefinedMatch(new AggregateUnit(L1Units.M.u, 2, L1Units.MilliM.u, 1)));
    Assertions.assertEquals(
        L1Units.DeciM.u,
        unitIndex.bestPredefinedMatch(new AggregateUnit(L3Units.L.u, new SmallFraction(1, 3))));
  }

  @Test
  void simpleSymbolMap() {
    UnitIndex unitIndex = new UnitIndex();
    Map<String, LiteralUnit> map = unitIndex.simpleSymbolMap();

    Assertions.assertEquals(L1Units.MicroM.u, map.get("um"));
    Assertions.assertEquals(H1Units.DEG_C.u, map.get("degC"));
  }

  @Test
  void predefinedSymbol() {
    UnitIndex unitIndex = new UnitIndex();
    Assertions.assertEquals(L3Units.L.u, unitIndex.predefinedUnitBySymbol("L"));
    Assertions.assertEquals(L1Units.M.u, unitIndex.predefinedUnitBySymbol("m"));
    Assertions.assertEquals(L1M1Units.KiloGM_M.u, unitIndex.predefinedUnitBySymbol("kg*m"));
    Assertions.assertEquals(
        L1M1T_2Units.KiloGM_M_PER_SEC2.u, unitIndex.predefinedUnitBySymbol("kg*m/s2"));
  }

  @Test
  void parse() {
    UnitIndex unitIndex = new UnitIndex();
    Assertions.assertTrue(L3Units.L.u.equivalent(unitIndex.parseAsAggregateUnit("L")));
    Assertions.assertTrue(L1Units.M.u.equivalent(unitIndex.parseAsAggregateUnit("m")));
    Assertions.assertTrue(L1M1Units.KiloGM_M.u.equivalent(unitIndex.parseAsAggregateUnit("kg*m")));
    AggregateUnit kgms_2 = unitIndex.parseAsAggregateUnit("kg*m/s2");
    Assertions.assertTrue(L1M1T_2Units.KiloGM_M_PER_SEC2.u.equivalent(kgms_2));
    Assertions.assertEquals("kg⋅m⋅s⁻²", kgms_2.symbol());
  }

  @Test
  void parseQuantity() {
    UnitIndex unitIndex = new UnitIndex();
    QuantityValue quantityValue = unitIndex.parseQuantity("18.3 kN");
    Assertions.assertEquals(L1M1T_2Units.KiloN.u, quantityValue.unit);
    Assertions.assertEquals(18.3, quantityValue.value());
    Assertions.assertEquals(18300, quantityValue.unscaled);

    quantityValue = unitIndex.parseQuantity("3e5mL/mol/K");
    AggregateUnit builtUnit =
        new AggregateUnit(
            Map.of(
                L3Units.MilliL.u,
                SmallFraction.ONE,
                A1Units.MOL.u,
                SmallFraction.NEG_ONE,
                H1Units.K.u,
                SmallFraction.NEG_ONE));

    Assertions.assertEquals(builtUnit, quantityValue.unit);
    Assertions.assertEquals(3e5, quantityValue.value());
    Assertions.assertEquals(0.3, quantityValue.unscaled);
  }
}
