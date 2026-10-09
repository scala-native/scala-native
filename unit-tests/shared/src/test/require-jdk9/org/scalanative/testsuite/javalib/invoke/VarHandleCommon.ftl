<#-- Shared matrix data and source-level helpers. VarHandle call sites remain statically typed. -->
<#assign shape = isStatic?then("Static", "Instance")>
<#assign primitives = ["Boolean", "Byte", "Short", "Char", "Int", "Long", "Float", "Double"]>
<#assign types = primitives + ["AnyRef"]>
<#assign wrappers = {"Boolean": "Boolean", "Byte": "Byte", "Short": "Short", "Char": "Character",
                    "Int": "Integer", "Long": "Long", "Float": "Float", "Double": "Double", "AnyRef": "Object"}>
<#assign initialValues = {"Boolean": "false", "Byte": "12.toByte", "Short": "12.toShort", "Char": "12.toChar",
                         "Int": "12", "Long": "12L", "Float": "12.0f", "Double": "12.0d",
                         "AnyRef": 'new String("same")'}>
<#assign desiredValues = {"Boolean": "true", "Byte": "3.toByte", "Short": "3.toShort", "Char": "3.toChar",
                         "Int": "3", "Long": "3L", "Float": "3.0f", "Double": "3.0d",
                         "AnyRef": 'new String("different")'}>
<#assign edgeValues = {"Boolean": "true", "Byte": "(-12).toByte", "Short": "(-1200).toShort", "Char": "65530.toChar",
                      "Int": "16777217", "Long": "9007199254740993L",
                      "Float": "java.lang.Float.intBitsToFloat(Int.MinValue)",
                      "Double": "java.lang.Double.longBitsToDouble(Long.MinValue)"}>
<#assign widening = {"Byte": ["Short", "Int", "Long", "Float", "Double"], "Short": ["Int", "Long", "Float", "Double"],
                    "Char": ["Int", "Long", "Float", "Double"], "Int": ["Long", "Float", "Double"],
                    "Long": ["Float", "Double"], "Float": ["Double"]}>
<#assign reads = ["get", "getOpaque", "getAcquire", "getVolatile"]>
<#assign writes = ["set", "setOpaque", "setRelease", "setVolatile"]>
<#assign cas = ["compareAndSet", "weakCompareAndSetPlain", "weakCompareAndSet",
               "weakCompareAndSetAcquire", "weakCompareAndSetRelease"]>
<#assign compareExchange = ["compareAndExchange", "compareAndExchangeAcquire", "compareAndExchangeRelease"]>
<#assign exchange = ["getAndSet", "getAndSetAcquire", "getAndSetRelease"]>
<#assign numeric = []>
<#list ["getAndAdd", "getAndBitwiseOr", "getAndBitwiseAnd", "getAndBitwiseXor"] as prefix>
<#list ["", "Acquire", "Release"] as suffix>
<#assign numeric += [prefix + suffix]>
</#list>
</#list>
<#assign rmw = compareExchange + exchange + numeric>
<#assign methods = reads + writes + cas + rmw>

<#function label T><#return (T == "AnyRef")?then("Reference", T)></#function>
<#function fixture T><#return "VarHandle" + label(T) + shape + "Fixture"></#function>
<#function owner T><#return isStatic?then(fixture(T), "box")></#function>
<#function field T><#return owner(T) + ".value"></#function>
<#function literal T value booleanValue=true>
<#return (T == "Boolean")?then(booleanValue?c, value?c + ".to" + T)>
</#function>
<#function boxed T value><#return "java.lang." + wrappers[T] + ".valueOf(" + value + ")"></#function>
<#function compare method><#return method?starts_with("compare") || method?starts_with("weakCompare")></#function>
<#function booleanResult method><#return method == "compareAndSet" || method?starts_with("weakCompare")></#function>
<#function supported T method>
<#return !(method?starts_with("getAndAdd") && ["Boolean", "AnyRef"]?seq_contains(T)) &&
         !(method?starts_with("getAndBitwise") && ["Float", "Double", "AnyRef"]?seq_contains(T))>
</#function>
<#function call method operands="">
<#local arguments = isStatic?then(operands, "box" + operands?has_content?then(", " + operands, ""))>
<#return "handle." + method + "(" + arguments + ")">
</#function>
<#function invoke method>
<#return call(method, compare(method)?then("initial, desired", reads?seq_contains(method)?then("", "desired")))>
</#function>
<#function after T method>
<#if reads?seq_contains(method)><#return "initial"></#if>
<#if method?starts_with("getAndAdd")><#return "(initial + desired).to" + T></#if>
<#if method?starts_with("getAndBitwise")>
<#local operator = method?contains("BitwiseOr")?then("|", method?contains("BitwiseAnd")?then("&", "^"))>
<#return "(initial " + operator + " desired)" + (T == "Boolean")?then("", ".to" + T)>
</#if>
<#return "desired">
</#function>

<#macro assertValue T expected actual>
<#if T == "AnyRef">
    assertSame(${expected}, ${actual})
<#elseif T == "Float">
    assertEquals(java.lang.Float.floatToRawIntBits(${expected}), java.lang.Float.floatToRawIntBits(${actual}))
<#elseif T == "Double">
    assertEquals(java.lang.Double.doubleToRawLongBits(${expected}), java.lang.Double.doubleToRawLongBits(${actual}))
<#else>
    assertEquals(${expected}, ${actual})
</#if>
</#macro>
<#macro setup T initial desired useInvocationHandle=false>
<#if !isStatic>
    val box = new ${fixture(T)}
</#if>
<#if useInvocationHandle>
    val handle: VarHandle = invocationHandle(${owner(T)}.handle)
<#else>
    val handle: VarHandle = ${owner(T)}.handle
</#if>
    val initial: ${T} = ${initial}
    val desired: ${T} = ${desired}
</#macro>
package org.scalanative.testsuite.javalib.invoke

// Generated from VarHandle templates by sbt; edit the .ftl sources.
// format: off
import java.lang.invoke.{MethodHandles, VarHandle, WrongMethodTypeException}
import org.junit.Assert._
import org.junit.Test
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
<#if isStatic>
import scala.annotation.static
</#if>
