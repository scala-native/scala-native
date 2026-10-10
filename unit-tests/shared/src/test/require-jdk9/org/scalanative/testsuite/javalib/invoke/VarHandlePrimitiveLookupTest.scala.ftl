<#include "VarHandleCommon.ftl">
<#assign initialValues = {"Boolean": "true", "Byte": "37.toByte", "Short": "37.toShort", "Char": "37.toChar",
                         "Int": "37", "Long": "37L", "Float": "37.0f", "Double": "37.0d"}>
<#assign desiredValues = {"Boolean": "false", "Byte": "38.toByte", "Short": "38.toShort", "Char": "38.toChar",
                         "Int": "38", "Long": "38L", "Float": "38.0f", "Double": "38.0d"}>
<#list primitives as T>
<#assign wrapper = "java.lang." + wrappers[T]>
<#assign initial = initialValues[T]>
<#assign desired = desiredValues[T]>
class VarHandle${T}PrimitiveLookupTest {
  @Test def classTokensAreDistinct(): Unit = {
    assertSame(classOf[${T}], ${wrapper}.TYPE)
    assertNotSame(classOf[${T}], classOf[${wrapper}])
  }

  private class Owner {
    var value: ${T} = ${initial}
    val classOfHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", classOf[${T}])
    val typeHandle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[Owner], "value", ${wrapper}.TYPE)
  }

  private def verify(owner: Owner, handle: VarHandle): Unit = {
    val before: ${T} = handle.get(owner)
<@assertValue T initial "before" />
    assertTrue(handle.compareAndSet(owner, ${initial}, ${desired}))
<@assertValue T desired "owner.value" />
    handle.set(owner, ${initial})
<@assertValue T initial "owner.value" />
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
    var value: ${wrapper} = ${boxed(T, initial)}
    val handle: VarHandle = MethodHandles.lookup()
      .findVarHandle(classOf[BoxedOwner], "value", classOf[${wrapper}])
  }

  @Test def boxedFieldSupportsPrimitiveReads(): Unit = {
    val owner = new BoxedOwner
<#list reads as method>
    val ${method}Value: ${T} = owner.handle.${method}(owner)
<@assertValue T initial method + "Value" />
</#list>
  }

  @Test def boxedFieldUsesReferenceOperations(): Unit = {
    val owner = new BoxedOwner
    val initial = owner.value
    val desired = ${boxed(T, desired)}
    val before: ${wrapper} = owner.handle.get(owner)
    assertSame(initial, before)
    val equalButNotSame = new ${wrapper}(${initial})
    assertNotSame(initial, equalButNotSame)
    assertFalse(owner.handle.compareAndSet(owner, equalButNotSame, desired))
    assertSame(initial, owner.value)
    assertTrue(owner.handle.compareAndSet(owner, initial, desired))
    assertSame(desired, owner.value)
    val witness: ${wrapper} = owner.handle.getAndSet(owner, initial)
    assertSame(desired, witness)
    assertSame(initial, owner.value)
    owner.handle.set(owner, desired)
    assertSame(desired, owner.value)
  }
}
</#list>
