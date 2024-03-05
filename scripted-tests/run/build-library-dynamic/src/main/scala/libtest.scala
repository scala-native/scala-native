import scala.scalanative.unsafe._
import scala.scalanative.libc.stdlib.malloc

object libtest {
  @exportAccessors("native_number", "native_set_number")
  var fourtyTwo = 42.toShort
  @exportAccessors("native_constant_string")
  val snRocks: CString = c"ScalaNativeRocks!"

  println(fourtyTwo)

  type Foo = CStruct5[Short, Int, Long, Double, CString]

  @exported
  def sayHello(): Unit = {
    println(s"""
         |==============================
         |Hello Scala Native from library
         |==============================
         |
    """.stripMargin)
  }

  @exported("add_longs")
  def addLongs(l: Long, r: Long): Long = l + r

  @exported
  def retStructPtr(): Ptr[Foo] = {
    val ptr = malloc(sizeof[Foo]).asInstanceOf[Ptr[Foo]]

    ptr._1 = 42
    ptr._2 = 2020
    ptr._3 = 27
    ptr._4 = 14.4556
    ptr._5 = snRocks
    ptr
  }

  @exported
  def updateStruct(ptr: Ptr[Foo]): Unit = {
    updateInternally(ptr)
  }

  @noinline
  def updateInternally(ptr: Ptr[Foo]): Unit = {
    ptr._2 = addLongs(2020, 1).toInt
  }

  @exported
  def fail(): Unit = {
    throw new RuntimeException("Exception from ScalaNative")
  }

  @exported
  @name("sn_runGC")
  @noinline
  def enforceGC(): Unit = System.gc()
}
