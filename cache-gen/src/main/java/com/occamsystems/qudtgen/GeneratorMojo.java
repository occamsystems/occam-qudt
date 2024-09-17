package com.occamsystems.qudtgen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** Copyright (c) 2024 Occam Systems, Inc. */
@Mojo(name = "generate-all", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {
  @Parameter(
      property = "toDirectory",
      defaultValue = "${project.build.directory}/generated-sources/qudt-cache-gen")
  String generatedSourcesDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String base = generatedSourcesDirectory;
    if (!base.endsWith("/")) {
      base += "/";
    }

    String dir = base + "com/occamsystems/qudt/predefined/";
    new GenerateDimensionVectors().run(dir);
    new GenerateKinds().run(dir);
    new GenerateUnits().run(dir);
  }
}
