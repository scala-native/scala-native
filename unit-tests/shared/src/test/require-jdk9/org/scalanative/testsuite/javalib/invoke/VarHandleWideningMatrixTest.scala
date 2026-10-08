package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleWideningMatrixTest.scala.gyb; edit the template.
// format: off
import java.lang.invoke.{VarHandle, WrongMethodTypeException}
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBooleanInstanceWideningMatrixTest {
  @Test def getAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
}
class VarHandleByteInstanceWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.get(box)
    assertEquals(initial.toShort, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getOpaque(box)
    assertEquals(initial.toShort, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAcquire(box)
    assertEquals(initial.toShort, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getVolatile(box)
    assertEquals(initial.toShort, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndSet(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Short = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toShort, failed)
    assertEquals(initial, box.value)
    val result: Short = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Short = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toShort, failed)
    assertEquals(initial, box.value)
    val result: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Short = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toShort, failed)
    assertEquals(initial, box.value)
    val result: Short = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndAdd(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddAcquireAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddReleaseAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsShort(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Short = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.get(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getOpaque(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAcquire(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getVolatile(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndSet(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndAdd(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddAcquireAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddReleaseAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsInt(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.get(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getOpaque(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAcquire(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getVolatile(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndSet(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndAdd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.get(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getOpaque(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAcquire(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getVolatile(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndSet(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.get(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getOpaque(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAcquire(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getVolatile(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndSet(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toByte, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toByte, box.value)
  }
}
class VarHandleShortInstanceWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.get(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getOpaque(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAcquire(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getVolatile(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndSet(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndAdd(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddAcquireAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddReleaseAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsInt(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.get(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getOpaque(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAcquire(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getVolatile(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndSet(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndAdd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.get(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getOpaque(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAcquire(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getVolatile(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndSet(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.get(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getOpaque(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAcquire(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getVolatile(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndSet(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toShort, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toShort, box.value)
  }
}
class VarHandleCharInstanceWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.get(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getOpaque(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAcquire(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getVolatile(box)
    assertEquals(initial.toInt, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndSet(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Int = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, box.value)
    val result: Int = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndAdd(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddAcquireAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddReleaseAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsInt(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Int = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.get(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getOpaque(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAcquire(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getVolatile(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndSet(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndAdd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.get(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getOpaque(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAcquire(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getVolatile(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndSet(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.get(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getOpaque(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAcquire(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getVolatile(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndSet(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toChar, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toChar, box.value)
  }
}
class VarHandleIntInstanceWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.get(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getOpaque(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAcquire(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getVolatile(box)
    assertEquals(initial.toLong, result)
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndSet(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndSetAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndSetRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchange(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Long = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, box.value)
    val result: Long = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndAdd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndAddAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndAddRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOr(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAnd(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXor(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Long = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.get(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getOpaque(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAcquire(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getVolatile(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndSet(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.get(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getOpaque(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAcquire(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getVolatile(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndSet(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toInt, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toInt, box.value)
  }
}
class VarHandleLongInstanceWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile(box)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(box, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(initial, box.value)
  }
  @Test def getAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.get(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getOpaque(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAcquire(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getVolatile(box)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndSet(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: Float = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, box.value)
    val result: Float = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Float = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.get(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getOpaque(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAcquire(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getVolatile(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, box.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndSet(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value)
    val result: Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, box.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOr(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAnd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXor(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toLong, box.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: Double = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toLong, box.value)
  }
}
class VarHandleFloatInstanceWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getOpaqueAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAcquireAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getVolatileAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getOpaqueAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAcquireAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getVolatileAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getOpaqueAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAcquireAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getVolatileAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getOpaqueAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAcquireAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getVolatileAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.get(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getOpaqueAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAcquireAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getVolatileAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.get(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getOpaque(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAcquireAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getAcquire(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getVolatileAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getVolatile(box)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getAndSet(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getAndSetAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getAndSetRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val failed: Double = handle.compareAndExchange(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value, 0.0f)
    val result: Double = handle.compareAndExchange(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val failed: Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value, 0.0f)
    val result: Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val failed: Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, box.value, 0.0f)
    val result: Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndAddAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getAndAdd(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits((initial + desired).toFloat), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getAndAddAcquire(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits((initial + desired).toFloat), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    val result: Double = handle.getAndAddRelease(box, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits((initial + desired).toFloat), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(box.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(box.value))
  }
}
class VarHandleDoubleInstanceWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getOpaqueAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAcquireAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getVolatileAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getOpaqueAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAcquireAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getVolatileAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getOpaqueAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAcquireAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getVolatileAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getOpaqueAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAcquireAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getVolatileAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.get(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getOpaqueAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAcquireAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getVolatileAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.get(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAcquireAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getVolatileAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(box.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(box.value))
  }
}
