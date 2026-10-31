package scala.scalanative.runtime.monitor

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.MemoryLayout
import scala.scalanative.unsafe._

class StringBackingArrayMonitorRootTest {
  @Test def backingArrayMonitorSurvivesAfterStringSectionActivation(): Unit = {
    val literal = "static literal backing array"
    // Public APIs cannot expose the backing array. Activate the section through
    // its String before testing a monitor obtained through unsafe test access.
    ClassMonitorRootTest.assertStaticMonitorSurvives(literal)
    val backing = StringBackingArrayMonitorRootTest.backingArray(literal)
    val section = !BackingArraySections.strings()
    val address = castRawPtrToLong(castObjectToRawPtr(backing))
    assertTrue(
      "expected a static literal backing array",
      address >= section._1.toLong && address < section._2.toLong
    )
    ClassMonitorRootTest.assertStaticMonitorSurvives(backing)
  }
}

private[monitor] object StringBackingArrayMonitorRootTest {
  // The first String field is its backing array. Keep this unsafe access in
  // test code: public encoding APIs must not expose String-owned storage.
  @scala.noinline
  def backingArray(string: String): Array[Char] =
    castRawPtrToObject(
      loadRawPtr(
        elemRawPtr(castObjectToRawPtr(string), MemoryLayout.Object.FieldsOffset)
      )
    ).asInstanceOf[Array[Char]]
}

@extern
private[monitor] object BackingArraySections {
  @name("scalanative_static_strings_section")
  def strings(): Ptr[CStruct2[CVoidPtr, CVoidPtr]] = extern
}
