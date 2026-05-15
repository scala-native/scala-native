package scala.scalanative.runtime

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.unsafe._

@extern object MainThreadStackSizeTestFFI {
  def scalanative_test_rlimit_stack(): CSize = extern
  def scalanative_test_simulate_main_thread_setup(): CSize = extern
}

class MainThreadStackSizeTest {
  import MainThreadStackSizeTestFFI._

  @Test def mainThreadMaxStackSizeReflectsRLimit(): Unit = {
    val pageSize = Platform.pageSize.toLong
    val rlimit = scalanative_test_rlimit_stack().toLong
    assumeTrue("RLIMIT_STACK unavailable", rlimit > 4 * pageSize)

    val expected = rlimit - 4 * pageSize
    val observed = scalanative_test_simulate_main_thread_setup().toLong

    assertTrue(
      s"maxStackSize=$observed < expected=$expected (RLIMIT_STACK=$rlimit)",
      observed >= expected
    )
  }
}
