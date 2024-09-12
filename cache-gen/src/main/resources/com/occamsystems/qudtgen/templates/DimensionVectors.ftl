package com.occamsystems.qudt.predefined;

import com.occamsystems.qudt.DimensionVector;
import java.util.List;

/**
 * This file was generated based on ${vocabUrl}.
 */
public class DimensionVectors {
<#list vectors as name, array>
  public static final DimensionVector ${name} = new DimensionVector(new int[]${array});
</#list>

  public static final List<DimensionVector> PREDEFINED = List.of(
    ${names});
}