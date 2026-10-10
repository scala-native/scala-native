<#include "VarHandleCommon.ftl">
<#list primitives as T>
class VarHandle${T}${shape}WideningMatrixTest {
<#list primitives as target>
<#if target != T>
<#assign allowed = (widening[T]![])?seq_contains(target)>
<#list reads + compareExchange + exchange + allowed?then(numeric, []) as method>
  @Test def ${method}As${target}(): Unit = {
<@setup T edgeValues[T] literal(T, 3, false) />
    ${call("set", "initial")}
<#if T == "Float" || T == "Double">
<@assertValue T "initial" field(T) />
</#if>
<#if !allowed || !supported(T, method)>
    assertThrows(classOf[${supported(T, method)?then("WrongMethodTypeException", "UnsupportedOperationException")}], {
      val result: ${target} = ${invoke(method)}
    })
<#else>
<#if compareExchange?seq_contains(method)>
    val failed: ${target} = ${call(method, "desired, desired")}
<@assertValue target "initial.to" + target "failed" />
<@assertValue T "initial" field(T) />
</#if>
    val result: ${target} = ${invoke(method)}
<@assertValue target "initial.to" + target "result" />
</#if>
<#assign expected = (allowed && supported(T, method))?then(after(T, method), "initial")>
<@assertValue T expected field(T) />
  }

</#list>
</#if>
</#list>
}
</#list>
