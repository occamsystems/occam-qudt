package com.occamsystems.qudtgen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
class GenerateUnitsTest {

  @Test
  void run() {
    GenerateUnits gen = new GenerateUnits();
    Assertions.assertDoesNotThrow(() -> gen.run("./testUnits.java"));
  }
}