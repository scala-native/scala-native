import scala.scalanative.unsigned._

// given CanEqual[Int, NewUInt] = ???
object Test {
  def printn(v: Any): Unit = ()
  def main(args: Array[String]): Unit = {
    // println("Hello, World!")
    // println(classOf[NewUInt])
    // val a0 = 42L.toInt
    // println(a0)
    // val a1 = 42
    // println(a)
    val a = 1.u
    val b = a.toInt.U * 2.u
    val c = a + b
    val d = a + b + c
    // val x: Any = 1.u

    // println(1.isInstanceOf[Int])
    // println(1.u.isInstanceOf[NewUInt])
    // println(x.isInstanceOf[Int])
    // println(x.isInstanceOf[NewUInt])

    // println(1.asInstanceOf[Int])
    // println(1.asInstanceOf[Integer])
    // println(1.u.asInstanceOf[NewUInt])
    // println(1.u.asInstanceOf[UnsignedInt])
    // println(x.asInstanceOf[NewUInt])
    // 42 match {
    //   case v @ 21 => println(v)
    //   case v: Int => println(v)
    // }

    // 42.u match {
    //   case v: NewUInt => println(s"uint $v")
    // }
    // val v: Any = 1.u
    // v match {
    //   case v: Int => println("int: "+ v)
    //   case v: NewUInt => println("uint: " + v)
    //   case other => println("other: " + other)
    // }

    // var mutV: Any = 1
    // mutV = 1.u
    // mutV = (1.u: Any)
    
    // println(c)
    // println(-1.U)
  }
}
