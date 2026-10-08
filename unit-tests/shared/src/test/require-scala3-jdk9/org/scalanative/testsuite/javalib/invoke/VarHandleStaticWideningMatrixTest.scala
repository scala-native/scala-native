package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleWideningMatrixTest.scala.gyb; edit the template.
// format: off
import java.lang.invoke.{VarHandle, WrongMethodTypeException}
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBooleanStaticWideningMatrixTest {
  @Test def getAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.get()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getOpaque()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAcquire()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getVolatile()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.get()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getOpaque()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAcquire()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getVolatile()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.get()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getOpaque()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAcquire()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getVolatile()
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Double = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleBooleanStaticFixture.value)
  }
}
class VarHandleByteStaticWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.get()
    assertEquals(initial.toShort, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getOpaque()
    assertEquals(initial.toShort, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAcquire()
    assertEquals(initial.toShort, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getVolatile()
    assertEquals(initial.toShort, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndSet(desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndSetAcquire(desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndSetRelease(desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Short = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toShort, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Short = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Short = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toShort, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Short = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Short = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toShort, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Short = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toShort, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndAdd(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndAddAcquire(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndAddRelease(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Short = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toShort, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile()
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.get()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getOpaque()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAcquire()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getVolatile()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndSet(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndSetAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndSetRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Int = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Int = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Int = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Int = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Int = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Int = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndAdd(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndAddAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndAddRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.get()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getOpaque()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAcquire()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getVolatile()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndSet(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndSetAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndSetRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Long = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Long = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Long = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Long = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndAdd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndAddAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndAddRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.get()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getOpaque()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAcquire()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getVolatile()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndSet(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Float = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Float = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Float = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Float = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndAdd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.get()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getOpaque()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAcquire()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getVolatile()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndSet(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Double = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Double = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Double = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleByteStaticFixture.value)
    val result: Double = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndAdd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = (-12).toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toByte, VarHandleByteStaticFixture.value)
  }
}
class VarHandleShortStaticWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile()
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.get()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getOpaque()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAcquire()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getVolatile()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndSet(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndSetAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndSetRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Int = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Int = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Int = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Int = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Int = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Int = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndAdd(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndAddAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndAddRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.get()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getOpaque()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAcquire()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getVolatile()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndSet(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndSetAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndSetRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Long = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Long = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Long = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Long = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndAdd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndAddAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndAddRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.get()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getOpaque()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAcquire()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getVolatile()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndSet(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Float = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Float = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Float = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Float = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndAdd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.get()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getOpaque()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAcquire()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getVolatile()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndSet(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Double = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Double = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Double = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleShortStaticFixture.value)
    val result: Double = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndAdd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = (-1200).toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toShort, VarHandleShortStaticFixture.value)
  }
}
class VarHandleCharStaticWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile()
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.get()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getOpaque()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAcquire()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getVolatile()
    assertEquals(initial.toInt, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndSet(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndSetAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndSetRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Int = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Int = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Int = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Int = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Int = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toInt, failed)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Int = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toInt, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndAdd(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndAddAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndAddRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Int = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toInt, result)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.get()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getOpaque()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAcquire()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getVolatile()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndSet(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndSetAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndSetRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Long = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Long = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Long = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Long = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndAdd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndAddAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndAddRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.get()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getOpaque()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAcquire()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getVolatile()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndSet(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Float = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Float = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Float = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Float = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndAdd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.get()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getOpaque()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAcquire()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getVolatile()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndSet(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Double = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Double = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Double = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleCharStaticFixture.value)
    val result: Double = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndAdd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 65530.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toChar, VarHandleCharStaticFixture.value)
  }
}
class VarHandleIntStaticWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile()
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.get()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getOpaqueAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getOpaque()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAcquire()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getVolatileAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getVolatile()
    assertEquals(initial.toLong, result)
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndSet(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndSetAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndSetRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Long = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Long = handle.compareAndExchange(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Long = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Long = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toLong, failed)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Long = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(initial.toLong, result)
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndAdd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndAddAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndAddRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOr(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseOrRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAnd(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseAndRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXor(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Long = handle.getAndBitwiseXorRelease(desired)
    assertEquals(initial.toLong, result)
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.get()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getOpaque()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAcquire()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getVolatile()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndSet(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Float = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Float = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Float = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Float = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndAdd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.get()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getOpaque()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAcquire()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getVolatile()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndSet(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Double = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Double = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Double = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleIntStaticFixture.value)
    val result: Double = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndAdd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 16777217
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toInt, VarHandleIntStaticFixture.value)
  }
}
class VarHandleLongStaticWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getOpaqueAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getVolatileAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getOpaqueAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getVolatileAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getOpaqueAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getVolatileAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getOpaqueAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getVolatileAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile()
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.get()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getOpaque()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAcquire()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getVolatileAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getVolatile()
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndSet(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: Float = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleLongStaticFixture.value)
    val result: Float = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleLongStaticFixture.value)
    val result: Float = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: Float = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toFloat, failed, 0.0f)
    assertEquals(initial, VarHandleLongStaticFixture.value)
    val result: Float = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndAdd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Float = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Float.floatToRawIntBits(initial.toFloat), java.lang.Float.floatToRawIntBits(result))
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.get()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getOpaque()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAcquire()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getVolatileAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getVolatile()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(initial, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndSet(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: Double = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleLongStaticFixture.value)
    val result: Double = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleLongStaticFixture.value)
    val result: Double = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: Double = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleLongStaticFixture.value)
    val result: Double = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(desired, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndAdd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial + desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOr(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseOrRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial | desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAnd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseAndRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial & desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXor(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 9007199254740993L
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: Double = handle.getAndBitwiseXorRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals((initial ^ desired).toLong, VarHandleLongStaticFixture.value)
  }
}
class VarHandleFloatStaticWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.get()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getOpaque()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAcquire()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getVolatile()
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.get()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getOpaque()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getAcquire()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getVolatile()
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getAndSet(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getAndSetAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getAndSetRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val failed: Double = handle.compareAndExchange(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
    val result: Double = handle.compareAndExchange(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val failed: Double = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
    val result: Double = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val failed: Double = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(initial.toDouble, failed, 0.0d)
    assertEquals(initial, VarHandleFloatStaticFixture.value, 0.0f)
    val result: Double = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits(desired), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getAndAdd(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits((initial + desired).toFloat), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getAndAddAcquire(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits((initial + desired).toFloat), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    val result: Double = handle.getAndAddRelease(desired)
    assertEquals(java.lang.Double.doubleToRawLongBits(initial.toDouble), java.lang.Double.doubleToRawLongBits(result))
    assertEquals(java.lang.Float.floatToRawIntBits((initial + desired).toFloat), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsDouble(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertEquals(Int.MinValue, java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
    assertThrows(classOf[UnsupportedOperationException], {
      val result: Double = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Float.floatToRawIntBits(initial), java.lang.Float.floatToRawIntBits(VarHandleFloatStaticFixture.value))
  }
}
class VarHandleDoubleStaticWideningMatrixTest {
  @Test def getAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.get()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getOpaque()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAcquire()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getVolatile()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsBoolean(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Boolean = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.get()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getOpaque()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAcquire()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getVolatile()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsByte(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Byte = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.get()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getOpaque()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAcquire()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getVolatile()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsShort(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Short = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.get()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getOpaque()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAcquire()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getVolatile()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsChar(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Char = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.get()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getOpaque()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAcquire()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getVolatile()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsInt(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Int = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.get()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getOpaque()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAcquire()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getVolatile()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsLong(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Long = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.get()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getOpaque()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAcquire()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getVolatile()
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsFloat(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertEquals(Long.MinValue, java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
    assertThrows(classOf[WrongMethodTypeException], {
      val result: Float = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.doubleToRawLongBits(initial), java.lang.Double.doubleToRawLongBits(VarHandleDoubleStaticFixture.value))
  }
}
