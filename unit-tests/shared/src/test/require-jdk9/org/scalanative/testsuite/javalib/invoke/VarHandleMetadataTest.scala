package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.VarHandle.AccessMode
import java.lang.invoke.{MethodHandles, VarHandle}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

// scalafmt: { maxColumn = 120 }

class VarHandleMetadataBox {
  var booleanValue: Boolean = false
  var byteValue: Byte = 0
  var shortValue: Short = 0
  var charValue: Char = 0
  var intValue: Int = 0
  var longValue: Long = 0L
  var floatValue: Float = 0.0f
  var doubleValue: Double = 0.0
  var referenceValue: java.lang.Integer = null
}
class VarHandleMetadataChild extends VarHandleMetadataBox

class VarHandleMetadataTest {
  private val handles = List(
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "booleanValue", classOf[Boolean]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "byteValue", classOf[Byte]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "shortValue", classOf[Short]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "charValue", classOf[Char]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "intValue", classOf[Int]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "longValue", classOf[Long]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "floatValue", classOf[Float]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "doubleValue", classOf[Double]),
    MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataBox], "referenceValue", classOf[java.lang.Integer])
  )
  private val types: List[Class[_]] = List(
    classOf[Boolean],
    classOf[Byte],
    classOf[Short],
    classOf[Char],
    classOf[Int],
    classOf[Long],
    classOf[Float],
    classOf[Double],
    classOf[java.lang.Integer]
  )

  @Test def metadataPreservesPrimitiveAndReferenceTypes(): Unit = {
    handles.zip(types).foreach {
      case (handle, tpe) =>
        assertSame(tpe, handle.varType())
        assertEquals(1, handle.coordinateTypes().size())
        assertSame(classOf[VarHandleMetadataBox], handle.coordinateTypes().get(0))
        assertThrows(classOf[UnsupportedOperationException], handle.coordinateTypes().add(classOf[Object]))
        val description = handle.toString()
        assertTrue(description, description.contains(tpe.getName))
        assertTrue(description, description.contains(classOf[VarHandleMetadataBox].getName))
    }
    assertNotSame(handles(4).varType(), handles(8).varType())
    assertSame(java.lang.Integer.TYPE, handles(4).varType())
  }

  @Test def capabilityMatrix(): Unit = {
    handles.zip(types).foreach {
      case (handle, tpe) =>
        AccessMode.values().foreach { mode =>
          val name = mode.methodName()
          val expected =
            if (name.startsWith("getAndAdd")) tpe.isPrimitive && tpe != classOf[Boolean]
            else if (name.startsWith("getAndBitwise"))
              tpe.isPrimitive && tpe != classOf[Float] && tpe != classOf[Double]
            else true
          assertEquals(tpe.getName + "/" + name, expected, handle.isAccessModeSupported(mode))
        }
        assertThrows(classOf[NullPointerException], handle.isAccessModeSupported(null))
    }
  }

  @Test def inheritedAndProtectedCoordinatesDescribeLookupRestrictions(): Unit = {
    val inherited = MethodHandles
      .privateLookupIn(classOf[VarHandleMetadataBox], MethodHandles.lookup())
      .findVarHandle(classOf[VarHandleMetadataChild], "intValue", classOf[Int])
    assertSame(classOf[VarHandleMetadataChild], inherited.coordinateTypes().get(0))
    val child = new VarHandleProtectedChild
    assertSame(classOf[VarHandleProtectedChild], child.handle.coordinateTypes().get(0))
  }

  @Test def accessModeEnumRoundTripsAndProtectsItsValues(): Unit = {
    val modes = AccessMode.values()
    assertEquals(31, modes.length)
    modes.zipWithIndex.foreach {
      case (mode, index) =>
        assertEquals(index, mode.ordinal())
        assertSame(mode, AccessMode.valueOf(mode.name()))
        assertSame(mode, AccessMode.valueFromMethodName(mode.methodName()))
        assertEquals(mode.name(), mode.toString())
    }
    assertSame(AccessMode.GET_AND_BITWISE_OR_RELEASE, modes(23))
    assertSame(AccessMode.GET_AND_BITWISE_OR_ACQUIRE, modes(24))
    modes(0) = null
    assertSame(AccessMode.GET, AccessMode.values()(0))
    assertThrows(classOf[IllegalArgumentException], AccessMode.valueOf("UNKNOWN"))
    assertThrows(classOf[IllegalArgumentException], AccessMode.valueFromMethodName("unknown"))
    assertThrows(classOf[NullPointerException], AccessMode.valueOf(null))
    assertThrows(classOf[NullPointerException], AccessMode.valueFromMethodName(null))
  }
}
