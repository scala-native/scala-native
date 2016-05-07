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

  private def abs(file: File): String =
    file.getAbsolutePath

  /** Compiles application nir to llvm ir. */
  private def compileNir(opts: NativeOpts): Unit = {
    val compiler = new NativeCompiler(opts)
    compiler.apply()
  }

  /** Compiles rt to llvm ir using clang. */
  private def unpackRtlib(classpath: Seq[String]): Unit = {
    val rtlibjar = classpath.collectFirst {
      case p if p.contains("org.scala-native") && p.contains("rtlib") => p
    }.get

    IO.delete(rtlib)
    IO.unzip(file(rtlibjar), rtlib)
  }

  /** Compiles application and runtime llvm ir file to binary using clang. */
  private def compileLl(clang: File,
                        target: File,
                        appll: File,
                        binary: File,
                        opts: Seq[String]): Unit = {
    val outpath = abs(binary)
    val apppath = abs(appll)
    val rtpaths = (rtlib ** "*.cpp").get.map(abs)
    val paths   = apppath +: rtpaths
    val flags   = Seq("-o", outpath,
                      "-l", "gc") ++ opts
    val compile = abs(clang) +: (flags ++ paths)

    Process(compile, target).!
  }

  lazy val projectSettings = Seq(
    addCompilerPlugin("org.scala-native" %% "nscplugin" % "0.1-SNAPSHOT"),

    libraryDependencies += "org.scala-native" %% "rtlib" % nir.Versions.current,

    nativeVerbose := false,

    nativeClang := file(Process(Seq("which", "clang++")).lines_!.head),

    nativeClangOptions := Seq(),

    nativeEmitDependencyGraphPath := None,

    run := {
      val entry     = (mainClass in Compile).value.get.toString
      val classpath = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val target    = (crossTarget in Compile).value
      val appll     = target / (moduleName.value + "-out.ll")
      val binary    = target / (moduleName.value + "-out")
      val verbose   = nativeVerbose.value
      val clang     = nativeClang.value
      val clangOpts = nativeClangOptions.value
      val dotpath   = nativeEmitDependencyGraphPath.value
      val opts      = new NativeOpts(classpath,
                                     abs(appll),
                                     dotpath.map(abs),
                                     entry,
                                     verbose)

      IO.createDirectory(target)
      unpackRtlib(classpath)
      compileNir(opts)
      compileLl(clang, target, appll, binary, clangOpts)
      Process(abs(binary)).!
    }
  )
}
