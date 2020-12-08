package scala.scalanative.reporter

class CFuncPtrSyntaxReporterTest extends NirErrorReporterSpec {

  def cantInferFuncPtrWithCaptures(msg: String) =
    s"can't infer a function pointer to a closure with captures: $msg"

  it should "reject function pointers with captures of value" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val x = 10
         |    val fn = CFuncPtr1.fromScalaFunction[CInt, Long](_ + x)
         |    fn(0)
         |  }
         |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("value x"))
  }

  it should "reject function pointers with captures of method" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def x(): Int = 10
         |  def test(): Unit = {
         |    val fn = CFuncPtr1.fromScalaFunction[CInt, Long](_ + x())
         |    fn(0)
         |  }
         |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("method x"))
  }

  it should "allow function pointers without captures" in {
    allows(
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  val fn = CFuncPtr1.fromScalaFunction[CInt, CLong](_ + 1)
         |  fn(0)
         |}
         |""".stripMargin
    )
  }

  it should "allow function pointers without captures 2" in {
    allows {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val z = 10
         |    val fn = CFuncPtr1.fromScalaFunction[CInt, CLong]{ y =>
         |      val z = y
         |      z
         |    }
         |    fn(0)
         |  }
         |}""".stripMargin
    }
  }

  it should "verify that not allows function pointers with local values" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val z = 10
         |    val fn = CFuncPtr1.fromScalaFunction[CInt, Long](_ => z)
         |    fn(0)
         |  }
         |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("value z"))
  }

  it should "allows functions pointers with local values" in {
    allows {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val z = 10
         |    val fn = CFuncPtr1.fromScalaFunction[CInt, CLong]{ n =>
         |        class foo { def z: Int = 10 }
         |        val b = new foo
         |        b.z + n
         |    }
         |    fn(0)
         |  }
         |}""".stripMargin
    }
  }

  it should "not allows function pointers with class captures" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val z = 10
         |    class foo { def z: Int = 10 }
         |    val fn = CFuncPtr1.fromScalaFunction[CInt, Long]{ n =>
         |        val b = new foo
         |        b.z + n
         |    }
         |    fn(0)
         |  }
         |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("class foo"))
  }

  it should "lift the argument method into function" in {
    allows {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val x = 10
         |    def z(y: CInt): Long = 10
         |    val fn = CFuncPtr1.fromScalaFunction(z _)
         |    fn(0)
         |  }
         |}""".stripMargin
    }
  }

  it should "not lift the argument value into function" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val x = 10
         |    val z = (y: CInt) => 10L
         |    val fn = CFuncPtr1.fromScalaFunction(z)
         |    fn(0)
         |  }
         |}""".stripMargin
    }("Cannot lift value into CFuncPtr")
  }

  it should "should not allow to declare local closures in function pointer body" in {
    reportsErrors {
      s"""
         |import scala.scalanative.unsafe._
         |object test {
         |  def test(): Unit = {
         |    val x = 10
         |    val fn = CFuncPtr1.fromScalaFunction[CInt, Long]{
         |      val z = 1
         |      new Function1[CInt, Long]{
         |        def apply(y: CInt): Long = y + z
         |      }
         |    }
         |    fn(0)
         |  }
         |}""".stripMargin
    }(cantInferFuncPtrWithCaptures("value z"))
  }

}
