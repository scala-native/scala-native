package org.scalanative.testsuite.javalib.nio.channels

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util.concurrent.{CyclicBarrier, CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import scala.scalanative.junit.utils.AssumesHelper

/** Tests demonstrating FileChannel thread-safety issues (Issue #4385).
 *
 *  Java's FileChannel contract requires thread-safety: "Channels are safe
 *  for use by multiple concurrent threads." The current Scala Native
 *  FileChannelImpl uses an unsynchronized save/restore position pattern
 *  that races under concurrent access.
 *
 *  These tests should PASS on JVM (which has proper synchronization)
 *  and are expected to FAIL on Scala Native until internal locking is added.
 */
object FileChannelConcurrencyTest {
  @BeforeClass
  def setup(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
  }

  val ITERATIONS = 500
  val TRANSFER_ITERATIONS = 100
  val TIMEOUT_MS = 30000L
}

class FileChannelConcurrencyTest {
  import FileChannelConcurrencyTest._

  // ---- Utilities ----

  private def withTemporaryDirectory(fn: Path => Unit): Unit = {
    val dir = Files.createTempDirectory("fc-conc-test")
    try fn(dir)
    finally {
      Files.walk(dir)
        .sorted(java.util.Comparator.reverseOrder())
        .forEach(p => Files.deleteIfExists(p))
    }
  }

  private def createPatternedFile(
      dir: Path,
      name: String,
      size: Int
  ): Path = {
    val f = dir.resolve(name)
    val data = Array.tabulate[Byte](size)(i => (i % 256).toByte)
    Files.write(f, data)
    f
  }

  private def startDaemonThread(r: Runnable): Thread = {
    val t = new Thread(r)
    t.setDaemon(true)
    t.start()
    t
  }

  private def rethrowIfFailed(failure: AtomicReference[Throwable]): Unit = {
    val t = failure.get()
    if (t != null)
      throw new AssertionError("Worker thread failed", t)
  }

  // =================================================================
  // Test 1: Two concurrent absolute reads corrupt channel position
  // =================================================================
  // Both threads call read(buf, pos) which saves/restores position.
  // The interleaving of save/restore across threads corrupts the
  // channel's position — it ends up at one of the read offsets
  // instead of the original known position.

  @Test
  def absoluteReadPositionRace(): Unit = {
    withTemporaryDirectory { dir =>
      val filePath = createPatternedFile(dir, "posrace.dat", 4096)
      val channel = FileChannel.open(filePath, StandardOpenOption.READ)
      try {
        val failure = new AtomicReference[Throwable](null)
        val corruptionCount = new AtomicInteger(0)
        val knownPosition = 100L

        for (_ <- 0 until ITERATIONS) {
          channel.position(knownPosition)
          val barrier = new CyclicBarrier(3)

          val t1 = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                val buf = ByteBuffer.allocate(10)
                channel.read(buf, 0L)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          val t2 = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                val buf = ByteBuffer.allocate(10)
                channel.read(buf, 2000L)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
          t1.join(TIMEOUT_MS)
          t2.join(TIMEOUT_MS)
          rethrowIfFailed(failure)

          if (channel.position() != knownPosition)
            corruptionCount.incrementAndGet()
        }

        assertEquals(
          s"Position corrupted in ${corruptionCount.get()} of $ITERATIONS iterations",
          0,
          corruptionCount.get()
        )
      } finally channel.close()
    }
  }

  // =================================================================
  // Test 2: Absolute read clobbers relative write's advancing position
  // =================================================================
  // Writer thread does sequential relative writes (position advances).
  // Reader thread does absolute reads (save/restore position).
  // Reader's restore overwrites writer's advancing position, causing
  // the writer to overwrite previously written data.

  @Test
  def absoluteReadVsRelativeWriteRace(): Unit = {
    withTemporaryDirectory { dir =>
      val filePath = createPatternedFile(dir, "rw-race.dat", 8192)
      val channel = FileChannel.open(
        filePath,
        StandardOpenOption.READ,
        StandardOpenOption.WRITE
      )
      try {
        val failure = new AtomicReference[Throwable](null)
        val corruptionCount = new AtomicInteger(0)
        val writeCount = 200
        val writeSize = 4

        channel.position(0L)
        val done = new CountDownLatch(1)

        val writer = startDaemonThread(new Runnable {
          def run(): Unit =
            try {
              for (i <- 0 until writeCount) {
                val buf = ByteBuffer.allocate(writeSize)
                buf.putInt(i)
                buf.flip()
                channel.write(buf)
              }
              done.countDown()
            } catch {
              case e: Throwable =>
                failure.compareAndSet(null, e)
                done.countDown()
            }
        })

        val reader = startDaemonThread(new Runnable {
          def run(): Unit =
            try {
              while (done.getCount() > 0) {
                val buf = ByteBuffer.allocate(10)
                channel.read(buf, 5000L)
              }
            } catch {
              case e: Throwable => failure.compareAndSet(null, e)
            }
        })

        writer.join(TIMEOUT_MS)
        done.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        reader.join(TIMEOUT_MS)
        rethrowIfFailed(failure)

        // Verify sequential data integrity
        channel.position(0L)
        val verifyBuf = ByteBuffer.allocate(writeCount * writeSize)
        channel.read(verifyBuf)
        verifyBuf.flip()

        for (i <- 0 until writeCount) {
          val value = verifyBuf.getInt()
          if (value != i)
            corruptionCount.incrementAndGet()
        }

        assertEquals(
          s"Data corrupted: ${corruptionCount.get()} of $writeCount writes were wrong",
          0,
          corruptionCount.get()
        )
      } finally channel.close()
    }
  }

  // =================================================================
  // Test 3: Two concurrent absolute writes write data to wrong offsets
  // =================================================================
  // Thread A writes 0xAA to region [0..99].
  // Thread B writes 0xBB to region [200..299].
  // Both use write(buf, pos) which save/restores position.
  // Race: Thread A's compelPosition(0) is overwritten by B's
  // compelPosition(200) before A's actual write, so A writes 0xAA
  // at offset 200 instead of 0.

  @Test
  def concurrentAbsoluteWriteDataIntegrity(): Unit = {
    withTemporaryDirectory { dir =>
      val filePath = createPatternedFile(dir, "ww-race.dat", 8192)
      val channel = FileChannel.open(
        filePath,
        StandardOpenOption.READ,
        StandardOpenOption.WRITE
      )
      try {
        val failure = new AtomicReference[Throwable](null)
        var dataCorruptions = 0
        var positionCorruptions = 0
        val knownPosition = 4096L

        val regionAOffset = 0L
        val regionBOffset = 200L
        val regionSize = 100
        val patternA: Byte = 0xAA.toByte
        val patternB: Byte = 0xBB.toByte

        for (_ <- 0 until ITERATIONS) {
          channel.position(knownPosition)
          val barrier = new CyclicBarrier(3)

          val t1 = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                val buf = ByteBuffer.allocate(regionSize)
                java.util.Arrays.fill(buf.array(), patternA)
                channel.write(buf, regionAOffset)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          val t2 = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                val buf = ByteBuffer.allocate(regionSize)
                java.util.Arrays.fill(buf.array(), patternB)
                channel.write(buf, regionBOffset)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
          t1.join(TIMEOUT_MS)
          t2.join(TIMEOUT_MS)
          rethrowIfFailed(failure)

          if (channel.position() != knownPosition)
            positionCorruptions += 1

          // Check region A: should be all 0xAA
          val checkA = ByteBuffer.allocate(regionSize)
          channel.read(checkA, regionAOffset)
          checkA.flip()
          var regionAOk = true
          while (checkA.hasRemaining && regionAOk) {
            if (checkA.get() != patternA) regionAOk = false
          }

          // Check region B: should be all 0xBB
          val checkB = ByteBuffer.allocate(regionSize)
          channel.read(checkB, regionBOffset)
          checkB.flip()
          var regionBOk = true
          while (checkB.hasRemaining && regionBOk) {
            if (checkB.get() != patternB) regionBOk = false
          }

          if (!regionAOk || !regionBOk) dataCorruptions += 1
        }

        assertEquals(
          s"Position corrupted in $positionCorruptions of $ITERATIONS iterations",
          0,
          positionCorruptions
        )
        assertEquals(
          s"Data corrupted in $dataCorruptions of $ITERATIONS iterations",
          0,
          dataCorruptions
        )
      } finally channel.close()
    }
  }

  // =================================================================
  // Test 4: transferTo exposes intermediate position to observers
  // =================================================================
  // transferTo saves position, moves to transfer offset, reads in a
  // loop, then restores in finally. During the loop, another thread
  // calling position() sees the transfer offset instead of the
  // logical channel position.

  @Test
  def transferToLeaksIntermediatePosition(): Unit = {
    withTemporaryDirectory { dir =>
      val srcPath = createPatternedFile(dir, "transfer-src.dat", 8192)
      val dstPath = dir.resolve("transfer-dst.dat")
      Files.write(dstPath, Array.emptyByteArray)

      val srcChannel = FileChannel.open(srcPath, StandardOpenOption.READ)
      try {
        val failure = new AtomicReference[Throwable](null)
        var leakedPositionCount = 0
        val knownPosition = 4000L

        for (_ <- 0 until TRANSFER_ITERATIONS) {
          srcChannel.position(knownPosition)

          val dstChannel = FileChannel.open(
            dstPath,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
          )
          try {
            val barrier = new CyclicBarrier(3)
            val transferDone = new CountDownLatch(1)
            val observedPositions =
              new java.util.concurrent.ConcurrentLinkedQueue[Long]()

            val transferThread = startDaemonThread(new Runnable {
              def run(): Unit =
                try {
                  barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                  srcChannel.transferTo(0L, 4096L, dstChannel)
                  transferDone.countDown()
                } catch {
                  case e: Throwable =>
                    failure.compareAndSet(null, e)
                    transferDone.countDown()
                }
            })

            val observer = startDaemonThread(new Runnable {
              def run(): Unit =
                try {
                  barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                  while (transferDone.getCount() > 0) {
                    observedPositions.add(srcChannel.position())
                  }
                } catch {
                  case e: Throwable => failure.compareAndSet(null, e)
                }
            })

            barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            transferThread.join(TIMEOUT_MS)
            observer.join(TIMEOUT_MS)
            rethrowIfFailed(failure)

            val iter = observedPositions.iterator()
            while (iter.hasNext) {
              val p = iter.next()
              if (p != knownPosition)
                leakedPositionCount += 1
            }
          } finally dstChannel.close()
        }

        assertEquals(
          s"Leaked intermediate position $leakedPositionCount times",
          0,
          leakedPositionCount
        )
      } finally srcChannel.close()
    }
  }

  // =================================================================
  // Test 5: truncate + concurrent position setter race
  // =================================================================
  // truncate reads currentPosition, then conditionally repositions
  // if currentPosition > newSize. Between the read and the
  // conditional reposition, another thread can change position,
  // and truncate operates on stale data.

  @Test
  def truncatePositionRace(): Unit = {
    withTemporaryDirectory { dir =>
      val failure = new AtomicReference[Throwable](null)
      var corruptionCount = 0

      for (_ <- 0 until ITERATIONS) {
        val filePath = createPatternedFile(dir, "truncate-race.dat", 4096)
        val channel = FileChannel.open(
          filePath,
          StandardOpenOption.READ,
          StandardOpenOption.WRITE
        )
        try {
          channel.position(4000L)
          val barrier = new CyclicBarrier(3)

          val truncater = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                channel.truncate(2048L)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          val positionSetter = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                channel.position(500L)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
          truncater.join(TIMEOUT_MS)
          positionSetter.join(TIMEOUT_MS)
          rethrowIfFailed(failure)

          // After both complete, position should be either:
          // - 2048 (truncate ran last, original pos 4000 > 2048)
          // - 500 (position setter ran last, 500 < 2048)
          val pos = channel.position()
          if (pos != 2048L && pos != 500L)
            corruptionCount += 1
        } finally {
          channel.close()
          Files.deleteIfExists(filePath)
        }
      }

      rethrowIfFailed(failure)
      assertEquals(
        s"Position corrupted in $corruptionCount of $ITERATIONS iterations",
        0,
        corruptionCount
      )
    }
  }

  // =================================================================
  // Test 6: transferFrom + absolute read both save/restore position
  // =================================================================
  // Both transferFrom and read(buf, pos) independently save/restore
  // the channel's position using the same unsynchronized fd offset.
  // Their restores conflict, corrupting the final position.

  @Test
  def transferFromVsAbsoluteReadRace(): Unit = {
    withTemporaryDirectory { dir =>
      val filePath = createPatternedFile(dir, "xfer-read-race.dat", 8192)
      val srcPath = createPatternedFile(dir, "xfer-src.dat", 2048)
      val failure = new AtomicReference[Throwable](null)
      var corruptionCount = 0
      val knownPosition = 6000L

      for (_ <- 0 until TRANSFER_ITERATIONS) {
        val channel = FileChannel.open(
          filePath,
          StandardOpenOption.READ,
          StandardOpenOption.WRITE
        )
        val srcChannel = FileChannel.open(srcPath, StandardOpenOption.READ)
        try {
          channel.position(knownPosition)
          val barrier = new CyclicBarrier(3)

          val transferThread = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                srcChannel.position(0L)
                channel.transferFrom(srcChannel, 0L, 1024L)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          val readerThread = startDaemonThread(new Runnable {
            def run(): Unit =
              try {
                barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                val buf = ByteBuffer.allocate(100)
                channel.read(buf, 4000L)
              } catch {
                case e: Throwable => failure.compareAndSet(null, e)
              }
          })

          barrier.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
          transferThread.join(TIMEOUT_MS)
          readerThread.join(TIMEOUT_MS)
          rethrowIfFailed(failure)

          if (channel.position() != knownPosition)
            corruptionCount += 1
        } finally {
          channel.close()
          srcChannel.close()
        }
      }

      rethrowIfFailed(failure)
      assertEquals(
        s"Position corrupted in $corruptionCount of $TRANSFER_ITERATIONS iterations",
        0,
        corruptionCount
      )
    }
  }
}
