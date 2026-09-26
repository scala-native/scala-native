/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 * file: src/test/tck/PhaserTest.java
 * revision 1.51, dated: 2021-01-26
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include John Vint
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, Phaser, TimeoutException}
import java.util.{ArrayList, List}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class PhaserTest extends JSR166Test {
  import JSR166Test._

  final val maxParties = 65535

  /** Checks state of unterminated phaser. */
  protected def assertState(
      phaser: Phaser,
      phase: Int,
      parties: Int,
      unarrived: Int
  ): Unit = {
    assertEquals("phase", phase, phaser.getPhase())
    assertEquals("parties", parties, phaser.getRegisteredParties())
    assertEquals("unarrived", unarrived, phaser.getUnarrivedParties())
    assertEquals(
      "parties - unarrived",
      parties - unarrived,
      phaser.getArrivedParties()
    )
    assertFalse("isTerminated", phaser.isTerminated())
  }

  /** Checks state of terminated phaser. */
  protected def assertTerminated(
      phaser: Phaser,
      maxPhase: Int,
      parties: Int
  ): Unit = {
    assertTrue("isTerminated", phaser.isTerminated())
    val expectedPhase = maxPhase + Integer.MIN_VALUE
    assertEquals("expectedPhase", expectedPhase, phaser.getPhase())
    assertEquals(
      "getRegisteredParties",
      parties,
      phaser.getRegisteredParties()
    )
    assertEquals("register", expectedPhase, phaser.register())
    assertEquals("arrive", expectedPhase, phaser.arrive())
    assertEquals(
      "arriveAndDeregister",
      expectedPhase,
      phaser.arriveAndDeregister()
    )
  }

  protected def assertTerminated(phaser: Phaser, maxPhase: Int): Unit = {
    assertTerminated(phaser, maxPhase, 0)
  }

  /** Empty constructor builds a new Phaser with no parent, no registered
   *  parties and initial phase number of 0
   */
  @Test def testConstructorDefaultValues(): Unit = {
    val phaser = new Phaser()
    assertNull("a1", phaser.getParent())
    assertEquals("a2", 0, phaser.getRegisteredParties())
    assertEquals("a3", 0, phaser.getArrivedParties())
    assertEquals("a4", 0, phaser.getUnarrivedParties())
    assertEquals("a5", 0, phaser.getPhase())
  }

  /** Constructing with a negative number of parties throws
   *  IllegalArgumentException
   */
  @Test def testConstructorNegativeParties(): Unit = {
    assertThrows(
      "negative parties",
      classOf[IllegalArgumentException],
      new Phaser(-1)
    )
  }

  /** Constructing with a negative number of parties throws
   *  IllegalArgumentException
   */
  @Test def testConstructorNegativeParties2(): Unit = {
    assertThrows(
      "negative parties",
      classOf[IllegalArgumentException],
      new Phaser(new Phaser(), -1)
    )
  }

  /** Constructing with a number of parties > 65535 throws
   *  IllegalArgumentException
   */
  @Test def testConstructorPartiesExceedsLimit(): Unit = {
    assertNotNull("Phaser(maxParties)", new Phaser(maxParties))

    assertThrows(
      "a1",
      classOf[IllegalArgumentException],
      new Phaser(maxParties + 1)
    )

    assertNotNull(
      "Phaser(parent, maxParties)",
      new Phaser(new Phaser(), maxParties)
    )

    assertThrows(
      "a2",
      classOf[IllegalArgumentException],
      new Phaser(new Phaser(), maxParties + 1)
    )
  }

  /** The parent provided to the constructor should be returned from a later
   *  call to getParent
   */
  @Test def testConstructor3(): Unit = {
    val parent = new Phaser()
    assertSame("a1", parent, new Phaser(parent).getParent())
    assertNull("a2", new Phaser(null).getParent())
  }

  /** The parent being input into the parameter should equal the original parent
   *  when being returned
   */
  @Test def testConstructor5(): Unit = {
    val parent = new Phaser()
    assertSame("a1", parent, new Phaser(parent, 0).getParent())
    assertNull("a2", new Phaser(null, 0).getParent())
  }

  /** register() will increment the number of unarrived parties by one and not
   *  affect its arrived parties
   */
  @Test def testRegister1(): Unit = {
    val phaser = new Phaser()
    assertState(phaser, 0, 0, 0)
    assertEquals(0, phaser.register())
    assertState(phaser, 0, 1, 1)
  }

  /** Registering more than 65536 parties causes IllegalStateException
   */
  @Test def testRegister2(): Unit = {
    val phaser = new Phaser(0)
    assertState(phaser, 0, 0, 0)
    assertEquals(0, phaser.bulkRegister(maxParties - 10))
    assertState(phaser, 0, maxParties - 10, maxParties - 10)
    for (i <- 0 until 10) {
      assertState(phaser, 0, maxParties - 10 + i, maxParties - 10 + i)
      assertEquals(0, phaser.register())
    }
    assertState(phaser, 0, maxParties, maxParties)

    assertThrows(
      "register",
      classOf[IllegalStateException],
      phaser.register()
    )

    assertThrows(
      "bulkRegister",
      classOf[IllegalStateException],
      phaser.bulkRegister(Integer.MAX_VALUE)
    )

    assertEquals(0, phaser.bulkRegister(0))
    assertState(phaser, 0, maxParties, maxParties)
  }

  /** register() correctly returns the current barrier phase number when invoked
   */
  @Test def testRegister3(): Unit = {
    val phaser = new Phaser()
    assertEquals("a1", 0, phaser.register())
    assertEquals("a2", 0, phaser.arrive())
    assertEquals("a3", 1, phaser.register())
    assertState(phaser, 1, 2, 2)
  }

  /** register causes the next arrive to not increment the phase rather retain
   *  the phase number
   */
  @Test def testRegister4(): Unit = {
    val phaser = new Phaser(1)
    assertEquals(0, phaser.arrive())
    assertEquals(1, phaser.register())
    assertEquals(1, phaser.arrive())
    assertState(phaser, 1, 2, 1)
  }

  /** register on a subphaser that is currently empty succeeds, even in the
   *  presence of another non-empty subphaser
   */
  @Test def testRegisterEmptySubPhaser(): Unit = {
    val root = new Phaser()
    val child1 = new Phaser(root, 1)
    val child2 = new Phaser(root, 0)
    assertEquals(0, child2.register())
    assertState(root, 0, 2, 2)
    assertState(child1, 0, 1, 1)
    assertState(child2, 0, 1, 1)
    assertEquals(0, child2.arriveAndDeregister())
    assertState(root, 0, 1, 1)
    assertState(child1, 0, 1, 1)
    assertState(child2, 0, 0, 0)
    assertEquals(0, child2.register())
    assertEquals(0, child2.arriveAndDeregister())
    assertState(root, 0, 1, 1)
    assertState(child1, 0, 1, 1)
    assertState(child2, 0, 0, 0)
    assertEquals(0, child1.arriveAndDeregister())
    assertTerminated(root, 1)
    assertTerminated(child1, 1)
    assertTerminated(child2, 1)
  }

  /** Invoking bulkRegister with a negative parameter throws an
   *  IllegalArgumentException
   */
  @Test def testBulkRegister1(): Unit = {
    assertThrows(
      "negative parties",
      classOf[IllegalArgumentException],
      new Phaser().bulkRegister(-1)
    )
  }

  /** bulkRegister should correctly record the number of unarrived parties with
   *  the number of parties being registered
   */
  @Test def testBulkRegister2(): Unit = {
    val phaser = new Phaser()
    assertEquals(0, phaser.bulkRegister(0))
    assertState(phaser, 0, 0, 0)
    assertEquals(0, phaser.bulkRegister(20))
    assertState(phaser, 0, 20, 20)
  }

  /** Registering with a number of parties greater than or equal to 1<<16 throws
   *  IllegalStateException.
   */
  @Test def testBulkRegister3(): Unit = {
    assertEquals(0, new Phaser().bulkRegister((1 << 16) - 1))

    assertThrows(
      "a1",
      classOf[IllegalStateException],
      new Phaser().bulkRegister(1 << 16)
    )

    assertThrows(
      "a2",
      classOf[IllegalStateException],
      new Phaser(2).bulkRegister((1 << 16) - 2)
    )
  }

  /** the phase number increments correctly when tripping the barrier
   */
  @Test def testPhaseIncrement1(): Unit = {
    for (size <- 1 until 9) {
      val phaser = new Phaser(size)
      for (index <- 0 to (1 << size)) {
        val phase = phaser.arrive()
        assertTrue(
          if (index % size == 0) (index / size) == phase
          else index - (phase * size) > 0
        )
      }
    }
  }

  /** arrive() on a registered phaser increments phase.
   */
  @Test def testArrive1(): Unit = {
    val phaser = new Phaser(1)
    assertState(phaser, 0, 1, 1)
    assertEquals(0, phaser.arrive())
    assertState(phaser, 1, 1, 1)
  }

  /** arriveAndDeregister does not wait for others to arrive at barrier
   */
  @Test def testArriveAndDeregister(): Unit = {
    val phaser = new Phaser(1)
    for (i <- 0 until 10) {
      assertState(phaser, 0, 1, 1)
      assertEquals(0, phaser.register())
      assertState(phaser, 0, 2, 2)
      assertEquals(0, phaser.arriveAndDeregister())
      assertState(phaser, 0, 1, 1)
    }
    assertEquals(0, phaser.arriveAndDeregister())
    assertTerminated(phaser, 1)
  }

  /** arriveAndDeregister does not wait for others to arrive at barrier
   */
  @Test def testArrive2(): Unit = {
    val phaser = new Phaser()
    assertEquals(0, phaser.register())
    val threads = new ArrayList[Thread]()

    for (i <- 0 until 10) {
      assertEquals(0, phaser.register())
      threads.add(newStartedThread(new CheckedRunnable() {
        def realRun(): Unit = {
          assertEquals(0, phaser.arriveAndDeregister())
        }
      }))
    }

    threads.forEach(awaitTermination(_))
    assertState(phaser, 0, 1, 1)
    assertEquals(0, phaser.arrive())
    assertState(phaser, 1, 1, 1)
  }

  /** arrive() returns a negative number if the Phaser is terminated
   */
  @Test def testArrive3(): Unit = {
    val phaser = new Phaser(1)
    phaser.forceTermination()
    assertTerminated(phaser, 0, 1)
    assertEquals(0, phaser.getPhase() + Integer.MIN_VALUE)
    assertTrue(phaser.arrive() < 0)
    assertTrue(phaser.register() < 0)
    assertTrue(phaser.arriveAndDeregister() < 0)
    assertTrue(phaser.awaitAdvance(1) < 0)
    assertTrue(phaser.getPhase() < 0)
  }

  /** arriveAndDeregister() throws IllegalStateException if number of registered
   *  or unarrived parties would become negative
   */
  @Test def testArriveAndDeregister1(): Unit = {
    val phaser = new Phaser()
    assertThrows(
      classOf[IllegalStateException],
      phaser.arriveAndDeregister()
    )
  }

  /** arriveAndDeregister reduces the number of arrived parties
   */
  @Test def testArriveAndDeregister2(): Unit = {
    val phaser = new Phaser(1)
    assertEquals(0, phaser.register())
    assertEquals(0, phaser.arrive())
    assertState(phaser, 0, 2, 1)
    assertEquals(0, phaser.arriveAndDeregister())
    assertState(phaser, 1, 1, 1)
  }

  /** arriveAndDeregister arrives at the barrier on a phaser with a parent and
   *  when a deregistration occurs and causes the phaser to have zero parties
   *  its parent will be deregistered as well
   */
  @Test def testArriveAndDeregister3(): Unit = {
    val parent = new Phaser()
    val child = new Phaser(parent)
    assertState(child, 0, 0, 0)
    assertState(parent, 0, 0, 0)
    assertEquals(0, child.register())
    assertState(child, 0, 1, 1)
    assertState(parent, 0, 1, 1)
    assertEquals(0, child.arriveAndDeregister())
    assertTerminated(child, 1)
    assertTerminated(parent, 1)
  }

  /** arriveAndDeregister deregisters one party from its parent when the number
   *  of parties of child is zero after deregistration
   */
  @Test def testArriveAndDeregister4(): Unit = {
    val parent = new Phaser()
    val child = new Phaser(parent)
    assertEquals(0, parent.register())
    assertEquals(0, child.register())
    assertState(child, 0, 1, 1)
    assertState(parent, 0, 2, 2)
    assertEquals(0, child.arriveAndDeregister())
    assertState(child, 0, 0, 0)
    assertState(parent, 0, 1, 1)
  }

  /** arriveAndDeregister deregisters one party from its parent when the number
   *  of parties of root is nonzero after deregistration.
   */
  @Test def testArriveAndDeregister5(): Unit = {
    val root = new Phaser()
    val parent = new Phaser(root)
    val child = new Phaser(parent)
    assertState(root, 0, 0, 0)
    assertState(parent, 0, 0, 0)
    assertState(child, 0, 0, 0)
    assertEquals(0, child.register())
    assertState(root, 0, 1, 1)
    assertState(parent, 0, 1, 1)
    assertState(child, 0, 1, 1)
    assertEquals(0, child.arriveAndDeregister())
    assertTerminated(child, 1)
    assertTerminated(parent, 1)
    assertTerminated(root, 1)
  }

  /** arriveAndDeregister returns the phase in which it leaves the phaser in
   *  after deregistration
   */
  @Test def testArriveAndDeregister6(): Unit = {
    val phaser = new Phaser(2)
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertEquals(0, phaser.arrive())
      }
    })
    assertEquals("a1", 1, phaser.arriveAndAwaitAdvance())
    assertState(phaser, 1, 2, 2)
    assertEquals("a2", 1, phaser.arriveAndDeregister())
    assertState(phaser, 1, 1, 1)
    assertEquals("a3", 1, phaser.arriveAndDeregister())
    assertTerminated(phaser, 2)
    awaitTermination(t)
  }

  /** awaitAdvance succeeds upon advance
   */
  @Test def testAwaitAdvance1(): Unit = {
    val phaser = new Phaser(1)
    assertEquals(0, phaser.arrive())
    assertEquals(1, phaser.awaitAdvance(0))
  }

  /** awaitAdvance with a negative parameter will return without affecting the
   *  phaser
   */
  @Test def testAwaitAdvance2(): Unit = {
    val phaser = new Phaser()
    assertTrue(phaser.awaitAdvance(-1) < 0)
    assertState(phaser, 0, 0, 0)
  }

  /** awaitAdvanceInterruptibly blocks interruptibly
   */
  @Test def testAwaitAdvanceInterruptibly_Interruptible(): Unit = {
    val phaser = new Phaser(1)
    val pleaseInterrupt = new CountDownLatch(2)

    val t1 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        Thread.currentThread().interrupt()

        assertThrows(
          "a1",
          classOf[InterruptedException],
          phaser.awaitAdvanceInterruptibly(0)
        )

        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()

        assertThrows(
          "a2",
          classOf[InterruptedException],
          phaser.awaitAdvanceInterruptibly(0)
        )

        assertFalse(Thread.interrupted())
      }
    })

    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        Thread.currentThread().interrupt()

        assertThrows(
          "a3",
          classOf[InterruptedException],
          phaser.awaitAdvanceInterruptibly(0, randomTimeout(), randomTimeUnit())
        )
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()

        assertThrows(
          "a4",
          classOf[InterruptedException],
          phaser.awaitAdvanceInterruptibly(0, LONGER_DELAY_MS, MILLISECONDS)
        )

        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    assertState(phaser, 0, 1, 1)
    if (randomBoolean())
      assertThreadBlocks(t1, Thread.State.WAITING)
    if (randomBoolean())
      assertThreadBlocks(t2, Thread.State.TIMED_WAITING)
    t1.interrupt()
    t2.interrupt()
    awaitTermination(t1)
    awaitTermination(t2)
    assertState(phaser, 0, 1, 1)
    assertEquals(0, phaser.arrive())
    assertState(phaser, 1, 1, 1)
  }

  /** awaitAdvance continues waiting if interrupted before waiting
   */
  @Test def testAwaitAdvanceAfterInterrupt(): Unit = {
    val phaser = new Phaser()
    assertEquals(0, phaser.register())
    val pleaseArrive = new CountDownLatch(1)

    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        Thread.currentThread().interrupt()
        assertEquals(0, phaser.register())
        assertEquals(0, phaser.arrive())
        pleaseArrive.countDown()
        assertTrue(Thread.currentThread().isInterrupted())
        assertEquals(1, phaser.awaitAdvance(0))
        assertTrue(Thread.interrupted())
      }
    })

    await(pleaseArrive)
    assertThreadBlocks(t, Thread.State.WAITING)
    assertEquals(0, phaser.arrive())
    awaitTermination(t)

    Thread.currentThread().interrupt()
    assertEquals(1, phaser.awaitAdvance(0))
    assertTrue(Thread.interrupted())
  }

  /** awaitAdvance continues waiting if interrupted while waiting
   */
  @Test def testAwaitAdvanceBeforeInterrupt(): Unit = {
    val phaser = new Phaser()
    assertEquals(0, phaser.register())
    val pleaseArrive = new CountDownLatch(1)

    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertEquals(0, phaser.register())
        assertEquals(0, phaser.arrive())
        assertFalse(Thread.currentThread().isInterrupted())
        pleaseArrive.countDown()
        assertEquals(1, phaser.awaitAdvance(0))
        assertTrue(Thread.interrupted())
      }
    })

    await(pleaseArrive)
    assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    assertEquals(0, phaser.arrive())
    awaitTermination(t)

    Thread.currentThread().interrupt()
    assertEquals(1, phaser.awaitAdvance(0))
    assertTrue(Thread.interrupted())
  }

  /** arriveAndAwaitAdvance continues waiting if interrupted before waiting
   */
  @Test def testArriveAndAwaitAdvanceAfterInterrupt(): Unit = {
    val phaser = new Phaser()
    assertEquals(0, phaser.register())
    val pleaseArrive = new CountDownLatch(1)

    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        Thread.currentThread().interrupt()
        assertEquals(0, phaser.register())
        pleaseArrive.countDown()
        assertTrue(Thread.currentThread().isInterrupted())
        assertEquals(1, phaser.arriveAndAwaitAdvance())
        assertTrue(Thread.interrupted())
      }
    })

    await(pleaseArrive)
    assertThreadBlocks(t, Thread.State.WAITING)
    Thread.currentThread().interrupt()
    assertEquals(1, phaser.arriveAndAwaitAdvance())
    assertTrue(Thread.interrupted())
    awaitTermination(t)
  }

  /** arriveAndAwaitAdvance continues waiting if interrupted while waiting
   */
  @Test def testArriveAndAwaitAdvanceBeforeInterrupt(): Unit = {
    val phaser = new Phaser()
    assertEquals(0, phaser.register())
    val pleaseInterrupt = new CountDownLatch(1)

    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertEquals(0, phaser.register())
        assertFalse(Thread.currentThread().isInterrupted())
        pleaseInterrupt.countDown()
        assertEquals(1, phaser.arriveAndAwaitAdvance())
        assertTrue(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    Thread.currentThread().interrupt()
    assertEquals(1, phaser.arriveAndAwaitAdvance())
    assertTrue(Thread.interrupted())
    awaitTermination(t)
  }

  /** awaitAdvance atomically waits for all parties within the same phase to
   *  complete before continuing
   */
  @Test def testAwaitAdvance4(): Unit = {
    val phaser = new Phaser(4)
    val count = new AtomicInteger(0)
    val threads = new ArrayList[Thread]()
    for (i <- 0 until 4)
      threads.add(newStartedThread(new CheckedRunnable() {
        def realRun(): Unit = {
          for (k <- 0 until 3) {
            assertEquals(2 * k + 1, phaser.arriveAndAwaitAdvance())
            count.incrementAndGet()
            assertEquals(2 * k + 1, phaser.arrive())
            assertEquals(2 * k + 2, phaser.awaitAdvance(2 * k + 1))
            assertEquals(4 * (k + 1), count.get())
          }
        }
      }))

    threads.forEach(awaitTermination(_))
  }

  /** awaitAdvance returns the current phase
   */
  @Test def testAwaitAdvance5(): Unit = {
    val phaser = new Phaser(1)
    assertEquals(1, phaser.awaitAdvance(phaser.arrive()))
    assertEquals(1, phaser.getPhase())
    assertEquals(1, phaser.register())
    val threads = new ArrayList[Thread]()
    for (i <- 0 until 8) {
      val latch = new CountDownLatch(1)
      val goesFirst = ((i & 1) == 0)
      threads.add(newStartedThread(new CheckedRunnable() {
        def realRun(): Unit = {
          if (goesFirst)
            latch.countDown()
          else
            await(latch)
          phaser.arrive()
        }
      }))
      if (goesFirst)
        await(latch)
      else
        latch.countDown()
      assertEquals(i + 2, phaser.awaitAdvance(phaser.arrive()))
      assertEquals(i + 2, phaser.getPhase())
    }
    threads.forEach(awaitTermination(_))

  }

  /** awaitAdvance returns the current phase in child phasers
   */
  @Test def testAwaitAdvanceTieredPhaser(): Unit = {
    val parent = new Phaser()
    val zeroPartyChildren = new ArrayList[Phaser](3)
    val onePartyChildren = new ArrayList[Phaser](3)
    for (i <- 0 until 3) {
      zeroPartyChildren.add(new Phaser(parent, 0))
      onePartyChildren.add(new Phaser(parent, 1))
    }

    val phasers = new ArrayList[Phaser]()
    phasers.addAll(zeroPartyChildren)
    phasers.addAll(onePartyChildren)
    phasers.add(parent)

    phasers.forEach(phaser => {
      assertEquals(-42, phaser.awaitAdvance(-42))
      assertEquals(-42, phaser.awaitAdvanceInterruptibly(-42))
      assertEquals(
        -42,
        phaser.awaitAdvanceInterruptibly(-42, MEDIUM_DELAY_MS, MILLISECONDS)
      )
    })

    onePartyChildren.forEach(child => assertEquals(0, child.arrive()))

    phasers.forEach(phaser => {
      assertEquals(-42, phaser.awaitAdvance(-42))
      assertEquals(-42, phaser.awaitAdvanceInterruptibly(-42))
      assertEquals(
        -42,
        phaser.awaitAdvanceInterruptibly(-42, MEDIUM_DELAY_MS, MILLISECONDS)
      )
      assertEquals(1, phaser.awaitAdvance(0))
      assertEquals(1, phaser.awaitAdvanceInterruptibly(0))
      assertEquals(
        1,
        phaser.awaitAdvanceInterruptibly(0, MEDIUM_DELAY_MS, MILLISECONDS)
      )
    })

    onePartyChildren.forEach(child => assertEquals(1, child.arrive()))

    phasers.forEach(phaser => {
      assertEquals(-42, phaser.awaitAdvance(-42))
      assertEquals(-42, phaser.awaitAdvanceInterruptibly(-42))
      assertEquals(
        -42,
        phaser.awaitAdvanceInterruptibly(-42, MEDIUM_DELAY_MS, MILLISECONDS)
      )
      assertEquals(2, phaser.awaitAdvance(0))
      assertEquals(2, phaser.awaitAdvanceInterruptibly(0))
      assertEquals(
        2,
        phaser.awaitAdvanceInterruptibly(0, MEDIUM_DELAY_MS, MILLISECONDS)
      )
      assertEquals(2, phaser.awaitAdvance(1))
      assertEquals(2, phaser.awaitAdvanceInterruptibly(1))
      assertEquals(
        2,
        phaser.awaitAdvanceInterruptibly(1, MEDIUM_DELAY_MS, MILLISECONDS)
      )
    })
  }

  /** awaitAdvance returns when the phaser is externally terminated
   */
  @Test def testAwaitAdvance6(): Unit = {
    val phaser = new Phaser(3)
    val pleaseForceTermination = new CountDownLatch(2)
    val threads = new ArrayList[Thread]()

    for (i <- 0 until 2) {
      val r = new CheckedRunnable() {
        def realRun(): Unit = {
          assertEquals(0, phaser.arrive())
          pleaseForceTermination.countDown()
          assertTrue(phaser.awaitAdvance(0) < 0)
          assertTrue(phaser.isTerminated())
          assertTrue(phaser.getPhase() < 0)
          assertEquals(0, phaser.getPhase() + Integer.MIN_VALUE)
          assertEquals(3, phaser.getRegisteredParties())
        }
      }
      threads.add(newStartedThread(r))
    }
    await(pleaseForceTermination)
    phaser.forceTermination()
    assertTrue(phaser.isTerminated())
    assertEquals(0, phaser.getPhase() + Integer.MIN_VALUE)
    threads.forEach(awaitTermination(_))

    assertEquals(3, phaser.getRegisteredParties())
  }

  /** arriveAndAwaitAdvance throws IllegalStateException with no unarrived
   *  parties
   */
  @Test def testArriveAndAwaitAdvance1(): Unit = {
    val phaser = new Phaser()

    assertThrows(
      "arriveAndAwaitAdvance",
      classOf[IllegalStateException],
      phaser.arriveAndAwaitAdvance()
    )
  }

  /** arriveAndAwaitAdvance waits for all threads to arrive, the number of
   *  arrived parties is the same number that is accounted for when the main
   *  thread awaitsAdvance
   */
  @Test def testArriveAndAwaitAdvance3(): Unit = {
    val phaser = new Phaser(1)
    val THREADS = 3
    val pleaseArrive = new CountDownLatch(THREADS)
    val threads = new ArrayList[Thread]()
    for (i <- 0 until THREADS)
      threads.add(newStartedThread(new CheckedRunnable() {
        def realRun(): Unit = {
          assertEquals(0, phaser.register())
          pleaseArrive.countDown()
          assertEquals(1, phaser.arriveAndAwaitAdvance())
        }
      }))

    await(pleaseArrive)
    val startTime = System.nanoTime()
    while (phaser.getArrivedParties() < THREADS)
      Thread.`yield`()

    assertEquals(THREADS, phaser.getArrivedParties())
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    threads.forEach(thread => assertThreadBlocks(thread, Thread.State.WAITING))

    threads.forEach(thread => assertTrue(thread.isAlive()))
    assertState(phaser, 0, THREADS + 1, 1)
    phaser.arriveAndAwaitAdvance()
    threads.forEach(awaitTermination(_))
    assertState(phaser, 1, THREADS + 1, THREADS + 1)
  }
}
