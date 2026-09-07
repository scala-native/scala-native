package scala.scalanative

import java.io.File.pathSeparator
import java.nio.file.Paths

import scala.scalanative.build._

package object benchmarks {
  lazy val defaultNativeConfig = NativeConfig.empty
    .withClang(Discover.clang())
    .withClangPP(Discover.clangpp())
    .withCompileOptions(Discover.compileOptions())
    .withLinkingOptions(Discover.linkingOptions())

  lazy val defaultConfig = Config.empty
    .withClassPath(
      BuildInfo.fullTestSuiteClasspath
        .split(pathSeparator)
        .filter(_.nonEmpty)
        .map(Paths.get(_))
        .toSeq
    )
    .withLogger(Logger.nullLogger)
    .withCompilerConfig(defaultNativeConfig)

  val TestMain = "scala.scalanative.testinterface.TestMain"
}
