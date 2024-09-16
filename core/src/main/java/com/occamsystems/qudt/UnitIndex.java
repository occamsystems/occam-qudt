package com.occamsystems.qudt;

import com.occamsystems.qudt.predefined.QuantityKinds;
import com.occamsystems.qudt.predefined.Units;
import com.occamsystems.qudt.predefined.units.H1Units;
import com.occamsystems.qudt.predefined.units.L3Units;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Copyright (c)  2024 Occam Systems, Inc.
 */
public class UnitIndex {

  public static final Predicate<String> UCUM_COMPOSITE = Pattern.compile(".*[0-9³⁴√·\\./_-].*").asMatchPredicate();
  public static final Logger log = Logger.getLogger(UnitIndex.class.getName());
  private List<LiteralUnit> simpleUnits = null;
  private Map<String, LiteralUnit> simpleSymbolMap = null;
  private Map<DimensionVector, List<QuantityKind>> qkByDv;

  private List<Unit> preferredUnits = new ArrayList<>(3);

  /**
   * Creates a new unit index.
   * @param preferredUnits Optionally specify units that should be preferentially selected in the
   *                       case of ambiguity.
   */
  public UnitIndex(Unit... preferredUnits) {
    this.preferredUnits.add(H1Units.K.u);
    this.preferredUnits.add(L3Units.L.u);
    this.preferredUnits.addAll(List.of(preferredUnits));
  }

  /**
   * Returns a list of units that are neither powers nor explicit compositions of other units.
   * For example, N and km are simple units, while m/s and m2 are not.
   */
  List<LiteralUnit> simpleUnits() {
    if (simpleUnits == null) {
      simpleUnits = Units.byDV.values().stream().flatMap(Arrays::stream)
          .filter(unit -> !UCUM_COMPOSITE.test(unit.ucumCode()) &&
              !UCUM_COMPOSITE.test(unit.symbol()))
          .filter(unit -> !(unit.dv().isEmpty() && unit.conversionMultiplier() == 1.))
          .toList();
    }

    return simpleUnits;
  }

  public Map<String, LiteralUnit> simpleSymbolMap() {
    if (this.simpleSymbolMap == null) {
      List<LiteralUnit> simpleUnits = this.simpleUnits();
      this.simpleSymbolMap = new HashMap<>(simpleUnits.size());
      for (LiteralUnit simpleUnit : simpleUnits) {
        String key = toKeyboardChars(simpleUnit.symbol());
        if (simpleSymbolMap.containsKey(key)) {
          LiteralUnit prev = simpleSymbolMap.get(key);

          if (!prev.equivalent(simpleUnit)) {

            boolean prefSu = this.preferredUnits.contains(simpleUnit);
            boolean prefPrev = this.preferredUnits.contains(prev);

            int suQk = kindsByDimensionVector().getOrDefault(simpleUnit.dv(),
                Collections.emptyList()).size();
            int pQk = kindsByDimensionVector().getOrDefault(prev.dv(),
                Collections.emptyList()).size();

            if (prefSu && !prefPrev) {
              log.info("Explicitly resolve symbol collision on " + key + " = " + prev.label() + " and <" + simpleUnit.label() + ">");
              simpleSymbolMap.put(key, simpleUnit);
            } else if (!prefSu && prefPrev) {
              log.info("Explicitly resolve symbol collision on " + key + " = <" + prev.label() + "> and " + simpleUnit.label());
            } else if (suQk > pQk) {
                log.info("Automatically resolve symbol collision on " + key + " = " + prev.label() + " and <" + simpleUnit.label() + ">");
                simpleSymbolMap.put(key, simpleUnit);
              } else {
                log.info("Automatically resolve symbol collision on " + key + " = <" + prev.label() + "> and " + simpleUnit.label());
              }
            }
          } else {
          simpleSymbolMap.put(key, simpleUnit);
        }
      }
    }

    return this.simpleSymbolMap;
  }

  public Map<DimensionVector, List<QuantityKind>> kindsByDimensionVector() {
    if (qkByDv == null) {
      qkByDv = Arrays.stream(QuantityKinds.values())
          .map(qke -> qke.qk)
          .collect(Collectors.groupingBy(qk -> qk.dimensionVector));
    }

    return qkByDv;
  }

  /**
   * If the unit is not already a LiteralUnit, returns the best matched unit from among predefined
   * units.
   * Match quality is defined as similarity of conversion multiplier, then symbolic concision.
   * (e.g., L is preferred to dm3.)
   * This will be null if and only if the dimension vector of the supplied unit has no
   * predefined units.
   */
  LiteralUnit bestPredefinedMatch(Unit base) {
    if (base instanceof LiteralUnit lu) {
      return lu;
    }

    LiteralUnit[] matches = Units.byDV.getOrDefault(base.dv().indexCode(), new LiteralUnit[]{});

    double baseConvLog = Math.log(base.conversionMultiplier());
    return Arrays.stream(matches)
        .sorted(Comparator.comparing(u -> Math.abs(Math.log(((Unit) u).conversionMultiplier()) - baseConvLog))
        .thenComparing(u -> ((Unit) u).symbol().length()))
        .findFirst().orElse(null);
  }

  public static String toKeyboardChars(String symbol) {
    return symbol
        .chars()
        .mapToObj(
            c -> {
              if (c == 185) {
                return '1'; // Handle 1 to superscript
              } else if (c == 178 || c == 179) {
                return (char) (c - 128); // Handle 2-3 to superscript
              } else if (c == (8256 + 48) || (c > (8256 + 51) && c < (8256 + 58))) {
                return (char) (c - 8256); // Handle 4-0 to superscript
              } else if (c == 0x207b) {
                return '-';
              } else if (((int) '⋅') == c || 183 == c) {
                return '*';
              } else if (((int) '°') == c) {
                return "deg";
              } else if (((int) 'Ω') == c || (((int) 'Ω') == c)) {
                return "ohm";
              } else if (((int) '℧') == c) {
                return "mho";
              } else if (((int) 'μ') == c || (((int)'µ') == c)){
                return 'u';
              } else if (((int) 'χ') == c) {
                return "chi";
              } else if (((int) 'ᵣ') == c) {
                return 'r';
              } else if (((int) 'ₚ') == c) {
                return 'p';
              } else if (((int) 'ᵨ') == c) {
                return 'g';
              } else if (((int) 'Å') == c) {
                return "Ao";
              } else if (((int) '₂') == c) {
                return '2';
              } else if (((int) '₀') == c) {
                return '0';
              } else if (((int) 'γ') == c) {
                return "gamma";
              }

              return (char) c;
            })
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();

  }
}
