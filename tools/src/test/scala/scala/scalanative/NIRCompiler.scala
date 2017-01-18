package scala.scalanative

import java.nio.file.Files
import java.io.{File, PrintWriter}
import java.net.URLClassLoader

object NIRCompiler {

  private val allow: String => Boolean =
    n => n.startsWith("scala.scalanative.api.") || !n.startsWith("scala.")

  private val classLoader = {
    val parts = sys
      .props("scalanative.testingcompiler.cp")
      .split(":")
      .map(new java.io.File(_))
      .filter(f => f.exists && f.getName.endsWith(".jar"))
      .map(_.toURI.toURL)

    // We must share some parts of our classpath with the classloader used for the NIR compiler,
    // because we want to be able to cast the NIRCompiler that we get back to its interface and
    // be able to use it seamlessly.
    // We filter out the scala library from out classloader (so that it gets delegated to the
    // scala library that is in `scalanative.testingcompiler.cp`, and we keep `api.NIRCompiler`.
    val parent = new FilteredClassLoader(allow, this.getClass.getClassLoader)
    new URLClassLoader(parts.toArray, parent)
  }

  /**
   * Returns an instance of the NIRCompiler that will compile to a temporary
   * directory.
   *
   * @return An NIRCompiler that will compile to a temporary directory.
   */
  def getCompiler(): api.NIRCompiler = {
    val clazz =
      classLoader.loadClass("scala.scalanative.NIRCompiler")
    clazz.newInstance match {
      case compiler: api.NIRCompiler => compiler
      case other =>
        throw new ReflectiveOperationException(
          "Expected an object of type `scala.scalanative.NIRCompiler`, " +
            s"but found `${other.getClass.getName}`.")
    }
  }

  /**
   * Returns an instance of the NIRCompiler that will compile to `outDir`.
   *
   * @param outDir Where to write all products of compilation.
   * @return An NIRCompiler that will compile to `outDir`.
   */
  def getCompiler(outDir: File): api.NIRCompiler = {
    val clazz =
      classLoader.loadClass("scala.scalanative.NIRCompiler")
    val constructor = clazz.getConstructor(classOf[File])
    constructor.newInstance(outDir) match {
      case compiler: api.NIRCompiler => compiler
      case other =>
        throw new ReflectiveOperationException(
          "Expected an object of type `scala.scalanative.NIRCompiler`, but " +
            s"found `${other.getClass.getName}`.")
    }
  }

  /**
   * Applies `fn` to an NIRCompiler that compiles to `outDir`.
   *
   * @param outDir Where to write all products of compilation.
   * @param fn     The function to apply to the NIRCompiler.
   * @return The result of applying fn to the NIRCompiler
   */
  def apply[T](outDir: File)(fn: api.NIRCompiler => T): T =
    withSources(outDir)(Map.empty) { case (_, compiler) => fn(compiler) }

  /**
   * Applies `fn` to an NIRCompiler that compiles to a temporary directory.
   *
   * @param fn     The function to apply to the NIRCompiler.
   * @return The result of applying fn to the NIRCompiler
   */
  def apply[T](fn: api.NIRCompiler => T): T =
    withSources(Map.empty[String, String]) {
      case (_, compiler) => fn(compiler)
    }

  /**
   * Writes the sources `sources` and applies `fn` to the base directory
   * holding the sources and the NIRCompiler.
   *
   * @param outDir  Where to write all products of compilation.
   * @param sources Map from file name to file content representing the sources.
   * @param fn      The function to apply to the NIRCompiler and the base dir.
   * @return The result of applying `fn` to the NIRCompiler and the base dir.
   */
  def withSources[T](outDir: File)(sources: Map[String, String])(
      fn: (File, api.NIRCompiler) => T): T = {
    val sourcesDir = writeSources(sources)
    fn(sourcesDir, getCompiler(outDir))
  }

  /**
   * Writes the sources `sources` and applies `fn` to the base directory
   * holding the sources and the NIRCompiler.
   *
   * @param sources Map from file name to file content representing the sources.
   * @param fn      The function to apply to the NIRCompiler and the base dir.
   * @return The result of applying `fn` to the NIRCompiler and the base dir.
   */
  def withSources[T](sources: Map[String, String])(
      fn: (File, api.NIRCompiler) => T): T = {
    val sourcesDir = writeSources(sources)
    fn(sourcesDir, getCompiler())
  }

  /**
   * Writes the sources `sources` to a temporary directory.
   *
   * @param sources Map from file name to file content representing the sources.
   * @return The base directory that contains the sources.
   */
  def writeSources(sources: Map[String, String]): File = {
    val baseDir = Files.createTempDirectory("scala-native-sources").toFile()
    sources foreach {
      case (name, content) => makeFile(baseDir, name, content)
    }
    baseDir
  }

  private def makeFile(base: File, name: String, content: String): Unit = {
    val writer = new PrintWriter(new File(base, name))
    writer.write(content)
    writer.close()
  }

}
