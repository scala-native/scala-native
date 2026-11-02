// Partially ported from Scala.js commit: 3cc6db1 dated: 2020-10-28

package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import java.util.concurrent.{CyclicBarrier, TimeUnit}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class ClassValueTest {
  import ClassValueTest._

  private class Counting extends ClassValue[AnyRef] {
    val computed = new AtomicInteger(0)
    protected def computeValue(cls: Class[_]): AnyRef =
      s"${cls.getName()} ${computed.incrementAndGet()}"
  }

  @Test def computesValueOncePerClass(): Unit = {
    val classValue = new Counting

    assertEquals("java.lang.String 1", classValue.get(classOf[String]))
    assertEquals("scala.Option 2", classValue.get(classOf[Option[_]]))
    assertEquals("java.lang.String 1", classValue.get(classOf[String]))
    assertEquals("scala.Option 2", classValue.get(classOf[Option[_]]))
    assertEquals(2, classValue.computed.get())
  }

  @Test def cachesNullValues(): Unit = {
    val computed = new AtomicInteger(0)
    val classValue = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef = {
        computed.incrementAndGet()
        null
      }
    }

    assertNull(classValue.get(classOf[String]))
    assertNull(classValue.get(classOf[String]))
    assertEquals(1, computed.get())
  }

  @Test def isIndependentForEachClassValue(): Unit = {
    val first, second = new Counting

    assertEquals("java.lang.String 1", first.get(classOf[String]))
    assertEquals("java.lang.Object 2", first.get(classOf[Object]))
    assertEquals("java.lang.Object 1", second.get(classOf[Object]))
    first.remove(classOf[Object])
    assertEquals("java.lang.Object 1", second.get(classOf[Object]))
  }

  @Test def removeForcesRecomputation(): Unit = {
    val classValue = new Counting

    assertEquals("java.lang.String 1", classValue.get(classOf[String]))
    assertEquals("java.lang.Object 2", classValue.get(classOf[Object]))

    classValue.remove(classOf[String])
    assertEquals("java.lang.Object 2", classValue.get(classOf[Object]))
    assertEquals("java.lang.String 3", classValue.get(classOf[String]))
    assertEquals("java.lang.String 3", classValue.get(classOf[String]))
  }

  @Test def removeOfMissingValueIsAllowed(): Unit = {
    val classValue = new Counting

    classValue.remove(classOf[String])
    classValue.remove(classOf[String])
    assertEquals("java.lang.String 1", classValue.get(classOf[String]))
  }

  @Test def acceptsEveryKindOfClass(): Unit = {
    val classValue = new ClassValue[Class[_]] {
      protected def computeValue(cls: Class[_]): Class[_] = cls
    }
    val classes = Seq[Class[_]](
      classOf[Int],
      classOf[Boolean],
      java.lang.Void.TYPE,
      classOf[Object],
      classOf[Runnable], // interface
      classOf[Array[Int]],
      classOf[Array[Object]],
      classOf[Marker],
      Marker.getClass()
    )

    classes.foreach(cls => assertSame(cls, classValue.get(cls)))
    classes.foreach(cls => assertSame(cls, classValue.get(cls)))
  }

  @Test def keepsValuesWhileGrowing(): Unit = {
    val classValue = new ClassValue[Class[_]] {
      protected def computeValue(cls: Class[_]): Class[_] = cls
    }

    manyClasses.foreach(cls => assertSame(cls, classValue.get(cls)))
    manyClasses.foreach(cls => assertSame(cls, classValue.get(cls)))
    manyClasses.foreach(classValue.remove(_))
    manyClasses.foreach(cls => assertSame(cls, classValue.get(cls)))
  }

  @Test def nullClassThrows(): Unit = {
    val classValue = new Counting

    assertThrows(classOf[NullPointerException], classValue.get(null))
    assertThrows(classOf[NullPointerException], classValue.remove(null))
  }

  @Test def failedComputationIsNotCached(): Unit = {
    val computed = new AtomicInteger(0)
    val classValue = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef =
        if (computed.incrementAndGet() == 1)
          throw new IllegalStateException("first")
        else "second"
    }

    assertThrows(
      classOf[IllegalStateException],
      classValue.get(classOf[String])
    )
    assertEquals("second", classValue.get(classOf[String]))
    assertEquals(2, computed.get())
  }

  @Test def computationCanUseOtherClassValue(): Unit = {
    val inner = new Counting
    val outer = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef =
        "outer " + inner.get(cls)
    }

    assertEquals(
      "outer java.lang.String 1",
      outer.get(classOf[String])
    )
  }

  @Test def computationCanUseSameClassValueWithOtherClass(): Unit = {
    // Every computation runs while the previous ones hold no lock, which would
    // hang or fail if it would be executed inside of the underlying map.
    val classValue: ClassValue[Integer] = new ClassValue[Integer] {
      protected def computeValue(cls: Class[_]): Integer = {
        val superclass = cls.getSuperclass()
        if (superclass == null) Integer.valueOf(0)
        else Integer.valueOf(get(superclass).intValue() + 1)
      }
    }

    assertEquals(0, classValue.get(classOf[Object]).intValue())
    assertEquals(3, classValue.get(classOf[Deepest]).intValue())
    assertEquals(2, classValue.get(classOf[Deep]).intValue())
  }

  @Test def recursiveGetOfSameClassInitiatesTheValue(): Unit = {
    val computed = new AtomicInteger(0)
    val classValue: ClassValue[AnyRef] = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef = {
        val call = computed.incrementAndGet()
        if (call == 1) get(cls) // discarded in favor of the recursive result
        s"call $call"
      }
    }

    assertEquals("call 2", classValue.get(classOf[String]))
    assertEquals("call 2", classValue.get(classOf[String]))
  }

  @Test def removeOfMissingClassesGrowsTheTable(): Unit = {
    val classValue = new Counting

    manyClasses.foreach(classValue.remove(_))
    manyClasses.foreach(classValue.get(_))
    assertEquals(manyClasses.size, classValue.computed.get())
  }

  @Test def exceptionFromRecursiveGetIsNotCached(): Unit = {
    val computed = new AtomicInteger(0)
    val classValue: ClassValue[AnyRef] = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef =
        computed.incrementAndGet() match {
          case 1 => get(cls)
          case 2 => throw new IllegalStateException("recursive")
          case n => s"call $n"
        }
    }

    assertThrows(
      classOf[IllegalStateException],
      classValue.get(classOf[String])
    )
    assertEquals("call 3", classValue.get(classOf[String]))
  }

  @Test def getAfterRemoveOnOtherThreadComputesOnce(): Unit = {
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
    val classValue = new Counting
    classValue.get(classOf[String])

    runConcurrently(1)(_ => classValue.remove(classOf[String]))

    assertEquals("java.lang.String 2", classValue.get(classOf[String]))
    assertEquals(2, classValue.computed.get())
  }

  @Test def racingThreadsObserveSingleValue(): Unit = {
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
    val threads = 4
    val allComputing = new CyclicBarrier(threads)
    val classValue = new ClassValue[AnyRef] {
      protected def computeValue(cls: Class[_]): AnyRef = {
        allComputing.await(TimeoutSeconds, TimeUnit.SECONDS)
        new Object
      }
    }
    val results = new Array[AnyRef](threads)

    runConcurrently(threads) { index =>
      results(index) = classValue.get(classOf[String])
    }

    assertNotNull(results(0))
    results.foreach(assertSame(results(0), _))
    assertSame(results(0), classValue.get(classOf[String]))
  }

  @Test def racingThreadsKeepValuesWhileTableGrows(): Unit = {
    assumeTrue(
      "requires multithreading",
      Platform.isMultithreadingEnabled
    )
    final class Computed(val cls: Class[_])

    // Only a new ClassValue grows, so that readers would meet the resizing
    for (_ <- 0 until 200) {
      val classValue = new ClassValue[Computed] {
        protected def computeValue(cls: Class[_]): Computed = new Computed(cls)
      }
      runConcurrently(4) { offset =>
        var index = 0
        while (index < manyClasses.size) {
          val cls = manyClasses((index + offset * 9) % manyClasses.size)
          val value = classValue.get(cls)
          assertSame(cls, value.cls)
          assertSame(value, classValue.get(cls))
          index += 1
        }
      }
    }
  }
}

object ClassValueTest {
  final val TimeoutSeconds = 30L

  /** Runs the body in the given number of threads, started together. Fails if
   *  any of them fails or doesn't finish in time.
   */
  def runConcurrently(threads: Int)(body: Int => Unit): Unit = {
    val start = new CyclicBarrier(threads)
    val failure = new AtomicReference[Throwable](null)
    val workers = (0 until threads).map { index =>
      val worker = new Thread(() =>
        try {
          start.await(TimeoutSeconds, TimeUnit.SECONDS)
          body(index)
        } catch {
          case ex: Throwable => failure.set(ex)
        }
      )
      worker.setDaemon(true)
      worker
    }

    workers.foreach(_.start())
    workers.foreach(_.join(TimeoutSeconds * 1000))

    workers.foreach(worker => assertFalse("timed out", worker.isAlive()))
    if (failure.get() != null) throw failure.get()
  }

  class Marker
  object Marker
  class Shallow
  class Deep extends Shallow
  class Deepest extends Deep

  // Enough classes to require resizing the table multiple times
  val manyClasses: Seq[Class[_]] = Seq(
    classOf[Byte],
    classOf[Short],
    classOf[Int],
    classOf[Long],
    classOf[Float],
    classOf[Double],
    classOf[Char],
    classOf[Boolean],
    classOf[Object],
    classOf[String],
    classOf[Runnable],
    classOf[Comparable[_]],
    classOf[CharSequence],
    classOf[Thread],
    classOf[Exception],
    classOf[RuntimeException],
    classOf[Error],
    classOf[Throwable],
    classOf[Integer],
    classOf[java.lang.Long],
    classOf[java.lang.Byte],
    classOf[java.lang.Short],
    classOf[java.lang.Float],
    classOf[java.lang.Double],
    classOf[java.lang.Boolean],
    classOf[Character],
    classOf[StringBuilder],
    classOf[Array[Int]],
    classOf[Array[Byte]],
    classOf[Array[Long]],
    classOf[Array[Object]],
    classOf[Marker],
    classOf[Shallow],
    classOf[Deep],
    classOf[Deepest],
    classOf[Option[_]],
    classOf[List[_]]
  )
}
