package com.occamsystems.qudtgen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Copyright (c) 2024 Occam Systems, Inc. */
class GenerateDimensionVectorsTest {

  @Test
  void run() throws IOException {
    GenerateDimensionVectors gen = new GenerateDimensionVectors();
    Path tempDirectory = Files.createTempDirectory("test-temp");
    tempDirectory.toFile().deleteOnExit();
    Assertions.assertDoesNotThrow(() -> gen.run(tempDirectory.toString()));
  }
}
