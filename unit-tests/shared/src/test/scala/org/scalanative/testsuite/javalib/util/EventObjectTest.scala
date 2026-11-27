package org.scalanative.testsuite.javalib.util

import java.util.{EventListener, EventListenerProxy, EventObject}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class EventObjectTest {
  @Test def getSource(): Unit = {
    val source = new Object
    assertSame(source, new EventObject(source).getSource())
  }

  @Test def nullSourceThrows(): Unit =
    assertThrows(classOf[IllegalArgumentException], new EventObject(null))

  @Test def toStringFormat(): Unit =
    assertEquals(
      "java.util.EventObject[source=src]",
      new EventObject("src").toString()
    )

  @Test def listenerProxy(): Unit = {
    val listener = new EventListener {}
    val proxy = new EventListenerProxy[EventListener](listener) {}
    assertSame(listener, proxy.getListener())
  }
}
