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

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class GenerateUnits {
  public static final String UNIT_VOCAB = "https://qudt.org/2.1/vocab/unit";
  private static final String CONV_M = "http://qudt.org/schema/qudt/conversionMultiplier";
  private static final String CONV_O = "http://qudt.org/schema/qudt/conversionOffset";
  private static final String SYMBOL = "http://qudt.org/schema/qudt/symbol";
  private static final String UCUM_CODE = "http://qudt.org/schema/qudt/ucumCode";

  public void run(String outputFilePath) {
    Model model = ModelFactory.createDefaultModel();
    model.read(UNIT_VOCAB, "TTL");

    Property hasVector = model.createProperty(GeneratorUtils.HAS_VECTOR);
    Property conversionOffset = model.createProperty(CONV_O);
    Property conversionMult = model.createProperty(CONV_M);
    Property symbol = model.createProperty(SYMBOL);
    Property ucumCode = model.createProperty(UCUM_CODE);
    Property label = model.createProperty(GeneratorUtils.LABEL);

    ResIterator iterator = model.listSubjectsWithProperty(hasVector);

    Map<String, String> units = new TreeMap<>();

    iterator.forEach(res -> {
      String localName = res.getLocalName();
      String vectorName = res.getProperty(hasVector).getObject().asResource().getLocalName()
          .replace("-","_")
          .replace("pt","dot");
      String args = "\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s".formatted(
        GeneratorUtils.bestString(res, label),
        res.getURI(),
          GeneratorUtils.bestString(res, symbol),
          GeneratorUtils.bestString(res, ucumCode),
          "DimensionVectors." + vectorName,
          String.valueOf(GeneratorUtils.doubleOrElse(res, conversionOffset, 0.)),
          String.valueOf(GeneratorUtils.doubleOrElse(res, conversionMult, 1.))
      );

      units.put(localName.replace("-","_")
              .replace("pt","dot"),
            args);
    });

    Configuration freemarker = new Configuration(Configuration.VERSION_2_3_33);
    freemarker.setClassForTemplateLoading(this.getClass(), "templates");
    try {
      Template template = freemarker.getTemplate("Units.ftl");
      Environment env = template.createProcessingEnvironment(Map.of(
              "vocabUrl", UNIT_VOCAB,
              "units", units,
              "names", units.keySet().stream().collect(Collectors.joining(",\n\t\t"))),
          Files.newBufferedWriter(Path.of(outputFilePath)));
      env.process();
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
  }

}
