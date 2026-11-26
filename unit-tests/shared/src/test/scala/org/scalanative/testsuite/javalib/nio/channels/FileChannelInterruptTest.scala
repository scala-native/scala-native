package org.scalanative.testsuite.javalib.nio.channels

import java.io._
import java.nio.ByteBuffer
import java.nio.channels._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.javalib.io.IoTestHelpers
import org.scalanative.testsuite.utils.Platform

import scala.scalanative.junit.utils.AssumesHelper._

// Issue #5260: interruptible and asynchronously closeable FileChannel.
object FileChannelInterruptTest {
  final val BlockMillis = 300L
  final val JoinMillis = 5000L

  def withTemporaryDirectory(fn: Path => Unit): Unit =
    IoTestHelpers.withTemporaryDirectory(dir => fn(dir.toPath()))

  def daemon(r: Runnable): Thread = {
    val t = new Thread(r)
    t.setDaemon(true)
    t.start()
    t
  }

  // Runs body on a fresh thread, so the test thread's interrupt flag stays clear.
  def inThread(body: => Unit): Unit = {
    assumeMultithreadingIsEnabled()
    val failure = new AtomicReference[Throwable]()
    val t = daemon(() =>
      try body
      catch { case e: Throwable => failure.set(e) }
    )
    t.join(4 * JoinMillis)
    assertFalse("test body timed out", t.isAlive())
    if (failure.get() != null) throw failure.get()
  }

  def mkfifo(dir: Path, name: String = "fifo"): Path = {
    val fifo = dir.resolve(name)
    val p = new ProcessBuilder("mkfifo", fifo.toString()).start()
    assertTrue("mkfifo timed out", p.waitFor(10, TimeUnit.SECONDS))
    assertEquals("mkfifo exit code", 0, p.exitValue())
    fifo
  }

  // Writes one byte into the FIFO from another process.
  def feed(fifo: Path): Process =
    new ProcessBuilder("sh", "-c", s"printf x > '$fifo'").start()

  // Reads the FIFO from another process until killed.
  def drain(fifo: Path): Process =
    new ProcessBuilder("cat", fifo.toString())
      .redirectOutput(ProcessBuilder.Redirect.DISCARD)
      .start()

  final class Outcome {
    @volatile var result: Any = _
    @volatile var error: Throwable = _
    @volatile var interrupted = false
    @volatile var actionMillis = -1L
    @volatile var finished = false
    // Finished before blocked() had to unblock it.
    @volatile var woken = false

    def capture(op: => Any): Unit = {
      try result = op
      catch { case e: Throwable => error = e }
      interrupted = Thread.currentThread().isInterrupted()
      finished = true
    }

    override def toString(): String =
      if (!finished) "still blocked"
      else if (error != null) s"threw $error; interrupted=$interrupted"
      else s"returned $result; interrupted=$interrupted"
  }

  /* Runs op on a thread, lets it block, then runs action. If op is still
   * blocked after a bounded wait, unblock frees it: no test can hang.
   */
  def blocked(
      op: => Any,
      action: Thread => Unit,
      unblock: () => Process,
      startThread: Runnable => Thread = daemon(_)
  ): Outcome = {
    val out = new Outcome
    val started = new CountDownLatch(1)
    val t = startThread { () =>
      started.countDown()
      out.capture(op)
    }
    assertTrue(
      "thread started",
      started.await(JoinMillis, TimeUnit.MILLISECONDS)
    )
    Thread.sleep(BlockMillis)
    assertFalse(s"operation did not block: $out", out.finished)
    val start = System.nanoTime()
    val actor = daemon(() => action(t))
    actor.join(JoinMillis)
    if (!actor.isAlive())
      out.actionMillis = (System.nanoTime() - start) / 1000000L
    t.join(JoinMillis)
    out.woken = out.finished
    if (t.isAlive() || actor.isAlive()) {
      val helper = unblock()
      try {
        t.join(JoinMillis)
        actor.join(JoinMillis)
      } finally helper.destroyForcibly()
    }
    out
  }

  def assertPrompt(out: Outcome): Unit =
    assertTrue(s"action must return promptly: $out", out.actionMillis >= 0)

  // The action itself ended the operation, not the unblock fallback.
  def assertWoken(out: Outcome): Unit = {
    assertPrompt(out)
    assertTrue(s"action must wake the operation: $out", out.woken)
  }

  def assertThrew[T <: Throwable](
      expected: Class[T],
      out: Outcome
  ): Unit = {
    assertTrue("operation still blocked", out.finished)
    assertTrue(
      s"expected ${expected.getSimpleName()}: $out",
      out.error != null && expected.isInstance(out.error)
    )
    // ClosedByInterruptException extends AsynchronousCloseException.
    if (expected == classOf[AsynchronousCloseException])
      assertFalse(
        s"unexpected ClosedByInterruptException: $out",
        out.error.isInstanceOf[ClosedByInterruptException]
      )
  }

  def assumeFifos(): Unit = {
    assumeFalse("FIFOs are not available on Windows", Platform.isWindows)
    assumeMultithreadingIsEnabled()
    // mkfifo, printf and cat run as subprocesses.
    assumeSupportsSubprocesses()
  }

  def assumeFifoTransferSource(): Unit = {
    assumeFifos()
    assumeFalse(
      "JDK < 21 rejects a FIFO source: Illegal seek",
      Platform.executingInJVMOnLowerThanJDK(21)
    )
  }

  // Also run on virtual threads by FileChannelVirtualThreadTest.
  def checkCloseWakesRead(start: Runnable => Thread): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      val out = blocked(
        ch.read(ByteBuffer.allocate(1)),
        _ => ch.close(),
        () => feed(fifo),
        start
      )
      assertWoken(out)
      assertThrew(classOf[AsynchronousCloseException], out)
      assertFalse("interrupted", out.interrupted)
    }
  }

  def checkInterruptWakesRead(start: Runnable => Thread): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          ch.read(ByteBuffer.allocate(1)),
          _.interrupt(),
          () => feed(fifo),
          start
        )
        assertWoken(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }
  }

  def checkInterruptWakesWrite(start: Runnable => Thread): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          ch.write(ByteBuffer.allocate(1 << 20)),
          _.interrupt(),
          () => drain(fifo),
          start
        )
        assertWoken(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }
  }
}

class FileChannelInterruptTest {
  import FileChannelInterruptTest._

  private val opsOnRegularFile: List[(String, FileChannel => Any)] = List(
    ("read(buf)", _.read(ByteBuffer.allocate(4))),
    ("read(buf,pos)", _.read(ByteBuffer.allocate(4), 0L)),
    ("read(bufs)", _.read(Array(ByteBuffer.allocate(4)))),
    ("write(buf)", _.write(ByteBuffer.allocate(4))),
    ("write(buf,pos)", _.write(ByteBuffer.allocate(4), 0L)),
    ("write(bufs)", _.write(Array(ByteBuffer.allocate(4)))),
    ("position()", _.position()),
    ("position(5)", _.position(5L)),
    ("size()", _.size()),
    ("force(true)", _.force(true)),
    ("truncate(50)", _.truncate(50L)),
    (
      "transferTo",
      _.transferTo(0L, 4L, Channels.newChannel(new ByteArrayOutputStream()))
    ),
    (
      "transferFrom",
      _.transferFrom(
        Channels.newChannel(new ByteArrayInputStream(new Array[Byte](4))),
        0L,
        4L
      )
    ),
    ("map", _.map(FileChannel.MapMode.READ_ONLY, 0L, 4L))
  )

  @Test def interruptFlagSetBeforeCallClosesChannel(): Unit =
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      inThread {
        for ((name, op) <- opsOnRegularFile) {
          Files.write(f, new Array[Byte](100))
          val ch = FileChannel.open(f, READ, WRITE)
          try {
            Thread.currentThread().interrupt()
            val res =
              try s"returned ${op(ch)}"
              catch { case e: Throwable => s"threw $e" }
            val flag = Thread.interrupted()
            assertTrue(
              s"$name: $res",
              res.startsWith("threw java.nio.channels.ClosedByInterrupt")
            )
            assertTrue(s"$name: interrupt status kept", flag)
            assertFalse(s"$name: channel closed", ch.isOpen())
            val again =
              try { op(ch); "returned" }
              catch { case e: Throwable => e.getClass().getSimpleName() }
            assertEquals(s"$name: next call", "ClosedChannelException", again)
          } finally {
            Thread.interrupted()
            ch.close()
          }
        }
      }
    }

  @Test def openAndCloseIgnoreInterruptFlag(): Unit =
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](10))
      inThread {
        Thread.currentThread().interrupt()
        val ch = FileChannel.open(f, READ)
        assertTrue("open", ch.isOpen())
        ch.close()
        assertTrue("interrupt status kept", Thread.interrupted())
      }
    }

  @Test def streamChannelInterruptClosesStream(): Unit =
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](10))
      inThread {
        val in = new FileInputStream(f.toFile())
        try {
          Thread.currentThread().interrupt()
          try {
            in.getChannel().read(ByteBuffer.allocate(1))
            fail("expected ClosedByInterruptException")
          } catch { case _: ClosedByInterruptException => () }
          assertTrue("interrupt status kept", Thread.interrupted())
          try { in.read(); fail("stream must be closed") }
          catch { case _: IOException => () }
        } finally in.close()

        val raf = new RandomAccessFile(f.toFile(), "rw")
        try {
          Thread.currentThread().interrupt()
          try {
            raf.getChannel().position()
            fail("expected ClosedByInterruptException")
          } catch { case _: ClosedByInterruptException => () }
          assertTrue("interrupt status kept", Thread.interrupted())
          try { raf.read(); fail("stream must be closed") }
          catch { case _: IOException => () }
        } finally raf.close()
      }
    }

  @Test def streamsIgnoreInterruptFlag(): Unit =
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](10))
      inThread {
        def check(name: String)(body: => Any): Unit = {
          Thread.currentThread().interrupt()
          try body
          catch {
            case e: Throwable =>
              Thread.interrupted()
              throw new AssertionError(s"$name threw $e", e)
          }
          assertTrue(s"$name: interrupt status kept", Thread.interrupted())
        }
        check("FileInputStream.read") {
          val in = new FileInputStream(f.toFile())
          try assertEquals(0, in.read())
          finally in.close()
        }
        check("FileOutputStream.write") {
          val out = new FileOutputStream(f.toFile(), true)
          try out.write(1)
          finally out.close()
        }
        check("RandomAccessFile") {
          val raf = new RandomAccessFile(f.toFile(), "rw")
          try {
            assertEquals(0, raf.read())
            raf.seek(2)
            assertEquals(2L, raf.getFilePointer())
            raf.write(7)
            raf.setLength(5)
            assertEquals(5L, raf.length())
            assertEquals(1, raf.skipBytes(1))
          } finally raf.close()
        }
        check("Files.newInputStream") {
          val in = Files.newInputStream(f)
          try assertEquals(0, in.read())
          finally in.close()
        }
        check("Files.newOutputStream") {
          val out = Files.newOutputStream(f, StandardOpenOption.APPEND)
          try out.write(1)
          finally out.close()
        }
        check("Files.readAllBytes")(Files.readAllBytes(f))
        check("Files.write")(Files.write(f, new Array[Byte](10)))
        check("Files.newBufferedReader") {
          val r = Files.newBufferedReader(f)
          try assertEquals(0, r.read())
          finally r.close()
        }
        check("System.out") {
          System.out.print("")
          System.out.flush()
          assertFalse("System.out error", System.out.checkError())
        }
      }
    }

  @Test def closeWakesBlockedRead(): Unit = checkCloseWakesRead(daemon(_))

  @Test def interruptWakesBlockedRead(): Unit =
    checkInterruptWakesRead(daemon(_))

  @Test def closeWakesBlockedWrite(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      val size = 1 << 20
      val out = blocked(
        ch.write(ByteBuffer.allocate(size)),
        _ => ch.close(),
        () => drain(fifo)
      )
      assertWoken(out)
      // A short count or AsynchronousCloseException; both are allowed.
      if (out.error != null)
        assertThrew(classOf[AsynchronousCloseException], out)
      else
        assertTrue(s"short count: $out", out.result.asInstanceOf[Int] < size)
    }
  }

  @Test def interruptWakesBlockedWrite(): Unit =
    checkInterruptWakesWrite(daemon(_))

  @Test def closeWakesTransferFromBlockedSource(): Unit = {
    assumeFifoTransferSource()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](10))
      val src = FileChannel.open(fifo, READ, WRITE)
      val dst = FileChannel.open(f, WRITE)
      try {
        val out = blocked(
          dst.transferFrom(src, 0L, 10L),
          _ => src.close(),
          () => feed(fifo)
        )
        assertWoken(out)
        assertThrew(classOf[AsynchronousCloseException], out)
      } finally dst.close()
    }
  }

  @Test def interruptWakesTransferFromBlockedSource(): Unit = {
    assumeFifoTransferSource()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](10))
      val src = FileChannel.open(fifo, READ, WRITE)
      val dst = FileChannel.open(f, WRITE)
      try {
        val out = blocked(
          dst.transferFrom(src, 0L, 10L),
          _.interrupt(),
          () => feed(fifo)
        )
        assertWoken(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("source closed", src.isOpen())
        assertFalse("destination closed", dst.isOpen())
      } finally {
        dst.close()
        src.close()
      }
    }
  }

  @Test def interruptWakesTransferToBlockedTarget(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](1 << 16))
      val src = FileChannel.open(f, READ)
      val dst = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          (0 until 64).foreach(_ => src.transferTo(0L, 1L << 16, dst)),
          _.interrupt(),
          () => drain(fifo)
        )
        assertWoken(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("source closed", src.isOpen())
        assertFalse("destination closed", dst.isOpen())
      } finally {
        dst.close()
        src.close()
      }
    }
  }

  @Test def closeWakesAllBlockedReaders(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      val second = new Outcome
      val t2 = daemon(() => second.capture(ch.read(ByteBuffer.allocate(1))))
      val out = blocked(
        ch.read(ByteBuffer.allocate(1)),
        _ => ch.close(),
        () => feed(fifo)
      )
      t2.join(JoinMillis)
      val secondWoken = !t2.isAlive()
      if (t2.isAlive()) {
        val p = feed(fifo)
        try t2.join(JoinMillis)
        finally p.destroyForcibly()
      }
      assertWoken(out)
      assertThrew(classOf[AsynchronousCloseException], out)
      assertTrue(s"second reader woken: $second", secondWoken)
      assertThrew(classOf[AsynchronousCloseException], second)
    }
  }

  @Test def closeOfAnotherChannelDoesNotWakeRead(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      val other = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          ch.read(ByteBuffer.allocate(1)),
          _ => other.close(),
          () => feed(fifo)
        )
        assertPrompt(out)
        assertTrue(s"read completes after feed: $out", out.finished)
        assertEquals(s"$out", 1, out.result)
        assertTrue("channel open", ch.isOpen())
      } finally ch.close()
    }
  }

  // Interrupting a stream read neither wakes nor closes it.
  @Test def interruptDoesNotWakeBlockedStreamRead(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val raf = new RandomAccessFile(fifo.toFile(), "rw")
      try {
        val out = blocked(raf.read(), _.interrupt(), () => feed(fifo))
        assertTrue(s"read completes after feed: $out", out.finished)
        assertEquals(s"$out", 'x'.toInt, out.result)
        assertTrue("interrupt status kept", out.interrupted)
        assertTrue("channel open", raf.getChannel().isOpen())
      } finally raf.close()
    }
  }

  @Test def closeDuringBlockedStreamReadDefersClose(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val raf = new RandomAccessFile(fifo.toFile(), "rw")
      val out = blocked(raf.read(), _ => raf.close(), () => feed(fifo))
      // The JDK's close() waits for the read on macOS.
      if (!(Platform.executingInJVM && Platform.isMacOs)) assertPrompt(out)
      assertTrue(s"read completes after feed: $out", out.finished)
      assertEquals(s"$out", 'x'.toInt, out.result)
      try { raf.read(); fail("stream must be closed") }
      catch { case _: IOException => () }
    }
  }

  @Test def streamCloseWakesBlockedChannelRead(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val raf = new RandomAccessFile(fifo.toFile(), "rw")
      val ch = raf.getChannel()
      val out = blocked(
        ch.read(ByteBuffer.allocate(1)),
        _ => raf.close(),
        () => feed(fifo)
      )
      assertWoken(out)
      assertThrew(classOf[AsynchronousCloseException], out)
    }
  }

  // A channel operation queued behind a blocked stream read is woken.
  @Test def closeWakesChannelReadWaitingForStreamRead(): Unit = {
    assumeFifos()
    assumeFalse(
      "the JDK does not order stream and channel reads",
      Platform.executingInJVM
    )
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val raf = new RandomAccessFile(fifo.toFile(), "rw")
      val ch = raf.getChannel()
      val streamRead = new Outcome
      val reader = daemon(() => streamRead.capture(raf.read()))
      Thread.sleep(BlockMillis) // the stream read takes the position lock
      try {
        val out = blocked(
          ch.read(ByteBuffer.allocate(1)),
          _ => ch.close(),
          () => feed(fifo)
        )
        assertWoken(out)
        assertThrew(classOf[AsynchronousCloseException], out)
      } finally {
        reader.join(JoinMillis)
        if (reader.isAlive()) {
          val p = feed(fifo)
          try reader.join(JoinMillis)
          finally p.destroyForcibly()
        }
      }
      // The stream read locked first, so it got the byte.
      assertEquals(s"stream read: $streamRead", 'x'.toInt, streamRead.result)
    }
  }

  // Positional I/O fails at once on a FIFO, even through a stream's fd.
  @Test def positionalReadOnStreamFifoFailsPromptly(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val raf = new RandomAccessFile(fifo.toFile(), "rw")
      try {
        val out = new Outcome
        val t = daemon { () =>
          out.capture(raf.getChannel().read(ByteBuffer.allocate(1), 0L))
        }
        t.join(JoinMillis)
        if (t.isAlive()) {
          val p = feed(fifo)
          try t.join(JoinMillis)
          finally p.destroyForcibly()
          fail(s"positional read blocked: $out")
        }
        assertTrue(
          s"expected IOException: $out",
          out.error.isInstanceOf[IOException] &&
            !out.error.isInstanceOf[ClosedChannelException]
        )
      } finally raf.close()
    }
  }

  // Like JDK 25: a pending interrupt closes both channels of a transfer.
  @Test def interruptFlagSetBeforeTransferClosesBothChannels(): Unit = {
    assumeFalse(
      "older JDKs close only one channel",
      Platform.executingInJVMOnLowerThanJDK(25)
    )
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      val g = dir.resolve("g")
      Files.write(f, new Array[Byte](10))
      Files.write(g, new Array[Byte](10))
      inThread {
        val transfers: List[(String, (FileChannel, FileChannel) => Any)] =
          List(
            ("transferFrom", (a, b) => a.transferFrom(b, 0L, 4L)),
            ("transferTo", (a, b) => a.transferTo(0L, 4L, b))
          )
        for ((name, op) <- transfers) {
          val a = FileChannel.open(f, READ, WRITE)
          val b = FileChannel.open(g, READ, WRITE)
          try {
            Thread.currentThread().interrupt()
            try {
              op(a, b); fail(s"$name: expected ClosedByInterruptException")
            } catch { case _: ClosedByInterruptException => () }
            assertTrue(s"$name: interrupt status kept", Thread.interrupted())
            assertFalse(s"$name: channel closed", a.isOpen())
            assertFalse(s"$name: other channel closed", b.isOpen())
          } finally {
            Thread.interrupted()
            a.close()
            b.close()
          }
        }
      }
    }
  }

  // Like JDK 25: an interrupt during a call on a non-interruptible peer
  // leaves both channels open and the transfer completes.
  @Test def interruptDuringPeerCallDoesNotCloseChannels(): Unit = {
    class Peer extends ReadableByteChannel with WritableByteChannel {
      @volatile var open = true
      def isOpen(): Boolean = open
      def close(): Unit = open = false
      def read(dst: ByteBuffer): Int = {
        Thread.currentThread().interrupt()
        -1
      }
      def write(src: ByteBuffer): Int = {
        Thread.currentThread().interrupt()
        val n = src.remaining()
        src.position(src.limit())
        n
      }
    }
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      Files.write(f, new Array[Byte](10))
      inThread {
        val transfers: List[(String, Long, (FileChannel, Peer) => Long)] =
          List(
            ("transferFrom", 0L, (a, p) => a.transferFrom(p, 0L, 4L)),
            ("transferTo", 4L, (a, p) => a.transferTo(0L, 4L, p))
          )
        for ((name, expected, op) <- transfers) {
          val a = FileChannel.open(f, READ, WRITE)
          val peer = new Peer
          try {
            assertEquals(s"$name: transferred", expected, op(a, peer))
            assertTrue(s"$name: interrupt status kept", Thread.interrupted())
            assertTrue(s"$name: channel open", a.isOpen())
            assertTrue(s"$name: peer open", peer.isOpen())
          } finally {
            Thread.interrupted()
            a.close()
          }
        }
      }
    }
  }

  // Like JDK 25: end() waits for the interrupting thread to finish closing.
  @Test def endWaitsForInterruptClose(): Unit = {
    assumeMultithreadingIsEnabled()
    assumeFalse(
      "older JDKs do not wait",
      Platform.executingInJVMOnLowerThanJDK(25)
    )
    val started = new CountDownLatch(1)
    val sawClosed = new CountDownLatch(1)
    val ended = new CountDownLatch(1)
    val endedDuringClose = new java.util.concurrent.atomic.AtomicBoolean()
    val closeSawOp = new java.util.concurrent.atomic.AtomicBoolean()
    class Probe extends spi.AbstractInterruptibleChannel {
      def op(): Unit = {
        begin()
        try {
          started.countDown()
          while (isOpen()) Thread.`yield`()
          sawClosed.countDown()
        } finally end(false)
      }
      // Runs on the interrupting thread, after the channel reads as closed.
      protected def implCloseChannel(): Unit = {
        closeSawOp.set(sawClosed.await(JoinMillis, TimeUnit.MILLISECONDS))
        if (ended.await(BlockMillis, TimeUnit.MILLISECONDS))
          endedDuringClose.set(true)
      }
    }
    val ch = new Probe
    val failure = new AtomicReference[Throwable]()
    val t = daemon { () =>
      try ch.op()
      catch { case e: Throwable => failure.set(e) }
      finally ended.countDown()
    }
    assertTrue("op started", started.await(JoinMillis, TimeUnit.MILLISECONDS))
    t.interrupt()
    t.join(JoinMillis)
    assertFalse("op finished", t.isAlive())
    assertTrue("op saw the close in progress", closeSawOp.get())
    assertFalse(
      "end() returned before the close finished",
      endedDuringClose.get()
    )
    assertTrue(
      s"expected ClosedByInterruptException, got ${failure.get()}",
      failure.get().isInstanceOf[ClosedByInterruptException]
    )
    assertFalse("channel closed", ch.isOpen())
  }

  // No IOException may leak from a race with close().
  @Test def closeDuringPositionalReadsOnRegularFile(): Unit = {
    assumeMultithreadingIsEnabled()
    withTemporaryDirectory { dir =>
      val f = dir.resolve("big")
      Files.write(f, new Array[Byte](1 << 20))
      for (_ <- 0 until 50) {
        val ch = FileChannel.open(f, READ)
        val out = new Outcome
        val t = daemon { () =>
          val b = ByteBuffer.allocate(4096)
          out.capture(while ({ b.clear(); ch.read(b, 0L) >= 0 }) ())
        }
        Thread.sleep(1)
        ch.close()
        t.join(JoinMillis)
        assertTrue("reader finished", out.finished)
        assertTrue(
          s"unexpected $out",
          out.error.isInstanceOf[ClosedChannelException]
        )
      }
    }
  }
}
