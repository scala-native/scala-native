package scala.scalanative.runtime

import java.util.concurrent.atomic.AtomicInteger

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.unsafe._

@extern
object UnwindBacktraceTestFFI {
  def _Unwind_Backtrace(
      trace: CFuncPtr2[Ptr[Byte], Ptr[Byte], CInt],
      context: Ptr[Byte]
  ): CInt = extern

  def _Unwind_GetIP(context: Ptr[Byte]): Ptr[Byte] = extern

  def dladdr(addr: Ptr[Byte], info: Ptr[Byte]): CInt = extern
}

/** Backtrace has to step through libsystem frames. On macOS 27 / arm64e.x1
 *  those frames are compact-unwind mode FRAME_PAUTH_LR, and the vendored
 *  unwinder used to abort there.
 */
class UnwindBacktraceTest {

  @Test def backtraceOnMainThreadFinishes(): Unit = {
    // assumeFalse still links the calls. Windows has no dladdr or Itanium
    // _Unwind_*, so the calls themselves have to be link-time dead.
    assumeFalse("unwind backtrace is not available", isWindows)
    if (isWindows) ()
    else checkMainThread()
  }

  @Test def backtraceOnWorkerReachesThreadStart(): Unit = {
    assumeFalse("unwind backtrace is not available", isWindows)
    if (isWindows) ()
    else checkWorker()
  }

  private def checkMainThread(): Unit = {
    val (status, frames, _) = UnwindBacktrace.walk()
    assertEquals(UnwindBacktrace.EndOfStack, status)
    assertTrue(s"expected at least one frame, got $frames", frames >= 1)
  }

  private def checkWorker(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    val status = new AtomicInteger(-1)
    val frames = new AtomicInteger
    val sawThreadStart = new AtomicInteger

    val worker = new Thread(() => {
      val (rc, n, saw) = UnwindBacktrace.walk()
      status.set(rc)
      frames.set(n)
      if (saw) sawThreadStart.set(1)
    })
    worker.start()
    worker.join()

    assertEquals(UnwindBacktrace.EndOfStack, status.get())
    assertTrue(
      s"worker backtrace returned ${frames.get()} frames",
      frames.get() >= 2
    )
    if (scala.scalanative.runtime.Platform.isMac())
      assertTrue(
        "worker backtrace did not reach pthread thread_start",
        sawThreadStart.get() == 1
      )
  }
}

object UnwindBacktrace {
  // _Unwind_Reason_Code._URC_END_OF_STACK, from the vendored libunwind.
  val EndOfStack = 5

  private type DlInfo = CStruct4[CString, Ptr[Byte], CString, Ptr[Byte]]
  private type WalkState = CStruct2[CInt, CInt]

  def walk(): (Int, Int, Boolean) = {
    val state = stackalloc[WalkState]()
    state._1 = 0
    state._2 = 0
    val status = UnwindBacktraceTestFFI._Unwind_Backtrace(
      onFrame _,
      state.asInstanceOf[Ptr[Byte]]
    )
    (status.toInt, state._1.toInt, state._2 != 0)
  }

  def onFrame(context: Ptr[Byte], arg: Ptr[Byte]): CInt = {
    val state = arg.asInstanceOf[Ptr[WalkState]]
    state._1 = state._1 + 1

    val info = stackalloc[DlInfo]()
    val ip = UnwindBacktraceTestFFI._Unwind_GetIP(context)
    if (ip != null && UnwindBacktraceTestFFI.dladdr(
          ip,
          info.asInstanceOf[Ptr[Byte]]
        ) != 0)
      if (contains(info._3, c"thread_start"))
        state._2 = 1
    0 // _URC_NO_REASON: keep walking
  }

  private def contains(haystack: CString, needle: CString): Boolean = {
    if (haystack == null || needle == null) false
    else {
      var i = 0
      var found = false
      while (!found && haystack(i) != 0.toByte) {
        var j = 0
        var matched = true
        while (matched && needle(j) != 0.toByte) {
          if (haystack(i + j) != needle(j)) matched = false
          else j += 1
        }
        if (matched) found = true
        else i += 1
      }
      found
    }
  }
}
