package scala.scalanative
package linker

import scala.scalanative.LinkerSpec

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.build.{NativeConfig, Config}
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
    if (isScala3) SymbolsCount(types = 650, members = 2912)
    else if (isScala2_13) SymbolsCount(types = 581, members = 2943)
    else SymbolsCount(types = 683, members = 4007)
  )

  @Test def debugMetadata(): Unit =
    checkMinimalRequiredSymbols(withDebugMetadata = true)(expected =
      if (isScala3) SymbolsCount(types = 650, members = 2912)
      else if (isScala2_13) SymbolsCount(types = 581, members = 2943)
      else SymbolsCount(types = 683, members = 4007)
    )

  // Only MacOS and Linux DWARF metadata currently
  @Test def debugMetadataMacOs(): Unit =
    checkMinimalRequiredSymbols(
      withDebugMetadata = true,
      withTargetTriple = "x86_64-apple-darwin22.6.0"
    )(expected =
      if (isScala3) SymbolsCount(types = 1021, members = 6725)
      else if (isScala2_13) SymbolsCount(types = 981, members = 6742)
      else SymbolsCount(types = 981, members = 6873)
    )

  // Only MacOS and Linux DWARF metadata currently
  @Test def debugMetadataLinux(): Unit =
    checkMinimalRequiredSymbols(
      withDebugMetadata = true,
      withTargetTriple = "x86_64-pc-linux-gnu"
    )(expected =
      if (isScala3) SymbolsCount(types = 1080, members = 6970)
      else if (isScala2_13) SymbolsCount(types = 1040, members = 7052)
      else SymbolsCount(types = 1030, members = 7232)
    )

  @Test def multithreading(): Unit =
    checkMinimalRequiredSymbols(withMultithreading = true)(expected =
      if (isScala3) SymbolsCount(types = 1100, members = 6650)
      else if (isScala2_13) SymbolsCount(types = 1040, members = 6760)
      else SymbolsCount(types = 985, members = 6688)
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
      fail(s"""
          |Found more symbols then expected, config=$mode:
          |Expected at most: ${expected}
          |Found:            ${found}
          |Diff:             ${found - expected}
          |""".stripMargin)
    } else {
      println(s"""
          |Amount of found symbols in norm, config=$mode:
          |Expected at most: ${expected}
          |Found:            ${found}
          |Diff:             ${found - expected}
          |""".stripMargin)
    }
  }

  private def usingMinimalApp(setupConfig: NativeConfig => NativeConfig)(
      fn: (Config, ReachabilityAnalysis.Result) => Unit
  ): Unit = link(
    entry = mainClass,
    setupConfig = setupConfig,
    sources = Map(sourceFile -> s"""
        |object $mainClass{
        |  def main(args: Array[String]): Unit = ()
        |}
        """.stripMargin)
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
