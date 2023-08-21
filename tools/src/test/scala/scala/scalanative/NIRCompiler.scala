package scala.scalanative

import java.nio.file.{Files, Path}
import java.io.{File, PrintWriter}
import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import java.lang.ProcessBuilder
import java.nio.charset.StandardCharsets

object NIRCompiler {

  /** Returns an instance of the NIRCompiler that will compile to a temporary
   *  directory.
   *
   *  @return
   *    An NIRCompiler that will compile to a temporary directory.
   */
  def getCompiler(): api.NIRCompiler =
    new NIRCompilerImpl()

  /** Returns an instance of the NIRCompiler that will compile to `outDir`.
   *
   *  @param outDir
   *    Where to write all products of compilation.
   *  @return
   *    An NIRCompiler that will compile to `outDir`.
   */
  def getCompiler(outDir: Path): api.NIRCompiler =
    new NIRCompilerImpl(outDir)

  /** Applies `fn` to an NIRCompiler that compiles to `outDir`.
   *
   *  @param outDir
   *    Where to write all products of compilation.
   *  @param fn
   *    The function to apply to the NIRCompiler.
   *  @return
   *    The result of applying fn to the NIRCompiler
   */
  def apply[T](outDir: Path)(fn: api.NIRCompiler => T): T =
    withSources(outDir)(Map.empty) { case (_, compiler) => fn(compiler) }

  /** Applies `fn` to an NIRCompiler that compiles to a temporary directory.
   *
   *  @param fn
   *    The function to apply to the NIRCompiler.
   *  @return
   *    The result of applying fn to the NIRCompiler
   */
  def apply[T](fn: api.NIRCompiler => T): T =
    withSources(Map.empty[String, String]) {
      case (_, compiler) => fn(compiler)
    }

  /** Writes the sources `sources` and applies `fn` to the base directory
   *  holding the sources and the NIRCompiler.
   *
   *  @param outDir
   *    Where to write all products of compilation.
   *  @param sources
   *    Map from file name to file content representing the sources.
   *  @param fn
   *    The function to apply to the NIRCompiler and the base dir.
   *  @return
   *    The result of applying `fn` to the NIRCompiler and the base dir.
   */
  def withSources[T](
      outDir: Path
  )(sources: Map[String, String])(fn: (Path, api.NIRCompiler) => T): T = {
    val sourcesDir = writeSources(sources)
    fn(sourcesDir, getCompiler(outDir))
  }

  /** Writes the sources `sources` and applies `fn` to the base directory
   *  holding the sources and the NIRCompiler.
   *
   *  @param sources
   *    Map from file name to file content representing the sources.
   *  @param fn
   *    The function to apply to the NIRCompiler and the base dir.
   *  @return
   *    The result of applying `fn` to the NIRCompiler and the base dir.
   */
  def withSources[T](
      sources: Map[String, String]
  )(fn: (Path, api.NIRCompiler) => T): T = {
    val sourcesDir = writeSources(sources)
    fn(sourcesDir, getCompiler())
  }

  /** Writes the sources `sources` to a temporary directory.
   *
   *  @param sources
   *    Map from file name to file content representing the sources.
   *  @return
   *    The base directory that contains the sources.
   */
  def writeSources(sources: Map[String, String]): Path = {
    val baseDir = Files.createTempDirectory("scala-native-sources")
    sources foreach {
      case (name, content) => makeFile(baseDir, name, content)
    }
    baseDir
  }

  private def makeFile(base: Path, name: String, content: String): Unit = {
    val writer = new PrintWriter(Files.newBufferedWriter(base.resolve(name)))
    writer.write(content)
    writer.close()
  }

}

class NIRCompilerImpl(outDir: Path) extends api.NIRCompiler {

  def this() = this(Files.createTempDirectory("scala-native-target"))

  override def compile(base: Path): Array[Path] = {
    val files = getFiles(base.toFile(), _.getName endsWith ".scala")
    compile(files)
  }

  override def compile(source: String): Array[Path] = {
    val tempFile = File.createTempFile("scala-native-input", ".scala").toPath
    val p = Files.write(tempFile, source.getBytes(StandardCharsets.UTF_8))
    compile(Seq(p.toFile()))
  }

  private def compile(files: Seq[File]): Array[Path] = {
    val mainClass =
      if (ScalaNativeBuildInfo.scalaVersion.startsWith("3"))
        "dotty.tools.dotc.Main"
      else "scala.tools.nsc.Main"
    val outPath = outDir.toAbsolutePath()
    val fileArgs = files.map(_.getAbsolutePath())
    // Invoke Scala compiler as an external process to compile Scala program into NIR
    // We don't use testingCompiler that classload (which isn't supported by SN) the Scala compiler to native compile `tools`.
    val args = Seq(
      "java",
      "-cp",
      ScalaNativeBuildInfo.scalacJars,
      mainClass,
      "-d",
      outPath.toString(),
      "-cp",
      ScalaNativeBuildInfo.compileClasspath + File.pathSeparator + ScalaNativeBuildInfo.nativelibCp,
      s"-Xplugin:${ScalaNativeBuildInfo.pluginJar}"
    ) ++ fileArgs
    val procBuilder = new ProcessBuilder(args: _*)
    val cmd = args.mkString(" ")
    val proc = procBuilder.start()
    if (proc.waitFor() != 0) {
      val stderr =
        scala.io.Source.fromInputStream(proc.getErrorStream()).mkString
      throw new CompilationFailedException(stderr)
    }

    val acceptedExtension = Seq(".class", ".tasty", ".nir")
    getFiles(
      outPath.toFile,
      f => acceptedExtension.exists(f.getName().endsWith)
    ).map(_.toPath()).toArray
  }

  /** List of the files contained in `base` that sastisfy `filter`
   */
  private def getFiles(base: File, filter: File => Boolean): Seq[File] =
    (if (filter(base)) Seq(base) else Seq.empty) ++
      (Option(base.listFiles()) getOrElse Array.empty[File] flatMap (getFiles(
        _,
        filter
      )))

}

// TODO: integrate with api.CompilationFailedException
class CompilationFailedException(stderr: String)
    extends RuntimeException(stderr)
