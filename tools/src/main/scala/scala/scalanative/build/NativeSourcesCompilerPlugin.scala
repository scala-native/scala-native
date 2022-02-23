package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scalanative.build.IO.RichPath
import scalanative.build.Mode._

import NativeSourcesCompilerPlugin._

/** A plugin interface for compiling native sources, such as C, C++, and Rust.
 */
trait NativeSourcesCompilerPlugin {

  /** The name of this plugin */
  def name: String

  /** The extensions this plugin accepts. e.g. `.c` */
  def extensions: Seq[String]

  /** Return a command to compile the given file and the result.
   */
  def compile(config: Config, path: Path): Option[CompilationContext]

}

object NativeSourcesCompilerPlugin {

  /** The command to compile a file and the result.
   */
  case class CompilationContext(
      command: Seq[String],
      result: CompilationResult
  )

}

sealed abstract class ClangSourcesCompilerPlugin
    extends NativeSourcesCompilerPlugin {

  protected def stdflag(config: Config): Seq[String]

  protected def compiler(config: Config): String = config.clang.abs

  protected def forceRebuild(input: Path): Boolean = false

  private def opt(config: Config): String =
    config.mode match {
      case Mode.Debug       => "-O0"
      case Mode.ReleaseFast => "-O2"
      case Mode.ReleaseFull => "-O3"
    }

  def compile(config: Config, inpath: Path): Option[CompilationContext] = {
    val outpath = Paths.get(inpath.abs + LLVM.oExt)

    val context = if (forceRebuild(inpath) || !Files.exists(outpath)) {
      val platformFlags = {
        if (config.targetsWindows) Seq("-g")
        else Nil
      }
      val exceptionsHandling =
        List("-fexceptions", "-fcxx-exceptions", "-funwind-tables")
      val flags = opt(config) +:
        "-fvisibility=hidden" +:
        stdflag(config) ++:
        platformFlags ++:
        exceptionsHandling ++:
        config.compileOptions

      CompilationContext(
        command = Seq(compiler(config)) ++
          LLVM.flto(config) ++
          flags ++
          LLVM.target(config) ++
          Seq("-c", inpath.abs, "-o", outpath.abs),
        result = ObjectFile(outpath)
      )
    } else CompilationContext(Nil, ObjectFile(outpath))

    Some(context)
  }

}

object LlSourcesCompilerPlugin extends ClangSourcesCompilerPlugin {

  override def name = "LlSourcesCompilerPlugin"

  /** LLVM intermediate file extension: ".ll" */
  val llExt = ".ll"

  val extensions = Seq(llExt)

  // LL is generated so always rebuild
  // TODO check if `input` is an SN-generated file
  override def forceRebuild(input: Path) = true

  protected def stdflag(config: Config): Seq[String] = Seq()

}

case object CSourcesCompilerPlugin extends ClangSourcesCompilerPlugin {

  override def name = "CSourcesCompilerPlugin"

  /** C file extension: ".c" */
  val cExt = ".c"

  /** Assembler file extension: ".S" */
  val assemblerExt = ".S"

  /** List of source patterns used: ".c, .S" */
  val extensions = Seq(cExt, assemblerExt)

  protected def stdflag(config: Config): Seq[String] = Seq("-std=gnu11")

}

case object CppSourcesCompilerPlugin extends ClangSourcesCompilerPlugin {

  override def name = "CppSourcesCompilerPlugin"

  /** C++ file extension: ".cpp" */
  val cppExt = ".cpp"

  val extensions = Seq(cppExt)

  protected def stdflag(config: Config): Seq[String] =
    // C++14 or newer standard is needed to compile code using Windows API
    // shipped with Windows 10 / Server 2016+ (we do not plan supporting older versions)
    if (config.targetsWindows) Seq("-std=c++14")
    else Seq("-std=c++11")

  protected override def compiler(config: Config): String = config.clangPP.abs

}

case object RustSourcesCompilerPlugin extends NativeSourcesCompilerPlugin {

  override def name = "RustSourcesCompilerPlugin"

  /** Rust source file extension */
  val rustExt = ".rs"

  val extensions = Seq(rustExt)

  private val compiledCargoCrates = collection.mutable.Set.empty[File]

  def compile(
      config: Config,
      inPath: Path
  ): Option[CompilationContext] = {
    val optional = List(
      config.compilerConfig.targetTriple.map("--target=" + _)
    ).collect {
      case Some(config) => config
    }

    def genStaticLibraryName(name: String): String = {
      if (Platform.isWindows) s"$name.lib"
      else s"lib$name.a"
    }

    val cargoDefinition = {
      def loop(currentDir: Path): Option[File] = {
        if (currentDir.endsWith(NativeLib.nativeCodeDir)) None
        else {
          currentDir
            .toFile()
            .listFiles()
            .find(_.getName().toLowerCase == "cargo.toml")
            .orElse(loop(currentDir.getParent()))
        }
      }
      loop(inPath.getParent())
    }

    def compileUsingCargo(cargoFile: File) = {
      val shouldBuildCrate = compiledCargoCrates.synchronized {
        compiledCargoCrates.add(cargoFile)
      }
      if (!shouldBuildCrate) None
      else
        Some {
          val content = scala.io.Source.fromFile(cargoFile).getLines().toList
          def getScope(name: String) = {
            val header = s"[$name]"
            content
              .dropWhile(!_.trim().startsWith(header))
              .drop(1)
              .takeWhile(!_.trim().startsWith("["))
          }

          def findValue(
              prefix: String
          )(scopeContent: List[String]): Option[String] = {
            scopeContent
              .find(_.trim().startsWith(prefix))
              .map(_.split("="))
              .map {
                case Array(_, rhs) => rhs.trim()
                case arr           => arr.tail.mkString("=")
              }
          }

          def findInLibOrPackage(field: String) =
            findValue(field)(getScope("lib")) orElse
              findValue(field)(getScope("package"))

          val name = findInLibOrPackage("name").map(_.replace("\"", ""))
          val crateTypes = findInLibOrPackage("crate-type").toList.flatMap {
            _.replace("\"", "")
              .stripPrefix("[")
              .stripSuffix("]")
              .split(",")
              .map(_.trim())
          }.toSet

          if (!crateTypes.contains("staticlib")) {
            throw new BuildException(
              s"Rust crete needs to be published as staticlib, currently published as: ${crateTypes
                .mkString(", ")};"
            )
          }

          val (releaseDir, releaseOptions) = config.mode match {
            case Debug                     => "debug" -> None
            case ReleaseFast | ReleaseFull => "release" -> Some("--release")
          }

          val outPath = name
            .fold {
              throw new BuildException(
                "Failed to compute expected name of cargo artifact"
              )
            } { name =>
              Paths.get(
                cargoFile.getParent(),
                "target",
                releaseDir,
                genStaticLibraryName(name)
              )
            }

          CompilationContext(
            command = Discover.cargo.abs ::
              "build" ::
              "--lib" ::
              s"--manifest-path=${cargoFile.getAbsolutePath()}" ::
              "--quiet" ::
              releaseOptions.toList :::
              optional,
            result = Library(outPath)
          )
        }
    }

    def compileStandalone = Some {
      val outPath = inPath.resolveSibling(
        genStaticLibraryName(
          inPath.getFileName().toString.stripSuffix(rustExt)
        )
      )

      CompilationContext(
        command = Discover.rustc.abs ::
          inPath.abs ::
          "-o" + outPath.abs ::
          "--crate-type=staticlib" :: // Need to be compiled as staticlib to include rust stdlib
          optional,
        result = Library(outPath)
      )
    }

    cargoDefinition match {
      case None       => compileStandalone
      case Some(path) => compileUsingCargo(path)
    }
  }

}
