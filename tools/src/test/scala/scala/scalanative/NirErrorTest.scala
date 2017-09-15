package scala.scalanative

import java.io.File

import org.scalatest._


case class TestCompilerError(err: (Int, String)) extends api.CompilerError {
  override def getPosition: Integer = err._1
  override def getErrorMsg: String = err._2
}

object CompilerError {
  implicit class CompilerOps(val x: Tuple2[Int, String]) extends AnyVal {
    def toError: api.CompilerError = TestCompilerError(x)
  }
}



class NirErrorTest extends FlatSpec with Matchers with Assertions {

  import CompilerError._

  it should "verify extern objects and methods" in {

    assertResult(
      Array(
        (49, "extern objects may only contain extern fields").toError)) {
     NIRCompiler {_ compileAndReport
       s"""|@scala.scalanative.native.extern
           |object test {
           |  val t = 1
           |}""".stripMargin }
    }

    assertResult(
      Array((57, "methods in extern objects must have extern body").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object test {
            |  def t = 1
            |}""".stripMargin }
    }


    assertResult(
      Array()) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object bar extends foo {
            |  var z: Int = scala.scalanative.native.extern
            |}
            |trait foo {
            |}  """.stripMargin }
    }

    assertResult(
      Array((164, "extern objects may only contain extern fields").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object bar extends foo{
            |  var z: Int = scala.scalanative.native.extern
            |}
            |trait foo {
            |  val y: Int = 1
            |}  """.stripMargin }
    }

    assertResult(
      Array((40, "extern objects may only contain extern fields").toError,
            (58, "methods in extern objects must have extern body").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object test {
            |  lazy val t = 1
            |}""".stripMargin }
    }


  }

}
