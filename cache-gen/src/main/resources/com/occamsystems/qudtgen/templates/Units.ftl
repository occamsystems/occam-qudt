package com.occamsystems.qudt.predefined.units;

import static com.occamsystems.qudt.predefined.DimensionVectors.*;

import com.occamsystems.qudt.DimensionVector;
import com.occamsystems.qudt.LiteralUnit;
import java.util.List;

/**
 * This file was generated based on ${vocabUrl}.
 */
public enum ${vector}Units {
<#list units as name, args>
  ${name}(${args}),
</#list>;

  public final LiteralUnit u;

  ${vector}Units(String label, String uri, String symbol, String code, DimensionVector dv,
      double co, double cm) {
    u = new LiteralUnit(label, uri, symbol, code, dv, co, cm);
  }
}