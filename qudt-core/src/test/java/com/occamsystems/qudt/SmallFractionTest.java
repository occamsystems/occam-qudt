package com.occamsystems.qudt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Copyright (c) 2024 Occam Systems, Inc. */
class SmallFractionTest {
  @Test
  void create() {
    SmallFraction sf1 = new SmallFraction(3);
    SmallFraction sf2 = new SmallFraction(-3);
    Assertions.assertEquals(3, sf1.intValue());
    Assertions.assertEquals(-3, sf2.intValue());

    Assertions.assertEquals(1.5, new SmallFraction(3, 2).floatValue());
    Assertions.assertEquals(1.5, new SmallFraction(-3, -2).floatValue());
    Assertions.assertEquals(-1.5, new SmallFraction(3, -2).floatValue());
  }

  @Test
  void reduce() {
    SmallFraction smallFraction = new SmallFraction(-12, -8);

    Assertions.assertEquals("3/2", smallFraction.toString());
    Assertions.assertEquals(1.5, smallFraction.floatValue());
  }

  @Test
  void encode() {
    SmallFraction sf1 = new SmallFraction(-12, -8);

    Assertions.assertEquals(Integer.toHexString(19), sf1.encodeReduced());
    SmallFraction sf2 = new SmallFraction(12, -8);

    Assertions.assertEquals(Integer.toHexString(147), sf2.encodeReduced());
  }

  @Test
  void approximate() {
    Assertions.assertEquals(new SmallFraction(3, 2), SmallFraction.approximate(1.5));
    Assertions.assertEquals(new SmallFraction(13, 7), SmallFraction.approximate(1.8571));
    Assertions.assertEquals(new SmallFraction(-2, 3), SmallFraction.approximate(-0.6667));
  }
}
