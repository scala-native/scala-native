import sbt._, Keys._

object ScalaNativeBuild extends Build {
  lazy val commonSettings = Seq(
    organization := "org.scala-native",
    version := "0.1-SNAPSHOT",
    libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.2.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    scalaVersion := "2.11.8"
    // Can I have some exhaustivity checking? Pretty please?
    //scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "1000000")
  )

  lazy val nir =
    project.in(file("nir")).
      settings(commonSettings: _*).
      dependsOn(util)

  lazy val util =
    project.in(file("util")).
      settings(commonSettings: _*)

  lazy val compiler =
    project.in(file("compiler")).
      settings(commonSettings: _*).
      settings(
        libraryDependencies += "commonSettingss-io" % "commonSettingss-io" % "2.4"
      ).
      dependsOn(nir)

  lazy val javalib =
    project.in(file("javalib")).
      settings(commonSettings).
      settings(compileWithDottySettings)

  lazy val nativelib =
    project.in(file("nativelib")).
      settings(commonSettings).
      settings(compileWithDottySettings)

  lazy val sandbox =
    project.in(file("sandbox")).
      settings(commonSettings).
      settings(compileWithDottySettings)

  lazy val dummy =
    project.in(file("dummy")).
      settings(commonSettings).
      settings(
        scalaVersion := "2.11.8",
        libraryDependencies += "org.scala-lang" %% "dotty" % "0.1-SNAPSHOT"
      )

  // Compile with dotty
  lazy val compileWithDottySettings = {
    inConfig(Compile)(inTask(compile)(Defaults.runnerTask) ++ Seq(
      // Compile with dotty
      fork in compile := true,

      scalacOptions += "-nir",

      compile := {
        val inputs = (compileInputs in compile).value
        import inputs.config._

        val s = streams.value
        val logger = s.log
        val cacheDir = s.cacheDirectory

        // Discover classpaths

        def cpToString(cp: Seq[File]) =
          cp.map(_.getAbsolutePath).mkString(java.io.File.pathSeparator)

        val compilerCp = Attributed.data((fullClasspath in (dummy, Compile)).value)
        val cpStr = cpToString(classpath ++ compilerCp)

        // List all my dependencies (recompile if any of these changes)

        val allMyDependencies = classpath filterNot (_ == classesDirectory) flatMap { cpFile =>
          if (cpFile.isDirectory) (cpFile ** "*.class").get
          else Seq(cpFile)
        }

        // Compile

        val cachedCompile = FileFunction.cached(cacheDir / "compile",
            FilesInfo.lastModified, FilesInfo.exists) { dependencies =>

          logger.info(
              "Compiling %d Scala sources to %s..." format (
              sources.size, classesDirectory))

          if (classesDirectory.exists)
            IO.delete(classesDirectory)
          IO.createDirectory(classesDirectory)

          val sourcesArgs = sources.map(_.getAbsolutePath()).toList

          /* run.run() below in doCompile() will emit a call to its
           * logger.info("Running dotty.tools.dotc.Main [...]")
           * which we do not want to see. We use this patched logger to
           * filter out that particular message.
           */
          val patchedLogger = new Logger {
            def log(level: Level.Value, message: => String) = {
              val msg = message
              if (level != Level.Info ||
                  !msg.startsWith("Running dotty.tools.dotc.Main"))
                logger.log(level, msg)
            }
            def success(message: => String) = logger.success(message)
            def trace(t: => Throwable) = logger.trace(t)
          }

          def doCompile(sourcesArgs: List[String]): Unit = {
            val run = (runner in compile).value
            run.run("dotty.tools.dotc.Main", compilerCp,
                "-classpath" :: cpStr ::
                "-d" :: classesDirectory.getAbsolutePath() ::
                options ++:
                sourcesArgs,
                patchedLogger) foreach sys.error
          }

          // Work around the Windows limitation on command line length.
          val isWindows =
            System.getProperty("os.name").toLowerCase().indexOf("win") >= 0
          if ((fork in compile).value && isWindows &&
              (sourcesArgs.map(_.length).sum > 1536)) {
            IO.withTemporaryFile("sourcesargs", ".txt") { sourceListFile =>
              IO.writeLines(sourceListFile, sourcesArgs)
              doCompile(List("@"+sourceListFile.getAbsolutePath()))
            }
          } else {
            doCompile(sourcesArgs)
          }

          // Output is all files in classesDirectory
          (classesDirectory ** AllPassFilter).get.toSet
        }

        cachedCompile((sources ++ allMyDependencies).toSet)

        // We do not have dependency analysis when compiling externally
        sbt.inc.Analysis.Empty
      }
    ))
  }
}
