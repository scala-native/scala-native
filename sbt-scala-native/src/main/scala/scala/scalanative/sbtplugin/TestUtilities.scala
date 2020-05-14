package scala.scalanative
package sbtplugin

import sbt.testing._

object TestUtilities {

  def makeTestMain(frameworks: Seq[Framework]): String = {
    val frameworksList = frameworks
      .map(f => s"new ${fullClassName(f.getClass.getName)}")
      .mkString("List(", ", ", ")")

    println(s"### frameworks = $frameworks, $frameworksList")

    s"""object ScalaNativeTestMain extends scala.scalanative.testinterface.TestMain {
       |  override val frameworks = $frameworksList
       |  def main(args: Array[String]): Unit =
       |    testMain(args)
       |}""".stripMargin
  }

  private def fullClassName(name: String): String = {
    val isInAPackage = name.contains(".")
    if (isInAPackage) s"_root_.$name" else name
  }
}
