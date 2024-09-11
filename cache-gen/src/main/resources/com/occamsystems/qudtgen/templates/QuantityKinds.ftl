package com.occamsystems.qudt.predefined;

import com.occamsystems.qudt.DimensionVector;
import java.util.List;

/**
 * This file was generated based on ${vocabUrl}.
 */
public class QuantityKinds {
<#list kinds as name, array>
  public static final QuantityKind ${name} = new QuantityKind(${array});
</#list>

  public static final List<QuantityKind> PREDEFINED = List.of(
    ${names});
}