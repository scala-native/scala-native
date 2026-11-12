package scala.scalanative.runtime

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isMultiShotContinuationsSupported

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

end MultiShotContinuationsTest
