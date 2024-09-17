package com.occamsystems.qudt;

import static org.junit.jupiter.api.Assertions.*;

import com.occamsystems.qudt.predefined.units.L1T_1Units;
import com.occamsystems.qudt.predefined.units.L1Units;
import com.occamsystems.qudt.predefined.units.L2Units;
import com.occamsystems.qudt.predefined.units.L_2Units;
import com.occamsystems.qudt.predefined.units.T1Units;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** */
class QuantityValueTest {

  @Test
  void divide() {
    QuantityValue qv1 = QuantityValue.ofScaled(4, L1Units.M.u);
    QuantityValue qv2 = QuantityValue.ofScaled(2, T1Units.SEC.u);
    QuantityValue qv3 = QuantityValue.ofScaled(2, L1T_1Units.M_PER_SEC.u);

    Assertions.assertEquals(0, qv3.compareTo(QuantityValue.divide(qv1, qv2)));
  }

  @Test
  void add() {
    QuantityValue qv1 = QuantityValue.ofScaled(4, L1Units.M.u);
    QuantityValue qv2 = QuantityValue.ofScaled(3000, L1Units.MilliM.u);
    Assertions.assertEquals(QuantityValue.ofScaled(7, L1Units.M.u), QuantityValue.add(qv1, qv2));
  }

  @Test
  void subtract() {
    QuantityValue qv1 = QuantityValue.ofScaled(4, L1Units.M.u);
    QuantityValue qv2 = QuantityValue.ofScaled(3000, L1Units.MilliM.u);
    Assertions.assertEquals(
        QuantityValue.ofScaled(1, L1Units.M.u), QuantityValue.subtract(qv1, qv2));
  }

  @Test
  void multiply() {
    QuantityValue qv1 = QuantityValue.ofScaled(4, L1Units.M.u);
    QuantityValue qv2 = QuantityValue.ofScaled(3000, L1Units.MilliM.u);
    Assertions.assertEquals(
        QuantityValue.ofScaled(12, L2Units.M2.u), QuantityValue.multiply(qv1, qv2));
  }

  @Test
  void pow() {
    QuantityValue qv1 = QuantityValue.ofScaled(4, L1Units.M.u);
    Assertions.assertEquals(QuantityValue.ofScaled(16, L2Units.M2.u), QuantityValue.pow(qv1, 2));

    QuantityValue qv2 = QuantityValue.ofScaled(1. / 16., L_2Units.PER_M2.u);
    Assertions.assertEquals(qv1, QuantityValue.pow(qv2, -1, 2));
  }

  @Test
  void compareTo() {
    QuantityValue qv1 = QuantityValue.ofScaled(4, L1Units.M.u);
    QuantityValue qv2 = QuantityValue.ofScaled(2, T1Units.SEC.u);
    Assertions.assertThrows(IllegalArgumentException.class, () -> qv1.compareTo(qv2));

    QuantityValue qv3 = QuantityValue.ofScaled(200, L1Units.MilliM.u);
    Assertions.assertEquals(1, qv1.compareTo(qv3));
    Assertions.assertEquals(-1, qv3.compareTo(qv1));
  }
}
