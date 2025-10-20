package scala.scalanative.junit

import org.junit.*
import org.junit.Assert.*
import org.junit.Assume.*

import scala.scalanative.junit.utils.JUnitTest

object TraitParent {
  @BeforeClass def beforeTrait(): Unit =
    assumeFalse("before should be ignored in trait", true)
  @AfterClass def afterTrait(): Unit =
    assumeFalse("after should be ignored in trait", true)
}

trait TraitParent {
  @Test def test(): Unit = ()
}

object ClassParent {
  var wasReached = false
  @BeforeClass def beforeParentClass(): Unit = {
    assertFalse("beforeParent class should not be yet reached", wasReached)
    wasReached = true
  }
  @AfterClass def afterParentClass(): Unit = {
    assertFalse("afterParent should be reset in child", wasReached)
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
