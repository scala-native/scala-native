<#include "VarHandleCommon.ftl">
<#list primitives as T>
<#assign targets = [
  {"label": "Object", "type": "AnyRef", "allowed": true},
  {"label": "Wrapper", "type": "java.lang." + wrappers[T], "allowed": true},
  {"label": "Serializable", "type": "java.io.Serializable", "allowed": true},
  {"label": "Number", "type": "java.lang.Number", "allowed": T != "Boolean" && T != "Char"},
  {"label": "String", "type": "String", "allowed": false},
  {"label": "WrongWrapper", "type": (T == "Int")?then("java.lang.Long", "java.lang.Integer"), "allowed": false}
]>
class VarHandle${T}${shape}BoxingMatrixTest {
<#list targets as target>
<#list reads + exchange + compareExchange + target.allowed?then(numeric, []) as method>
  @Test def ${method}As${target.label}(): Unit = {
<#assign initial = ["Float", "Double"]?seq_contains(T)?then(edgeValues[T], literal(T, 7))>
<@setup T initial literal(T, 3, false) />
    ${call("set", "initial")}
<#if !target.allowed || !supported(T, method)>
    assertThrows(classOf[${target.allowed?then("UnsupportedOperationException", "WrongMethodTypeException")}], {
      val result: ${target.type} = ${invoke(method)}
    })
<#else>
<#if compareExchange?seq_contains(method)>
    val failed: ${target.type} = ${call(method, "desired, desired")}
    assertEquals(${boxed(T, "initial")}, failed)
    assertEquals(${boxed(T, "initial")}, ${boxed(T, field(T))})
</#if>
    val result: ${target.type} = ${invoke(method)}
    assertEquals(classOf[java.lang.${wrappers[T]}], result.getClass)
    assertEquals(${boxed(T, "initial")}, result)
</#if>
    assertEquals(${boxed(T, (target.allowed && supported(T, method))?then(after(T, method), "initial"))}, ${boxed(T, field(T))})
  }

</#list>
</#list>
}
</#list>
