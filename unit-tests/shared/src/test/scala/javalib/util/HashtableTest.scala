package javalib.util

import java.util._

import org.junit.Test

import scala.scalanative.junit.utils.AssertThrows.assertThrows

class HashtableTest {

  @Test def putOnNullKeyOrValue(): Unit = {
    val t = new Hashtable[AnyRef, AnyRef]()
    assertThrows(classOf[NullPointerException], t.put(null, "value"))
    assertThrows(classOf[NullPointerException], t.put("key", null))
  }
}
