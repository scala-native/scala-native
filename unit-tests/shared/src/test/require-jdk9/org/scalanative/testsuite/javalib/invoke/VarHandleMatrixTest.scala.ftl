<#include "VarHandleCommon.ftl">
<#list types as T>
<#assign default = (T == "AnyRef")?then("null", literal(T, 0, false))>
<#assign classToken = (T == "AnyRef")?then("classOf[AnyRef]", "java.lang." + wrappers[T] + ".TYPE")>
<#if isStatic>
class ${fixture(T)}
object ${fixture(T)} {
  @static var value: ${T} = ${default}
<#else>
class ${fixture(T)} {
  var value: ${T} = ${default}
</#if>
  val handle: VarHandle = MethodHandles.lookup()
    .${isStatic?then("findStaticVarHandle", "findVarHandle")}(classOf[${fixture(T)}], "value", ${classToken})
}

class VarHandle${label(T)}${shape}MatrixTest {
  protected def invocationHandle(handle: VarHandle): VarHandle = handle

  private def assertValue(expected: ${T}, actual: ${T}): Unit = {
<@assertValue T "expected" "actual" />
  }

<#list methods as method>
  @Test def ${method}_${supported(T, method)?then("supported", "unsupported")}(): Unit = {
<@setup T initialValues[T] desiredValues[T] true />
    ${field(T)} = initial
<#if !supported(T, method)>
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: ${T} = ${invoke(method)}
    })
    assertValue(initial, ${field(T)})
<#elseif reads?seq_contains(method)>
    val read: ${T} = ${call(method)}
    assertValue(initial, read)
    assertValue(initial, ${field(T)})
<#elseif writes?seq_contains(method)>
    ${call(method, "desired")}
    assertValue(desired, ${field(T)})
<#elseif cas?seq_contains(method)>
    assertFalse(${call(method, "desired, initial")})
    assertValue(initial, ${field(T)})
<#if method?starts_with("weak")>
    var attempts = 0
    var success = false
    while (!success && attempts < 10000) {
      success = ${call(method, "initial, desired")}
      attempts += 1
    }
    assertTrue("weak CAS never succeeded", success)
<#else>
    assertTrue(${call(method, "initial, desired")})
</#if>
    assertValue(desired, ${field(T)})
    assertFalse(${call(method, "initial, initial")})
    assertValue(desired, ${field(T)})
<#elseif compareExchange?seq_contains(method)>
    val failed: ${T} = ${call(method, "desired, desired")}
    assertValue(initial, failed)
    assertValue(initial, ${field(T)})
    val succeeded: ${T} = ${invoke(method)}
    assertValue(initial, succeeded)
    assertValue(desired, ${field(T)})
<#elseif exchange?seq_contains(method) || method?starts_with("getAndAdd")>
    val witness: ${T} = ${invoke(method)}
    assertValue(initial, witness)
    assertValue(${method?starts_with("getAndAdd")?then("15.to" + T, "desired")}, ${field(T)})
<#elseif T == "Boolean">
    for (before <- List(false, true); mask <- List(false, true)) {
      ${field(T)} = before
      val witness: Boolean = ${call(method, "mask")}
      assertValue(before, witness)
      assertValue(${after(T, method)?replace("initial", "before")?replace("desired", "mask")}, ${field(T)})
    }
<#else>
    val witness: ${T} = ${invoke(method)}
    assertValue(initial, witness)
    assertValue(${literal(T, method?contains("BitwiseAnd")?then(0, 15))}, ${field(T)})
</#if>
  }

</#list>
}
</#list>
