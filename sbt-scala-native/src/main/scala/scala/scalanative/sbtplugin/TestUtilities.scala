package scala.scalanative
package sbtplugin

import scala.scalanative.test.{FakeFingerprint, NativeFingerprint}

import sbt._
import sbt.testing._

object TestUtilities {
  private val indent = "    "
  def remapTestDefinition(test: TestDefinition): TestDefinition = {
    val newFingerprint = new FakeFingerprint(test.fingerprint)
    new TestDefinition(test.name,
                       newFingerprint,
                       test.explicitlySpecified,
                       test.selectors)
  }

  def createTestMain(tests: Seq[TestDefinition]): String = {
    val cases = tests.map(createTestCase)
    s"""|object TestMain {
        |  def main(args: Array[String]): Unit = args match {
        |    ${cases.mkString(System.lineSeparator + indent)}
        |    case Array() =>
        |      ()
        |  }
        |}""".stripMargin
  }

  private def createTestCase(test: TestDefinition): String = {
    val body =
      test.fingerprint match {
        case fake: FakeFingerprint =>
          fake.original match {
            case NativeFingerprint =>
              s"""new ${test.name}().test()"""
            case UTest() =>
              s"""|implicit val executor = utest.framework.ExecutionContext.RunNow
                  |${test.name}.tests.runAsync()""".stripMargin
            case other =>
              throw new UnsupportedOperationException(
                "Unsupported fingerprint: " + other)
          }
      }

    s"""|case Array("${test.name}", rest @ _*) =>
        |      $body
        |      main(rest.toArray)""".stripMargin
  }

  private object UTest {
    def unapply(fingerprint: Fingerprint): Boolean =
      fingerprint match {
        case scf: SubclassFingerprint =>
          scf.superclassName == "utest.TestSuite" &&
            scf.isModule &&
            scf.requireNoArgConstructor
        case _ => false
      }
  }
}
