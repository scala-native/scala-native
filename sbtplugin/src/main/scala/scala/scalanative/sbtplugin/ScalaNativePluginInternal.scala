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

  /** Compiles nrt to llvm ir using clang. */
  private def compileNrt(classpath: Seq[String]): Int = {
    val nrtjar   = classpath.collectFirst {
      case p if p.contains("org.scala-native") && p.contains("nrt") => p
    }.get
    val srcfiles = (nrt ** "*.c").get.map(abs)
    val clang    = Seq(abs(llvm / "clang"), "-S", "-emit-llvm") ++ srcfiles

    IO.delete(nrt)
    IO.unzip(file(nrtjar), nrt)
    Process(clang, nrt).!
  }

  /** Links application and runtime ir into a single bitcode file. */
  private def linkLl(target: File, appll: File, linkedll: File): Int = {
    val outpath  = abs(linkedll)
    val apppath  = abs(appll)
    val nrtpaths = (nrt ** "*.ll").get.map(abs)
    val link     = Seq(abs(llvm / "llvm-link"), s"-o=$outpath", apppath) ++ nrtpaths

    Process(link, target).!
  }

  /** Compiles linked application and runtime llvm ir file to assembly using llc. */
  private def compileLl(target: File, linkedll: File, linkedasm: File): Int = {
    val inpath  = abs(linkedll)
    val outpath = abs(linkedasm)
    val llc     = Seq(abs(llvm / "llc"), s"-o=$outpath", inpath)

    Process(llc, target).!
  }

  /** Compiles assembly to object file using as. */
  private def compileAsm(target: File, linkedasm: File, linkedobj: File): Unit = {
    val inpath  = abs(linkedasm)
    val outpath = abs(linkedobj)
    val as      = Seq("as", s"-o", outpath, inpath)

    Process(as, target).!
  }

  /** Links assembly-generated object files and generates a native binary using ld. */
  private def linkAsm(target: File, linkedobj: File, binary: File): Unit = {
    val inpath  = abs(linkedobj)
    val outpath = abs(binary)
    val ld      = Seq("ld", "-o", outpath, "-e", "_main", inpath)

    Process(ld, target).!
  }

  lazy val commonProjectSettings = Seq(
    nativeVerbose := false,

    nativeCompile := {
      val entry     = (OptSpace ~> StringBasic).parsed
      val classpath = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val target    = (crossTarget in Compile).value
      val appll     = target / (moduleName.value + "-app.ll")
      val linkedll  = target / (moduleName.value + "-linked.bc")
      val linkedasm = target / (moduleName.value + "-linked.s")
      val linkedobj = target / (moduleName.value + "-linked.o")
      val binary    = target / (moduleName.value + "-out")
      val verbose   = nativeVerbose.value
      val opts      = new NativeOpts(classpath, appll.getAbsolutePath, entry, verbose)

      IO.createDirectory(target)
      compileNir(opts)
      compileNrt(classpath)
      linkLl(target, appll, linkedll)
      compileLl(target, linkedll, linkedasm)
      compileAsm(target, linkedasm, linkedobj)
      linkAsm(target, linkedobj, binary)
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
