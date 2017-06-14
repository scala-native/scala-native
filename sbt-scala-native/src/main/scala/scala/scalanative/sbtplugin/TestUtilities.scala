package scala.scalanative
package sbtplugin

import sbt._
import sbt.testing._

object TestUtilities {

  def makeTestMain(frameworks: Seq[Framework],
                   tests: Seq[TestDefinition]): String = {
    val frameworksList =
      frameworks
        .map(_.getClass.getName)
        .mkString("new _root_.", ", new _root_.", "")
    val testsMap = makeTestsMap(tests)

    s"""package scala.scalanative.testinterface
       |object TestMain extends TestMainBase {
       |  override val frameworks = List($frameworksList)
       |  override val tests = Map($testsMap)
       |  def main(args: Array[String]): Unit =
       |    testMain(args)
       |}""".stripMargin
  }

  private def makeTestsMap(tests: Seq[TestDefinition]): String =
    tests
      .map { t =>
        val isModule = t.fingerprint match {
          case af: AnnotatedFingerprint => af.isModule
          case sf: SubclassFingerprint  => sf.isModule
        }

        val inst = if (isModule) t.name else s"new ${t.name}"
        s""""${t.name}" -> _root_.$inst"""
      }
      .mkString(", ")
}
