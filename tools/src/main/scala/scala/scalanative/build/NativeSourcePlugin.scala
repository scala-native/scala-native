package scala.scalanative
package build

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scalanative.build.IO.RichPath
import scalanative.build.Mode._

import NativeSourcePlugin._

trait NativeSourcePlugin {

  def extensions: Seq[String]

  def compile(config: Config, path: Path): Option[CompilationContext]

}

object NativeSourcePlugin {

  case class CompilationContext(
      command: Seq[String],
      result: CompilationResult
  )

}

sealed abstract class ClangSourcePlugin extends NativeSourcePlugin {

  protected def stdflag(config: Config): Seq[String]

  protected def compiler(config: Config): String = config.clang.abs

  protected def forceRebuild: Boolean = false

  private def opt(config: Config): String =
    config.mode match {
      case Mode.Debug       => "-O0"
      case Mode.ReleaseFast => "-O2"
      case Mode.ReleaseFull => "-O3"
    }

  def compile(config: Config, inpath: Path): Option[CompilationContext] = {
    val outpath = Paths.get(inpath.abs + LLVM.oExt)
    val alreadyExists = Files.exists(outpath)
    if (forceRebuild || !alreadyExists) Some {
      val platformFlags = {
        if (config.targetsWindows) Seq("-g")
        else Nil
      }
      val flags = opt(config) +: "-fvisibility=hidden" +:
        stdflag(config) ++: platformFlags ++: config.compileOptions

      CompilationContext(
        command = Seq(compiler(config)) ++
          LLVM.flto(config) ++
          flags ++
          LLVM.target(config) ++
          Seq("-c", inpath.abs, "-o", outpath.abs),
        result = ObjectFile(outpath)
      )
    }
    else if (alreadyExists) Some(CompilationContext(Nil, ObjectFile(outpath)))
    else None
  }

}

case object LlSourcePlugin extends ClangSourcePlugin {

  /** LLVM intermediate file extension: ".ll" */
  val llExt = ".ll"

  val extensions = Seq(llExt)

  // LL is generated so always rebuild
  override def forceRebuild = true

  protected def stdflag(config: Config): Seq[String] = Seq()

}

case object CSourcePlugin extends ClangSourcePlugin {

  /** C file extension: ".c" */
  val cExt = ".c"

  /** Assembler file extension: ".S" */
  val assemblerExt = ".S"

  /** List of source patterns used: ".c, .cpp, .S" */
  val extensions = Seq(cExt, assemblerExt)

  protected def stdflag(config: Config): Seq[String] = Seq("-std=gnu11")

}

case object CppSourcePlugin extends ClangSourcePlugin {

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

case object RustSourcePlugin extends NativeSourcePlugin {

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
