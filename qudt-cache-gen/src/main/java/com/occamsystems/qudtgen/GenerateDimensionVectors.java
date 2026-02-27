package com.occamsystems.qudtgen;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

/** Copyright (c) 2024-2026 Occam Systems, Inc. */
public class GenerateDimensionVectors {
  public static final String VECTOR_VOCAB = "https://qudt.org/2.1/vocab/dimensionvector";
  public static final String DEFINED_BY = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
  private static final String MASS_EXPONENT =
      "http://qudt.org/schema/qudt/dimensionExponentForLength";
  public static final Logger log = Logger.getLogger(GenerateDimensionVectors.class.getName());

  public void run(String outputFilePath) {
    Model model = ModelFactory.createDefaultModel();
    model.read(VECTOR_VOCAB, "TTL");

    Property massExp = model.createProperty(MASS_EXPONENT);

    ResIterator iterator = model.listSubjectsWithProperty(massExp);
    Property replaced = model.createProperty(GeneratorUtils.REPLACED_BY);

    Map<String, String> vectors = new TreeMap<>();
    Map<String, String> replacementMap = new HashMap<>();

    iterator.forEach(
        res -> {
          Statement isReplaced = res.getProperty(replaced);
          if (isReplaced == null) {
            String localName = res.getLocalName();
            String regex = "[AELIMHTD]";
            String[] split = localName.split(regex);
            int[] array = new int[14];
            if (split.length >= 9) {
              for (int i = 0; i < 7; i++) {
                String expStr = split[i + 1];
                int dot = expStr.indexOf("dot");
                int pt = expStr.indexOf("pt");
                String[] fracSplit = expStr.split("[(dot)(pt)]");
                int val = fracSplit[0].isBlank() ? 0 : Integer.parseInt(fracSplit[0]);
                if (dot >= 0 || pt >= 0) {
                  if (fracSplit.length > 1) {
                    array[2 * i] = val * 2 + (fracSplit[0].charAt(0) == '-' ? -1 : 1);
                  } else {
                    array[2 * i] = 1;
                  }
                  val = 1; // This helps with correct name generation.
                  array[2 * i + 1] = 2;
                } else {
                  array[2 * i] = val;
                  array[2 * i + 1] = 1;
                }
              }

              vectors.put(
                  GeneratorUtils.shortenVectorName(localName),
                  Arrays.toString(array).replace('[', '{').replace(']', '}'));
            } else {
              log.warning("Unable to parse dimension vector " + localName);
              vectors.put(localName, "{0,0,0,0,0,0,0}");
            }
          } else {
            try {
              replacementMap.put(
                  isReplaced.getSubject().getURI(), isReplaced.getObject().asResource().getURI());
            } catch (Exception e) {
              String[] split = isReplaced.getString().split(":");
              String value = model.getNsPrefixURI(split[0]) + split[1];
              Logger.getLogger(GenerateUnits.class.getName())
                  .info("Invalid replacement uri for " + isReplaced + "\n Repairing  as " + value);
              replacementMap.put(isReplaced.getSubject().getURI(), value);
            }
          }
        });

    Configuration freemarker = new Configuration(Configuration.VERSION_2_3_33);
    freemarker.setClassForTemplateLoading(this.getClass(), "templates");
    try {
      Template template = freemarker.getTemplate("DimensionVectors.ftl");
      File outDir = new File(outputFilePath);
      boolean ok = outDir.exists() || outDir.mkdirs();
      Environment env =
          template.createProcessingEnvironment(
              Map.of(
                  "vocabUrl",
                  VECTOR_VOCAB,
                  "vectors",
                  vectors,
                  "names",
                  vectors.keySet().stream().collect(Collectors.joining(",\n\t\t"))),
              Files.newBufferedWriter(Path.of(outputFilePath, "DimensionVectors.java")));
      env.process();
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
  }
}
