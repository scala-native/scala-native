package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandlePrimitiveLookupTest.scala.gyb; edit the template.
// format: off
import java.lang.invoke.{MethodHandles, VarHandle}
import org.junit.Assert._
import org.junit.Test

class VarHandleBooleanPrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Boolean], java.lang.Boolean.TYPE)
    assertNotSame(classOf[Boolean], classOf[java.lang.Boolean])
  }

  private class Owner {
    var value: Boolean = true
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Boolean])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Boolean.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Boolean = handle.get(owner)
    assertEquals(true, before)
    assertTrue(handle.compareAndSet(owner, true, false))
    assertEquals(false, owner.value)
    handle.set(owner, true)
    assertEquals(true, owner.value)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Boolean = java.lang.Boolean.valueOf(true)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Boolean])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Boolean.valueOf(false)
    val before: java.lang.Boolean = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Boolean(true)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Boolean = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
class VarHandleBytePrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Byte], java.lang.Byte.TYPE)
    assertNotSame(classOf[Byte], classOf[java.lang.Byte])
  }

  private class Owner {
    var value: Byte = 37.toByte
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Byte])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Byte.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Byte = handle.get(owner)
    assertEquals(37.toByte, before)
    assertTrue(handle.compareAndSet(owner, 37.toByte, 38.toByte))
    assertEquals(38.toByte, owner.value)
    handle.set(owner, 37.toByte)
    assertEquals(37.toByte, owner.value)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Byte = java.lang.Byte.valueOf(37.toByte)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Byte])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Byte.valueOf(38.toByte)
    val before: java.lang.Byte = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Byte(37.toByte)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Byte = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
class VarHandleShortPrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Short], java.lang.Short.TYPE)
    assertNotSame(classOf[Short], classOf[java.lang.Short])
  }

  private class Owner {
    var value: Short = 37.toShort
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Short])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Short.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Short = handle.get(owner)
    assertEquals(37.toShort, before)
    assertTrue(handle.compareAndSet(owner, 37.toShort, 38.toShort))
    assertEquals(38.toShort, owner.value)
    handle.set(owner, 37.toShort)
    assertEquals(37.toShort, owner.value)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Short = java.lang.Short.valueOf(37.toShort)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Short])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Short.valueOf(38.toShort)
    val before: java.lang.Short = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Short(37.toShort)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Short = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
class VarHandleCharPrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Char], java.lang.Character.TYPE)
    assertNotSame(classOf[Char], classOf[java.lang.Character])
  }

  private class Owner {
    var value: Char = 37.toChar
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Char])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Character.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Char = handle.get(owner)
    assertEquals(37.toChar, before)
    assertTrue(handle.compareAndSet(owner, 37.toChar, 38.toChar))
    assertEquals(38.toChar, owner.value)
    handle.set(owner, 37.toChar)
    assertEquals(37.toChar, owner.value)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Character = java.lang.Character.valueOf(37.toChar)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Character])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Character.valueOf(38.toChar)
    val before: java.lang.Character = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Character(37.toChar)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Character = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
class VarHandleIntPrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Int], java.lang.Integer.TYPE)
    assertNotSame(classOf[Int], classOf[java.lang.Integer])
  }

  private class Owner {
    var value: Int = 37
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Int])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Integer.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Int = handle.get(owner)
    assertEquals(37, before)
    assertTrue(handle.compareAndSet(owner, 37, 38))
    assertEquals(38, owner.value)
    handle.set(owner, 37)
    assertEquals(37, owner.value)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Integer = java.lang.Integer.valueOf(37)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Integer])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Integer.valueOf(38)
    val before: java.lang.Integer = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Integer(37)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Integer = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
class VarHandleLongPrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Long], java.lang.Long.TYPE)
    assertNotSame(classOf[Long], classOf[java.lang.Long])
  }

  private class Owner {
    var value: Long = 37L
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Long])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Long.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Long = handle.get(owner)
    assertEquals(37L, before)
    assertTrue(handle.compareAndSet(owner, 37L, 38L))
    assertEquals(38L, owner.value)
    handle.set(owner, 37L)
    assertEquals(37L, owner.value)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Long = java.lang.Long.valueOf(37L)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Long])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Long.valueOf(38L)
    val before: java.lang.Long = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Long(37L)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Long = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
class VarHandleFloatPrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Float], java.lang.Float.TYPE)
    assertNotSame(classOf[Float], classOf[java.lang.Float])
  }

  private class Owner {
    var value: Float = 37.0f
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Float])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Float.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Float = handle.get(owner)
    assertEquals(37.0f, before, 0.0f)
    assertTrue(handle.compareAndSet(owner, 37.0f, 38.0f))
    assertEquals(38.0f, owner.value, 0.0f)
    handle.set(owner, 37.0f)
    assertEquals(37.0f, owner.value, 0.0f)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Float = java.lang.Float.valueOf(37.0f)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Float])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Float.valueOf(38.0f)
    val before: java.lang.Float = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Float(37.0f)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Float = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
class VarHandleDoublePrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[Double], java.lang.Double.TYPE)
    assertNotSame(classOf[Double], classOf[java.lang.Double])
  }

  private class Owner {
    var value: Double = 37.0d
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[Double])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", java.lang.Double.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: Double = handle.get(owner)
    assertEquals(37.0d, before, 0.0d)
    assertTrue(handle.compareAndSet(owner, 37.0d, 38.0d))
    assertEquals(38.0d, owner.value, 0.0d)
    handle.set(owner, 37.0d)
    assertEquals(37.0d, owner.value, 0.0d)
  }

  @Test def classOfPrimitiveSupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.classOfHandle)
  }

  @Test def wrapperTYPESupportsReadWriteAndCAS(): Unit = {
    val owner = new Owner
    verify(owner, owner.typeHandle)
  }

  private class BoxedOwner {
    var value: java.lang.Double = java.lang.Double.valueOf(37.0d)
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[java.lang.Double])
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = java.lang.Double.valueOf(38.0d)
    val before: java.lang.Double = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new java.lang.Double(37.0d)
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: java.lang.Double = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
