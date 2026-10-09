package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleMatrixTest.scala.gyb; edit the template.
// format: off
import java.lang.invoke.{MethodHandles, VarHandle}
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.annotation.static

class VarHandleBooleanStaticFixture
object VarHandleBooleanStaticFixture {
  @static var value: Boolean = false
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleBooleanStaticFixture], "value", java.lang.Boolean.TYPE)
}

class VarHandleBooleanStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Boolean, actual: Boolean): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val read: Boolean = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleBooleanStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val read: Boolean = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val read: Boolean = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleBooleanStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val read: Boolean = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleBooleanStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleBooleanStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val failed: Boolean = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    val succeeded: Boolean = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val failed: Boolean = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    val succeeded: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val failed: Boolean = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleBooleanStaticFixture.value)
    val succeeded: Boolean = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val witness: Boolean = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val witness: Boolean = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    val witness: Boolean = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAndAdd_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Boolean = handle.getAndAdd(desired)
    })
    assertValue(initial, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAndAddAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Boolean = handle.getAndAddAcquire(desired)
    })
    assertValue(initial, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAndAddRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Boolean = handle.getAndAddRelease(desired)
    })
    assertValue(initial, VarHandleBooleanStaticFixture.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseOr(mask)
      assertValue(before, witness)
      assertValue(before | mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseOrAcquire(mask)
      assertValue(before, witness)
      assertValue(before | mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseOrRelease(mask)
      assertValue(before, witness)
      assertValue(before | mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseAnd(mask)
      assertValue(before, witness)
      assertValue(before & mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseAndAcquire(mask)
      assertValue(before, witness)
      assertValue(before & mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseAndRelease(mask)
      assertValue(before, witness)
      assertValue(before & mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseXor(mask)
      assertValue(before, witness)
      assertValue(before ^ mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseXorAcquire(mask)
      assertValue(before, witness)
      assertValue(before ^ mask, VarHandleBooleanStaticFixture.value)
    }
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleBooleanStaticFixture.handle)
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    for (before <- List(false, true); mask <- List(false, true)) {
      VarHandleBooleanStaticFixture.value = before
      val witness: Boolean = handle.getAndBitwiseXorRelease(mask)
      assertValue(before, witness)
      assertValue(before ^ mask, VarHandleBooleanStaticFixture.value)
    }
  }

}
class VarHandleByteStaticFixture
object VarHandleByteStaticFixture {
  @static var value: Byte = 0.toByte
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleByteStaticFixture], "value", java.lang.Byte.TYPE)
}

class VarHandleByteStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Byte, actual: Byte): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val read: Byte = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleByteStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val read: Byte = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleByteStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val read: Byte = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleByteStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val read: Byte = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleByteStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleByteStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleByteStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleByteStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleByteStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleByteStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleByteStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleByteStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleByteStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleByteStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleByteStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val failed: Byte = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleByteStaticFixture.value)
    val succeeded: Byte = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val failed: Byte = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleByteStaticFixture.value)
    val succeeded: Byte = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val failed: Byte = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleByteStaticFixture.value)
    val succeeded: Byte = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleByteStaticFixture.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndAdd(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndAddAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndAddRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseOr(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseOrAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseOrRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseAnd(desired)
    assertValue(initial, witness)
    assertValue(0.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseAndAcquire(desired)
    assertValue(initial, witness)
    assertValue(0.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseAndRelease(desired)
    assertValue(initial, witness)
    assertValue(0.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseXor(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseXorAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleByteStaticFixture.handle)
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    val witness: Byte = handle.getAndBitwiseXorRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toByte, VarHandleByteStaticFixture.value)
  }

}
class VarHandleShortStaticFixture
object VarHandleShortStaticFixture {
  @static var value: Short = 0.toShort
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleShortStaticFixture], "value", java.lang.Short.TYPE)
}

class VarHandleShortStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Short, actual: Short): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val read: Short = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleShortStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val read: Short = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleShortStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val read: Short = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleShortStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val read: Short = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleShortStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleShortStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleShortStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleShortStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleShortStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleShortStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleShortStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleShortStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleShortStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleShortStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleShortStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val failed: Short = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleShortStaticFixture.value)
    val succeeded: Short = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val failed: Short = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleShortStaticFixture.value)
    val succeeded: Short = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val failed: Short = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleShortStaticFixture.value)
    val succeeded: Short = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleShortStaticFixture.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndAdd(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndAddAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndAddRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseOr(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseOrAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseOrRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseAnd(desired)
    assertValue(initial, witness)
    assertValue(0.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseAndAcquire(desired)
    assertValue(initial, witness)
    assertValue(0.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseAndRelease(desired)
    assertValue(initial, witness)
    assertValue(0.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseXor(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseXorAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleShortStaticFixture.handle)
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    val witness: Short = handle.getAndBitwiseXorRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toShort, VarHandleShortStaticFixture.value)
  }

}
class VarHandleCharStaticFixture
object VarHandleCharStaticFixture {
  @static var value: Char = 0.toChar
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleCharStaticFixture], "value", java.lang.Character.TYPE)
}

class VarHandleCharStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Char, actual: Char): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val read: Char = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleCharStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val read: Char = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleCharStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val read: Char = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleCharStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val read: Char = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleCharStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleCharStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleCharStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleCharStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleCharStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleCharStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleCharStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleCharStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleCharStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleCharStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleCharStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val failed: Char = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleCharStaticFixture.value)
    val succeeded: Char = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val failed: Char = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleCharStaticFixture.value)
    val succeeded: Char = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val failed: Char = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleCharStaticFixture.value)
    val succeeded: Char = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleCharStaticFixture.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndAdd(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndAddAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndAddRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseOr(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseOrAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseOrRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseAnd(desired)
    assertValue(initial, witness)
    assertValue(0.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseAndAcquire(desired)
    assertValue(initial, witness)
    assertValue(0.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseAndRelease(desired)
    assertValue(initial, witness)
    assertValue(0.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseXor(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseXorAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleCharStaticFixture.handle)
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    val witness: Char = handle.getAndBitwiseXorRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toChar, VarHandleCharStaticFixture.value)
  }

}
class VarHandleIntStaticFixture
object VarHandleIntStaticFixture {
  @static var value: Int = 0.toInt
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleIntStaticFixture], "value", java.lang.Integer.TYPE)
}

class VarHandleIntStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Int, actual: Int): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val read: Int = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleIntStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val read: Int = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleIntStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val read: Int = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleIntStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val read: Int = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleIntStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleIntStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleIntStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleIntStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleIntStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleIntStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleIntStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleIntStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleIntStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleIntStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleIntStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val failed: Int = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleIntStaticFixture.value)
    val succeeded: Int = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val failed: Int = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleIntStaticFixture.value)
    val succeeded: Int = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val failed: Int = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleIntStaticFixture.value)
    val succeeded: Int = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleIntStaticFixture.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndAdd(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndAddAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndAddRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseOr(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseOrAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseOrRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseAnd(desired)
    assertValue(initial, witness)
    assertValue(0.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseAndAcquire(desired)
    assertValue(initial, witness)
    assertValue(0.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseAndRelease(desired)
    assertValue(initial, witness)
    assertValue(0.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseXor(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseXorAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleIntStaticFixture.handle)
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    val witness: Int = handle.getAndBitwiseXorRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toInt, VarHandleIntStaticFixture.value)
  }

}
class VarHandleLongStaticFixture
object VarHandleLongStaticFixture {
  @static var value: Long = 0.toLong
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleLongStaticFixture], "value", java.lang.Long.TYPE)
}

class VarHandleLongStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Long, actual: Long): Unit =
    assertEquals(expected, actual)

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val read: Long = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleLongStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val read: Long = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleLongStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val read: Long = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleLongStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val read: Long = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleLongStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleLongStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleLongStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleLongStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleLongStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleLongStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleLongStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleLongStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleLongStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleLongStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleLongStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val failed: Long = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleLongStaticFixture.value)
    val succeeded: Long = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val failed: Long = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleLongStaticFixture.value)
    val succeeded: Long = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val failed: Long = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleLongStaticFixture.value)
    val succeeded: Long = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleLongStaticFixture.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndAdd(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndAddAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndAddRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseOr_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseOr(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseOrAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseOrRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseAnd(desired)
    assertValue(initial, witness)
    assertValue(0.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseAndAcquire(desired)
    assertValue(initial, witness)
    assertValue(0.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseAndRelease(desired)
    assertValue(initial, witness)
    assertValue(0.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseXor_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseXor(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseXorAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleLongStaticFixture.handle)
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    val witness: Long = handle.getAndBitwiseXorRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toLong, VarHandleLongStaticFixture.value)
  }

}
class VarHandleFloatStaticFixture
object VarHandleFloatStaticFixture {
  @static var value: Float = 0.toFloat
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleFloatStaticFixture], "value", java.lang.Float.TYPE)
}

class VarHandleFloatStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Float, actual: Float): Unit =
    assertEquals(java.lang.Float.floatToRawIntBits(expected), java.lang.Float.floatToRawIntBits(actual))

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val read: Float = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val read: Float = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val read: Float = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val read: Float = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleFloatStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleFloatStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleFloatStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleFloatStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleFloatStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleFloatStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleFloatStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleFloatStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleFloatStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleFloatStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val failed: Float = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleFloatStaticFixture.value)
    val succeeded: Float = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val failed: Float = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleFloatStaticFixture.value)
    val succeeded: Float = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val failed: Float = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleFloatStaticFixture.value)
    val succeeded: Float = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val witness: Float = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val witness: Float = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val witness: Float = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val witness: Float = handle.getAndAdd(desired)
    assertValue(initial, witness)
    assertValue(15.toFloat, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val witness: Float = handle.getAndAddAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toFloat, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    val witness: Float = handle.getAndAddRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toFloat, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseOr_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseOr(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseOrAcquire(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseOrRelease(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseAnd(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseAndAcquire(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseAndRelease(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseXor_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseXor(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseXorAcquire(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleFloatStaticFixture.handle)
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Float = handle.getAndBitwiseXorRelease(desired)
    })
    assertValue(initial, VarHandleFloatStaticFixture.value)
  }

}
class VarHandleDoubleStaticFixture
object VarHandleDoubleStaticFixture {
  @static var value: Double = 0.toDouble
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleDoubleStaticFixture], "value", java.lang.Double.TYPE)
}

class VarHandleDoubleStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: Double, actual: Double): Unit =
    assertEquals(java.lang.Double.doubleToRawLongBits(expected), java.lang.Double.doubleToRawLongBits(actual))

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val read: Double = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val read: Double = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val read: Double = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val read: Double = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleDoubleStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val failed: Double = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    val succeeded: Double = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val failed: Double = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    val succeeded: Double = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val failed: Double = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleDoubleStaticFixture.value)
    val succeeded: Double = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val witness: Double = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val witness: Double = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val witness: Double = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndAdd_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val witness: Double = handle.getAndAdd(desired)
    assertValue(initial, witness)
    assertValue(15.toDouble, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndAddAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val witness: Double = handle.getAndAddAcquire(desired)
    assertValue(initial, witness)
    assertValue(15.toDouble, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndAddRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    val witness: Double = handle.getAndAddRelease(desired)
    assertValue(initial, witness)
    assertValue(15.toDouble, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseOr_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseOr(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseOrAcquire(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseOrRelease(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseAnd(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseAndAcquire(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseAndRelease(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseXor_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseXor(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseXorAcquire(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleDoubleStaticFixture.handle)
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: Double = handle.getAndBitwiseXorRelease(desired)
    })
    assertValue(initial, VarHandleDoubleStaticFixture.value)
  }

}
class VarHandleReferenceStaticFixture
object VarHandleReferenceStaticFixture {
  @static var value: AnyRef = null
  val handle: VarHandle = MethodHandles.lookup()
    .findStaticVarHandle(classOf[VarHandleReferenceStaticFixture], "value", classOf[AnyRef])
}

class VarHandleReferenceStaticMatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: AnyRef, actual: AnyRef): Unit =
    assertSame(expected, actual)

  @Test def get_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val read: AnyRef = handle.get()
    assertValue(initial, read)
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val read: AnyRef = handle.getOpaque()
    assertValue(initial, read)
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val read: AnyRef = handle.getAcquire()
    assertValue(initial, read)
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val read: AnyRef = handle.getVolatile()
    assertValue(initial, read)
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def set_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    handle.set(desired)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def setOpaque_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    handle.setOpaque(desired)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def setRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    handle.setRelease(desired)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def setVolatile_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    handle.setVolatile(desired)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def compareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertFalse(handle.compareAndSet(desired, initial))
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    assertTrue(handle.compareAndSet(initial, desired))
    assertValue(desired, VarHandleReferenceStaticFixture.value)
    assertFalse(handle.compareAndSet(initial, initial))
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def weakCompareAndSetPlain_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetPlain(desired, initial))
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetPlain(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
    assertFalse(handle.weakCompareAndSetPlain(initial, initial))
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def weakCompareAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSet(desired, initial))
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSet(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
    assertFalse(handle.weakCompareAndSet(initial, initial))
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def weakCompareAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetAcquire(desired, initial))
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetAcquire(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
    assertFalse(handle.weakCompareAndSetAcquire(initial, initial))
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def weakCompareAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertFalse(handle.weakCompareAndSetRelease(desired, initial))
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = handle.weakCompareAndSetRelease(initial, desired)
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
    assertFalse(handle.weakCompareAndSetRelease(initial, initial))
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def compareAndExchange_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    val succeeded: AnyRef = handle.compareAndExchange(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def compareAndExchangeAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    val succeeded: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def compareAndExchangeRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertValue(initial, failed)
    assertValue(initial, VarHandleReferenceStaticFixture.value)
    val succeeded: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertValue(initial, succeeded)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndSet_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val witness: AnyRef = handle.getAndSet(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndSetAcquire_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val witness: AnyRef = handle.getAndSetAcquire(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndSetRelease_supported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    val witness: AnyRef = handle.getAndSetRelease(desired)
    assertValue(initial, witness)
    assertValue(desired, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndAdd_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndAdd(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndAddAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndAddAcquire(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndAddRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndAddRelease(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseOr_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseOr(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseOrAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseOrRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseOrRelease(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseAnd_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseAnd(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseAndAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseAndRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseAndRelease(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseXor_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseXor(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseXorAcquire_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

  @Test def getAndBitwiseXorRelease_unsupported(): Unit = {
    val handle = invocationHandle(VarHandleReferenceStaticFixture.handle)
    val initial: AnyRef = new String("same")
    val desired: AnyRef = new String("different")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: AnyRef = handle.getAndBitwiseXorRelease(desired)
    })
    assertValue(initial, VarHandleReferenceStaticFixture.value)
  }

}
