package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandleBoxingMatrixTest.scala.gyb; edit the template.
// format: off
import java.lang.invoke.{VarHandle, WrongMethodTypeException}
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class VarHandleBooleanInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndAdd(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndAddAcquire(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndAddRelease(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.get(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: java.lang.Boolean = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: java.lang.Boolean = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: java.lang.Boolean = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: java.lang.Boolean = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: java.lang.Boolean = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: java.lang.Boolean = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Boolean = handle.getAndAdd(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Boolean = handle.getAndAddAcquire(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Boolean = handle.getAndAddRelease(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.lang.Boolean = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Boolean.valueOf(initial), failed)
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf(desired), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndAdd(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial | desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial & desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Boolean], result.getClass)
    assertEquals(java.lang.Boolean.valueOf(initial), result)
    assertEquals(java.lang.Boolean.valueOf((initial ^ desired)), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.get(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getOpaque(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAcquire(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getVolatile(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile(box)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleBooleanInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Boolean = true
    val desired: Boolean = false
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Boolean.valueOf(initial), java.lang.Boolean.valueOf(box.value))
  }
}
class VarHandleByteInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.get(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.lang.Byte = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.lang.Byte = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.lang.Byte = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.lang.Byte = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.lang.Byte = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.lang.Byte = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Byte = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.get(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Byte.valueOf(initial), failed)
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf(desired), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial + desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial | desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial & desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Byte], result.getClass)
    assertEquals(java.lang.Byte.valueOf(initial), result)
    assertEquals(java.lang.Byte.valueOf((initial ^ desired).toByte), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile(box)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleByteInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Byte = 7.toByte
    val desired: Byte = 3.toByte
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Byte.valueOf(initial), java.lang.Byte.valueOf(box.value))
  }
}
class VarHandleShortInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.get(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.lang.Short = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.lang.Short = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.lang.Short = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.lang.Short = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.lang.Short = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.lang.Short = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Short = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.get(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Short.valueOf(initial), failed)
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf(desired), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial + desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial | desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial & desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Short], result.getClass)
    assertEquals(java.lang.Short.valueOf(initial), result)
    assertEquals(java.lang.Short.valueOf((initial ^ desired).toShort), java.lang.Short.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile(box)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleShortInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Short = 7.toShort
    val desired: Short = 3.toShort
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Short.valueOf(initial), java.lang.Short.valueOf(box.value))
  }
}
class VarHandleCharInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.get(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: java.lang.Character = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: java.lang.Character = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: java.lang.Character = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: java.lang.Character = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: java.lang.Character = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: java.lang.Character = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.lang.Character = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Character.valueOf(initial), failed)
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf(desired), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial + desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial | desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial & desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Character], result.getClass)
    assertEquals(java.lang.Character.valueOf(initial), result)
    assertEquals(java.lang.Character.valueOf((initial ^ desired).toChar), java.lang.Character.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.get(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getOpaque(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAcquire(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getVolatile(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile(box)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleCharInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Char = 7.toChar
    val desired: Char = 3.toChar
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Character.valueOf(initial), java.lang.Character.valueOf(box.value))
  }
}
class VarHandleIntInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.get(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.lang.Integer = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.lang.Integer = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.lang.Integer = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Integer = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.get(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Integer.valueOf(initial), failed)
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf(desired), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial + desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial | desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial & desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Integer], result.getClass)
    assertEquals(java.lang.Integer.valueOf(initial), result)
    assertEquals(java.lang.Integer.valueOf((initial ^ desired).toInt), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.get(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getOpaque(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAcquire(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getVolatile(box)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleIntInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Int = 7.toInt
    val desired: Int = 3.toInt
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Long = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Integer.valueOf(initial), java.lang.Integer.valueOf(box.value))
  }
}
class VarHandleLongInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.get(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.lang.Long = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.lang.Long = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.lang.Long = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.lang.Long = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.lang.Long = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.lang.Long = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Long = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.get(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Long.valueOf(initial), failed)
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf(desired), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial + desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOr(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseOrRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial | desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAnd(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseAndRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial & desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXor(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorAcquire(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndBitwiseXorRelease(box, desired)
    assertEquals(classOf[java.lang.Long], result.getClass)
    assertEquals(java.lang.Long.valueOf(initial), result)
    assertEquals(java.lang.Long.valueOf((initial ^ desired).toLong), java.lang.Long.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile(box)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleLongInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Long = 7.toLong
    val desired: Long = 3.toLong
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Long.valueOf(initial), java.lang.Long.valueOf(box.value))
  }
}
class VarHandleFloatInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.get(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.lang.Float = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.lang.Float = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.lang.Float = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.lang.Float = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.lang.Float = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.lang.Float = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Float = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Float = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.get(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Float.valueOf(initial), failed)
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf(desired), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Float], result.getClass)
    assertEquals(java.lang.Float.valueOf(initial), result)
    assertEquals(java.lang.Float.valueOf((initial + desired).toFloat), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile(box)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleFloatInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Float = java.lang.Float.intBitsToFloat(Int.MinValue)
    val desired: Float = 3.toFloat
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Float.valueOf(initial), java.lang.Float.valueOf(box.value))
  }
}
class VarHandleDoubleInstanceBoxingMatrixTest {
  @Test def getAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.get(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getOpaqueAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAcquireAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getVolatileAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: AnyRef = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: AnyRef = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: AnyRef = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsObject(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: AnyRef = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.get(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getOpaqueAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAcquireAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getVolatileAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.lang.Double = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.lang.Double = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.lang.Double = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.lang.Double = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.lang.Double = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.lang.Double = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Double = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Double = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.get(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getOpaqueAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAcquireAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getVolatileAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.io.Serializable = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.io.Serializable = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.io.Serializable = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsSerializable(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.io.Serializable = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.get(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getOpaqueAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getOpaque(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAcquireAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAcquire(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getVolatileAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getVolatile(box)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSet(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndSetRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchange(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchange(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeAcquire(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeAcquire(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val failed: java.lang.Number = handle.compareAndExchangeRelease(box, desired, desired)
    assertEquals(java.lang.Double.valueOf(initial), failed)
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
    val result: java.lang.Number = handle.compareAndExchangeRelease(box, initial, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf(desired), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAdd(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddAcquireAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddAcquire(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndAddReleaseAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    val result: java.lang.Number = handle.getAndAddRelease(box, desired)
    assertEquals(classOf[java.lang.Double], result.getClass)
    assertEquals(java.lang.Double.valueOf(initial), result)
    assertEquals(java.lang.Double.valueOf((initial + desired).toDouble), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOr(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrAcquireAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseOrReleaseAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseOrRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAnd(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndAcquireAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseAndReleaseAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseAndRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXor(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorAcquireAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndBitwiseXorReleaseAsNumber(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[UnsupportedOperationException], {
      val result: java.lang.Number = handle.getAndBitwiseXorRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.get(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getOpaqueAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAcquireAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getVolatileAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsString(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: String = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.get(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getOpaqueAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getOpaque(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAcquire(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getVolatileAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getVolatile(box)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSet(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetAcquire(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def getAndSetReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.getAndSetRelease(box, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchange(box, initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeAcquireAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeAcquire(box, initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
  @Test def compareAndExchangeReleaseAsWrongWrapper(): Unit = {
    val box = new VarHandleDoubleInstanceFixture
    val handle: VarHandle = box.handle
    val initial: Double = java.lang.Double.longBitsToDouble(Long.MinValue)
    val desired: Double = 3.toDouble
    handle.set(box, initial)
    assertThrows(classOf[WrongMethodTypeException], {
      val result: java.lang.Integer = handle.compareAndExchangeRelease(box, initial, desired)
    })
    assertEquals(java.lang.Double.valueOf(initial), java.lang.Double.valueOf(box.value))
  }
}
