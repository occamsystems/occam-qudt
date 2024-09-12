package com.occamsystems.qudtgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
class GenerateUnitsTest {

  @Test
  void run() throws IOException {
    GenerateUnits gen = new GenerateUnits();
    Path tempDirectory = Files.createTempDirectory("test-temp");
    tempDirectory.toFile().deleteOnExit();
    Assertions.assertDoesNotThrow(() -> gen.run(tempDirectory.toString()));
  }
}