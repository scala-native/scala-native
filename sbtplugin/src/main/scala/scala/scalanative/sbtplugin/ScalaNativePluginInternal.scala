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

  /** Compiles application nir to llvm ir. */
  private def compileNir(opts: NativeOpts): Unit = {
    IO.createDirectory(file(opts.outpath).getParentFile)
    val compiler = new NativeCompiler(opts)
    compiler.apply()
  }

  /** Compiles nrt to llvm ir using clang. */
  private def compileNrt(classpath: Seq[String]): Int = {
    val dest     = Path.userHome / ".scalanative" / ("nrt-" + nir.Versions.current)
    val nrtjar   = classpath.collectFirst {
      case p if p.contains("org.scala-native") && p.contains("nrt") => p
    }.get
    val srcfiles = (dest ** "*.c").get.map(_.getAbsolutePath)
    val cmd      = Seq("clang", "-S", "-emit-llvm") ++ srcfiles

    IO.delete(dest)
    IO.unzip(file(nrtjar), dest)
    Process(cmd, dest).!
  }

  /** Compiles runtime and application llvm ir files to assembly using llc. */
  private def compileLl(): Unit = {}

  /** Compiles assembly to object file using as. */
  private def compileAsm(): Unit = {}

  /** Links assembly-generated object files and generates a native binary using ld. */
  private def linkAsm(): Unit = {}

  lazy val commonProjectSettings = Seq(
    artifactPath :=
      (crossTarget in Compile).value / (moduleName.value + "-out.ll"),

    nativeVerbose := false,

    nativeCompile := {
      val entry     = (OptSpace ~> StringBasic).parsed
      val classpath = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val outfile   = (artifactPath in Compile).value.getAbsolutePath
      val debug     = nativeVerbose.value
      val opts      = new NativeOpts(classpath, outfile, entry, debug)

      compileNir(opts)
      compileNrt(classpath)
      compileLl()
      compileAsm()
      linkAsm()
    },

    nativeRun := {
      ???
    }
  )

  lazy val projectSettings = {
    commonProjectSettings ++ Seq(
      addCompilerPlugin("org.scala-native" %% "nscplugin" % "0.1-SNAPSHOT"),

      libraryDependencies += "org.scala-native" %% "nrt" % nir.Versions.current
    )
  }
}
