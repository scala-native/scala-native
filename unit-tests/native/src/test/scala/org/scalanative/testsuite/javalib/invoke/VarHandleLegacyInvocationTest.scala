package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.{MethodHandles, VarHandle}

import org.junit.Assert._
import org.junit.Test

/** Native also lowers ordinary varargs calls from older Scala 3 compilers. The
 *  same calls are not executable on the JVM without signature-polymorphic
 *  typing.
 */
class VarHandleLegacyInvocationTest {
  private class Box {
    var value: Int = 7
    val handle: VarHandle = MethodHandles
      .lookup()
      .findVarHandle(classOf[Box], "value", classOf[Int])
  }

  @Test def explicitWitnessCastsAndAtomicOperations(): Unit = {
    val box = new Box
    assertEquals(7, box.handle.get(box).asInstanceOf[Int])
    assertEquals(7, box.handle.getAndAdd(box, 2).asInstanceOf[Int])
    assertEquals(9, box.value)
    assertTrue(box.handle.compareAndSet(box, 9, 11))
    assertEquals(
      11,
      box.handle.compareAndExchange(box, 11, 13).asInstanceOf[Int]
    )
    box.handle.set(box, 17)
    assertEquals(17, box.value)
  }

  @Test def discardedWitnessStillUpdatesTheField(): Unit = {
    val box = new Box
    box.handle.getAndAdd(box, 2)
    assertEquals(9, box.value)
  }
}
