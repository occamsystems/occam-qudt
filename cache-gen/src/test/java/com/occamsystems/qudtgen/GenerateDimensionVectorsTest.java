package com.occamsystems.qudtgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
class GenerateDimensionVectorsTest {

  @Test
  void run() {
    GenerateDimensionVectors gen = new GenerateDimensionVectors();
    Assertions.assertDoesNotThrow(() -> gen.run("./testVecs.java"));
  }
}