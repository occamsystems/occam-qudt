package com.occamsystems.qudt;

import static org.junit.jupiter.api.Assertions.*;

import com.occamsystems.qudt.predefined.units.D1Units;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
class UnitTest {
  @Test
  void numbers() {
    Assertions.assertTrue(D1Units.UNITLESS.u.isNumber());
    Assertions.assertTrue(D1Units.UNITLESS.u.isBasicNumber());
    Assertions.assertTrue(D1Units.PERCENT.u.isNumber());
    Assertions.assertFalse(D1Units.PERCENT.u.isBasicNumber());
  }
}
