package com.occamsystems.qudtgen;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;

/**
 * Copyright (c) 2022 - 2024 Occam Systems, Inc. All rights reserved.
 */
public class GenerateDimensionVectors {
  public static final String VECTOR_VOCAB = "https://qudt.org/2.1/vocab/dimensionvector";
  public static final String DEFINED_BY = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
  private static final String MASS_EXPONENT = "http://qudt.org/schema/qudt/dimensionExponentForLength";
  public static final Logger log = Logger.getLogger(GenerateDimensionVectors.class.getName());

  public void run(String outputFilePath) {
    Model model = ModelFactory.createDefaultModel();
    model.read(VECTOR_VOCAB, "TTL");

    Property definedBy = model.createProperty(DEFINED_BY);
    Property massExp = model.createProperty(MASS_EXPONENT);

    ResIterator iterator = model.listSubjectsWithProperty(massExp);

    Map<String, String> vectors = new HashMap<>(300);

    iterator.forEach(res -> {
      String localName = res.getLocalName();
      String regex = "[AELIMHTD]";
      String[] split = localName.split(regex);
      StringBuilder name = new StringBuilder();
      int[] array = new int[14];
      if (split.length >= 7) {
        for (int i = 0; i < 7; i++) {
          String expStr = split[i + 1];
          String[] fracSplit = expStr.split("[(dot)(pt)]");
          int val = fracSplit[0].isBlank() ? 0 : Integer.parseInt(fracSplit[0]);
          if (fracSplit.length > 1) {
            array[2 * i] = val * 2 + (fracSplit[0].charAt(0) == '-' ? -1 : 1);
            array[2 * i + 1] = 2;
          } else {
            array[2 * i] = val;
            array[2 * i + 1] = 1;
          }
          if (val != 0 || fracSplit.length > 1) {
            name.append(regex.charAt(i + 1)).append(expStr);
          }
        }

        if (!split[8].equals("0")) {
          name.append('D').append(split[8]);
        }

        String finalName = name.toString().replace("-", "_");
        if (finalName.isBlank()) {
          log.warning("Blank dimension vector name for " + localName);
        } else {
          vectors.put(finalName,
              Arrays.toString(array));
        }
      } else {
        log.warning("Unable to process dimension vector " + localName);
      }
    });

    Configuration freemarker = new Configuration(Configuration.VERSION_2_3_33);
    freemarker.setClassForTemplateLoading(this.getClass(), "templates");
    try {
      Template template = freemarker.getTemplate("DimensionVectors.ftl");
      Environment env = template.createProcessingEnvironment(Map.of(
          "vocabUrl", VECTOR_VOCAB,
          "vectors", vectors,
              "names", vectors.keySet().stream().collect(Collectors.joining(",\n\t\t"))),
          Files.newBufferedWriter(Path.of(outputFilePath)));
      env.process();
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
  }
}
