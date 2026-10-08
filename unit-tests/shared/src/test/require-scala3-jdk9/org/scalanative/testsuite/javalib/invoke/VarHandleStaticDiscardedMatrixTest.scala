package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleDiscardedMatrixTest.scala.gyb; edit the template.
// format: off
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBooleanStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAdd(desired)
      ()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddAcquire(desired)
      ()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddRelease(desired)
      ()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseOr(desired)
    assertEquals((initial | desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    invoke()
    assertEquals((initial | desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseOrAcquire(desired)
    assertEquals((initial | desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    invoke()
    assertEquals((initial | desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseOrRelease(desired)
    assertEquals((initial | desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    invoke()
    assertEquals((initial | desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseAnd(desired)
    assertEquals((initial & desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    invoke()
    assertEquals((initial & desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseAndAcquire(desired)
    assertEquals((initial & desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    invoke()
    assertEquals((initial & desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseAndRelease(desired)
    assertEquals((initial & desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    invoke()
    assertEquals((initial & desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseXor(desired)
    assertEquals((initial ^ desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    invoke()
    assertEquals((initial ^ desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseXorAcquire(desired)
    assertEquals((initial ^ desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    invoke()
    assertEquals((initial ^ desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    handle.getAndBitwiseXorRelease(desired)
    assertEquals((initial ^ desired), VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = false
    val desired: Boolean = true
    VarHandleBooleanStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    invoke()
    assertEquals((initial ^ desired), VarHandleBooleanStaticFixture.value)
  }
}
class VarHandleByteStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndAdd(desired)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    invoke()
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndAddAcquire(desired)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    invoke()
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndAddRelease(desired)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    invoke()
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseOr(desired)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    invoke()
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseOrAcquire(desired)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    invoke()
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseOrRelease(desired)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    invoke()
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseAnd(desired)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    invoke()
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseAndAcquire(desired)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    invoke()
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseAndRelease(desired)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    invoke()
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseXor(desired)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    invoke()
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseXorAcquire(desired)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    invoke()
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    handle.getAndBitwiseXorRelease(desired)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleByteStaticFixture.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    VarHandleByteStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    invoke()
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
}
class VarHandleShortStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndAdd(desired)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    invoke()
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndAddAcquire(desired)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    invoke()
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndAddRelease(desired)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    invoke()
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseOr(desired)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    invoke()
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseOrAcquire(desired)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    invoke()
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseOrRelease(desired)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    invoke()
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseAnd(desired)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    invoke()
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseAndAcquire(desired)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    invoke()
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseAndRelease(desired)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    invoke()
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseXor(desired)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    invoke()
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseXorAcquire(desired)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    invoke()
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    handle.getAndBitwiseXorRelease(desired)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleShortStaticFixture.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    VarHandleShortStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    invoke()
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
}
class VarHandleCharStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndAdd(desired)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    invoke()
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndAddAcquire(desired)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    invoke()
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndAddRelease(desired)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    invoke()
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseOr(desired)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    invoke()
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseOrAcquire(desired)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    invoke()
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseOrRelease(desired)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    invoke()
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseAnd(desired)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    invoke()
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseAndAcquire(desired)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    invoke()
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseAndRelease(desired)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    invoke()
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseXor(desired)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    invoke()
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseXorAcquire(desired)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    invoke()
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    handle.getAndBitwiseXorRelease(desired)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleCharStaticFixture.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    VarHandleCharStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    invoke()
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
}
class VarHandleIntStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndAdd(desired)
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    invoke()
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndAddAcquire(desired)
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    invoke()
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndAddRelease(desired)
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    invoke()
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseOr(desired)
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    invoke()
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseOrAcquire(desired)
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    invoke()
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseOrRelease(desired)
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    invoke()
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseAnd(desired)
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    invoke()
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseAndAcquire(desired)
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    invoke()
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseAndRelease(desired)
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    invoke()
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseXor(desired)
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    invoke()
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseXorAcquire(desired)
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    invoke()
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    handle.getAndBitwiseXorRelease(desired)
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleIntStaticFixture.handle
    val initial: Int = 12
    val desired: Int = 3
    VarHandleIntStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    invoke()
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
}
class VarHandleLongStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndAdd(desired)
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    invoke()
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndAddAcquire(desired)
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    invoke()
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndAddRelease(desired)
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    invoke()
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseOr(desired)
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    invoke()
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseOrAcquire(desired)
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    invoke()
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseOrRelease(desired)
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    invoke()
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseAnd(desired)
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    invoke()
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseAndAcquire(desired)
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    invoke()
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseAndRelease(desired)
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    invoke()
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseXor(desired)
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    invoke()
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseXorAcquire(desired)
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    invoke()
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    handle.getAndBitwiseXorRelease(desired)
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleLongStaticFixture.handle
    val initial: Long = 12L
    val desired: Long = 3L
    VarHandleLongStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    invoke()
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
}
class VarHandleFloatStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.getAndAdd(desired)
    assertEquals((initial + desired).toFloat, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    invoke()
    assertEquals((initial + desired).toFloat, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.getAndAddAcquire(desired)
    assertEquals((initial + desired).toFloat, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    invoke()
    assertEquals((initial + desired).toFloat, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    handle.getAndAddRelease(desired)
    assertEquals((initial + desired).toFloat, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    invoke()
    assertEquals((initial + desired).toFloat, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOr(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrAcquire(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrRelease(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAnd(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndAcquire(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndRelease(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXor(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorAcquire(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorRelease(desired)
      ()
    })
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleFloatStaticFixture.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    VarHandleFloatStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
  }
}
class VarHandleDoubleStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.getAndSet(desired)
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertEquals(desired, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.getAndAdd(desired)
    assertEquals((initial + desired).toDouble, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    invoke()
    assertEquals((initial + desired).toDouble, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.getAndAddAcquire(desired)
    assertEquals((initial + desired).toDouble, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    invoke()
    assertEquals((initial + desired).toDouble, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    handle.getAndAddRelease(desired)
    assertEquals((initial + desired).toDouble, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    invoke()
    assertEquals((initial + desired).toDouble, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOr(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrAcquire(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrRelease(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAnd(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndAcquire(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndRelease(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXor(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorAcquire(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorRelease(desired)
      ()
    })
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleDoubleStaticFixture.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    VarHandleDoubleStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, VarHandleDoubleStaticFixture.value, 0.0d)
  }
}
class VarHandleReferenceStaticDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    handle.compareAndExchange(initial, desired)
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchange(initial, desired)
    invoke()
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    handle.compareAndExchangeAcquire(initial, desired)
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(initial, desired)
    invoke()
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    handle.compareAndExchangeRelease(initial, desired)
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(initial, desired)
    invoke()
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    handle.getAndSet(desired)
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSet(desired)
    invoke()
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    handle.getAndSetAcquire(desired)
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(desired)
    invoke()
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    handle.getAndSetRelease(desired)
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndSetRelease(desired)
    invoke()
    assertSame(desired, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAdd(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAdd(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddAcquire(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddRelease(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndAddRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOr(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrAcquire(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrRelease(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAnd(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndAcquire(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndRelease(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXor(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorAcquire(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorRelease(desired)
      ()
    })
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val handle = VarHandleReferenceStaticFixture.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    VarHandleReferenceStaticFixture.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, VarHandleReferenceStaticFixture.value)
  }
}
