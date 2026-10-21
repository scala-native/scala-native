import sbt._

import scala.scalanative.build.{BuildTarget, Discover, GC, NativeConfig}

/** Native host build and loading conventions shared by the scripted fixtures.
 */
object ExternalThreadPlatform {
  final case class Platform(windows: Boolean, linux: Boolean) {
    def executable(name: String): String = name + (if (windows) ".exe" else "")
    def cppStandard: String = if (windows) "-std=c++14" else "-std=c++11"
    def compileOptions: Seq[String] =
      if (windows) Seq("-D_WIN32_WINNT=0x0602")
      else Seq("-D_XOPEN_SOURCE=700", "-pthread")
    def linkOptions: Seq[String] =
      if (windows) Seq("-ladvapi32", "-luserenv", "-ldbghelp")
      else Seq("-pthread", "-lm") ++ (if (linux) Seq("-ldl") else Nil)
    def libraryLinkOptions(library: File, cfg: NativeConfig): Seq[String] =
      cfg.linkingOptions ++ linkOptions ++
        (if (cfg.gc == GC.boehm) Seq("-lgc") else Nil) ++
        (if (!windows && cfg.buildTarget == BuildTarget.libraryDynamic)
           Seq("-Wl,-rpath," + library.getParent)
         else Nil)
  }
  val current: Platform = {
    val os = sys.props("os.name").toLowerCase
    Platform(os.contains("windows"), os.contains("linux"))
  }

  /** Copy the DLL's import library as well as the loadable DLL. */
  def archiveLibrary(
      source: File,
      destination: File,
      cfg: NativeConfig
  ): File = {
    val library = destination / source.getName
    IO.copyFile(source, library)
    if (current.windows && cfg.buildTarget == BuildTarget.libraryDynamic) {
      val basename = source.getName.stripSuffix(".dll")
      val imports = Seq(
        source.getParentFile / (basename + ".lib"),
        source.getParentFile / (basename + ".dll.a"),
        source.getParentFile / ("lib" + basename + ".dll.a")
      )
      val importLibrary = imports
        .find(_.isFile)
        .getOrElse(sys.error("Missing DLL import library for " + source))
      IO.copyFile(importLibrary, destination / importLibrary.getName)
    }
    library
  }

  def linkLibrary(library: File, cfg: NativeConfig): File = {
    if (!current.windows || cfg.buildTarget != BuildTarget.libraryDynamic)
      library
    else {
      val basename = library.getName.stripSuffix(".dll")
      Seq(
        library.getParentFile / (basename + ".lib"),
        library.getParentFile / (basename + ".dll.a"),
        library.getParentFile / ("lib" + basename + ".dll.a")
      )
        .find(_.isFile)
        .getOrElse(sys.error("Missing archived DLL import library"))
    }
  }

  def configureLoading(
      builder: ProcessBuilder,
      libraryDirectory: File
  ): Unit = {
    if (current.windows) {
      // Windows searches beside the executable and along PATH, not an rpath.
      val environment = builder.environment()
      val key = sys.env.keys.find(_.equalsIgnoreCase("PATH")).getOrElse("PATH")
      val existing = Option(environment.get(key)).getOrElse("")
      environment.put(
        key,
        libraryDirectory.getAbsolutePath + java.io.File.pathSeparator + existing
      )
    }
  }

}
