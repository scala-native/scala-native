package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleMatrixTest.scala.gyb; edit the template.
// format: off
import java.lang.invoke.{MethodHandles, VarHandle}
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBooleanInstanceFixture
{
  var value: Boolean = false
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleBooleanInstanceFixture], "value", java.lang.Boolean.TYPE)
}

class VarHandleBooleanInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Boolean, actual: Boolean): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val read: Boolean = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val read: Boolean = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val read: Boolean = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val read: Boolean = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val failed: Boolean = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Boolean = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val failed: Boolean = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val failed: Boolean = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val witness: Boolean = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val witness: Boolean = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    val witness: Boolean = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_unsupported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Boolean = handle.getAndAdd(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndAddAcquire_unsupported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Boolean = handle.getAndAddAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndAddRelease_unsupported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Boolean = handle.getAndAddRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseOr(box, mask)
      assertValue(before, witness)
      assertValue(before | mask, box.value)
    }
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseOrAcquire(box, mask)
      assertValue(before, witness)
      assertValue(before | mask, box.value)
    }
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseOrRelease(box, mask)
      assertValue(before, witness)
      assertValue(before | mask, box.value)
    }
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseAnd(box, mask)
      assertValue(before, witness)
      assertValue(before & mask, box.value)
    }
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseAndAcquire(box, mask)
      assertValue(before, witness)
      assertValue(before & mask, box.value)
    }
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseAndRelease(box, mask)
      assertValue(before, witness)
      assertValue(before & mask, box.value)
    }
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseXor(box, mask)
      assertValue(before, witness)
      assertValue(before ^ mask, box.value)
    }
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseXorAcquire(box, mask)
      assertValue(before, witness)
      assertValue(before ^ mask, box.value)
    }
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      box.value = before
      val witness: Boolean = handle.getAndBitwiseXorRelease(box, mask)
      assertValue(before, witness)
      assertValue(before ^ mask, box.value)
    }
  }

}
class VarHandleByteInstanceFixture
{
  var value: Byte = 0.toByte
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleByteInstanceFixture], "value", java.lang.Byte.TYPE)
}

class VarHandleByteInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Byte, actual: Byte): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val read: Byte = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val read: Byte = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val read: Byte = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val read: Byte = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val failed: Byte = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Byte = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val failed: Byte = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val failed: Byte = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndAdd(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndAddAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndAddRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseOr(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseOrAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseOrRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseAnd(box, desired)
    assertValue(initial, witness)
    assertValue(0.toByte, box.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseAndAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(0.toByte, box.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseAndRelease(box, desired)
    assertValue(initial, witness)
    assertValue(0.toByte, box.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseXor(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseXorAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    val witness: Byte = handle.getAndBitwiseXorRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toByte, box.value)
  }

}
class VarHandleShortInstanceFixture
{
  var value: Short = 0.toShort
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleShortInstanceFixture], "value", java.lang.Short.TYPE)
}

class VarHandleShortInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Short, actual: Short): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val read: Short = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val read: Short = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val read: Short = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val read: Short = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val failed: Short = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Short = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val failed: Short = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val failed: Short = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Short = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndAdd(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndAddAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndAddRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseOr(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseOrAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseOrRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseAnd(box, desired)
    assertValue(initial, witness)
    assertValue(0.toShort, box.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseAndAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(0.toShort, box.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseAndRelease(box, desired)
    assertValue(initial, witness)
    assertValue(0.toShort, box.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseXor(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseXorAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    val witness: Short = handle.getAndBitwiseXorRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toShort, box.value)
  }

}
class VarHandleCharInstanceFixture
{
  var value: Char = 0.toChar
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleCharInstanceFixture], "value", java.lang.Character.TYPE)
}

class VarHandleCharInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Char, actual: Char): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val read: Char = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val read: Char = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val read: Char = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val read: Char = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val failed: Char = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Char = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val failed: Char = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val failed: Char = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Char = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndAdd(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndAddAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndAddRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseOr(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseOrAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseOrRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseAnd(box, desired)
    assertValue(initial, witness)
    assertValue(0.toChar, box.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseAndAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(0.toChar, box.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseAndRelease(box, desired)
    assertValue(initial, witness)
    assertValue(0.toChar, box.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseXor(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseXorAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    val witness: Char = handle.getAndBitwiseXorRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toChar, box.value)
  }

}
class VarHandleIntInstanceFixture
{
  var value: Int = 0.toInt
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleIntInstanceFixture], "value", java.lang.Integer.TYPE)
}

class VarHandleIntInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Int, actual: Int): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val read: Int = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val read: Int = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val read: Int = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val read: Int = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val failed: Int = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Int = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val failed: Int = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val failed: Int = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Int = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndAdd(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndAddAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndAddRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseOr(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseOrAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseOrRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseAnd(box, desired)
    assertValue(initial, witness)
    assertValue(0.toInt, box.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseAndAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(0.toInt, box.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseAndRelease(box, desired)
    assertValue(initial, witness)
    assertValue(0.toInt, box.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseXor(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseXorAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    val witness: Int = handle.getAndBitwiseXorRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toInt, box.value)
  }

}
class VarHandleLongInstanceFixture
{
  var value: Long = 0.toLong
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleLongInstanceFixture], "value", java.lang.Long.TYPE)
}

class VarHandleLongInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Long, actual: Long): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val read: Long = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val read: Long = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val read: Long = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val read: Long = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val failed: Long = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Long = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val failed: Long = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val failed: Long = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Long = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndAdd(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndAddAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndAddRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseOr(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseOrAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseOrRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseAnd(box, desired)
    assertValue(initial, witness)
    assertValue(0.toLong, box.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseAndAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(0.toLong, box.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseAndRelease(box, desired)
    assertValue(initial, witness)
    assertValue(0.toLong, box.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseXor(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseXorAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    val witness: Long = handle.getAndBitwiseXorRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toLong, box.value)
  }

}
class VarHandleFloatInstanceFixture
{
  var value: Float = 0.toFloat
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleFloatInstanceFixture], "value", java.lang.Float.TYPE)
}

class VarHandleFloatInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Float, actual: Float): Unit =
    assertEquals(java.lang.Float.floatToRawIntBits(expected), java.lang.Float.floatToRawIntBits(actual))

  @Test def get_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val read: Float = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val read: Float = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val read: Float = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val read: Float = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val failed: Float = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Float = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val failed: Float = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val failed: Float = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Float = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val witness: Float = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val witness: Float = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val witness: Float = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val witness: Float = handle.getAndAdd(box, desired)
    assertValue(initial, witness)
    assertValue(15.toFloat, box.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val witness: Float = handle.getAndAddAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toFloat, box.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    val witness: Float = handle.getAndAddRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toFloat, box.value)
  }

  @Test def getAndBitwiseOr_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseOr(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOrAcquire_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOrRelease_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAnd_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseAnd(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAndAcquire_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAndRelease_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXor_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseXor(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXorAcquire_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXorRelease_unsupported(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

}
class VarHandleDoubleInstanceFixture
{
  var value: Double = 0.toDouble
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleDoubleInstanceFixture], "value", java.lang.Double.TYPE)
}

class VarHandleDoubleInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Double, actual: Double): Unit =
    assertEquals(java.lang.Double.doubleToRawLongBits(expected), java.lang.Double.doubleToRawLongBits(actual))

  @Test def get_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val read: Double = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val read: Double = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val read: Double = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val read: Double = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val failed: Double = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Double = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val failed: Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val failed: Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val witness: Double = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val witness: Double = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val witness: Double = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val witness: Double = handle.getAndAdd(box, desired)
    assertValue(initial, witness)
    assertValue(15.toDouble, box.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val witness: Double = handle.getAndAddAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(15.toDouble, box.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    val witness: Double = handle.getAndAddRelease(box, desired)
    assertValue(initial, witness)
    assertValue(15.toDouble, box.value)
  }

  @Test def getAndBitwiseOr_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseOr(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOrAcquire_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOrRelease_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAnd_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseAnd(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAndAcquire_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAndRelease_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXor_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseXor(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXorAcquire_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXorRelease_unsupported(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

}
class VarHandleReferenceInstanceFixture
{
  var value: AnyRef = null
  val handle: VarHandle = MethodHandles.lookup()
    .findVarHandle(classOf[VarHandleReferenceInstanceFixture], "value", classOf[AnyRef])
}

class VarHandleReferenceInstanceMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: AnyRef, actual: AnyRef): Unit =
    assertSame(expected, actual)

  @Test def get_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val read: AnyRef = handle.get(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val read: AnyRef = handle.getOpaque(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val read: AnyRef = handle.getAcquire(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val read: AnyRef = handle.getVolatile(box)
    assertValue(initial, read)
    assertValue(initial, box.value)
  }

  @Test def set_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    handle.set(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    handle.setOpaque(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setRelease_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    handle.setRelease(box, desired)
    assertValue(desired, box.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    handle.setVolatile(box, desired)
    assertValue(desired, box.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertFalse(handle.compareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    assertTrue(handle.compareAndSet(box, initial, desired))
    assertValue(desired, box.value)
    assertFalse(handle.compareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertFalse(handle.weakCompareAndSetPlain(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetPlain(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertFalse(handle.weakCompareAndSet(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSet(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetAcquire(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertFalse(handle.weakCompareAndSetRelease(box, desired, initial))
    assertValue(initial, box.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(box, initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, box.value)
    assertFalse(handle.weakCompareAndSetRelease(box, initial, initial))
    assertValue(desired, box.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertValue(initial, failed)
    assertValue(initial, box.value)
    val succeeded: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, box.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val witness: AnyRef = handle.getAndSet(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val witness: AnyRef = handle.getAndSetAcquire(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    val witness: AnyRef = handle.getAndSetRelease(box, desired)
    assertValue(initial, witness)
    assertValue(desired, box.value)
  }

  @Test def getAndAdd_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndAdd(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndAddAcquire_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndAddAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndAddRelease_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndAddRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOr_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseOr(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOrAcquire_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseOrRelease_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAnd_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseAnd(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAndAcquire_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseAndRelease_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXor_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseXor(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXorAcquire_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertValue(initial, box.value)
  }

  @Test def getAndBitwiseXorRelease_unsupported(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = invocationHandle(box.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertValue(initial, box.value)
  }

}
