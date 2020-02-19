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
        .map(f => s"new ${fullClassName(f.getClass.getName)}")
        .mkString("List(", ", ", ")")
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
        val fullName = fullClassName(t.name)
        val inst         = if (isModule) fullName else s"new $fullName"
        s""""${t.name}" -> $inst"""
      }
      .mkString(", ")

  private def fullClassName(name: String): String = {
    val isInAPackage = name.contains(".")
    if (isInAPackage) s"_root_.$name" else name
  }
}
