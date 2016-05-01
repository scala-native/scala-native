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

  private lazy val rtlib =
    Path.userHome / ".scalanative" / ("rtlib-" + nir.Versions.current)

  private lazy val llvm =
    file("/usr/local/Cellar/llvm/HEAD/bin")

  private def abs(file: File): String =
    file.getAbsolutePath

  /** Compiles application nir to llvm ir. */
  private def compileNir(opts: NativeOpts): Unit = {
    val compiler = new NativeCompiler(opts)
    compiler.apply()
  }

  /** Compiles rt to llvm ir using clang. */
  private def compileRtlib(classpath: Seq[String]): Int = {
    val rtlibjar = classpath.collectFirst {
      case p if p.contains("org.scala-native") && p.contains("rtlib") => p
    }.get
    IO.delete(rtlib)
    IO.unzip(file(rtlibjar), rtlib)

    val srcfiles = (rtlib ** "*.cpp").get.map(abs)
    val clang    = Seq(abs(llvm / "clang++"), "-S", "-emit-llvm") ++ srcfiles
    Process(clang, rtlib).!
  }

  /** Compiles application and runtime llvm ir file to binary using clang. */
  private def compileLl(target: File, appll: File, binary: File): Int = {
    val outpath = abs(binary)
    val apppath = abs(appll)
    val rtpaths = (rtlib ** "*.ll").get.map(abs)
    val paths   = apppath +: rtpaths
    val flags   = Seq("-o", outpath,
                      "-l", "gc")
    val clang   = abs(llvm / "clang++") +: (flags ++ paths)

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

      IO.createDirectory(target)
      compileRtlib(classpath)
      compileNir(opts)
      compileLl(target, appll, binary)
      Process(abs(binary)).!
    }
  )

  lazy val projectSettings = {
    commonProjectSettings ++ Seq(
      addCompilerPlugin("org.scala-native" %% "nscplugin" % "0.1-SNAPSHOT"),

      libraryDependencies += "org.scala-native" %% "rtlib" % nir.Versions.current
    )
  }
}
