package com.occamsystems.qudt;

import com.occamsystems.qudt.predefined.units.A1Units;
import com.occamsystems.qudt.predefined.units.H1Units;
import com.occamsystems.qudt.predefined.units.L1M1T_2Units;
import com.occamsystems.qudt.predefined.units.L1M1Units;
import com.occamsystems.qudt.predefined.units.L1Units;
import com.occamsystems.qudt.predefined.units.L3Units;
import com.occamsystems.qudt.predefined.units.L5Units;
import com.occamsystems.qudt.predefined.units.M1Units;
import com.occamsystems.qudt.predefined.units.T1Units;
import com.occamsystems.qudt.predefined.units.T_1Units;
import java.time.Instant;
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
    Assertions.assertTrue(literalUnits.contains(L1Units.YD.u));
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
    String symbol = kgms_2.symbol();
    Assertions.assertTrue(symbol.contains("s⁻²"));
    Assertions.assertTrue(symbol.contains("kg"));
    Assertions.assertTrue(symbol.contains("m"));
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

    QuantityValue neg70m3 = unitIndex.parseQuantity("-70 m3");
    Assertions.assertEquals(-70, neg70m3.value());
    Assertions.assertEquals(L3Units.M3.u, neg70m3.unit());

    QuantityValue perSec = unitIndex.parseQuantity("10/s");
    Assertions.assertEquals(10, perSec.value());
    Assertions.assertEquals(T_1Units.PER_SEC.u, perSec.unit());

    QuantityValue hour = unitIndex.parseQuantity("10 h");
    Assertions.assertEquals(10, hour.value());
    Assertions.assertEquals(T1Units.HR.u, hour.unit());

    QuantityValue withComma = unitIndex.parseQuantity("7,500 g");
    Assertions.assertEquals(7500, withComma.value());
    Assertions.assertEquals(M1Units.GM.u, withComma.unit());
  }

  @Test
  void simplifyUnit() {
    UnitIndex index = new UnitIndex();
    AggregateUnit aggM10 = new AggregateUnit(L5Units.M5.u, 2);
    LiteralUnit litM10 = index.demandExactLiteral(aggM10, "http://occamsystems.com/test#");
    Assertions.assertEquals("m¹⁰", litM10.symbol());
    LiteralUnit m10 = index.demandExactLiteral("m10", "http://occamsystems.com/test#");
    Assertions.assertEquals(m10, litM10);
  }

  @Test
  void evaluateDemandLiteralPerformance() {
    UnitIndex index = new UnitIndex();
    long l = Double.doubleToLongBits(Math.random());
    List<LiteralUnit> literalUnits = index.units().toList();
    int size = literalUnits.size();
    LiteralUnit[] l1 = new LiteralUnit[29];
    for (int i = 0; i < 29; i++) {
      l1[i] = literalUnits.get((int) (size * Math.random()));
    }
    LiteralUnit[] l2 = new LiteralUnit[31];
    for (int i = 0; i < 31; i++) {
      l2[i] = literalUnits.get((int) (size * Math.random()));
    }

    AggregateUnit[] rawAggs = new AggregateUnit[8192];

    Instant now = Instant.now();

    for (int i = 0; i < 8192; i++) {
      int li = 4 * (i % 16);
      int l1Exp = (int) (((l >> li) & 3) + 1);
      int l2Exp = (int) (((l >> li + 2) & 3) + 1);
      rawAggs[i] = new AggregateUnit(l1[i % 29], l1Exp, l2[i % 31], l2Exp);
    }

    long time = Instant.now().toEpochMilli() - now.toEpochMilli();
    System.out.println("Created 8192 random aggregate pairs in " + time + "ms");

    LiteralUnit[] finals = new LiteralUnit[8192];
    index.simpleUnits();

    now = Instant.now();
    for (int i = 0; i < 8192; i++) {
      finals[i] = index.demandExactLiteral(rawAggs[i], "http://occamsystems.com/test#");
    }
    time = Instant.now().toEpochMilli() - now.toEpochMilli();
    System.out.println("Found exact literals for 8192 random aggregate pairs in " + time + "ms");
    System.out.println((int) (((double) time) / 8.192) + "us per literal");

    Assertions.assertTrue(time < 400, "Demanding units has gotten really slow.");
  }
}
