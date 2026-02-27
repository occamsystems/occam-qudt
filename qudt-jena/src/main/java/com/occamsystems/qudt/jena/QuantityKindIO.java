package com.occamsystems.qudt.jena;

import com.occamsystems.qudt.DimensionVector;
import com.occamsystems.qudt.QuantityKind;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public class QuantityKindIO {

  public static final String SKOS_CORE = "http://www.w3.org/2004/02/skos/core#";
  public static final String broader = "broader";
  public static final String HAS_DIMENSION_VECTOR = "hasDimensionVector";
  DimensionVectorIO vectorIO = new DimensionVectorIO();

  public DimensionVectorIO vectorIO() {
    return vectorIO;
  }

  public QuantityKindIO vectorIO(DimensionVectorIO vectorIO) {
    this.vectorIO = vectorIO;
    return this;
  }

  QuantityKind read(Model model, String uri) {
    Resource resource = model.getResource(uri);
    if (resource == null) {
      return null;
    }
    return this.read(model, resource);
  }

  QuantityKind read(Model model, Resource resource) {
    Property broaderProperty = ModelUtils.property(model, SKOS_CORE, broader);
    List<QuantityKind> broaderKinds =
        resource
            .listProperties(broaderProperty)
            .mapWith(st -> this.read(model, st.getObject().asResource()))
            .toList();

    Property dvProperty = ModelUtils.qudtProperty(model, HAS_DIMENSION_VECTOR);
    DimensionVector vector =
        vectorIO().read(model, resource.getProperty(dvProperty).getObject().asResource());
    String label = ModelUtils.orElse(resource, ModelUtils.rdfsProperty(model, "label"), "Unnamed");
    return new QuantityKind(
        label, resource.getURI(), vector, broaderKinds.toArray(new QuantityKind[0]));
  }

  Resource write(Model model, QuantityKind qk) {
    Resource r = model.getResource(qk.uri());

    Property broaderProperty = ModelUtils.property(model, SKOS_CORE, broader);
    Property dvProperty = ModelUtils.qudtProperty(model, HAS_DIMENSION_VECTOR);
    Property label = ModelUtils.rdfsProperty(model, "label");
    ModelUtils.setLiteral(r, label, qk.label());
    ModelUtils.setResources(
        model,
        r,
        broaderProperty,
        Arrays.stream(qk.broaderKinds()).map(QuantityKind::uri).toList());
    ModelUtils.setResource(model, r, dvProperty, qk.dimensionVector().uri());

    return r;
  }
}
