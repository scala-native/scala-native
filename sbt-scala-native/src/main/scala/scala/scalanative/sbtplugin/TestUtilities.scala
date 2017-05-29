package scala.scalanative
package sbtplugin

import sbt._
import sbt.testing._

object TestUtilities {

  def makeTestMain(frameworks: Seq[Framework],
                   tests: Seq[TestDefinition]): String = {
    val frameworksList =
      frameworks.map(_.getClass.getName).mkString("new ", ", new ", "")
    val testsMap = makeTestsMap(tests)

    s"""package scala.scalanative.testinterface
       |object TestMain extends TestMainBase {
       |  override val frameworks = List($frameworksList)
       |  override val tests = Map($testsMap)
       |  def main(args: Array[String]): Unit =
       |    testMain(args)
       |}""".stripMargin
  }

  // TODO: Use fingerprint to determine how to instantiate the frameworks.
  private def makeTestsMap(tests: Seq[TestDefinition]): String =
    tests.map(t => s""""${t.name}" -> ${t.name}""").mkString(", ")
}
