/*
 * Copyright 2013-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.ocaml;

import com.facebook.buck.cxx.Linker;
import com.facebook.buck.cxx.NativeLinkableInput;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleSourcePath;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.step.Step;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.nio.file.Path;

import javax.annotation.Nullable;

class OCamlStaticLibrary extends AbstractBuildRule implements OCamlLibrary {
  private final BuildTarget staticLibraryTarget;
  private final ImmutableList<String> linkerFlags;
  private final FluentIterable<Path> srcPaths;
  private final OCamlBuildContext ocamlContext;
  private final OCamlBuild ocamlLibraryBuild;

  public OCamlStaticLibrary(
      BuildRuleParams params,
      SourcePathResolver resolver,
      BuildRuleParams compileParams,
      ImmutableList<String> linkerFlags,
      FluentIterable<Path> srcPaths,
      OCamlBuildContext ocamlContext, OCamlBuild ocamlLibraryBuild) {
    super(params, resolver);
    this.linkerFlags = linkerFlags;
    this.srcPaths = srcPaths;
    this.ocamlContext = ocamlContext;
    this.ocamlLibraryBuild = ocamlLibraryBuild;
    staticLibraryTarget = OCamlRuleBuilder.createStaticLibraryBuildTarget(
        compileParams.getBuildTarget());
  }

  @Override
  public NativeLinkableInput getNativeLinkableInput(Linker linker, Type type) {
    Preconditions.checkArgument(
        type == Type.STATIC,
        "Only supporting static linking in OCaml");

    final Path staticLibraryPath = OCamlBuildContext.getArchiveOutputPath
        (staticLibraryTarget);

    ImmutableList.Builder<String> linkerArgsBuilder = ImmutableList.builder();
    linkerArgsBuilder.addAll(linkerFlags);

    FluentIterable<String> cObjs = srcPaths.filter(OCamlUtil.ext(OCamlCompilables.OCAML_C))
        .transform(ocamlContext.toCOutput())
        .transform(Functions.toStringFunction());

    linkerArgsBuilder.add(staticLibraryPath.toString());

    linkerArgsBuilder.addAll(cObjs);

    final ImmutableList<String> linkerArgs = linkerArgsBuilder.build();

    return new NativeLinkableInput(
        ImmutableList.<SourcePath>of(
            new BuildRuleSourcePath(ocamlLibraryBuild)),
        linkerArgs);
  }

  @Override
  public Path getIncludeLibDir() {
    return OCamlBuildContext.getCompileOutputDir(staticLibraryTarget, true);
  }

  @Override
  public Iterable<String> getBytecodeIncludeDirs() {
    return ocamlContext.getBytecodeIncludeFlags();
  }

  @Override
  protected ImmutableCollection<Path> getInputsToCompareToOutput() {
    return ImmutableList.of();
  }

  @Override
  protected RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) {
    return builder;
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {
    return ImmutableList.of();
  }

  @Nullable
  @Override
  public Path getPathToOutputFile() {
    return null;
  }

}
