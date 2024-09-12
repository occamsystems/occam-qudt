package com.occamsystems.qudtgen;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
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

    Map<String, String> kinds = new HashMap<>();
    DepthTree depthTree = new DepthTree();

    iterator.forEach(res -> {
      String localName = res.getLocalName();
      String vectorName = GeneratorUtils.shortenVectorName(res.getProperty(hasVector).getObject().asResource().getLocalName());
      StringBuilder args = new StringBuilder("\"%s\",\"%s\",%s".formatted(
          GeneratorUtils.bestString(res, label),
          localName,
          "DimensionVectors." + vectorName));

      StmtIterator stmtIterator = res.listProperties(broader);

      String resKey = GeneratorUtils.toConstName(localName);
      stmtIterator.forEach(stm -> {
        String lName = stm.getObject().asResource().getLocalName();
        String key = GeneratorUtils.toConstName(lName);
        args.append(',').append(key);
        depthTree.insert(resKey, key);
      });

      kinds.put(resKey,
          args.toString());
    });

    Map<String, String> sortedKinds = new TreeMap<>(Comparator.comparing(depthTree::depth).thenComparing(a -> a));
    sortedKinds.putAll(kinds);

    Configuration freemarker = new Configuration(Configuration.VERSION_2_3_33);
    freemarker.setClassForTemplateLoading(this.getClass(), "templates");
    try {
      Template template = freemarker.getTemplate("QuantityKinds.ftl");
      File outDir = new File(outputFilePath);
      boolean ok = outDir.exists() || outDir.mkdirs();
      Environment env = template.createProcessingEnvironment(Map.of(
              "vocabUrl", KIND_VOCAB,
              "kinds", sortedKinds,
              "names", sortedKinds.keySet().stream().collect(Collectors.joining(",\n\t\t"))),
          Files.newBufferedWriter(Path.of(outputFilePath, "QuantityKinds.java")));
      env.process();
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
  }

  private static void cascadeDepth(Map<String, Integer> depth, String key) {
    depth.putIfAbsent(key, 0);
    depth.compute(key, (k, n) -> n + 1);
  }

  private class DepthTree {
    private Map<String, DepthNode> nodes = new HashMap<>();
    public void insert(String key, String parent) {
      DepthNode pNode = this.nodes.computeIfAbsent(parent, DepthNode::new);
      DepthNode kNode = this.nodes.computeIfAbsent(key, DepthNode::new);
      kNode.parent = pNode;
    }

    public int depth(String key) {
      if (this.nodes.containsKey(key)) {
        return this.nodes.get(key).depth();
      }

      return -1;
    }

    private class DepthNode {

      public DepthNode(String key) {
        this.key = key;
      }

      String key;
      DepthNode parent;

      int depth() {
        if (this.parent == null) {
          return 0;
        } else {
          return 1 + this.parent.depth();
        }
      }
    }
  }
}
