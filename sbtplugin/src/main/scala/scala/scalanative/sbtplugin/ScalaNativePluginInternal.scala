package scala.scalanative
package sbtplugin

import sbt._, Keys._, complete.DefaultParsers._
import scalanative.compiler.{Compiler => NativeCompiler, Opts => NativeOpts}
import ScalaNativePlugin.autoImport._
import scala.util.Try

object ScalaNativePluginInternal {
  private def cpToStrings(cp: Seq[File]): Seq[String] =
    cp.map(_.getAbsolutePath)

  private def cpToString(cp: Seq[File]): String =
    cpToStrings(cp).mkString(java.io.File.pathSeparator)

  private lazy val nativelib =
    Path.userHome / ".scalanative" / ("nativelib-" + nir.Versions.current)

  private def abs(file: File): String =
    file.getAbsolutePath

  /** Compiles application nir to llvm ir. */
  private def compileNir(opts: NativeOpts): Seq[nir.Attr.Link] = {
    val compiler = new NativeCompiler(opts)
    compiler.apply()
  }

  /** Compiles rt to llvm ir using clang. */
  private def unpackRtlib(classpath: Seq[String]): Unit = {
    val nativelibjar = classpath.collectFirst {
      case p if p.contains("scala-native") && p.contains("nativelib") => p
    }.get

    IO.delete(nativelib)
    IO.unzip(file(nativelibjar), nativelib)
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
    val cpppaths = (nativelib ** "*.cpp").get.map(abs)
    val paths    = apppath +: cpppaths
    val linkopts = links.zip(links.map(linkage.get(_))).flatMap {
      case (name, Some("static"))         => Seq("-static", "-l", name)
      case (name, Some("dynamic") | None) => Seq("-l", name)
      case (name, Some(kind)) =>
        throw new MessageOnlyException(s"uknown linkage kind $kind for $name")
    }
    val flags   = Seq("-o", outpath) ++ linkopts ++ opts
    val compile = abs(clang) +: (flags ++ paths)

    Process(compile, target).!
  }

  lazy val projectSettings = Seq(
    libraryDependencies ++= Seq(
      "org.scala-native" %% "nativelib" % nativeVersion,
      "org.scala-native" %% "javalib"   % nativeVersion,
      "org.scala-native" %% "scalalib"  % nativeVersion
    ),
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    nativeVerbose := false,
    nativeClang := {
      val binaryName = "clang++"
      val binaryNames = Seq(("3", "8"), ("3", "7")).flatMap {
        case (major, minor) =>
          Seq(s"$binaryName$major$minor", s"$binaryName-$major.$minor")
      } :+ binaryName

      Process("which" +: binaryNames).lines_!
        .map(file(_))
        .headOption
        .getOrElse {
          throw new MessageOnlyException(
            s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang")
        }
    },
    nativeClangOptions := {
      val includes = ("/usr/local/include" #:: Try(
        Process("llvm-config --includedir").lines_!).getOrElse(Stream.empty))
        .map(s => s"-I$s")
      val libs =
        ("/usr/local/lib" #:: Try(Process("llvm-config --libdir").lines_!)
          .getOrElse(Stream.empty)).map(s => s"-L$s")

      includes #::: libs ++ maybeInjectShared(nativeSharedLibrary.value)
    },
    nativeEmitDependencyGraphPath := None,
    nativeLibraryLinkage := Map(),
    artifactPath in nativeLink := {
      (crossTarget in Compile).value / (moduleName.value + "-out")
    },
    nativeSharedLibrary := false,
    nativeLink := {
      val entry         = (selectMainClass in Compile).value.get.toString + "$"
      val classpath     = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val target        = (crossTarget in Compile).value
      val appll         = target / (moduleName.value + "-out.ll")
      val binary        = (artifactPath in nativeLink).value
      val verbose       = nativeVerbose.value
      val clang         = nativeClang.value
      val clangOpts     = nativeClangOptions.value
      val dotpath       = nativeEmitDependencyGraphPath.value
      val linkage       = nativeLibraryLinkage.value
      val sharedLibrary = nativeSharedLibrary.value
      val opts = new NativeOpts(classpath,
                                abs(appll),
                                dotpath.map(abs),
                                entry,
                                verbose,
                                sharedLibrary)

      checkThatClangIsRecentEnough(clang)

      IO.createDirectory(target)
      unpackRtlib(classpath)
      val links = compileNir(opts).map(_.name)
      compileLl(clang, target, appll, binary, links, linkage, clangOpts)

      binary
    },
    run := {
      val log    = streams.value.log
      val binary = abs(nativeLink.value)
      val args   = spaceDelimited("<arg>").parsed

      log.info("Running " + binary + " " + args.mkString(" "))
      val exitCode = Process(binary, args).!

      val message =
        if (exitCode == 0) None
        else Some("Nonzero exit code: " + exitCode)

      Defaults.toError(message)
    }
  )

  private def maybeInjectShared(lib: Boolean): Seq[String] =
    if (lib) Seq("-shared") else Seq.empty

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
      case none                => None
    }

    def definesBuiltIn(
        pathToClangBinary: Option[String]): Option[Seq[String]] = {
      def commandLineToListBuiltInDefines(clang: String) =
        Seq("echo", "") #| Seq(clang, "-dM", "-E", "-")
      def splitIntoLines(s: String)      = s.split(f"%n")
      def removeLeadingDefine(s: String) = s.substring(s.indexOf(' ') + 1)

      for {
        clang <- pathToClangBinary
        output = commandLineToListBuiltInDefines(clang).!!
        lines  = splitIntoLines(output)
      } yield lines map removeLeadingDefine
    }

    val clang                = maybeFile(pathToClangBinary)
    val defines: Seq[String] = definesBuiltIn(clang).to[Seq].flatten
    val clangIsRecentEnough =
      defines.contains("__DECIMAL_DIG__ __LDBL_DECIMAL_DIG__")

    if (!clangIsRecentEnough) {
      throw new MessageOnlyException(
        s"No recent installation of clang found " +
          s"at $pathToClangBinary.\nSee https://github.com/scala-native/scala-" +
          s"native/blob/master/docs/building.md for details.")
    }
  }
}
