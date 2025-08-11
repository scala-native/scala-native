# IDE setup

## Metals

Metals import should work out of the box for most of the modules, it's
the recommended IDE. To speed up indexing and prevent Bloop-related
issues by default we export only 1 version of `MultiScalaProject`,
otherwise it would need to cross-compile sources for all binary Scala
versions on each source-change. By default IDE would target Scala 3
projects, to change this behavior modify
`project/MyScalaNativePlugin.scala` and modify `ideScalaVersion`. This
change would be only required when developing Scala 2 compiler plugins,
sbt plugins or Scala 2 specific sources.

## IntelliJ IDEA

-   Select "Create project from existing sources" and choose the
    `build.sbt` file. When prompted, select "Open as project". Make
    sure you select the "Use sbt shell" for both import and build.
-   When the import is complete, we need to fix some module
    dependencies:
    -   `scalalib`: Right-click on the module, "Mark directory as" ->
        "Excluded". This is needed because `scalalib` is only meant to
        be used at runtime (it is the Scala library that the executables
        link against). Not excluding it makes IDEA think that the Scala
        library comes from it, which results into highlighting errors.
    -   `nscplugin`: We need to add what SBT calls
        `unmanagedSourceDirectories` as dependencies. Go to Project
        Structure -> Modules -> `nscplugin` -> Dependencies and click
        the + icon. Select "JARs or Directories" and navigate to the
        `nir` directory at the root of the Scala Native project. Repeat
        for the `util` directory.
    -   `native-build`: We need to add the `sbt-scala-native` module as
        a dependency. Go to Project Structure -> Modules ->
        `native-build` -> Dependencies and click the + icon. Select
        "Module Dependency" and select the `sbt-scala-native` module.

The above is not an exhaustive list, but it is the bare minimum to have
the build working. Please keep in mind that you will have to repeat the
above steps, in case you reload (re-import) the SBT build. This will
need to happen if you change some SBT-related file (e.g. `build.sbt`).

## Setup for clangd

`clangd` is a Language Server Protocol (LSP) server for C and C++ using the
`clang` compiler. Your IDE of choice can connect to `clangd` to help
development using C and C++.

-   VSCode: Add the `clangd` extension from LLVM. Full
    documentation for `clangd` is
    [here](https://clangd.llvm.org). You can also add the C/C++
    extensions from Microsoft if desired for highlighting and other
    features.

    **Warning:** Some features may conflict with the `clangd` extension.
- Other editor setups are not documented yet.

A `compile_flags.txt` is included for Scala Native contributors to get the best
setup to work on `nativelib` and the Garbage Collectors using `clangd`. We use
conditional compilation for garbage collection selection and the code is
in a `gc` directory so we need one include for the GC headers relative paths.
Defines are also added for the different garbage collectors we have
in the project so we can work on that code using `clangd`. Entries are also
added for `clib`, `javalib`, and `posixlib` to assist with these projects as
well. The Scala Native compiler adds these same includes when it compiles your
project.

`clangd` works well but only has certain defines for your platform for example.
Thus `clangd` can work out of the box for simple setups which is probably fine
for most projects that just need `C` glue code for Windows, Linux or macOS.

If you have [native code included](../user/native.md) in your own project,
you should add your own `clangd` setup at the root of your Scala Native
project. You can refer to, or copy and modify, the Scala Native
[compile_flags.txt](https://github.com/scala-native/scala-native/blob/main/compile_flags.txt)
file. The file should have an entry as follows pointing to your directory
containing your C/C++ source.

Example: `-I<path to>/src/main/resources/scala-native` 
