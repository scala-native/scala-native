package scala.scalanative
package sbtplugin

import sbt._
import sbt.testing._

object TestUtilities {

  def makeTestMain(frameworks: Seq[Framework],
                   tests: Seq[TestDefinition]): String = {
    val frameworksList = if (frameworks.isEmpty) {
      "Nil"
    } else {
      frameworks
        .map(_.getClass.getName)
        .mkString("List(new _root_.", ", new _root_.", ")")
    }
    val testsMap = makeTestsMap(tests)

    s"""object TestMain extends scala.scalanative.testinterface.TestMainBase {
       |  override val frameworks = $frameworksList
       |  override val tests = Map[String, AnyRef]($testsMap)
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
        val isInAPackage = t.name.contains(".")
        val fullName     = if (isInAPackage) s"_root_.${t.name}" else t.name
        val inst         = if (isModule) fullName else s"new $fullName"
        s""""${t.name}" -> $inst"""
      }
      .mkString(", ")
}
