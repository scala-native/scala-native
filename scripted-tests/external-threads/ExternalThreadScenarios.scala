import sbt._

import java.util.UUID
import java.util.concurrent.TimeUnit

import scala.sys.process.Process

import scala.scalanative.build.{BuildTarget, Discover, GC, LTO, NativeConfig}

/** Build native hosts and run every applicable catalog entry through scripted.
 */
object ExternalThreadScenarios {
  import ExternalThreadScenarioModel._

  def configurations(root: File): Seq[Configuration] = {
    val rows = dataLines(IO.readLines(root / "configurations.tsv"))
      .map(Configuration.parse)
    require(
      rows.nonEmpty && rows.map(_.name).distinct.size == rows.size,
      "Empty or duplicate configuration names"
    )
    rows
  }
  def records(root: File): Seq[Scenario] = {
    val rows =
      dataLines(IO.readLines(root / "scenarios.tsv")).map(Scenario.parse)
    require(
      rows.nonEmpty && rows.map(_.id).distinct.size == rows.size,
      "Empty or duplicate scenario IDs"
    )
    rows
  }
  def configuration(
      base: NativeConfig,
      root: File,
      name: String
  ): NativeConfig =
    configurations(root)
      .find(_.name == name)
      .getOrElse(sys.error("Unknown configuration: " + name))
      .configure(base)

  def virtualThreadsAvailable(scalaVersion: String): Boolean = {
    val os = sys.props("os.name").toLowerCase
    val arch = sys.props("os.arch").toLowerCase
    scalaVersion.startsWith("3.") && sys
      .props("java.specification.version")
      .toInt >= 21 &&
      Seq("linux", "mac", "freebsd", "openbsd", "netbsd").exists(os.contains) &&
      Set("x86_64", "amd64", "aarch64", "arm64")(arch)
  }

  def prepare(
      root: File,
      out: File,
      cfg: NativeConfig,
      library: File,
      name: String,
      virtualThreads: Boolean
  ): Unit = {
    records(root)
    val hosts = out / "configurations" / name
    IO.delete(hosts)
    IO.createDirectory(hosts)
    val platform = ExternalThreadPlatform.current
    val preparedLibrary =
      ExternalThreadPlatform.archiveLibrary(library, hosts, cfg)
    val cppExceptions = platform.windows ||
      (!cfg.cppOptions.contains("-fno-cxx-exceptions") &&
      (cfg.cppOptions.contains("-fcxx-exceptions") || cfg.lto != LTO.none))
    val options =
      (if (virtualThreads) Seq("-DEXTERNAL_VIRTUAL_THREAD_SUPPORTED")
       else Nil) ++
        (if (cfg.buildTarget == BuildTarget.libraryDynamic)
           Seq("-DEXTERNAL_SHARED_LIBRARY")
         else Nil) ++
        (if (cppExceptions) Seq("-DEXTERNAL_CPP_EXCEPTIONS") else Nil) ++
        (if (cfg.gc != GC.none) Seq("-DEXTERNAL_COLLECTING_GC") else Nil) ++
        (if (Set(GC.immix, GC.commix)(cfg.gc)) Seq("-DEXTERNAL_RECLAIMING_GC")
         else Nil)
    for ((language, compiler, standard, extension) <- Seq(
          ("c", Discover.clang(), "-std=c11", "c"),
          ("cpp", Discover.clangpp(), platform.cppStandard, "cpp")
        )) {
      val obj = hosts / (language + ".o")
      val compile = Seq(
        compiler.toString,
        standard,
        "-I" + (root / "src/main/native").getPath,
        "-c",
        (root / "src/main/native" / ("main." + extension)).getPath,
        "-o",
        obj.getPath
      ) ++ platform.compileOptions ++ options
      require(
        Process(compile, root).! == 0,
        "Host compilation failed: " + language
      )
      val link = Seq(
        Discover.clangpp().toString,
        obj.getPath,
        ExternalThreadPlatform.linkLibrary(preparedLibrary, cfg).getPath,
        "-o",
        (hosts / platform.executable(language)).getPath
      ) ++
        platform.libraryLinkOptions(preparedLibrary, cfg)
      require(Process(link, root).! == 0, "Host linking failed: " + language)
    }
  }

  def runAll(
      root: File,
      out: File,
      cfg: NativeConfig,
      name: String,
      virtualThreads: Boolean,
      log: Logger
  ): Unit = {
    val hosts = out / "configurations" / name
    val applicable = records(root).filter(_.accepts(cfg))
    require(applicable.nonEmpty, "No scenarios for " + name)
    IO.delete(hosts / "completed")
    applicable.foreach { scenario =>
      if (scenario.id == "CALL-04" && !virtualThreads)
        log.info(
          "SKIP CALL-04: virtual threads require Scala 3, JDK 21+, and runtime continuation support"
        )
      else {
        val language = if (scenario.hosts(C)) C else Cpp
        val args = Seq(scenario.id) ++
          (if (!scenario.initialize) Seq("--skip-init") else Nil) ++
          (if (cfg.multithreading.contains(true)) Seq("--multithreaded")
           else Nil)
        val output = hosts / (scenario.id + "-" + UUID.randomUUID() + ".log")
        val command = Seq(
          (hosts / ExternalThreadPlatform.current.executable(
            language.name
          )).getAbsolutePath
        ) ++ args
        val builder = new ProcessBuilder(command: _*)
          .directory(root)
          .redirectErrorStream(true)
          .redirectOutput(output)
        if (cfg.buildTarget == BuildTarget.libraryDynamic && !scenario.initialize)
          builder.environment().put("SCALANATIVE_NO_DYLIB_CTOR", "1")
        ExternalThreadPlatform.configureLoading(builder, hosts)
        val child = builder.start()
        val finished = child.waitFor(60, TimeUnit.SECONDS)
        if (!finished) {
          child.destroyForcibly()
          require(
            child.waitFor(10, TimeUnit.SECONDS),
            "Cannot terminate " + scenario.id
          )
        }
        val text = IO.read(output)
        log.info(text)
        // Keep logs after scripted removes successful temporary builds; avoid archiving binaries.
        sys.props.get("external.threads.results.dir").foreach { directory =>
          val category = IO.read(root / "category").trim
          IO.copyFile(
            output,
            file(directory) / category / name / output.getName
          )
        }
        require(finished, "Timed out: " + output)
        scenario.milestones.foreach(marker =>
          require(
            text.contains("MILESTONE " + marker),
            "Missing milestone " + marker + ": " + output
          )
        )
        val completed = text.contains("COMPLETE " + scenario.id)
        scenario.outcome match {
          case Success =>
            require(
              child.exitValue() == 0 && completed,
              "Scenario failed: " + output
            )
          case Fatal =>
            require(
              child.exitValue() != 0 && !completed && text.contains(
                scenario.diagnostic.get
              ),
              "Expected fatal diagnostic missing: " + output
            )
        }
        log.info(name + "/" + scenario.id + ": OK")
      }
    }
    IO.write(hosts / "completed", applicable.map(_.id).sorted.mkString("\n"))
  }

  /** A new catalog entry or configuration cannot silently remain unexecuted. */
  def verifyCoverage(
      root: File,
      out: File,
      base: NativeConfig,
      log: Logger
  ): Unit = {
    val scenarios = records(root)
    val rows = configurations(root)
    rows.foreach { row =>
      val expected =
        scenarios.filter(_.accepts(row.configure(base))).map(_.id).sorted
      val completed = out / "configurations" / row.name / "completed"
      require(
        completed.isFile && IO.readLines(completed).sorted == expected,
        "Incomplete scenario coverage for " + row.name
      )
    }
    scenarios.foreach { scenario =>
      require(
        rows.exists(row => scenario.accepts(row.configure(base))),
        "Scenario has no applicable configuration: " + scenario.id
      )
    }
    log.info(
      "All " + scenarios.size + " scenarios covered across all " + rows.size + " configurations"
    )
  }
}
