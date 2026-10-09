<#include "VarHandleCommon.ftl">
<#list types as T>
class VarHandle${label(T)}${shape}DiscardedMatrixTest {
<#list rmw as method>
<#list [true, false] as statement>
  @Test def ${method}_${statement?then("statement", "unitBody")}(): Unit = {
<@setup T initialValues[T] desiredValues[T] />
    ${field(T)} = initial
<#if !statement>
    def invoke(): Unit = ${invoke(method)}
</#if>
<#if !supported(T, method)>
    assertThrows(classOf[UnsupportedOperationException], {
      ${statement?then(invoke(method), "invoke()")}
<#if statement>
      ()
</#if>
    })
<#else>
    ${statement?then(invoke(method), "invoke()")}
</#if>
<@assertValue T supported(T, method)?then(after(T, method), "initial") field(T) />
  }

</#list>
</#list>
}
</#list>
