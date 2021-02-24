import scala.scalanative.runtime.{fromRawPtr, libc}
import scala.scalanative.unsafe._

object libtest {
  val fourtyTwo        = 42.toShort
  var snRocks: CString = _
  snRocks = c"ScalaNativeRocks!"

  type Foo = CStruct5[Short, Int, Long, Double, CString]

  @export
  def sayHello(): Unit = {
    println(s"""
         |==============================
         |Hello Scala Native from library
         |==============================
         |
    """.stripMargin)
  }

  @export
  def addLongs(l: Long, r: Long): Long = l + r

  @export
  def retStructPtr(): Ptr[Foo] = {
    val ptr = fromRawPtr[Foo](libc.malloc(sizeof[Foo]))

    ptr._1 = fourtyTwo
    ptr._2 = 2020
    ptr._3 = 27
    ptr._4 = 14.4556
    ptr._5 = snRocks
    ptr
  }

  @export
  def updateStruct(ptr: Ptr[Foo]): Unit = {
    updateInternally(ptr)
  }

  @noinline
  def updateInternally(ptr: Ptr[Foo]): Unit = {
    ptr._2 = addLongs(2020, 1).toInt
  }

  @export
  def fail(): Unit = {
    throw new RuntimeException("Exception from ScalaNative")
  }

  @export
  @name("sn_runGC")
  @noinline
  def enforceGC(): Unit = System.gc()
}
