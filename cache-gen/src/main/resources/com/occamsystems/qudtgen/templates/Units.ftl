package com.occamsystems.qudt.predefined;

import com.occamsystems.qudt.DimensionVector;
imoirt com.occamsystems.qudt.LiteralUnit;
import java.util.List;

/**
 * This file was generated based on ${vocabUrl}.
 */
public class Units {
<#list units as name, args>
  public static final LiteralUnit ${name} = new LiteralUnit(${args});
</#list>

  public static final List<LiteralUnit> PREDEFINED = List.of(
    ${names});
}