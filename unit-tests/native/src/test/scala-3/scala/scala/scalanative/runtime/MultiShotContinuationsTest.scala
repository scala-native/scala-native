package scala.scalanative.runtime

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.{
  isContinuationsSupported, isMultiShotContinuationsSupported
}

import Continuations.multi._

/** Multi-shot continuations, `Continuations.multi`. */
class MultiShotContinuationsTest extends ContinuationsApiTest:
  protected val api: Continuations.Api =
    if isMultiShotContinuationsSupported then Continuations.multi else null

  @Test def multiShotResumesManyTimes(): Unit =
    if isMultiShotContinuationsSupported then
      var resume: Int => Int = null
      val first = boundary[Int] {
        suspend[Int, Int] { k => resume = k; -1 } + 1
      }
      assertEquals(-1, first)
      assertEquals(2, resume(1))
      assertEquals(3, resume(2))

  @Test def cannotCaptureAcrossOneShotBoundary(): Unit =
    if isContinuationsSupported then
      val result = boundary[Int] { label ?=>
        Continuations.single.boundary[Int] {
          assertThrows(
            classOf[IllegalStateException],
            () => suspend[Int, Int](_ => 1)(using label)
          )
          2
        }
      }
      assertEquals(2, result)

  @Test def oneShotCannotSuspendAcrossMultiShotBoundary(): Unit =
    if isContinuationsSupported then
      val single = Continuations.single
      val result = single.boundary[Int] { label ?=>
        boundary[Int] {
          assertThrows(
            classOf[IllegalStateException],
            () => single.suspend[Int, Int](_ => 1)(using label)
          )
          2
        } + 1
      }
      assertEquals(3, result)

end MultiShotContinuationsTest
