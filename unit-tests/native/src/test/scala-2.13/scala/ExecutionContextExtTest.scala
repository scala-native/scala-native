package scala

import org.junit.Test
import org.junit.Assert._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

/* Dummy test used determinate if scala.concurrent.ExecutionContext was correctly overridden
 * In case if it is not it would fail at linking or with UndefinedBehaviourException in runtime
 */
class ExecutionContextExtTest {

  @Test
  def testOpportunisticTest(): Unit = {
    implicit val opportunistic: ExecutionContext =
      ExecutionContext.opportunistic

    assertNotNull(opportunistic)
    if (isMultithreadingEnabled)
      assertNotEquals(ExecutionContext.global, opportunistic)
    else {
      assertEquals(ExecutionContext.global, opportunistic)
      assertEquals(
        scala.scalanative.runtime.ExecutionContext.global,
        opportunistic
      )
    }

    var x = 0
    Future {
      x = 90
    }
    if (!isMultithreadingEnabled) {
      // always true, logic in Future would be executed after this Runnable ends
      assertEquals(0, x)
      x = 40
      assertEquals(40, x)
    }
  }

  @Test
  def testParasiteContext(): Unit = {
    implicit val parasitic: ExecutionContext = ExecutionContext.parasitic

    assertNotNull(parasitic)
    assertNotEquals(ExecutionContext.global, parasitic)

    var x = 0
    Future {
      x = 90
    }
    if (!isMultithreadingEnabled) {
      // always true, logic in Future would be executed in this thread before continuing
      assertEquals(90, x)
      x = 40
      assertEquals(40, x)
    }
  }
}
