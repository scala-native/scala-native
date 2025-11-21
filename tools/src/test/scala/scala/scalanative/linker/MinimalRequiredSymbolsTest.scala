package scala.scalanative
package linker

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.LinkerSpec
import scala.scalanative.build.{Config, NativeConfig}
import scala.scalanative.buildinfo.ScalaNativeBuildInfo

/** Tests minimal number of NIR symbols required when linking minimal
 *  application based on the predefined hard limits. In the future we shall try
 *  to limit these number even further
 */
class MinimalRequiredSymbolsTest extends LinkerSpec {
  private val mainClass = "Test"
  private val sourceFile = "Test.scala"

  def isScala3 = ScalaNativeBuildInfo.scalaVersion.startsWith("3.")
  def isScala2_13 = ScalaNativeBuildInfo.scalaVersion.startsWith("2.13")
  def isScala2_12 = ScalaNativeBuildInfo.scalaVersion.startsWith("2.12")

  @Test def default(): Unit = checkMinimalRequiredSymbols()(expected =
    if (isScala3) SymbolsCount(types = 622, members = 3058)
    else if (isScala2_13) SymbolsCount(types = 597, members = 3066)
    else SymbolsCount(types = 694, members = 4214)
  )

  @Test def debugMetadata(): Unit =
    checkMinimalRequiredSymbols(withDebugMetadata = true)(expected =
      if (isScala3) SymbolsCount(types = 622, members = 3058)
      else if (isScala2_13) SymbolsCount(types = 597, members = 3066)
      else SymbolsCount(types = 694, members = 4214)
    )

  // Only MacOS and Linux DWARF metadata currently
  @Test def debugMetadataMacOs(): Unit =
    checkMinimalRequiredSymbols(
      withDebugMetadata = true,
      withTargetTriple = "x86_64-apple-darwin22.6.0"
    )(expected =
      if (isScala3) SymbolsCount(types = 997, members = 6214)
      else if (isScala2_13) SymbolsCount(types = 959, members = 6255)
      else SymbolsCount(types = 989, members = 6954)
    )

  // Only MacOS and Linux DWARF metadata currently
  @Test def debugMetadataLinux(): Unit =
    checkMinimalRequiredSymbols(
      withDebugMetadata = true,
      withTargetTriple = "x86_64-pc-linux-gnu"
    )(expected =
      if (isScala3) SymbolsCount(types = 1095, members = 7042)
      else if (isScala2_13) SymbolsCount(types = 1054, members = 7111)
      else SymbolsCount(types = 1044, members = 7362)
    )

  @Test def multithreading(): Unit =
    checkMinimalRequiredSymbols(withMultithreading = true)(expected =
      if (isScala3) SymbolsCount(types = 1073, members = 6674)
      else if (isScala2_13) SymbolsCount(types = 1041, members = 6757)
      else SymbolsCount(types = 995, members = 6828)
    )

  private def checkMinimalRequiredSymbols(
      withDebugMetadata: Boolean = false,
      withMultithreading: Boolean = false,
      withTargetTriple: String = "x86_64-unknown-unknown"
  )(expected: SymbolsCount) = usingMinimalApp(
    _.withSourceLevelDebuggingConfig(conf =>
      if (withDebugMetadata) conf.enableAll else conf.disableAll
    )
      .withMultithreading(withMultithreading)
      .withTargetTriple(withTargetTriple)
  ) { (config: Config, result: ReachabilityAnalysis.Result) =>
    assertEquals(
      "debugMetadata",
      withDebugMetadata,
      config.compilerConfig.sourceLevelDebuggingConfig.enabled
    )
    assertEquals(
      "multithreading",
      withMultithreading,
      config.compilerConfig.multithreadingSupport
    )
    assertEquals(
      "targetTriple",
      withTargetTriple,
      config.compilerConfig.targetTriple.getOrElse("none")
    )

    val mode =
      s"{debugMetadata=$withDebugMetadata, multithreading=$withMultithreading, targetTriple=$withTargetTriple}"
    val found = SymbolsCount(result.defns)
    if (found.total > expected.total) {
      fail(
        s"""|
            |Found more symbols then expected, config=$mode:
            |Expected at most: ${expected}
            |Found:            ${found}
            |Diff:             ${found - expected}
            |""".stripMargin
      )
    } else {
      println(
        s"""|
            |Amount of found symbols in norm, config=$mode:
            |Expected at most: ${expected}
            |Found:            ${found}
            |Diff:             ${found - expected}
            |""".stripMargin
      )
    }
  }

  private def usingMinimalApp(setupConfig: NativeConfig => NativeConfig)(
      fn: (Config, ReachabilityAnalysis.Result) => Unit
  ): Unit = link(
    entry = mainClass,
    setupConfig = setupConfig,
    sources = Map(
      sourceFile -> s"""|
                        |object $mainClass{
                        |  def main(args: Array[String]): Unit = ()
                        |}
                        |""".stripMargin
    )
  ) { case (config, result) => fn(config, result) }

  case class SymbolsCount(types: Int, members: Int) {
    def total: Int = types + members
    def -(other: SymbolsCount): SymbolsCount = SymbolsCount(
      types = types - other.types,
      members = members - other.members
    )
    override def toString(): String =
      s"{types=$types, members=$members, total=${total}}"
  }
  object SymbolsCount {
    def apply(defns: Seq[nir.Defn]): SymbolsCount = {
      val names = defns.map(_.name)
      SymbolsCount(
        types = names.count(_.isInstanceOf[nir.Global.Top]),
        members = names.count(_.isInstanceOf[nir.Global.Member])
      )
    }
  }

}
