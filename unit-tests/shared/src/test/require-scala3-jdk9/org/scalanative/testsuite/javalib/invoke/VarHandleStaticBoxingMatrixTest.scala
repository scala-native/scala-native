package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleBoxingMatrixTest.scala.gyb; edit the template.
// format: off
import java.lang.invoke.{VarHandle, WrongMethodTypeException}
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBooleanStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndAdd(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndAddAcquire(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndAddRelease(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.get()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getOpaque()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAcquire()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getVolatile()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: java.lang.Boolean = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: java.lang.Boolean = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: java.lang.Boolean = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: java.lang.Boolean = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: java.lang.Boolean = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: java.lang.Boolean = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Boolean = handle.getAndAdd(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Boolean = handle.getAndAddAcquire(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Boolean = handle.getAndAddRelease(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.lang.Boolean = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndAdd(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndAddRelease(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.get()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getOpaque()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAcquire()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getVolatile()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile()
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleBooleanStaticFixture.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(VarHandleBooleanStaticFixture.value))
  }
}
class VarHandleByteStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.get()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getOpaque()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAcquire()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getVolatile()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.lang.Byte = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.lang.Byte = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.lang.Byte = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.lang.Byte = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.lang.Byte = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.lang.Byte = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Byte = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.get()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getOpaque()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAcquire()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getVolatile()
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile()
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleByteStaticFixture.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(VarHandleByteStaticFixture.value))
  }
}
class VarHandleShortStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.get()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getOpaque()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAcquire()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getVolatile()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.lang.Short = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.lang.Short = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.lang.Short = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.lang.Short = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.lang.Short = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.lang.Short = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Short = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.get()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getOpaque()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAcquire()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getVolatile()
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile()
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleShortStaticFixture.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(VarHandleShortStaticFixture.value))
  }
}
class VarHandleCharStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.get()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getOpaque()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAcquire()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getVolatile()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: java.lang.Character = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: java.lang.Character = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: java.lang.Character = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: java.lang.Character = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: java.lang.Character = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: java.lang.Character = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.lang.Character = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.get()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getOpaque()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAcquire()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getVolatile()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile()
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleCharStaticFixture.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(VarHandleCharStaticFixture.value))
  }
}
class VarHandleIntStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.get()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getOpaque()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAcquire()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getVolatile()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.lang.Integer = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.lang.Integer = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.lang.Integer = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Integer = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.get()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getOpaque()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAcquire()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getVolatile()
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.get()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getOpaque()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAcquire()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getVolatile()
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleIntStaticFixture.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(VarHandleIntStaticFixture.value))
  }
}
class VarHandleLongStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.get()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getOpaque()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAcquire()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getVolatile()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.lang.Long = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.lang.Long = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.lang.Long = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.lang.Long = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.lang.Long = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.lang.Long = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Long = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.get()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getOpaque()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAcquire()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getVolatile()
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile()
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleLongStaticFixture.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(VarHandleLongStaticFixture.value))
  }
}
class VarHandleFloatStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: AnyRef = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.get()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getOpaque()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getAcquire()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getVolatile()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.lang.Float = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.lang.Float = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.lang.Float = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.lang.Float = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.lang.Float = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.lang.Float = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Float = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.get()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getOpaque()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getAcquire()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getVolatile()
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile()
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleFloatStaticFixture.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(VarHandleFloatStaticFixture.value))
  }
}
class VarHandleDoubleStaticBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.get()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getOpaque()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getAcquire()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getVolatile()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: AnyRef = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: AnyRef = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: AnyRef = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.get()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getOpaque()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getAcquire()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getVolatile()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.lang.Double = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.lang.Double = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.lang.Double = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.lang.Double = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.lang.Double = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.lang.Double = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Double = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.get()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getOpaque()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getAcquire()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getVolatile()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.io.Serializable = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.get()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getOpaque()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getAcquire()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getVolatile()
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSet(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getAndSetRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchange(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchange(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAdd(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddAcquire(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    val result: java.lang.Number = handle.getAndAddRelease(desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOr(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAnd(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXor(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile()
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val handle: VarHandle = VarHandleDoubleStaticFixture.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(VarHandleDoubleStaticFixture.value))
  }
}
