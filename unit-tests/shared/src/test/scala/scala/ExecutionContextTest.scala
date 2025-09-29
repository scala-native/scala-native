package scala

import scala.concurrent.{ExecutionContext, Future}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

/* Dummy test used determinate if scala.concurrent.ExecutionContext was correctly overridden
 * In case if it is not it would fail at linking or with UndefinedBehaviourException in runtime
 */
class ExecutionContextTest {

  @Test
  def testGlobalContext(): Unit = {
    implicit val global = ExecutionContext.global
    assertNotNull(global)
    assertNotNull(ExecutionContext.Implicits.global)
    assertNotNull(ExecutionContext.defaultReporter)

    assertEquals(global, ExecutionContext.Implicits.global)

    var x = 0
    Future {
      x = 90
    }
    if (!Platform.isMultithreadingEnabled) {
      // always true, logic in Future would be executed after this Runnable ends
      assertEquals(0, x)
      x = 40
      assertEquals(40, x)
    }
  }

}
