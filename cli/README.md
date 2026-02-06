# Scala Native CLI

A standalone command-line tool that runs the lower half of the Scala Native
compilation pipeline — taking NIR bytecode (already compiled `.nir` files and
JARs) and producing a native binary. This bypasses sbt entirely for the
link → optimize → codegen → native-compile → system-link stages.

## Pipeline overview

```
                  ┌──────────────────────────────────────────────┐
  scalac/nsc      │              CLI (this project)              │
  + nscplugin     │                                              │
 ─────────────►   │  NIR ──► Link ──► Optimize ──► LLVM IR       │
 .scala → .nir    │          codegen ──► clang ──► native binary │
                  └──────────────────────────────────────────────┘
```

The CLI has two build variants:

| sbt project   | Description                                               |
|---------------|-----------------------------------------------------------|
| `cliJVM3`     | JVM-hosted — runs on a JVM, useful during development     |
| `cliNative3`  | Native-hosted — self-contained binary, no JVM required    |

## Building the CLI

From the repository root, in sbt:

```
# Build the JVM variant
sbt> cliJVM3/compile

# Build the native variant (produces a standalone binary)
sbt> cliNative3/nativeLink
```

The native binary is written to:

```
cli/native/.3/target/scala-<version>/cli
```

## Usage

```
cli --main-class <class> --base-dir <dir> [options]
```

### Required arguments

| Flag                | Description                                          |
|---------------------|------------------------------------------------------|
| `--main-class`      | Fully qualified name of the entry-point class         |
| `--base-dir`        | Base output directory (the sbt `crossTarget`)         |

### Classpath resolution

By default the CLI automatically includes:

1. **Scala Native runtime JARs** (scalalib, javalib, nativelib, etc.) — baked
   in at build time via `BuildInfo`.
2. **User NIR files** discovered under `<base-dir>/classes/` (and
   `<base-dir>/test-classes/` when `--test-config` is set).

To override this behaviour and supply the full classpath manually:

```
cli --classpath <path1>:<path2>:... --main-class Foo --base-dir out
```

### Optional flags

#### General

| Flag                | Description                                        |
|---------------------|----------------------------------------------------|
| `--classpath`, `-cp`| Override the entire NIR classpath                   |
| `--module-name`     | Module name (default: `out`)                        |
| `--base-name`       | Base name for the output artifact                   |
| `--test-config`     | Enable test configuration                           |
| `--cached`          | Skip build if inputs are unchanged                  |
| `--help`            | Show usage information                              |

#### Native configuration

| Flag                | Description                                        |
|---------------------|----------------------------------------------------|
| `--clang`           | Path to clang (auto-discovered by default)          |
| `--clangpp`         | Path to clang++ (auto-discovered by default)        |
| `--gc`              | GC: `none` · `boehm` · `immix` · `commix` · `experimental` |
| `--mode`            | Mode: `debug` · `release-fast` · `release-size` · `release-full` |
| `--lto`             | LTO: `none` · `thin` · `full`                      |
| `--build-target`    | Target: `application` · `library-dynamic` · `library-static` |
| `--target-triple`   | LLVM target triple                                  |
| `--sanitizer`       | Sanitizer: `address` · `thread` · `undefined`       |

#### Compilation flags

| Flag                      | Description                                |
|---------------------------|--------------------------------------------|
| `--no-optimize`           | Disable optimizations                      |
| `--link-stubs`            | Enable linking of stubs                    |
| `--multithreading`        | Enable multithreading support              |
| `--no-multithreading`     | Disable multithreading support             |
| `--embed-resources`       | Embed resources into the binary            |
| `--no-incremental`        | Disable incremental compilation            |
| `--check`                 | Enable NIR check                           |
| `--check-fatal-warnings`  | Treat check warnings as errors             |
| `--dump`                  | Dump NIR                                   |

#### Compiler / linker options

| Flag                      | Description                                        |
|---------------------------|----------------------------------------------------|
| `--compile-options`       | Additional compile options (comma-separated)        |
| `--linking-options`       | Additional linking options (comma-separated)        |
| `--sources-classpath`     | Source JARs classpath (colon-separated)              |

#### Logging

| Flag          | Description                            |
|---------------|----------------------------------------|
| `--verbose`   | Enable debug-level logging             |
| `--silent`    | Suppress all logging                   |

## Example: building the sandbox project

The `sandbox` sub-project provides a minimal Scala Native application useful
for testing the CLI end-to-end.

### Step 1 — Compile Scala sources to NIR

Use sbt to compile the sandbox (this runs `scalac` + `nscplugin`, producing
`.nir` files):

```sh
sbt sandbox3/compile
```

The compiled NIR files are written to:

```
sandbox/.3/target/scala-3.8.1/classes/
```

### Step 2 — Build the CLI (if not already built)

```sh
sbt cliNative3/nativeLink
```

### Step 3 — Link NIR into a native binary

```sh
./cli/native/.3/target/scala-3.8.1/cli \
  --main-class Test \
  --base-dir sandbox/.3/target/scala-3.8.1
```

The CLI will:

1. Load the Scala Native runtime JARs (from BuildInfo).
2. Discover user NIR under `sandbox/.3/target/scala-3.8.1/classes/`.
3. Run the linker, optimizer, and LLVM code generator.
4. Invoke `clang` / `clang++` to produce a native binary.
5. Print the path to the resulting executable.

### Step 4 — Run the resulting binary

```sh
./sandbox/.3/target/scala-3.8.1/out
# Hello, World!
```

### Using the JVM variant

The JVM variant works identically — just invoke it with `sbt run` or
`java -jar`:

```sh
sbt 'cliJVM3/run --main-class Test --base-dir sandbox/.3/target/scala-3.8.1'
```

## Architecture

The CLI lives in package `scala.scalanative.build` and delegates to the same
`Build.buildAwait` / `Build.buildCachedAwait` APIs used by the sbt plugin. It
is intentionally a thin argument-parsing wrapper — all build logic is in the
`tools` project.

```
cli/
  src/main/scala/scala/scalanative/build/
    BuildMain.scala          # CLI entry point and argument parser
```
