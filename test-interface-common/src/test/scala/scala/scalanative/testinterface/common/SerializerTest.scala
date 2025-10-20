package scala.scalanative.testinterface.common

// Ported from Scala.js

import org.junit.Test
import org.junit.Assert.*
import scala.scalanative.junit.async.*

class SerializerTest {
  def roundTrip[T: Serializer](x: T): T =
    Serializer.deserialize[T](Serializer.serialize(x))

  @Test
  def serializeThrowableWithNullFields: Unit = {
    val in = new Throwable(null, null)
    val out = roundTrip(in)
    assertEquals(in.getMessage(), out.getMessage())
    assertEquals(in.getCause(), out.getCause())
    assertEquals(in.toString(), out.toString())
    assertEquals(in.getStackTrace().size, out.getStackTrace().size)
  }

  // # 3611
  @Test
  def serializeStackTraceElementWithNullFilename: Unit = {
    val st = new StackTraceElement("MyClass", "myMethod", null, 1)
    val deserialized = roundTrip(st)
    assertNull(deserialized.getFileName)
    assertEquals("MyClass.myMethod(Unknown Source)", deserialized.toString)
  }
}
