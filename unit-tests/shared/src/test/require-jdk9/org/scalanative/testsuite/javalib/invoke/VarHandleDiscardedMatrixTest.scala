package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleDiscardedMatrixTest.scala.gyb; edit the template.
// format: off
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBooleanInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAdd(box, desired)
      ()
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddAcquire(box, desired)
      ()
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddRelease(box, desired)
      ()
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseOr(box, desired)
    assertEquals((initial | desired), box.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    invoke()
    assertEquals((initial | desired), box.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals((initial | desired), box.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    invoke()
    assertEquals((initial | desired), box.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseOrRelease(box, desired)
    assertEquals((initial | desired), box.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    invoke()
    assertEquals((initial | desired), box.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseAnd(box, desired)
    assertEquals((initial & desired), box.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    invoke()
    assertEquals((initial & desired), box.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals((initial & desired), box.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    invoke()
    assertEquals((initial & desired), box.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseAndRelease(box, desired)
    assertEquals((initial & desired), box.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    invoke()
    assertEquals((initial & desired), box.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseXor(box, desired)
    assertEquals((initial ^ desired), box.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    invoke()
    assertEquals((initial ^ desired), box.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals((initial ^ desired), box.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    invoke()
    assertEquals((initial ^ desired), box.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    handle.getAndBitwiseXorRelease(box, desired)
    assertEquals((initial ^ desired), box.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle = box.handle
    val initial: Boolean = false
    val desired: Boolean = true
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    invoke()
    assertEquals((initial ^ desired), box.value)
  }
}
class VarHandleByteInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndAdd(box, desired)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    invoke()
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndAddAcquire(box, desired)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    invoke()
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndAddRelease(box, desired)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    invoke()
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseOr(box, desired)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    invoke()
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    invoke()
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseOrRelease(box, desired)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    invoke()
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseAnd(box, desired)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    invoke()
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    invoke()
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseAndRelease(box, desired)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    invoke()
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseXor(box, desired)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    invoke()
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    invoke()
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    handle.getAndBitwiseXorRelease(box, desired)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle = box.handle
    val initial: Byte = 12.toByte
    val desired: Byte = 3.toByte
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    invoke()
    assertEquals((initial ^ desired).toByte, box.value)
  }
}
class VarHandleShortInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndAdd(box, desired)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    invoke()
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndAddAcquire(box, desired)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    invoke()
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndAddRelease(box, desired)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    invoke()
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseOr(box, desired)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    invoke()
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    invoke()
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseOrRelease(box, desired)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    invoke()
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseAnd(box, desired)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    invoke()
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    invoke()
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseAndRelease(box, desired)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    invoke()
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseXor(box, desired)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    invoke()
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    invoke()
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    handle.getAndBitwiseXorRelease(box, desired)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle = box.handle
    val initial: Short = 12.toShort
    val desired: Short = 3.toShort
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    invoke()
    assertEquals((initial ^ desired).toShort, box.value)
  }
}
class VarHandleCharInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndAdd(box, desired)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    invoke()
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndAddAcquire(box, desired)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    invoke()
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndAddRelease(box, desired)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    invoke()
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseOr(box, desired)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    invoke()
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    invoke()
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseOrRelease(box, desired)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    invoke()
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseAnd(box, desired)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    invoke()
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    invoke()
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseAndRelease(box, desired)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    invoke()
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseXor(box, desired)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    invoke()
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    invoke()
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    handle.getAndBitwiseXorRelease(box, desired)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle = box.handle
    val initial: Char = 12.toChar
    val desired: Char = 3.toChar
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    invoke()
    assertEquals((initial ^ desired).toChar, box.value)
  }
}
class VarHandleIntInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndAdd(box, desired)
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    invoke()
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndAddAcquire(box, desired)
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    invoke()
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndAddRelease(box, desired)
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    invoke()
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseOr(box, desired)
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    invoke()
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    invoke()
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseOrRelease(box, desired)
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    invoke()
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseAnd(box, desired)
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    invoke()
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    invoke()
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseAndRelease(box, desired)
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    invoke()
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseXor(box, desired)
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    invoke()
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    invoke()
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    handle.getAndBitwiseXorRelease(box, desired)
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle = box.handle
    val initial: Int = 12
    val desired: Int = 3
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    invoke()
    assertEquals((initial ^ desired).toInt, box.value)
  }
}
class VarHandleLongInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndAdd(box, desired)
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    invoke()
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndAddAcquire(box, desired)
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    invoke()
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndAddRelease(box, desired)
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    invoke()
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseOr(box, desired)
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    invoke()
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    invoke()
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseOrRelease(box, desired)
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    invoke()
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseAnd(box, desired)
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    invoke()
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    invoke()
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseAndRelease(box, desired)
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    invoke()
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseXor(box, desired)
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    invoke()
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    invoke()
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    handle.getAndBitwiseXorRelease(box, desired)
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle = box.handle
    val initial: Long = 12L
    val desired: Long = 3L
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    invoke()
    assertEquals((initial ^ desired).toLong, box.value)
  }
}
class VarHandleFloatInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value, 0.0f)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.getAndAdd(box, desired)
    assertEquals((initial + desired).toFloat, box.value, 0.0f)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    invoke()
    assertEquals((initial + desired).toFloat, box.value, 0.0f)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.getAndAddAcquire(box, desired)
    assertEquals((initial + desired).toFloat, box.value, 0.0f)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    invoke()
    assertEquals((initial + desired).toFloat, box.value, 0.0f)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    handle.getAndAddRelease(box, desired)
    assertEquals((initial + desired).toFloat, box.value, 0.0f)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    invoke()
    assertEquals((initial + desired).toFloat, box.value, 0.0f)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOr(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrAcquire(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrRelease(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAnd(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndAcquire(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndRelease(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXor(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorAcquire(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorRelease(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0f)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle = box.handle
    val initial: Float = 12.0f
    val desired: Float = 3.0f
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0f)
  }
}
class VarHandleDoubleInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.getAndSet(box, desired)
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertEquals(desired, box.value, 0.0d)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.getAndAdd(box, desired)
    assertEquals((initial + desired).toDouble, box.value, 0.0d)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    invoke()
    assertEquals((initial + desired).toDouble, box.value, 0.0d)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.getAndAddAcquire(box, desired)
    assertEquals((initial + desired).toDouble, box.value, 0.0d)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    invoke()
    assertEquals((initial + desired).toDouble, box.value, 0.0d)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    handle.getAndAddRelease(box, desired)
    assertEquals((initial + desired).toDouble, box.value, 0.0d)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    invoke()
    assertEquals((initial + desired).toDouble, box.value, 0.0d)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOr(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrAcquire(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrRelease(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAnd(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndAcquire(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndRelease(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXor(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorAcquire(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorRelease(box, desired)
      ()
    })
    assertEquals(initial, box.value, 0.0d)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle = box.handle
    val initial: Double = 12.0d
    val desired: Double = 3.0d
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertEquals(initial, box.value, 0.0d)
  }
}
class VarHandleReferenceInstanceDiscardedMatrixTest {
  @Test def compareAndExchange_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    handle.compareAndExchange(box, initial, desired)
    assertSame(desired, box.value)
  }
  @Test def compareAndExchange_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.compareAndExchange(box, initial, desired)
    invoke()
    assertSame(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    handle.compareAndExchangeAcquire(box, initial, desired)
    assertSame(desired, box.value)
  }
  @Test def compareAndExchangeAcquire_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeAcquire(box, initial, desired)
    invoke()
    assertSame(desired, box.value)
  }
  @Test def compareAndExchangeRelease_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    handle.compareAndExchangeRelease(box, initial, desired)
    assertSame(desired, box.value)
  }
  @Test def compareAndExchangeRelease_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.compareAndExchangeRelease(box, initial, desired)
    invoke()
    assertSame(desired, box.value)
  }
  @Test def getAndSet_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    handle.getAndSet(box, desired)
    assertSame(desired, box.value)
  }
  @Test def getAndSet_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndSet(box, desired)
    invoke()
    assertSame(desired, box.value)
  }
  @Test def getAndSetAcquire_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    handle.getAndSetAcquire(box, desired)
    assertSame(desired, box.value)
  }
  @Test def getAndSetAcquire_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndSetAcquire(box, desired)
    invoke()
    assertSame(desired, box.value)
  }
  @Test def getAndSetRelease_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    handle.getAndSetRelease(box, desired)
    assertSame(desired, box.value)
  }
  @Test def getAndSetRelease_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndSetRelease(box, desired)
    invoke()
    assertSame(desired, box.value)
  }
  @Test def getAndAdd_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAdd(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndAdd_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndAdd(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndAddAcquire_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddAcquire(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndAddAcquire_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndAddAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndAddRelease_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndAddRelease(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndAddRelease_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndAddRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseOr_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOr(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseOr_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOr(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseOrAcquire_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrAcquire(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseOrAcquire_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseOrRelease_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseOrRelease(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseOrRelease_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseOrRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseAnd_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAnd(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseAnd_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAnd(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseAndAcquire_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndAcquire(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseAndAcquire_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseAndRelease_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseAndRelease(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseAndRelease_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseAndRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseXor_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXor(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseXor_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXor(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseXorAcquire_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorAcquire(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseXorAcquire_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorAcquire(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseXorRelease_statement(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    assertThrows(classOf[UnsupportedOperationException], {
      handle.getAndBitwiseXorRelease(box, desired)
      ()
    })
    assertSame(initial, box.value)
  }
  @Test def getAndBitwiseXorRelease_unitBody(): Unit = {
    val box = new VarHandleReferenceInstanceFixture
    val handle = box.handle
    val initial: AnyRef = new String("initial")
    val desired: AnyRef = new String("desired")
    box.value = initial
    def invoke(): Unit = handle.getAndBitwiseXorRelease(box, desired)
    assertThrows(classOf[UnsupportedOperationException], invoke())
    assertSame(initial, box.value)
  }
}
