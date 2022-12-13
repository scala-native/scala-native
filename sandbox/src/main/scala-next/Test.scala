import scala.language.experimental.captureChecking

import scala.scalanative.runtime.Intrinsics.*
import scala.reflect.ClassTag
import scala.scalanative.unsigned.*
import scala.scalanative.unsafe.*

class Zone{
  import scala.scalanative.libc.stdlib.{malloc,free}
  import scala.scalanative.libc.string.memset
  import scala.scalanative.runtime.toRawPtr

  private def size = (sizeof[Byte].toInt * 1024).toUSize
  private var cursor = {
    val chunk = malloc(size)
    memset(chunk, 0, size)
    chunk
  }
  private val limit = cursor + size
  private val gcHandle = Zone.GC.register(cursor, limit)


  def close(): Unit = {
    Zone.GC.unregister(gcHandle)
    val start = limit-size
    memset(start, 0, size)
    free(start)
  }

  inline def alloc[T <: AnyRef](inline cls: Class[T]): T = {
    val size: CSize = castRawSizeToInt(sizeOf(cls)).toUInt
    val ptr = cursor
    cursor += size
    if(cursor.toLong > limit.toLong) throw new RuntimeException("OOM")
    println(s"alloc $cls size=$size")
    !(ptr.asInstanceOf[Ptr[Class[_]]]) = cls
    castRawPtrToObject(toRawPtr(ptr)).asInstanceOf[T]
  }
}

object Zone{
  opaque type GCRootHandle = Ptr[_]
  @extern object GC{
    @name("scalanative_gc_register_root")
    def register(start: Ptr[_], end: Ptr[_]): GCRootHandle = extern
    @name("scalanative_gc_unregister_root")
    def unregister(handle: GCRootHandle): Unit = extern
  }
}

case class Foo(var v: Int, var next: Foo):
  override def toString(): String = s"Foo@${System.identityHashCode(this).toHexString}{v=$v,next=$next}"

object Test {
  @noinline def setup() = {
    val zone = new Zone()
    val x = zone.alloc(classOf[Foo])
    println("allocated")
    x.v = 1
    x.next = {
      val local = new Foo(42, next = null)
      val local2 = new Foo(43, next = local)
      val local3 = new Foo(44, next= local2)
      val _ = local3.toString()
      local3
    }

    val x2 = zone.alloc(classOf[Foo])
    assert(x ne x2, "objects are different")
    x2.v = 2
    x2.next = x
    Seq.fill(10)(zone.alloc(classOf[Foo]))
    x2
  }

// @main def Test() =
  def main(args: Array[String]) = {
    println("Hello Scala Next!")
   
    val x2 = setup()
    for i <- 0 until 10
    do {
      println(s"iteration $i state=$x2")
      for _ <- 0 until 100 
      do {
        var local = new Foo(-1, x2)
        while(local.v != 42){
          assert(local.next != null, local)
          local = local.next
        }
        Seq.fill(100){
        scala.util.Random.alphanumeric.take(128).map(_.toUpper).mkString.take(10)
        }
      }
    }
  }
}
