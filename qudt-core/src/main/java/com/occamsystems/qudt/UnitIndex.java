package com.occamsystems.qudt;

import com.occamsystems.qudt.predefined.QuantityKinds;
import com.occamsystems.qudt.predefined.Units;
import com.occamsystems.qudt.predefined.units.D1Units;
import com.occamsystems.qudt.predefined.units.H1Units;
import com.occamsystems.qudt.predefined.units.L3Units;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Copyright (c) 2024 Occam Systems, Inc. */
public class UnitIndex {

  public static final Predicate<String> TEST_COMPOSITE =
      Pattern.compile(".*[0-9\\*/].*").asMatchPredicate();
  public static final Logger log = Logger.getLogger(UnitIndex.class.getName());
  private List<LiteralUnit> simpleUnits = null;
  private Map<String, LiteralUnit> simpleSymbolMap = null;
  private Map<DimensionVector, List<QuantityKind>> qkByDv;
  private Map<String, Collection<LiteralUnit>> runtimeUnits = new HashMap<>();

  private static final String SIMPLE_NUMBER_REGEX = "[-+]?\\d*(\\.\\d+)?";
  private static final String NUMBER_REGEX = SIMPLE_NUMBER_REGEX + "([eE][-+]?\\d+)?";
  private static final String UNIT_REGEX =
      "(?<name>([^-+.\\d]+))(?<exponent>(" + SIMPLE_NUMBER_REGEX + "))?";
  private static final Pattern UNIT_PATTERN = Pattern.compile(UNIT_REGEX);

  private static final String QTY_REGEX = "(?<value>(" + NUMBER_REGEX + "))( )*(?<unit>(.*))";

  private static final Pattern QTY_PATTERN = Pattern.compile(QTY_REGEX);

  private final Map<DimensionVector, List<LiteralUnit>> preferredUnits = new HashMap<>(3);

  /**
   * Creates a new unit index.
   *
   * @param preferredUnits Optionally specify units that should be preferentially selected in the
   *     case of ambiguity.
   */
  public UnitIndex(LiteralUnit... preferredUnits) {
    this.preferUnit(D1Units.UNITLESS.u);
    this.preferUnit(H1Units.K.u);
    this.preferUnit(L3Units.L.u);

    for (int i = 0; i < preferredUnits.length; i++) {
      preferUnit(preferredUnits[i]);
    }
  }

  public void preferUnit(LiteralUnit unit) {
    assert unit != null;

    this.preferredUnits.computeIfAbsent(unit.dv(), k -> new ArrayList<>(2)).add(unit);
  }

  /**
   * Returns a list of units that are neither powers nor explicit compositions of other units. For
   * example, N and km are simple units, while m/s and m2 are not.
   */
  List<LiteralUnit> simpleUnits() {
    if (simpleUnits == null) {
      simpleUnits =
          Units.byDV.values().stream()
              .flatMap(Arrays::stream)
              .filter(unit -> !TEST_COMPOSITE.test(toKeyboardChars(unit.symbol())))
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
            boolean prefSu = false;
            boolean prefPrev = false;

            if (this.preferredUnits.containsKey(simpleUnit.dv())) {
              prefSu = this.preferredUnits.get(simpleUnit.dv()).contains(simpleUnit);
            }
            if (this.preferredUnits.containsKey(prev.dv())) {
              prefPrev = this.preferredUnits.get(prev.dv()).contains(prev);
            }

            int suQk =
                kindsByDimensionVector()
                    .getOrDefault(simpleUnit.dv(), Collections.emptyList())
                    .size();
            int pQk =
                kindsByDimensionVector().getOrDefault(prev.dv(), Collections.emptyList()).size();

            if (prefSu && !prefPrev) {
              log.fine(
                  "Explicitly resolve symbol collision on "
                      + key
                      + " = "
                      + prev.label()
                      + " and <"
                      + simpleUnit.label()
                      + ">");
              simpleSymbolMap.put(key, simpleUnit);
            } else if (!prefSu && prefPrev) {
              log.fine(
                  "Explicitly resolve symbol collision on "
                      + key
                      + " = <"
                      + prev.label()
                      + "> and "
                      + simpleUnit.label());
            } else if (suQk > pQk) {
              log.fine(
                  "Automatically resolve symbol collision on "
                      + key
                      + " = "
                      + prev.label()
                      + " and <"
                      + simpleUnit.label()
                      + ">");
              simpleSymbolMap.put(key, simpleUnit);
            } else {
              log.fine(
                  "Automatically resolve symbol collision on "
                      + key
                      + " = <"
                      + prev.label()
                      + "> and "
                      + simpleUnit.label());
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
      qkByDv =
          Arrays.stream(QuantityKinds.values())
              .map(qke -> qke.qk)
              .collect(Collectors.groupingBy(qk -> qk.dimensionVector));
    }

    return qkByDv;
  }

  /**
   * Demands a LiteralUnit whose definition exactly matches the provided unit, follwing the logic
   * for exactMatch. If no such LiteralUnit exists, one will be created from the Aggregate returned
   * by exactMatch.
   */
  public LiteralUnit demandExactLiteral(Unit base, String namespace) {
    if (base instanceof LiteralUnit lu) {
      return lu;
    }

    Optional<LiteralUnit> match = this.exactMatch(base);

    if (match.isPresent()) {
      return match.get();
    }

    AggregateUnit unit = this.decomposeAsAggregate(base);

    LiteralUnit literalUnit =
        new LiteralUnit(
            unit.label(),
            namespace + toKeyboardChars(unit.symbol()),
            unit.symbol(),
            unit.dv(),
            unit.conversionOffset(),
            unit.conversionMultiplier());

    this.registerUnit(literalUnit);
    return literalUnit;
  }

  /**
   * Demands a LiteralUnit whose definition exactly matches the provided symbol, follwing the logic
   * for exactMatch. If no such LiteralUnit exists, one will be created from the Aggregate returned
   * by exactMatch.
   */
  public LiteralUnit demandExactLiteral(String symbol, String namespace) {
    Unit unit = this.exactMatch(symbol);

    if (unit instanceof LiteralUnit lu) {
      return lu;
    }

    LiteralUnit literalUnit =
        new LiteralUnit(
            unit.label(),
            namespace + toKeyboardChars(unit.symbol()),
            unit.symbol(),
            unit.dv(),
            unit.conversionOffset(),
            unit.conversionMultiplier());

    this.registerUnit(literalUnit);
    return literalUnit;
  }

  /**
   * Finds a unit whose definition exactly matches the provided symbol. This does not necessarily
   * mean that symbol of the returned unit will be exactly the provided symbol.
   */
  public Unit exactMatch(String symbol) {
    AggregateUnit aggregateUnit = parseAsAggregateUnit(symbol);
    Optional<LiteralUnit> literalUnit = exactMatch(aggregateUnit);
    if (literalUnit.isPresent()) {
      return literalUnit.get();
    } else {
      return aggregateUnit;
    }
  }

  /**
   * Returns a literal unit whose dimension vector and conversion exactly match the base unit. If
   * the base unit is already a LiteralUnit, it will just return the base unit.
   */
  public Optional<LiteralUnit> exactMatch(Unit base) {
    if (base instanceof LiteralUnit lu) {
      return Optional.of(lu);
    }

    if (this.preferredUnits.containsKey(base.dv())) {
      Optional<LiteralUnit> preferredMatch =
          exactMatch(base, this.preferredUnits.get(base.dv()).stream());
      if (preferredMatch.isPresent()) {
        return preferredMatch;
      }
    }

    LiteralUnit[] matches = Units.byDV.getOrDefault(base.dv().indexCode(), new LiteralUnit[] {});
    Collection<LiteralUnit> runtimeMatches =
        this.runtimeUnits.getOrDefault(base.dv().indexCode(), Collections.emptyList());

    return exactMatch(base, Stream.concat(Arrays.stream(matches), runtimeMatches.stream()));
  }

  private Optional<LiteralUnit> exactMatch(Unit base, Stream<LiteralUnit> candidates) {
    double cm = base.conversionMultiplier();
    double co = base.conversionOffset();

    return candidates
        .filter(u -> cm == u.conversionMultiplier() && co == u.conversionOffset())
        .sorted(
            Comparator.comparing(u -> symbolicDifference(base, (Unit) u))
                .thenComparing(u -> ((Unit) u).symbol().length())
                .thenComparing(u -> ((Unit) u).symbol()))
        .findFirst();
  }

  private int symbolicDifference(Unit base, Unit other) {
    String bs = toKeyboardChars(base.symbol());
    String os = toKeyboardChars(other.symbol());
    if (bs.equals(os)) {
      return 0;
    }

    return editDistance(bs, os);
  }

  private static int editDistance(String x, String y) {
    int[][] dp = new int[x.length() + 1][y.length() + 1];

    for (int i = 0; i <= x.length(); i++) {
      for (int j = 0; j <= y.length(); j++) {
        if (i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] =
              Math.min(
                  Math.min(
                      dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                      dp[i - 1][j] + 1),
                  dp[i][j - 1] + 1);
        }
      }
    }

    return dp[x.length()][y.length()];
  }

  static int costOfSubstitution(char a, char b) {
    return a == b ? 0 : 1;
  }

  /**
   * If the unit is not already a LiteralUnit, returns the best matched unit from among predefined
   * units. Match quality is defined as similarity of conversion multiplier, then symbolic
   * concision. (e.g., L is preferred to dm3.) This will be null if and only if the dimension vector
   * of the supplied unit has no predefined units.
   */
  public LiteralUnit bestPredefinedMatch(Unit base) {
    if (base instanceof LiteralUnit lu) {
      return lu;
    }

    LiteralUnit[] matches = Units.byDV.getOrDefault(base.dv().indexCode(), new LiteralUnit[] {});
    Collection<LiteralUnit> runtimeMatches =
        this.runtimeUnits.getOrDefault(base.dv().indexCode(), Collections.emptyList());

    double baseConvLog = Math.log(base.conversionMultiplier());
    return Stream.concat(Arrays.stream(matches), runtimeMatches.stream())
        .sorted(
            Comparator.comparing(
                    u -> Math.abs(Math.log(((Unit) u).conversionMultiplier()) - baseConvLog))
                .thenComparing(u -> ((Unit) u).symbol().length()))
        .findFirst()
        .orElse(null);
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
              } else if (((int) 'μ') == c || (((int) 'µ') == c)) {
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

  public Stream<LiteralUnit> units() {
    return Stream.concat(
        Units.byDV.values().stream().flatMap(Arrays::stream),
        this.runtimeUnits.values().stream().flatMap(Collection::stream));
  }

  public LiteralUnit predefinedUnitBySymbol(String symbol) {
    return this.units()
        .filter(u -> u.symbol().equals(symbol) || toKeyboardChars(u.symbol()).equals(symbol))
        .sorted(this::preferredUnit)
        .findFirst()
        .orElse(null);
  }

  private int preferredUnit(Unit u1, Unit u2) {
    boolean pref1 = false;
    boolean pref2 = false;

    if (this.preferredUnits.containsKey(u1.dv())) {
      pref1 = this.preferredUnits.get(u1.dv()).contains(u1);
    }

    if (this.preferredUnits.containsKey(u2.dv())) {
      pref2 = this.preferredUnits.get(u2.dv()).contains(u2);
    }

    if (pref1 && !pref2) {
      return -1;
    }
    if (!pref1 && pref2) {
      return 1;
    }

    int suQk = kindsByDimensionVector().getOrDefault(u1.dv(), Collections.emptyList()).size();
    int pQk = kindsByDimensionVector().getOrDefault(u2.dv(), Collections.emptyList()).size();

    if (suQk != pQk) {
      return suQk - pQk;
    }

    return u1.hashCode() - u2.hashCode();
  }

  public AggregateUnit decomposeAsAggregate(Unit unit) {
    if (unit instanceof LiteralUnit lu) {
      return parseAsAggregateUnit(lu.symbol());
    } else {
      AggregateUnit agg = (AggregateUnit) unit;
      Map<Unit, SmallFraction> map = new HashMap<>();
      agg.components.forEach((lu, exp) -> map.put(decomposeAsAggregate(lu), exp));
      return new AggregateUnit(map);
    }
  }

  public AggregateUnit parseAsAggregateUnit(String symbol) {
    String s = toKeyboardChars(symbol);
    boolean negative = false;
    char[] chars = s.toCharArray();
    StringBuilder b = new StringBuilder();
    AggregateUnit aggregateUnit = AggregateUnit.empty;
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == '/' || chars[i] == '*') {
        aggregateUnit = parseUnitExponent(negative, b, aggregateUnit);

        b = new StringBuilder();
        negative = chars[i] == '/';
      } else {
        b.append(chars[i]);

        if (i == chars.length - 1) {
          aggregateUnit = parseUnitExponent(negative, b, aggregateUnit);
        }
      }
    }

    return aggregateUnit;
  }

  private AggregateUnit parseUnitExponent(
      boolean negative, StringBuilder b, AggregateUnit aggregateUnit) {
    Matcher matcher = UNIT_PATTERN.matcher(b.toString());

    if (matcher.find()) {
      String name = matcher.group("name");
      String exponent = matcher.group("exponent");
      SmallFraction exp = SmallFraction.ONE;
      if (exponent != null && !exponent.isBlank()) {
        exp = SmallFraction.approximate(Double.parseDouble(exponent));
      }

      if (negative) {
        exp = SmallFraction.times(exp, -1);
      }

      LiteralUnit literalUnit = this.simpleSymbolMap().get(name);
      aggregateUnit = new AggregateUnit(aggregateUnit, SmallFraction.ONE, literalUnit, exp);
    }
    return aggregateUnit;
  }

  public QuantityValue parseQuantity(String qtyString) {
    Matcher matcher = QTY_PATTERN.matcher(qtyString);
    if (matcher.find()) {
      String valueString = matcher.group("value");
      String unitString = matcher.group("unit");
      Unit unit;
      if (unitString.isBlank()) {
        unit = D1Units.UNITLESS.u;
      } else {
        unit = this.exactMatch(unitString);
      }
      return QuantityValue.ofScaled(Double.parseDouble(valueString), unit);
    }

    return null;
  }

  public void registerUnit(LiteralUnit unit) {
    Collection<LiteralUnit> list =
        this.runtimeUnits.computeIfAbsent(unit.dv().indexCode(), k -> new HashSet<>());
    list.add(unit);
  }

  public Optional<LiteralUnit> unitByUri(String uri) {
    return units().filter(u -> uri.equals(u.uri())).findAny();
  }
}
