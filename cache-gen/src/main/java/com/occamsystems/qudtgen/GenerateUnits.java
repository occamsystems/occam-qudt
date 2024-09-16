package com.occamsystems.qudtgen;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Statement;

/**
 * Copyright (c)  2024 Occam Systems, Inc.
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
    Property replaced = model.createProperty(GeneratorUtils.REPLACED_BY);

    ResIterator iterator = model.listSubjectsWithProperty(hasVector);

    Map<String, Map<String, String>> vectorToUnits = new HashMap<>(300);
    Map<String, String> replacementMap = new HashMap<>();

    iterator.forEach(res -> {
      Statement isReplaced = res.getProperty(replaced);
      if (isReplaced == null) {
        String localName = res.getLocalName();
        String vectorName = GeneratorUtils.shortenVectorName(
            res.getProperty(hasVector).getObject().asResource().getLocalName());
        Map<String, String> units = vectorToUnits.computeIfAbsent(vectorName,
            n -> new TreeMap<>());
        String args = "\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s".formatted(
            GeneratorUtils.bestString(res, label),
            res.getLocalName(),
            GeneratorUtils.bestString(res, symbol),
            GeneratorUtils.bestString(res, ucumCode),
            vectorName,
            String.valueOf(GeneratorUtils.doubleOrElse(res, conversionOffset, 0.)),
            String.valueOf(GeneratorUtils.doubleOrElse(res, conversionMult, 1.))
        );

        units.put(localName.replace("-", "_")
                .replace("pt", "dot"),
            args);
      } else {
        try {
          replacementMap.put(isReplaced.getSubject().getURI(),
              isReplaced.getObject().asResource().getURI());
        } catch (Exception e) {
          String[] split = isReplaced.getString().split(":");
          String value = model.getNsPrefixURI(split[0]) + split[1];
          Logger.getLogger(GenerateUnits.class.getName()).info("Invalid replacement uri for " + isReplaced
            + "\n Repairing  as " + value);
          replacementMap.put(isReplaced.getSubject().getURI(),
              value);
        }
      }
    });

    Configuration freemarker = new Configuration(Configuration.VERSION_2_3_33);
    freemarker.setClassForTemplateLoading(this.getClass(), "templates");
    try {
      Template template = freemarker.getTemplate("VectorUnit.ftl");
      String oPath = Path.of(outputFilePath, "units").toString();
      File outDir = new File(oPath);
      boolean ok = outDir.exists() || outDir.mkdirs();
      vectorToUnits.forEach((vector, units) -> {
        Environment env = null;
        try {
          env = template.createProcessingEnvironment(Map.of(
                  "vocabUrl", UNIT_VOCAB,
                      "vector", vector,
                  "units", units,
                  "names", units.keySet().stream().collect(Collectors.joining(",\n\t\t"))),
          Files.newBufferedWriter(Path.of(oPath, vector+"Units.java")));
          env.process();
        } catch (TemplateException | IOException e) {
          throw new RuntimeException(e);
        }
      });

      Template indexTemplate = freemarker.getTemplate("UnitIndex.ftl");
        Environment env = null;
        try {
          Map<String, String> fullToShortVector = vectorToUnits.keySet().stream()
              .collect(Collectors.toMap(s -> s,
                  GeneratorUtils::shortenVectorName));
          env = indexTemplate.createProcessingEnvironment(Map.of(
                  "vocabUrl", UNIT_VOCAB,
                  "vectors", fullToShortVector),
              Files.newBufferedWriter(Path.of(oPath, "Units.java")));
          env.process();
        } catch (TemplateException | IOException e) {
          throw new RuntimeException(e);
        }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
