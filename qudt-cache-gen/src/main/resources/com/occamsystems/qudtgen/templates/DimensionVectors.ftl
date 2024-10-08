package com.occamsystems.qudt.predefined;

import com.occamsystems.qudt.DimensionVector;
import java.util.List;

/**
 * This file was generated based on ${vocabUrl}.
 */
public class DimensionVectors {
public static final DimensionVector <#list vectors as name, array>
  ${name} = new DimensionVector(new int[]${array})<#sep>,</#sep>
</#list>;

  public static final List<DimensionVector> PREDEFINED = List.of(
    ${names});
}