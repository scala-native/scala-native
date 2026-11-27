package org.scalanative.testsuite.javalib.util

import java.util.{ArrayList, Observable, Observer}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

@deprecated("tests deprecated API", "9")
class ObservableTest {
  class TestObservable extends Observable {
    def doChange(): Unit = setChanged()
    def undoChange(): Unit = clearChanged()
  }

  class Recorder(name: String, log: ArrayList[String]) extends Observer {
    var lastArg: Object = null
    var lastObservable: Observable = null
    def update(o: Observable, arg: Object): Unit = {
      log.add(name)
      lastObservable = o
      lastArg = arg
    }
  }

  @Test def addAndCountObservers(): Unit = {
    val o = new TestObservable
    val log = new ArrayList[String]
    val r = new Recorder("a", log)
    o.addObserver(r)
    o.addObserver(r)
    assertEquals(1, o.countObservers())
    o.addObserver(new Recorder("b", log))
    assertEquals(2, o.countObservers())
    assertThrows(classOf[NullPointerException], o.addObserver(null))
  }

  @Test def deleteObservers(): Unit = {
    val o = new TestObservable
    val r = new Recorder("a", new ArrayList[String])
    o.addObserver(r)
    o.addObserver(new Recorder("b", new ArrayList[String]))
    o.deleteObserver(r)
    assertEquals(1, o.countObservers())
    o.deleteObservers()
    assertEquals(0, o.countObservers())
  }

  @Test def changedFlag(): Unit = {
    val o = new TestObservable
    assertFalse(o.hasChanged())
    o.doChange()
    assertTrue(o.hasChanged())
    o.undoChange()
    assertFalse(o.hasChanged())
  }

  @Test def notifyOnlyWhenChanged(): Unit = {
    val o = new TestObservable
    val log = new ArrayList[String]
    val r = new Recorder("a", log)
    o.addObserver(r)
    o.notifyObservers("x")
    assertTrue(log.isEmpty())

    o.doChange()
    o.notifyObservers("x")
    assertEquals(1, log.size())
    assertSame(o, r.lastObservable)
    assertEquals("x", r.lastArg)
    assertFalse(o.hasChanged())

    o.notifyObservers("y")
    assertEquals(1, log.size())

    o.doChange()
    o.notifyObservers()
    assertNull(r.lastArg)
  }

  @Test def notifiesInReverseRegistrationOrder(): Unit = {
    val o = new TestObservable
    val log = new ArrayList[String]
    o.addObserver(new Recorder("a", log))
    o.addObserver(new Recorder("b", log))
    o.addObserver(new Recorder("c", log))
    o.doChange()
    o.notifyObservers()
    assertEquals("[c, b, a]", log.toString())
  }
}
