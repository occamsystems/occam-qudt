package com.occamsystems.qudt.jena;

import static com.occamsystems.qudt.jena.ModelUtils.qudtProperty;
import static com.occamsystems.qudt.jena.ModelUtils.rdfsProperty;
import static com.occamsystems.qudt.jena.ModelUtils.setLiteral;

import com.occamsystems.qudt.DimensionVector;
import com.occamsystems.qudt.SmallFraction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public class DimensionVectorIO {

  public static final String lengthExp = "dimensionExponentForLength";
  public static final String massExp = "dimensionExponentForMass";
  public static final String timeExp = "dimensionExponentForTime";
  public static final String currentExp = "dimensionExponentForElectricCurrent";
  public static final String amountExp = "dimensionExponentForAmountOfSubstance";
  public static final String luminousExp = "dimensionExponentForLuminousIntensity";
  public static final String temperatureExp = "dimensionExponentForThermodynamicTemperature";
  public static final String dimensionlessExp = "dimensionlessExponent";

  public LiteralDimensionVector read(Model model, String uri) {
    Resource resource = model.getResource(uri);
    if (resource == null) {
      return null;
    }
    return this.read(model, resource);
  }

  public LiteralDimensionVector read(Model model, Resource resource) {
    String localName = resource.getLocalName();
    if (DimensionVector.isSemanticUri(localName)) {
      return new LiteralDimensionVector(localName);
    } else {
      Property l = qudtProperty(model, lengthExp);
      Property m = qudtProperty(model, massExp);
      Property h = qudtProperty(model, temperatureExp);
      Property i = qudtProperty(model, luminousExp);
      Property a = qudtProperty(model, amountExp);
      Property e = qudtProperty(model, currentExp);
      Property t = qudtProperty(model, timeExp);

      DimensionVector rawDv =
          DimensionVector.builder()
              .withLength(ModelUtils.orElse(resource, l, SmallFraction.ZERO))
              .withMass(ModelUtils.orElse(resource, m, SmallFraction.ZERO))
              .withThermodynamicTemperature(ModelUtils.orElse(resource, h, SmallFraction.ZERO))
              .withLuminousIntensity(ModelUtils.orElse(resource, i, SmallFraction.ZERO))
              .withAmountOfSubstance(ModelUtils.orElse(resource, a, SmallFraction.ZERO))
              .withElectricCurrent(ModelUtils.orElse(resource, e, SmallFraction.ZERO))
              .withTime(ModelUtils.orElse(resource, t, SmallFraction.ZERO))
              .build();

      LiteralDimensionVector ldv =
          new LiteralDimensionVector(rawDv.vector()).uri(resource.getURI());
      Statement label = resource.getProperty(rdfsProperty(model, "label"));

      if (label != null) {
        ldv.label(label.getString());
      }

      return ldv;
    }
  }

  public Resource write(Model m, DimensionVector dv) {
    return this.write(m, dv, dv.uri());
  }

  public Resource write(Model m, DimensionVector dv, String uri) {
    Resource r = m.getResource(uri);
    if (dv instanceof LiteralDimensionVector ldv) {
      setLiteral(r, rdfsProperty(m, "label"), ldv.label());
    } else {
      setLiteral(r, rdfsProperty(m, "label"), dv.localName());
    }

    setLiteral(r, qudtProperty(m, lengthExp), dv.length().doubleValue());
    setLiteral(r, qudtProperty(m, massExp), dv.mass().doubleValue());
    setLiteral(r, qudtProperty(m, timeExp), dv.time().doubleValue());
    setLiteral(r, qudtProperty(m, temperatureExp), dv.temperature().doubleValue());
    setLiteral(r, qudtProperty(m, luminousExp), dv.luminous().doubleValue());
    setLiteral(r, qudtProperty(m, amountExp), dv.amount().doubleValue());
    setLiteral(r, qudtProperty(m, currentExp), dv.current().doubleValue());
    setLiteral(r, qudtProperty(m, dimensionlessExp), dv.dimensionlessExponent().doubleValue());

    return r;
  }
}
