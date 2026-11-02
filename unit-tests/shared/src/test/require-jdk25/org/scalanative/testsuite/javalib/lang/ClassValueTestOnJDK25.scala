package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.util.Try

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class ClassValueTestOnJDK25 {
  private final val TimeoutSeconds = 30L

  private class Blocking extends ClassValue[AnyRef] {
    val computed = new AtomicInteger(0)
    val firstComputing = new CountDownLatch(1)
    val resumeFirst = new CountDownLatch(1)

    protected def computeValue(cls: Class[_]): AnyRef = {
      val call = computed.incrementAndGet()
      if (call == 1) {
        firstComputing.countDown()
        assertTrue(resumeFirst.await(TimeoutSeconds, TimeUnit.SECONDS))
        onFirstResumed()
      }
      valueOf(call)
    }

    def onFirstResumed(): Unit = ()
    def valueOf(call: Int): AnyRef = s"call $call"
  }

  private def runBlocked(
      classValue: Blocking
  )(whileBlocked: => Unit): Try[AnyRef] = {
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
    @volatile var result: Try[AnyRef] = null
    val worker = new Thread(() => result = Try(classValue.get(classOf[String])))
    worker.setDaemon(true)
    worker.start()
    assertTrue(
      classValue.firstComputing.await(TimeoutSeconds, TimeUnit.SECONDS)
    )
    try whileBlocked
    finally classValue.resumeFirst.countDown()
    worker.join(TimeoutSeconds * 1000)
    assertFalse("timed out", worker.isAlive())
    result
  }

  @Test def racingRemoveDiscardsOutdatedComputation(): Unit = {
    val classValue = new Blocking

    val result = runBlocked(classValue) {
      classValue.remove(classOf[String])
    }

    assertEquals("call 2", result.get)
    assertEquals("call 2", classValue.get(classOf[String]))
    assertEquals(2, classValue.computed.get())
  }

  @Test def secondRacingRemoveDiscardsOutdatedComputation(): Unit = {
    val classValue = new Blocking
    // The computation starts from an existing token of an other thread
    classValue.remove(classOf[String])

    val result = runBlocked(classValue) {
      classValue.remove(classOf[String])
    }

    assertEquals("call 2", result.get)
    assertEquals(2, classValue.computed.get())
  }

  @Test def failedComputationRacingRemoveLeavesNoValue(): Unit = {
    val classValue = new Blocking {
      override def onFirstResumed(): Unit =
        throw new IllegalStateException("first")
    }

    val result = runBlocked(classValue) {
      classValue.remove(classOf[String])
    }

    assertThrows(classOf[IllegalStateException], result.get)
    assertEquals("call 2", classValue.get(classOf[String]))
  }

  @Test def nullFromRacingComputationIsTheAssociatedValue(): Unit = {
    val classValue = new Blocking {
      override def valueOf(call: Int): AnyRef =
        if (call == 2) null else super.valueOf(call)
    }

    val result = runBlocked(classValue) {
      assertNull(classValue.get(classOf[String]))
    }

    assertNull(result.get)
    assertNull(classValue.get(classOf[String]))
    assertEquals(2, classValue.computed.get())
  }

  @Test def getAfterRemoveNeverObservesOutdatedValue(): Unit = {
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
    val epoch = new AtomicInteger(0)
    val classValue = new ClassValue[Integer] {
      protected def computeValue(cls: Class[_]): Integer =
        Integer.valueOf(epoch.get())
    }
    val classes = ClassValueTest.manyClasses.take(3)

    ClassValueTest.runConcurrently(4) { _ =>
      var round = 0
      while (round < 5000) {
        val cls = classes(round % classes.size)
        val removedAt = epoch.incrementAndGet()
        classValue.remove(cls)
        assertTrue(classValue.get(cls).intValue() >= removedAt)
        round += 1
      }
    }
  }

  @Test def recursiveRemoveDoesNotDiscardComputation(): Unit = {
    val computed = new AtomicInteger(0)
    val classValue: ClassValue[AnyRef] = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef = {
        remove(cls)
        val call = computed.incrementAndGet()
        // A discarded computation would otherwise be retried forever
        assertTrue("the computation was discarded", call == 1)
        s"call $call"
      }
    }

    assertEquals("call 1", classValue.get(classOf[String]))
    assertEquals("call 1", classValue.get(classOf[String]))
    assertEquals(1, computed.get())
  }

  @Test def failedComputationReturnsValueOfRacingThread(): Unit = {
    val classValue = new Blocking {
      override def onFirstResumed(): Unit =
        throw new IllegalStateException("first")
    }

    val result = runBlocked(classValue) {
      assertEquals("call 2", classValue.get(classOf[String]))
    }

    assertEquals("call 2", result.get)
  }

  @Test def checkedExceptionsAreWrappedInError(): Unit = {
    val classValue = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef =
        throw new Exception("checked")
    }

    val error =
      assertThrows(classOf[Error], classValue.get(classOf[String]))
    assertEquals("checked", error.getCause().getMessage())
  }
}
