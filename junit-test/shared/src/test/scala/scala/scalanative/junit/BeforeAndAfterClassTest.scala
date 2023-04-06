package scala.scalanative.junit

import org.junit._
import org.junit.Assert._
import org.junit.Assume._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import java.util.concurrent.atomic.AtomicBoolean

import scala.scalanative.junit.utils.JUnitTest
import scala.scalanative.junit.async.await

object TraitParent {
  @BeforeClass def beforeTrait(): Unit =
    assumeFalse("before should be ignored in trait", true)
  @AfterClass def afterTrait(): Unit =
    assumeFalse("after should be ignored in trait", true)
}

object State {
  // Becouse we modify shared global state make sure only 1 test is being executed at a time
  def acquire() = while (!locked.compareAndSet(false, true)) {
    if (Thread.interrupted()) throw new InterruptedException()
    await { Future("waiting") }
  }
  def release() = locked.set(false)
  private val locked = new AtomicBoolean(false)
}

trait TraitParent {
  @Test def test(): Unit = ()
}

object ClassParent {
  var wasReached = false
  @BeforeClass def beforeParentClass(): Unit = {
    // It's the first handler that would be called, ensure its state would not be interfered
    State.acquire()
    assertFalse("beforeParent class should not be yet reached", wasReached)
    wasReached = true
  }
  @AfterClass def afterParentClass(): Unit = {
    assertFalse("afterParent should be reset in child", wasReached)
    State.release() // last handler
  }
}
abstract class ClassParent extends TraitParent()

object BeforeAndAfterClassTest {
  var wasChildReached = false
  @BeforeClass def beforeClass(): Unit = {
    assertTrue("beforeClass parent should be reached", ClassParent.wasReached)
    assertFalse("beforeClass child should not be reached", wasChildReached)
    wasChildReached = true
  }
  @AfterClass def afterClass(): Unit = {
    assertTrue("afterClass parent should be reached", ClassParent.wasReached)
    assertTrue("afterClass child should be reached", wasChildReached)

    // Clean the static state for the next run
    ClassParent.wasReached = false // checked in ClassParent::afterParentClass
    wasChildReached = false
  }
}

/* Expected order of execution:
 * - Before parent
 * - Before class
 * - Test
 * - After class
 * - After parent
 */
class BeforeAndAfterClassTest extends ClassParent()

class BeforeAndAfterClassTestAssertions extends JUnitTest
