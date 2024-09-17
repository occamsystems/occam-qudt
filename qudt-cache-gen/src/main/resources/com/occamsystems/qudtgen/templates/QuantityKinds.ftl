package com.occamsystems.qudt.predefined;

import com.occamsystems.qudt.DimensionVector;
import com.occamsystems.qudt.QuantityKind;
import java.util.List;

/**
 * This file was generated based on ${vocabUrl}.
 */
public enum QuantityKinds {
<#list kinds as name, array>
  ${name}(${array}),
</#list>;

  public final QuantityKind qk;

  QuantityKinds(String label, String uri, DimensionVector dv,
      QuantityKinds... broaderKinds) {
    QuantityKind[] bks = new QuantityKind[broaderKinds.length];
    for (int i = 0; i < broaderKinds.length; i++) {
      bks[i] = broaderKinds[i].qk;
    }
    this.qk = new QuantityKind(label, uri, dv, bks);
  }
}