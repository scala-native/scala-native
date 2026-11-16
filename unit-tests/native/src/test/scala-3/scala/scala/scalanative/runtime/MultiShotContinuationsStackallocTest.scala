package scala.scalanative.runtime

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.meta.LinktimeInfo.isMultiShotContinuationsSupported
import scala.scalanative.unsafe._

import Continuations.multi._

object MultiShotContinuationsStackallocTest:
  // Not constants, so that the allocations cannot be folded or moved
  @volatile var enabled = true
  @volatile var iterations = 8

  var resumeWith: Int => Long = null
  var resumeLoop: Unit => Long = null

  @noinline def read(p: Ptr[Long]): Long = !p
  @noinline def keep(result: Long, depth: Int): Long =
    if enabled then result else result + depth

  /** Resumes on a stack deeper than the one the continuation was captured or
   *  last resumed on, so that its frames are copied to another address.
   */
  @noinline def resumeDeeper[A](depth: Int, resume: A => Long, value: A): Long =
    if depth == 0 then resume(value)
    else keep(resumeDeeper(depth - 1, resume, value), depth)

  @noinline def conditionalStackalloc()(using BoundaryLabel[Long]): Long =
    var sum = 0L
    if enabled then
      val p = stackalloc[Long]()
      !p = 42L
      sum += read(p)
    val x = suspend[Int, Long] { k => resumeWith = k; -1L }
    if enabled then
      val p = stackalloc[Long]()
      !p = x.toLong
      sum += read(p)
    sum

  @noinline def conditionalStackallocOfArray()(using
      BoundaryLabel[Long]
  ): Long =
    var sum = 0L
    if enabled then
      val p = stackalloc[Long](16)
      var i = 0
      while i < 16 do
        sum += read(p + i) // zeroed
        p(i) = i.toLong
        i += 1
      sum += read(p + 15)
    val x = suspend[Int, Long] { k => resumeWith = k; -1L }
    if enabled then
      val p = stackalloc[Long](16)
      sum += read(p + 15) // zeroed
      p(3) = x.toLong
      sum += read(p + 3)
    sum

  /* Has no exception handlers, so that it is optimized in release mode. The
   * optimizer guards the stack allocations done in the loop with a stack
   * state restored in each iteration.
   */
  @noinline def stackallocInLoop()(using BoundaryLabel[Long]): Long =
    var sum = 0L
    var zeroed = true
    var i = 0
    while i < iterations do
      val p = stackalloc[Long]()
      if read(p) != 0L then zeroed = false
      !p = i.toLong + 1
      sum += read(p)
      // Pointers to the stack are not valid after the continuation is resumed
      suspend[Unit, Long] { k => resumeLoop = k; -1L }
      i += 1
    if zeroed then sum else -2L
end MultiShotContinuationsStackallocTest

/* The stack of a multi-shot continuation is copied to the stack of its
 * resumer. A frame which allocates stack memory outside of its entry block
 * would be addressed by the frame pointer, which is stale in the copy.
 */
class MultiShotContinuationsStackallocTest:
  import MultiShotContinuationsStackallocTest._

  @Test def conditionalStackallocAroundSuspend(): Unit =
    if isMultiShotContinuationsSupported then
      assertEquals(-1L, boundary[Long](conditionalStackalloc()))
      assertEquals(43L, resumeWith(1))
      assertEquals(44L, resumeDeeper(50, resumeWith, 2))
      assertEquals(45L, resumeWith(3))

  @Test def conditionalStackallocOfArrayAroundSuspend(): Unit =
    if isMultiShotContinuationsSupported then
      assertEquals(-1L, boundary[Long](conditionalStackallocOfArray()))
      assertEquals(16L, resumeWith(1))
      assertEquals(17L, resumeDeeper(50, resumeWith, 2))
      assertEquals(18L, resumeWith(3))

  @Test def stackallocInLoopWithSuspend(): Unit =
    if isMultiShotContinuationsSupported then
      // Allocations done in loops are static only if the optimizer has proven
      // that the memory does not escape the iteration
      assumeTrue(
        "stack allocations in loops are dynamic in debug mode",
        LinktimeInfo.releaseMode
      )
      var result = boundary[Long](stackallocInLoop())
      var resumes = 0
      while result == -1L do
        result = resumeDeeper(resumes % 4 * 10, resumeLoop, ())
        resumes += 1
      val n = iterations.toLong
      assertEquals(n * (n + 1) / 2, result)
      assertEquals(iterations, resumes)

end MultiShotContinuationsStackallocTest
