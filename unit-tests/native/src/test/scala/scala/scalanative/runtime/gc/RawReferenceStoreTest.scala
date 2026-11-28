package scala.scalanative.runtime.gc

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.annotation.nooptimize
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{GC, Intrinsics, ObjectArray, RawPtr}

object RawReferenceStoreTest {
  final class Payload(val x: Long, val y: Long)
  final class Junk(val a: Long)

  // The slot and the value come from the caller: the compiler knows neither
  // the object written into nor that the value is a reference.
  @noinline @nooptimize def put(slot: RawPtr, value: RawPtr): Unit =
    Intrinsics.storeRawPtr(slot, value)

  @noinline @nooptimize def fill(array: Array[AnyRef], i: Int, v: Int): Unit =
    put(
      array.asInstanceOf[ObjectArray].atRaw(i),
      Intrinsics.castObjectToRawPtr(new Payload(v, ~v.toLong))
    )

  @noinline @nooptimize def churn(count: Int): Long = {
    var sum = 0L
    var i = 0
    while (i < count) { sum += new Junk(i).a; i += 1 }
    sum
  }

  // Allocates until the GC has collected twice; false when the heap is so
  // large that it does not within the bound.
  def churnUntilCollected(): Boolean = {
    def collections = GC.getStatsCollectionTotal().toLong
    val before = collections
    var batches = 0
    while (collections < before + 2 && batches < 256) {
      churn(250000)
      batches += 1
    }
    collections >= before + 2
  }
}

class RawReferenceStoreTest {
  import RawReferenceStoreTest._

  /* A generational collector has to learn about a reference stored into an
   * object that survived earlier collections, also when the store is a raw
   * one.
   */
  @Test def referencesStoredThroughRawPointersSurvive(): Unit = {
    assumeFalse("requires a collecting GC", LinktimeInfo.gc.isNone)
    val array = new Array[AnyRef](512)
    System.gc()
    System.gc()
    var round = 0
    while (round < 4) {
      var i = 0
      while (i < array.length) { fill(array, i, i + round); i += 1 }
      assumeTrue(
        "no collection after the stores, the heap is too large",
        churnUntilCollected()
      )
      i = 0
      while (i < array.length) {
        val expected = i + round
        array(i) match {
          case p: Payload =>
            assertEquals(s"round=$round i=$i", expected.toLong, p.x)
            assertEquals(s"round=$round i=$i", ~expected.toLong, p.y)
          case other => fail(s"round=$round i=$i: $other")
        }
        i += 1
      }
      round += 1
    }
  }
}
