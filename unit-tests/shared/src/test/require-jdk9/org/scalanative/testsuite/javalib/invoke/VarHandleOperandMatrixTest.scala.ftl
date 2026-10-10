<#include "VarHandleCommon.ftl">
<#assign operandMethods = writes + cas + rmw>
<#list primitives as T>
<#list primitives as input>
<#if input == T || (widening[input]![])?seq_contains(T)>
<#-- Bound JUnit dispatcher depth by separating the input-type dimensions. -->
class VarHandle${T}${shape}${input}OperandMatrixTest {
<#assign flavors = ["Primitive", "Boxed", "Erased"] + (input == T)?then(["MixedExpected", "MixedDesired"], [])>
<#list flavors as flavor>
<#list operandMethods as method>
<#if !flavor?starts_with("Mixed") || compare(method)>
<#assign primitiveInitial = flavor == "Primitive" || flavor == "MixedExpected">
<#assign primitiveDesired = flavor == "Primitive" || flavor == "MixedDesired">
<#assign initialType = primitiveInitial?then(input, (flavor == "Erased")?then("AnyRef", "java.lang." + wrappers[input]))>
<#assign desiredType = primitiveDesired?then(input, (flavor == "Erased")?then("AnyRef", "java.lang." + wrappers[input]))>
<#assign initial = literal(input, 7)>
<#assign desired = literal(input, 2, false)>
  @Test def ${method}With${input}${flavor}(): Unit = {
<#if !isStatic>
    val box = new ${fixture(T)}
</#if>
    val handle: VarHandle = ${owner(T)}.handle
    ${field(T)} = ${literal(T, 7)}
    val initial: ${initialType} = ${primitiveInitial?then(initial, boxed(input, initial))}
    val desired: ${desiredType} = ${primitiveDesired?then(desired, boxed(input, desired))}
<#if !supported(T, method)>
    assertThrows(classOf[UnsupportedOperationException], {
      val witness: ${T} = ${invoke(method)}
    })
<@assertValue T literal(T, 7) field(T) />
<#else>
<#if writes?seq_contains(method)>
    ${invoke(method)}
<#elseif method?starts_with("weakCompare")>
    var success = false
    var attempts = 0
    while (!success && attempts < 1000) {
      success = ${invoke(method)}
      attempts += 1
    }
    assertTrue(success)
<#elseif method == "compareAndSet">
    assertTrue(${invoke(method)})
<#else>
    val witness: ${T} = ${invoke(method)}
<@assertValue T literal(T, 7) "witness" />
</#if>
<#assign expected = method?starts_with("getAndAdd")?then(literal(T, 9),
                    method?starts_with("getAndBitwiseOr")?then(literal(T, 7),
                    method?starts_with("getAndBitwiseXor")?then(literal(T, 5), literal(T, 2, false))))>
<@assertValue T expected field(T) />
</#if>
  }

</#if>
</#list>
</#list>
}

</#if>
</#list>
class VarHandle${T}${shape}OperandMatrixTest {
<#list operandMethods as method>
<#if compare(method)>
<#list ["Expected", "Desired"] as position>
  @Test def ${method}WithNull${position}(): Unit = {
<#if !isStatic>
    val box = new ${fixture(T)}
</#if>
    val handle: VarHandle = ${owner(T)}.handle
    ${field(T)} = ${literal(T, 7)}
    val wrong: ${T} = ${literal(T, 0, false)}
    val absent: java.lang.${wrappers[T]} = null
    assertThrows(classOf[NullPointerException], {
      val result: ${booleanResult(method)?then("Boolean", T)} = ${call(method, (position == "Expected")?then("absent, wrong", "wrong, absent"))}
    })
<@assertValue T literal(T, 7) field(T) />
  }

</#list>
</#if>
</#list>
}
</#list>
