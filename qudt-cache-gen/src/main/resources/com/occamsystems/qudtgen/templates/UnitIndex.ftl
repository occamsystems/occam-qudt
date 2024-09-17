package com.occamsystems.qudt.predefined;

import static com.occamsystems.qudt.predefined.DimensionVectors.*;

import com.occamsystems.qudt.DimensionVector;
import com.occamsystems.qudt.LiteralUnit;
import com.occamsystems.qudt.predefined.units.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This file was generated based on ${vocabUrl}.
 */
public class Units {
  public static final Map<String, LiteralUnit[]> byDV = new HashMap(225);

  static {
<#list vectors as longName, shortName>
    byDV.put("${longName}", ${shortName}Units.units());
</#list>
  }
}