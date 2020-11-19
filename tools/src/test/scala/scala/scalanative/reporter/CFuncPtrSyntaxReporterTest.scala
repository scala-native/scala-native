package scala.scalanative.reporter

class CFuncPtrSyntaxReporterTest extends NirErrorReporterSpec {

  def cantInferFuncPtrWithCaptures(msg: String) =
    s"can't infer a function pointer to a closure with captures: $msg"

  it should "reject function pointers with captures of value" in {
    reportsErrors {
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def test(): Unit = {
          |    val x = 10
		  |    val cFunc = new CFuncPtr1[CInt, Unit]{
		  |      def apply(y: CInt): Unit = x + y
		  |    }
		  |    f(cFunc)
          |  }
          |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("value x"))
  }

  it should "reject function pointers with captures of method" in {
    reportsErrors {
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def x(): Int = 10
          |  def test(): Unit = {
		  |    val cFunc = new CFuncPtr1[CInt, Unit]{
		  |       def apply(y: CInt): Unit = x() + y
		  |    }
          |    f(cFunc)
          |  }
          |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("method x"))
  }

  it should "allow function pointers without captures" in {
    allows(
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def test(): Unit = {
          |    val cFunc = new CFuncPtr1[CInt, Unit]{
		  |     def apply(y: CInt): Unit = y
		  |    }
		  |    f(cFunc)
          |  }
          |}""".stripMargin
    )
  }

  it should "allow function pointers without captures 2" in {
    allows {
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def test(): Unit = {
          |    val z = 10
          |    val cFunc = new CFuncPtr1[CInt, Unit]{
          |      def apply(y: CInt): Unit = {
		  |      val z = y
		  |      z; ()
          |     }
		  |    }
		  |    f(cFunc)
          |  }
          |}""".stripMargin
    }
  }

  it should "verify that not allows function pointers with local values" in {
    reportsErrors {
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def test(): Unit = {
          |    val z = 10
          |    val cFunc = new CFuncPtr1[CInt, Unit]{
          |      def apply(y: CInt): Unit = {
          |        z; ()
          |      }
		  |    }
          |    f(cFunc)
          |  }
          |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("value z"))
  }

  it should "allows functions pointers with local values" in {
    allows {
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def test(): Unit = {
          |    val z = 10
          |    val cFunc = new CFuncPtr1[CInt, Unit]{
          |      def apply(y: CInt): Unit = {
          |        class foo { def z: Unit = () }
          |        val b = new foo
          |        b.z
          |      }
		  |    }
		  |    f(cFunc)
          |  }
          |}""".stripMargin
    }
  }

  it should "not allows function pointers with class captures" in {
    reportsErrors {
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def test(): Unit = {
          |    val z = 10
          |    class foo { def z: Unit = () }
          |    val cFunc = new CFuncPtr1[CInt, Unit]{
          |      def apply(y: CInt): Unit = {
		  |        val b = new foo
		  |        b.z
          |      }
		  |    }
		  |    f(cFunc)
          |  }
          |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("class foo"))
  }

//  it should "notify that cannot lift the argument into function" in {
//    reportsErrors {
//      s"""|import scala.scalanative.unsafe._
//          |object test {
//          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ()
//          |  def test(): Unit = {
//          |    val x = 10
//          |    val z = (y: CInt) => ()
//          |    f(CFuncPtr.fromFunction1(z))
//          |  }
//          |}""".stripMargin
//    }("(scala-native limitation): cannot infer a function pointer, lift the argument into a function")
//  }

  // TODO: should be possible.
  it should "allow to declare local closures in function pointer body" in {
    allows {
      s"""|import scala.scalanative.unsafe._
          |object test {
          |  def f(ptr: CFuncPtr1[CInt, Unit]): Unit = ???
          |  def test(): Unit = {
          |    val x = 10
		  |    val cFunc = new CFuncPtr1[CInt, Unit]{
		  |      val z = 1
		  |      def apply(y: CInt): Unit = y + z
		  |    }
		  |    f(cFunc)
          |  }
          |}""".stripMargin
    }
  }

}
