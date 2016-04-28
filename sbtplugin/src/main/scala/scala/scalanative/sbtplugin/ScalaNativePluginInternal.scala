package scala.scalanative
package sbtplugin

import sbt._, Keys._, complete.DefaultParsers._
import scalanative.compiler.{Compiler => NativeCompiler, Opts => NativeOpts}
import ScalaNativePlugin.autoImport._

object ScalaNativePluginInternal {
  private def cpToStrings(cp: Seq[File]): Seq[String] =
    cp.map(_.getAbsolutePath)

  private def cpToString(cp: Seq[File]): String =
    cpToStrings(cp).mkString(java.io.File.pathSeparator)

  private lazy val nrt =
    Path.userHome / ".scalanative" / ("nrt-" + nir.Versions.current)

  private lazy val llvm =
    file("/usr/local/Cellar/llvm/HEAD/bin")

  private def abs(file: File): String =
    file.getAbsolutePath

  /** Compiles application nir to llvm ir. */
  private def compileNir(opts: NativeOpts): Unit = {
    val compiler = new NativeCompiler(opts)
    compiler.apply()
  }

  /** Compiles application and runtime llvm ir file to binary using clang. */
  private def compileLl(target: File, appll: File, binary: File): Int = {
    val outpath  = abs(binary)
    val apppath  = abs(appll)
    val nrtpaths = (nrt ** "*.ll").get.map(abs)
    val clang    = Seq(abs(llvm / "clang++"), "-o", outpath, apppath) ++ nrtpaths

    Process(clang, target).!
  }

  lazy val commonProjectSettings = Seq(
    nativeVerbose := false,

    run := {
      val entry     = (mainClass in Compile).value.get.toString
      val classpath = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val target    = (crossTarget in Compile).value
      val appll     = target / (moduleName.value + "-out.ll")
      val binary    = target / (moduleName.value + "-out")
      val verbose   = nativeVerbose.value
      val opts      = new NativeOpts(classpath, appll.getAbsolutePath, entry, verbose)

      println((mainClass in Compile).value)

      IO.createDirectory(target)
      compileNir(opts)
      compileLl(target, appll, binary)
      Process(abs(binary)).!
    }
  )

  lazy val projectSettings = {
    commonProjectSettings ++ Seq(
      addCompilerPlugin("org.scala-native" %% "nscplugin" % "0.1-SNAPSHOT"),

      libraryDependencies += "org.scala-native" %% "rt" % nir.Versions.current
    )
  }
}
