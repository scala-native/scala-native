package scala.scalanative

object IssuesSuite extends tests.Suite {
  test("#253") {
    class Cell(val value: Int)

    val arr     = Array(new Cell(1), new Cell(2))
    val reverse = arr.reverse

    assert(reverse(0).value == 2)
    assert(reverse(1).value == 1)
  }

  test("#260") {
    def getStr(): String = {
      val bytes = Array('h'.toByte, 'o'.toByte, 'l'.toByte, 'a'.toByte)
      new String(bytes)
    }

    val sz = getStr()

    assert("hola" == sz)
    assert("hola".equals(sz))
  }

  test("#314") {
    // Division by zero is undefined behavior in production mode.
    // Optimizer can assume it never happens and remove unused result.
    assert {
      try {
        5 / 0
        true
      } catch {
        case _: Throwable =>
          false
      }
    }
  }

  test("#326") {
    abstract class A
    case class S[T](a: T) extends A

    def check(a: A) = a match {
      case S(d: Double) => "double"
      case S(d: Int)    => "int"
      case _            => "neither"
    }

    def main(args: Array[String]): Unit = {
      val (dbl, int, obj) = (S(2.3), S(2), S(S(2)))
      assert(check(dbl) == "double")
      assert(check(int) == "int")
      assert(check(obj) == "neither")
    }
  }

  test("#327") {
    val a = BigInt(1)
    assert(a.toInt == 1)
  }

  test("#337") {
    case class TestObj(value: Int)
    val obj = TestObj(10)
    assert(obj.value == 10)
  }

  test("#350") {
    val div = java.lang.Long.divideUnsigned(42L, 2L)
    assert(div == 21L)
  }

  test("#374") {
    assert("42" == bar(42))
    assert("bar" == bar_i32())
  }

  def bar(i: Int): String = i.toString
  def bar_i32(): String   = "bar"

  test("#376") {
    val m     = scala.collection.mutable.Map.empty[String, String]
    val hello = "hello"
    val world = "world"
    m(hello) = world
    val h = m.getOrElse(hello, "Failed !")
    assert(h equals world)
  }

  test("#404") {
    // this must not throw an exception
    this.getClass.##
  }

  test("#424") {
    // this used not to link
    val cls = classOf[Array[Unit]]
  }
}
