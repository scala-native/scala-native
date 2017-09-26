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

  it should "verify that extern objects and classes only have extern members" in {

    assertResult(
      Array(
        (57, "fields in extern objects must have extern body").toError)) {
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
      Array((52, "extern objects may only have extern parents").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object bar extends foo {
            |  var z: Int = scala.scalanative.native.extern
            |}
            |trait foo {
            |}  """.stripMargin }
    }

    assertResult(
      Array((51, "extern classes may only have extern parents").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |class bar extends foo {
            |  var z: Int = scala.scalanative.native.extern
            |}
            |trait foo {
            |}  """.stripMargin }
    }


    assertResult(
      Array()) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object one extends two {
            |  var z: Int = scala.scalanative.native.extern
            |}
            |@scala.scalanative.native.extern
            |trait two {
            |}  """.stripMargin }
    }

    assertResult(
      Array((166, "fields in extern traits must have extern body").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object bar extends foo{
            |  var z: Int = scala.scalanative.native.extern
            |}
            |@scala.scalanative.native.extern
            |trait foo {
            |  val y: Int = 1
            |}  """.stripMargin }
    }

    assertResult(
      Array()) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object bar extends foo {
            |  var z: Int = scala.scalanative.native.extern
            |}
            |@scala.scalanative.native.extern
            |trait foo {
            |  val y: Int = scala.scalanative.native.extern
            |}  """.stripMargin }
    }

    assertResult(
      Array((58, "(limitation) fields in extern objects must not be lazy").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object test {
            |  lazy val t: Int = scala.scalanative.native.extern
            |}""".stripMargin }
    }

    assertResult(
      Array((74, "extern objects may only have extern parents").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|class Foo(val x: Int)
            |@scala.scalanative.native.extern
            |object Bar extends Foo(10)""".stripMargin }
    }

    // Previously, this would compile and execute but wouldn't
    // return the incorrect result (0) for `Bar.x`
    assertResult(
      Array((47, "parameters in extern classes are not allowed - only extern fields and methods are allowed").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |class Foo(val x: Int)
            |@scala.scalanative.native.extern
            |object Bar extends Foo(10)""".stripMargin }
    }


  }

  it should "verify that extern members are defined correctly" in {

    assertResult(
      Array(
        (49, "extern members must have an explicit type annotation").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object test {
            |  val t = scala.scalanative.native.extern
            |}""".stripMargin }
    }

    assertResult(
      Array(
        (53, "extern members must have an explicit type annotation").toError)) {
      NIRCompiler {_ compileAndReport
        s"""|@scala.scalanative.native.extern
            |object test {
            |  def t = scala.scalanative.native.extern
            |}""".stripMargin }
    }
  }

  it should "reject function pointers with captures" in {

    assertResult(
      Array(
        (170, "can't infer a function pointer to a closure with captures: value x").toError
      )) {
      NIRCompiler {_ compileAndReport
          s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def test(): Unit = {
            |    val x = 10
            |    f(CFunctionPtr.fromFunction1((y: CInt) => x + y))
            |  }
            |}""".
          stripMargin }
    }

    assertResult(
      Array(
        (175, "can't infer a function pointer to a closure with captures: method x").toError
      )) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def x(): Int = 10
            |  def test(): Unit = {
            |    f(CFunctionPtr.fromFunction1((y: CInt) => x() + y))
            |  }
            |}""".
          stripMargin }
    }

    assertResult(
      Array()) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def test(): Unit = {
            |    f(CFunctionPtr.fromFunction1((y: CInt) => y))
            |  }
            |}""".
          stripMargin }
    }

    assertResult(
      Array()) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def test(): Unit = {
            |    val z = 10
            |    f(CFunctionPtr.fromFunction1((y: CInt) => {
            |      val z = y
            |      z
            |      ()
            |    }))
            |  }
            |}""".
          stripMargin }
    }

    assertResult(
      Array(
        (170,"can't infer a function pointer to a closure with captures: value z").toError
      )) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def test(): Unit = {
            |    val z = 10
            |    f(CFunctionPtr.fromFunction1((y: CInt) => {
            |      z
            |      ()
            |    }))
            |  }
            |}""".
          stripMargin }
    }

    assertResult(
      Array(
      )) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def test(): Unit = {
            |    val z = 10
            |    f(CFunctionPtr.fromFunction1((y: CInt) => {
            |      class foo { def z: Unit = () }
            |      val b = new foo
            |      b.z
            |    }))
            |  }
            |}""".
          stripMargin }
    }

    assertResult(
      Array(
        (205, "can't infer a function pointer to a closure with captures: class foo").toError
      )) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def test(): Unit = {
            |    val z = 10
            |    class foo { def z: Unit = () }
            |    f(CFunctionPtr.fromFunction1((y: CInt) => {
            |      val b = new foo
            |      b.z
            |    }))
            |  }
            |}""".
          stripMargin }
    }

    assertResult(
      Array(
        (197, "(scala-native limitation): cannot infer a function pointer").toError
      )) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ()
            |  def test(): Unit = {
            |    val x = 10
            |    val z = (y: CInt) => ()
            |    f(CFunctionPtr.fromFunction1(z))
            |  }
            |}""".
          stripMargin }
    }

    // TODO: should be possible.
    assertResult(
      Array(
        (170, "(scala-native limitation): cannot infer a function pointer").toError
      )) {
      NIRCompiler {_ compileAndReport
        s"""|import scala.scalanative.native._
            |object test {
            |  def f(ptr: CFunctionPtr1[CInt, Unit]): Unit = ???
            |  def test(): Unit = {
            |    val x = 10
            |    f(CFunctionPtr.fromFunction1({
            |      val y = 1
            |      (z: CInt) => y + z
            |    }))
            |  }
            |}""".
          stripMargin }
    }
  }

  it should "verify the application of primitive functions" in {
    assertResult(
      Array()) {

      NIRCompiler {_ compileAndReport
        s"""|object test {
            |  def foo(): Unit = {
            |    val x = List(1)
            |    new PlusOps[List, Int]
            |  }
            |
            |  class PlusOps[A[_], B]() {
            |  }
            |}""".stripMargin }
    }
  }

}
