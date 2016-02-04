# Scala Native Overview

From a very high-level point of view, the pipeline for native compilation
looks like:

<img src="overview_pipeline.png"/>

1. The compilation starts by processing of the original source files
   by the front-end compiler, followed by subsequent lowering and code
   generation to [Native IR (NIR)](nir.md). Details of front-end
   code generation are not covered in this document.

1. A nirpath (equivalent of classpath for nir) of nir compilation units
   is fed to the [Native Compiler (NC)](nc.md). Additionally,
   an entry point(s) are provided. NC computes the transitive closure of all
   classes, modules and interfaces recursively references from the specified
   entry points. Then, the result is lowered to the LLVM IR.

1. LLVM IR is compiled by the LLVM toolchain and linked with the
   [Native Runtime (NRT)](nrt.md).
