# Occam QUDT
Occam QUDT is a library designed to support mathematical operations over physical quantities.
It is intended to be fast, simple, and compact.
It is based on the definitions found in QUDT (http://qudt.org) to enable openness and interoperability.

This library has been developed by occamsystems.com in support of their Model-Based Sourcing and Model-Based Systems Engineering products.

## Cache-Gen
The cache generator module provides a Maven plugin that generates code based on QUDT vocabularies.
By generating code at this phase, we isolate dependencies on RDF parsing technologies to compile time.
After eliminating units that have been replaced or are missing information that suggests they would be problematic in application,
about 1500 predefined units, plus their associated kinds and dimension vectors, are generated.

## Core
The core module provides key functionality for efficiently doing math on physical quantities.
This module has no runtime dependencies

UnitIndex provides most functionality beyond primitive operations, such as parsing Strings into quantities or units.
The vast majority of the compiled size is taken up by enumerations of predefined units.

### Units
Units are grouped into enumerations by their Dimension Vectors.
Dimension Vector enum nanmes include only the non-zero components and replace - with _.
Units can be accessed directly from these enums or via the index.

```
L1Units.M.u == unitIndex.exactMatch("m")
```

Units can also be created at runtime, assuming they can be constructed from known units.

```
new AggregateUnit(L2Units.M2.u, 5).equivalent(unitIndex.exactMatch("m10")
new AggregateUnit(L1Units.KiloM.u, 3, T1.Units.S.u, -4).equivalent(unitIndex.exactMatch("km3/s4")
```

All unit exponents must be rational, but need not be integers.

```
new AggregateUnit(L3Units.L.u, SmallFraction.approximate(0.3333)).equivalent(unitIndex.exactMatch("dm"))
new AggregateUnit(L_2Units.PER_M2.u, new SmallFraction(-3, 2)).equivalent(unitIndex.exactMatch("m3"))
```

Given an aggregate unit, you can find the closest predefined unit that matches it.
```
L5Units.M5.u == unitIndex.bestPredefinedMatch(new AggregateUnit(L3Units.L.u, new SmallFraction(5, 3)))
```

### Quantity Values

Quantity Values can be created based on scaled or unscaled values.

```
QuantityValue.ofScaled(1, L1Units.KiloM.u) == QuantityValue.ofUnscaled(1000, L1Units.KiloM.u)
```
All Quantity Values are stored in memory based on their *unscaled* values.
This means that unit conversion only rewrites a pointer.
Additionally, it means that all fundamental operations over quantities require no intermediate conversions. 
Operations are available as static and local methods, with local methods having mutating and non-mutating variants.
Non-mutating methods are the same as static methods and allocate new QuantityValues.
```
QuantityValue qv3 = QuantityValue.add(qv1, qv2); // Allocates
QuantityValue qv4 = qv1.plus(qv2); // Allocates
qv1.plusMut(qv2); // Mutates
```

## Jena QUDT

This module enables reading and writing of QUDT elements from RDF at runtime using Apache Jena.

Note that Jena is used during parsing of the vocabulary files during cache generation.
However, this dependency is intentionally separated from the Core module.
