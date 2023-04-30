package scala.scalanative

import scala.scalanative.build._

package object benchmarks {
  lazy val defaultNativeConfig = NativeConfig.empty
    .withClang(Discover.clang())
    .withClangPP(Discover.clangpp())
    .withCompileOptions(Discover.compileOptions())
    .withLinkingOptions(Discover.linkingOptions())

  lazy val defaultConfig = Config.empty
    .withClassPath(BuildInfo.fullTestSuiteClasspath.map(_.toPath))
    .withLogger(Logger.nullLogger)
    .withCompilerConfig(defaultNativeConfig)

  val TestMain = "scala.scalanative.testinterface.TestMain"
}
