import scala.scalanative.runtime.{fromRawPtr, libc}
import scala.scalanative.unsafe._

object libtest {
  val fourtyTwo = 42.toShort
  var snRocks: CString = _
  snRocks = c"ScalaNativeRocks!"

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

  @exported
  def addLongs(l: Long, r: Long): Long = l + r

  @exported
  def allocFoo(): Ptr[Foo] = {
    val ptr = fromRawPtr[Foo](libc.malloc(sizeof[Foo]))
    println("zzzb " + ptr)
    ptr
  }

  @exported
  def retStructPtr(): Ptr[Foo] = {
    println("zzz")
    val ptr = fromRawPtr[Foo](libc.malloc(sizeof[Foo]))

    ptr._1 = fourtyTwo
    ptr._2 = 2020
    ptr._3 = 27
    ptr._4 = 14.4556
    ptr._5 = snRocks
    println("zzza " + ptr)
    ptr
  }

  @exported
  def updateStruct(ptrxx: Ptr[Foo]): Unit = {
    println("yyy")
    updateInternally(ptrxx)
    //println("yyyx " + ptrxx._2)
    //updateInternally(ptrxx)
    //val x = addLongs(2020, 1).toInt
    //println("yyya " + x)
    //println("yyyb " + !ptr)
    //println("yyyc " + (!ptr)._2)
    //ptrxx._2 = x
    //println("yyy2")
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
