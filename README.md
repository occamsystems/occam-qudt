# Occam QUDT
Occam QUDT is a library designed to support mathematical operations over physical quantities.
It is intended to be fast, simple, and compact.
It is based on the definitions found in QUDT (http://qudt.org) to enable openness and interoperability.

This library has been developed by occamsystems.com in support of their Model-Based Sourcing and Model-Based Systems Engineering products.

## Cache-Gen
The cache generator module provides a Maven plugin that generates code based on QUDT vocabularies.
By generating code at this phase, we isolate dependencies on RDF parsing technologies to compile time.

## Core
The core module provides key functionality for efficiently doing math on physical quantities.
This module has no runtime dependencies and includes all predefined QUDT units, kinds, and dimension vectors.

UnitIndex provides most functionality beyond primitive operations, such as parsing Strings into quantities or units.

The vast majority of the compiled size is taken up by enumerations of predefined units.
These units are grouped into enumerations by their Dimension Vectors.
Units can be accessed directly from these enums or via the index.

Systems of units are not included, since they are inconsistently applied in the QUDT vocabulary, often leading to confusing results.
