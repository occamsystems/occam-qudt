package com.occamsystems.qudt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
class DimensionVectorTest {

  @Test
  void localName() {
    DimensionVector build = DimensionVector.builder().build();
    Assertions.assertEquals("NotApplicable", build.localName());
    build = DimensionVector.builder().withLength(1).build();
    Assertions.assertEquals("A0E0L1I0M0H0T0D0", build.localName());
    build = DimensionVector.builder().withThermodynamicTemperature(new SmallFraction(-3,2))
        .withLuminousIntensity(5).build();
    Assertions.assertEquals("A0E0L0I5M0H-1dot5T0D0", build.localName());
  }

  @Test
  void indexCode() {
    DimensionVector build = DimensionVector.builder().build();
    Assertions.assertEquals("NotApplicable", build.indexCode());
    build = DimensionVector.builder().withLength(1).build();
    Assertions.assertEquals("L1", build.indexCode());
    build = DimensionVector.builder().withThermodynamicTemperature(new SmallFraction(-3,2))
        .withLuminousIntensity(5).build();
    Assertions.assertEquals("I5H_1dot5", build.indexCode());
  }
}