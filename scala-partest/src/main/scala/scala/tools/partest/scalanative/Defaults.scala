package scala.tools.partest.scalanative

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.build._
import scala.scalanative.nir.Attr.Link

object Defaults {
  // List of all libraries that need to be linked when precompiling libraries
  val links: Seq[Link] = Seq("z", "pthread").map(Link)

  def workdir(): Path = Files.createTempDirectory("partest-")

  def errorFn(str: String): Boolean = {
    scala.Console.err println str
    false
  }

  val logger: Logger = Logger(
    traceFn = _ => (),
    debugFn = _ => (),
    infoFn = _ => (),
    warnFn = errorFn,
    errorFn = errorFn
  )

  lazy val config: Config =
    Config.empty
      .withLogger(logger)
      .withCompilerConfig(
        NativeConfig.empty
          .withClang(Discover.clang())
          .withClangPP(Discover.clangpp())
          .withCheck(true)
          .withLinkStubs(false)
          .withDump(false)
          .withOptimize(Discover.optimize())
          .withMode(Discover.mode())
          .withGC(Discover.GC())
          .withLTO(Discover.LTO())
          .withLinkingOptions(Discover.linkingOptions())
          .withCompileOptions(Discover.compileOptions())
      )
}
