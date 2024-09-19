package com.occamsystems.qudt.predefined.units;

import static com.occamsystems.qudt.predefined.DimensionVectors.*;

import com.occamsystems.qudt.DimensionVector;
import com.occamsystems.qudt.LiteralUnit;
import com.occamsystems.qudt.QuantityKind;
import com.occamsystems.qudt.predefined.QuantityKinds;
import java.util.List;

/**
 * This file was generated based on ${vocabUrl}.
 */
public enum ${vector}Units {
<#list units as name, args>
  ${name}(${args}),
</#list>;

  public final LiteralUnit u;

  ${vector}Units(String label, String uri, String symbol, DimensionVector dv,
      double co, double cm, QuantityKind... qks) {
    u = new LiteralUnit(label, uri, symbol, dv, co, cm, qks);
  }

  private static LiteralUnit[] cache = null;

  public static LiteralUnit[] units() {
    if (cache == null) {
      cache = new LiteralUnit[${vector}Units.values().length];
      for (int i = 0; i < ${vector}Units.values().length; i++) {
        cache[i] = ${vector}Units.values()[i].u;
      }
    }

    return cache;
  }
}