package scala

import org.junit.Test
import org.junit.Assert._
import scala.concurrent.{ExecutionContext, Future}

/* Dummy test used determinate if scala.concurrent.ExecutionContext was correctly overridden
 * In case if it is not it would fail at linking or with UndefinedBehaviourException in runtime
 */
class ExecutionContextExtTest {

  @Test
  def testOpportunisticTest(): Unit = {
    implicit val opportunistic: ExecutionContext = ExecutionContext.opportunistic

    assertNotNull(opportunistic)
    assertEquals(ExecutionContext.global, opportunistic)

    var x = 0
    Future {
      x = 90
    }
    assertEquals(0, x) // always true, logic in Future would be executed after this Runnable ends
    x = 40
    assertEquals(40, x)
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
    assertEquals(90, x) // always true, logic in Future would be executed in this thread before continuing
    x = 40
    assertEquals(40, x)
  }
}
