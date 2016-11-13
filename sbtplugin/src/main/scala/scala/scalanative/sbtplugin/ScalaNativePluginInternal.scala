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

  private lazy val nativelib: File =
    Path.userHome / ".scalanative" / ("nativelib-" + nir.Versions.current)

  private lazy val includes = {
    val includedir =
      Try(Process("llvm-config --includedir").lines_!.toSeq)
        .getOrElse(Seq.empty)
    ("/usr/local/include" +: includedir).map(s => s"-I$s")
  }

  private lazy val libs = {
    val libdir =
      Try(Process("llvm-config --libdir").lines_!.toSeq).getOrElse(Seq.empty)
    ("/usr/local/lib" +: libdir).map(s => s"-L$s")
  }

  private def abs(file: File): String =
    file.getAbsolutePath

  private def discover(binaryName: String,
                       binaryVersions: Seq[(String, String)]): File = {
    val binaryNames = binaryVersions.flatMap {
      case (major, minor) =>
        Seq(s"$binaryName$major$minor", s"$binaryName-$major.$minor")
    } :+ binaryName

    Process("which" +: binaryNames).lines_!.map(file(_)).headOption.getOrElse {
      throw new MessageOnlyException(
        s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang")
    }
  }

  /** Compiles application nir to llvm ir. */
  private def compileNir(opts: NativeOpts): Seq[nir.Attr.Link] = {
    val compiler = new NativeCompiler(opts)
    compiler.apply()
  }

  /** Compiles *.c[pp] in `cwd`. */
  def compileCSources(clang: File, clangpp: File, cwd: File): Boolean = {
    val cpaths     = (cwd ** "*.c").get.map(abs)
    val cpppaths   = (cwd ** "*.cpp").get.map(abs)
    val compilec   = abs(clang) +: (includes ++ ("-c" +: cpaths))
    val compilecpp = abs(clangpp) +: (includes ++ ("-c" +: cpppaths))

    val cExit   = Process(compilec, cwd).!
    val cppExit = Process(compilecpp, cwd).!

    cExit == 0 && cppExit == 0
  }

  /** Compiles rt to llvm ir using clang. */
  private def unpackRtlib(clang: File,
                          clangpp: File,
                          classpath: Seq[String]): Boolean = {
    val nativelibjar = classpath.collectFirst {
      case p if p.contains("scala-native") && p.contains("nativelib") =>
        file(p)
    }.get
    val jarhash     = Hash(nativelibjar).toSeq
    val jarhashfile = nativelib / "jarhash"
    def bootstrapped =
      nativelib.exists &&
        jarhashfile.exists &&
        jarhash == IO.readBytes(jarhashfile).toSeq

    if (!bootstrapped) {
      IO.delete(nativelib)
      IO.unzip(nativelibjar, nativelib)
      IO.write(jarhashfile, Hash(nativelibjar))

      compileCSources(clang, clangpp, nativelib)
    } else {
      true
    }
  }

  /** Compiles application and runtime llvm ir file to binary using clang. */
  private def compileLl(clangpp: File,
                        target: File,
                        appll: File,
                        binary: File,
                        links: Seq[String],
                        linkage: Map[String, String],
                        opts: Seq[String]): Unit = {
    val outpath = abs(binary)
    val apppath = abs(appll)
    val opaths  = (nativelib ** "*.o").get.map(abs)
    val paths   = apppath +: opaths
    val linkopts = links.zip(links.map(linkage.get(_))).flatMap {
      case (name, Some("static"))         => Seq("-static", "-l", name)
      case (name, Some("dynamic") | None) => Seq("-l", name)
      case (name, Some(kind)) =>
        throw new MessageOnlyException(s"uknown linkage kind $kind for $name")
    }
    val flags   = Seq("-o", outpath) ++ linkopts ++ opts
    val compile = abs(clangpp) +: (flags ++ paths)

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
    nativeEmitDependencyGraphPath := None,
    nativeLibraryLinkage := Map(),
    nativeSharedLibrary := false,
    nativeClang := {
      discover("clang", Seq(("3", "8"), ("3", "7")))
    },
    nativeClangPP := {
      discover("clang++", Seq(("3", "8"), ("3", "7")))
    },
    nativeClangOptions := {
      // We need to add `-lrt` for the POSIX realtime lib, which doesn't exist
      // on macOS.
      val lrt = Option(sys props "os.name") match {
        case Some("Linux") => Seq("-lrt")
        case _             => Seq()
      }
      includes ++ libs ++ maybeInjectShared(nativeSharedLibrary.value) ++ lrt
    },
    artifactPath in nativeLink := {
      (crossTarget in Compile).value / (moduleName.value + "-out")
    },
    nativeLink := {
      val mainClass = (selectMainClass in Compile).value.getOrElse(
        throw new MessageOnlyException("No main class detected.")
      )
      val entry         = mainClass.toString + "$"
      val classpath     = cpToStrings((fullClasspath in Compile).value.map(_.data))
      val target        = (crossTarget in Compile).value
      val appll         = target / (moduleName.value + "-out.ll")
      val binary        = (artifactPath in nativeLink).value
      val verbose       = nativeVerbose.value
      val clang         = nativeClang.value
      val clangpp       = nativeClangPP.value
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

      val nirFiles   = (Keys.target.value ** "*.nir").get.toSet
      val configFile = (streams.value.cacheDirectory / "native-config")
      val inputFiles = nirFiles + configFile

      writeConfigHash(configFile,
                      opts,
                      clang,
                      clangpp,
                      classpath,
                      target,
                      appll,
                      binary,
                      linkage,
                      clangOpts)

      val compileIfChanged =
        FileFunction.cached(streams.value.cacheDirectory / "native-cache",
                            FilesInfo.hash) {
          _ =>
            IO.createDirectory(target)
            val unpackSuccess = unpackRtlib(clang, clangpp, classpath)
            if (unpackSuccess) {
              val links = compileNir(opts).map(_.name)
              compileLl(clangpp,
                        target,
                        appll,
                        binary,
                        links,
                        linkage,
                        clangOpts)
              Set(binary)
            } else {
              throw new MessageOnlyException("Couldn't unpack nativelib.")
            }
        }

      val _ = compileIfChanged(inputFiles)
      binary
    },
    run := {
      val log    = streams.value.log
      val binary = abs(nativeLink.value)
      val args   = spaceDelimited("<arg>").parsed

      log.info("Running " + binary + " " + args.mkString(" "))
      val exitCode = Process(binary +: args).!

      val message =
        if (exitCode == 0) None
        else Some("Nonzero exit code: " + exitCode)

      Defaults.toError(message)
    }
  )

  private def writeConfigHash(file: File, config: Any*): Unit = {
    val _ = config.## // Force evaluation of lazy structures
    IO.write(file, Hash(config.toString))
  }

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
