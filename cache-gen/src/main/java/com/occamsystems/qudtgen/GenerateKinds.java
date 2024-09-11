package com.occamsystems.qudtgen;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class GenerateKinds {
  public static final String KIND_VOCAB = "https://qudt.org/2.1/vocab/quantitykind";

  public static final String BROADER = "http://www.w3.org/2004/02/skos/core#broader";
  public void run(String outputFilePath) {
    Model model = ModelFactory.createDefaultModel();
    model.read(KIND_VOCAB, "TTL");

    Property hasVector = model.createProperty(GeneratorUtils.HAS_VECTOR);
    Property label = model.createProperty(GeneratorUtils.LABEL);
    Property broader = model.createProperty(BROADER);

    ResIterator iterator = model.listSubjectsWithProperty(hasVector);

    Map<String, String> kinds = new TreeMap<>();

    iterator.forEach(res -> {
      String localName = res.getLocalName();
      String vectorName = res.getProperty(hasVector).getObject().asResource().getLocalName()
          .replace("-","_")
          .replace("pt","dot");
      StringBuilder args = new StringBuilder("\"%s\",\"%s\",%s".formatted(
          GeneratorUtils.bestString(res, label),
          res.getURI(),
          "DimensionVectors." + vectorName));

      StmtIterator stmtIterator = res.listProperties(broader);

      stmtIterator.forEach(stm -> args.append(',')
          .append(GeneratorUtils.toConstName(stm.getObject().asResource().getLocalName())));

      kinds.put(GeneratorUtils.toConstName(localName),
          args.toString());
    });

    Configuration freemarker = new Configuration(Configuration.VERSION_2_3_33);
    freemarker.setClassForTemplateLoading(this.getClass(), "templates");
    try {
      Template template = freemarker.getTemplate("QuantityKinds.ftl");
      Environment env = template.createProcessingEnvironment(Map.of(
              "vocabUrl", KIND_VOCAB,
              "kinds", kinds,
              "names", kinds.keySet().stream().collect(Collectors.joining(",\n\t\t"))),
          Files.newBufferedWriter(Path.of(outputFilePath)));
      env.process();
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
  }
}
