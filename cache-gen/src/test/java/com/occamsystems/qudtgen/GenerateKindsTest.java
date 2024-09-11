package com.occamsystems.qudtgen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
class GenerateKindsTest {

  @Test
  void run() {
    GenerateKinds gen = new GenerateKinds();
    Assertions.assertDoesNotThrow(() -> gen.run("./testKinds.java"));
  }
}