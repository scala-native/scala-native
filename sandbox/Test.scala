package test

object Test {
//  def classOfs = {
//    class C
//    val c = classOf[C]
//  }
//
//  def objGetClass = {
//    val obj = new Object
//    obj.getClass
//  }
//
//  def primGetClass = 42.getClass
//
//  def newC = {
//    class C(val x: C)
//    new C(null)
//  }
//
//  def objWait = {
//    val obj = new Object
//    obj.wait
//    obj.wait(1L)
//    obj.wait(1L, 2)
//  }
//
//  def objNotify = {
//    val obj = new Object
//    obj.notify
//    obj.notifyAll
//  }
//
//  def varargs(arg: Any*) = ()
//  def callsVarargs =
//    varargs(1, "two", 'three)
//
//  def closes1 = {
//    val x = 2
//    def foo(y: Int) = x + y
//    foo _
//  }
//
//  def closes2 = {
//    class C {
//      val x = 2
//      def foo(y: Int) = x + y
//    }
//    val c = new C
//    c.foo _
//  }
//
//  def syncs = {
//    val obj = new Object
//    obj.synchronized {
//      obj.toString
//    }
//  }
//
//  def hash = {
//    val x: Any = 42
//    x.##
//  }
//
//  def concat(a: String, b: String) = a + b
//
  def arrayOps = {
    val arr = new Array[Int](32)
    arr(0)
    arr(0) = 2
    arr.length
  }
//
//  def unary = {
//    val x = 2
//    -x
//  }
//
//  def binary = {
//    val x = 2
//    val y = 3
//    (x + y) * (x - y)
//  }
//
//  def box_unbox = {
//    def f[T](x: T): T = x
//    val x = f(10)
//  }
//
//  def matches: Int =
//    (null: Any) match {
//      case "foo" => 1
//      case 2 => 3
//      case 3.0 => 4
//      case `world` => 5
//      case _: AnyRef => 6
//    }
//
//  def isInstanceOfs =
//    (null: Any).asInstanceOf[world.type]
//
//  def asInstanceOfs =
//    (null: Any).asInstanceOf[world.type]
//
//  def tries: Unit =
//    try throws
//    catch {
//      case _ => ()
//    }
//
//  def throws: Unit =
//    throw new Exception
//
//  def returns: Int =
//    return 1
//
//  def ifs(): Unit = {
//    var msg: String = null
//    val cond: Boolean = true
//    if (cond)
//      msg = "hello, world"
//    else
//      msg = "hello, hell"
//    msg
//  }
}
