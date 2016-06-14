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
  private def compileNir(opts: NativeOpts): Seq[nir.Attr.Link] = {
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
                        links: Seq[String],
                        linkage: Map[String, String],
                        opts: Seq[String]): Unit = {
    val outpath  = abs(binary)
    val apppath  = abs(appll)
    val rtpaths  = (rtlib ** "*.cpp").get.map(abs)
    val paths    = apppath +: rtpaths
    val linkopts = links.zip(links.map(linkage.get(_))).flatMap {
      case (name, Some("static"))         => Seq("-static", "-l", name)
      case (name, Some("dynamic") | None) => Seq("-l", name)
      case (name, Some(kind)) =>
        throw new MessageOnlyException(s"uknown linkage kind $kind for $name")
    }
    val flags    = Seq("-o", outpath) ++ linkopts ++ opts
    val compile  = abs(clang) +: (flags ++ paths)

    Process(compile, target).!
  }

  lazy val projectSettings = Seq(
    addCompilerPlugin("org.scala-native" % "nscplugin" % "0.1-SNAPSHOT" cross CrossVersion.full),

    libraryDependencies += "org.scala-native" %% "rtlib" % nir.Versions.current,

    nativeVerbose := false,

    nativeClang := {
      val binaryName = "clang++"
      val binaryNames = Seq(("3", "8"), ("3", "7")).flatMap {
        case (major, minor) => Seq(s"$binaryName$major$minor", s"$binaryName-$major.$minor")
      } :+ binaryName

      Process("which" +: binaryNames)
        .lines_!
        .map(file(_))
        .headOption
        .getOrElse{
          throw new MessageOnlyException(s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang")
        }
    },

    nativeClangOptions := Seq("-I/usr/local/include", "-L/usr/local/lib"),

    nativeEmitDependencyGraphPath := None,

    nativeLibraryLinkage := Map(),

    artifactPath in nativeLink := {
      (crossTarget in Compile).value / (moduleName.value + "-out")
    },

    nativeLink := {
      val entry     = (mainClass in Compile).value.get.toString + "$"
      val classpath = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val target    = (crossTarget in Compile).value
      val appll     = target / (moduleName.value + "-out.ll")
      val binary    = (artifactPath in nativeLink).value
      val verbose   = nativeVerbose.value
      val clang     = nativeClang.value
      val clangOpts = nativeClangOptions.value
      val dotpath   = nativeEmitDependencyGraphPath.value
      val linkage   = nativeLibraryLinkage.value
      val opts      = new NativeOpts(classpath,
                                     abs(appll),
                                     dotpath.map(abs),
                                     entry,
                                     verbose)

      checkThatClangIsRecentEnough(clang)

      IO.createDirectory(target)
      unpackRtlib(classpath)
      val links = compileNir(opts).map(_.name)
      compileLl(clang, target, appll, binary, links, linkage, clangOpts)

      binary
    },

    run := {
      val binary = nativeLink.value

      Process(abs(binary)).!
    }
  )

  /**
    * Tests whether the clang compiler is recent enough.
    * <p/>
    * This is determined through looking up a built-in #define which is
    * more reliable than testing for a specific version.
    * <p/>
    * It might be better to use feature checking macros:
    * http://clang.llvm.org/docs/LanguageExtensions.html#feature-checking-macros
    */
  private def checkThatClangIsRecentEnough(pathToClangBinary: File): Unit = {
    def maybeFile(f: File) = f match {
      case file if file.exists => Some(abs(file))
      case none => None
    }

    def definesBuiltIn(pathToClangBinary: Option[String]): Option[Seq[String]] = {
      def commandLineToListBuiltInDefines(clang: String) =
        Seq("echo", "") #| Seq(clang, "-dM", "-E", "-")
      def splitIntoLines(s: String) = s.split(f"%n")
      def removeLeadingDefine(s: String) = s.substring(s.indexOf(' ') +1)

      for {
        clang  <- pathToClangBinary
        output  = commandLineToListBuiltInDefines(clang).!!
        lines   = splitIntoLines(output)
      } yield lines map removeLeadingDefine
    }

    val clang = maybeFile(pathToClangBinary)
    val defines: Seq[String] = definesBuiltIn(clang).to[Seq].flatten
    val clangIsRecentEnough = defines.contains("__DECIMAL_DIG__ __LDBL_DECIMAL_DIG__")

    if (!clangIsRecentEnough) {
      throw new MessageOnlyException(s"No recent installation of clang found " +
        s"at $pathToClangBinary.\nSee https://github.com/scala-native/scala-" +
        s"native/blob/master/docs/building.md for details.")
    }
  }
}
