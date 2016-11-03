package scala.scalanative

import java.nio.file.Files
import java.io.{File, PrintWriter}
import java.net.URLClassLoader

import api.NIRCompiler

object Compiler {

  private val allow: String => Boolean =
    n =>
      n.startsWith("scala.scalanative.api.") || !n.startsWith(
        "scala.")

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

  def getCompiler(): NIRCompiler = {
    val clazz =
      classLoader.loadClass("scala.scalanative.NIRCompiler")
    clazz.newInstance match {
      case compiler: NIRCompiler => compiler
      case other =>
        throw new ReflectiveOperationException(
          "Expected an object of type `scala.scalanative.NIRCompiler`, " +
            s"but found `${other.getClass.getName}`.")
    }
  }

  def getCompiler(outDir: File): NIRCompiler = {
    val clazz =
      classLoader.loadClass("scala.scalanative.NIRCompiler")
    val constructor = clazz.getConstructor(classOf[File])
    constructor.newInstance(outDir) match {
      case compiler: NIRCompiler => compiler
      case other =>
        throw new ReflectiveOperationException(
          "Expected an object of type `scala.scalanative.NIRCompiler`, but " +
            s"found `${other.getClass.getName}`.")
    }
  }

  def apply[T](outDir: File)(fn: NIRCompiler => T): T =
    withSources(outDir)(Map.empty) { case (_, compiler) => fn(compiler) }

  def apply[T](fn: NIRCompiler => T): T =
    withSources(Map.empty[String, String]) {
      case (_, compiler) => fn(compiler)
    }

  def withSources[T](outDir: File)(sources: Map[String, String])(
      fn: (File, NIRCompiler) => T): T = {
    val sourcesDir = writeSources(sources)
    fn(sourcesDir, getCompiler(outDir))
  }

  def withSources[T](sources: Map[String, String])(
      fn: (File, NIRCompiler) => T): T = {
    val sourcesDir = writeSources(sources)
    fn(sourcesDir, getCompiler())
  }

  private def writeSources(sources: Map[String, String]): File = {
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
