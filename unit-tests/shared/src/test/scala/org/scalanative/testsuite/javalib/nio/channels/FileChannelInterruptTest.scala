package org.scalanative.testsuite.javalib.nio.channels

import java.io._
import java.nio.ByteBuffer
import java.nio.channels._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path, StandardOpenOption}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

// Issue #5260: interruptible and asynchronously closeable FileChannel.
object FileChannelInterruptTest {
  final val BlockMillis = 300L
  final val JoinMillis = 5000L

  def withTemporaryDirectory(fn: Path => Unit): Unit = {
    val dir = Files.createTempDirectory("FileChannelInterruptTest")
    try fn(dir)
    finally {
      val it = Files.list(dir).iterator()
      while (it.hasNext()) Files.deleteIfExists(it.next())
      Files.deleteIfExists(dir)
    }
  }

  // Runs body on a fresh thread, so the test thread's interrupt flag stays clear.
  def inThread(body: => Unit): Unit = {
    val failure = new AtomicReference[Throwable]()
    val t = new Thread(() =>
      try body
      catch { case e: Throwable => failure.set(e) }
    )
    t.setDaemon(true)
    t.start()
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
    override def toString(): String =
      if (!finished) "still blocked"
      else if (error != null) s"threw $error; interrupted=$interrupted"
      else s"returned $result; interrupted=$interrupted"
  }

  /* Starts op on a thread, lets it block, then runs action from another
   * thread. If op is still blocked after the bounded wait, unblock releases
   * it, so no test can hang.
   */
  def blocked(
      op: => Any,
      action: Thread => Unit,
      unblock: () => Process,
      startThread: Runnable => Thread = { r =>
        val t = new Thread(r)
        t.setDaemon(true)
        t.start()
        t
      }
  ): Outcome = {
    val out = new Outcome
    val t = startThread { () =>
      try out.result = op
      catch { case e: Throwable => out.error = e }
      out.interrupted = Thread.currentThread().isInterrupted()
      out.finished = true
    }
    Thread.sleep(BlockMillis)
    assertFalse(s"operation did not block: $out", out.finished)
    val start = System.nanoTime()
    val actor = new Thread(() => action(t))
    actor.setDaemon(true)
    actor.start()
    actor.join(JoinMillis)
    if (!actor.isAlive())
      out.actionMillis = (System.nanoTime() - start) / 1000000L
    t.join(JoinMillis)
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

  def assertThrew[T <: Throwable](
      expected: Class[T],
      out: Outcome
  ): Unit = {
    assertTrue(s"operation still blocked", out.finished)
    assertTrue(
      s"expected ${expected.getSimpleName()}: $out",
      out.error != null && expected.isInstance(out.error)
    )
    // The more specific subclass must not be confused with the general one.
    if (expected == classOf[AsynchronousCloseException])
      assertFalse(
        s"unexpected ClosedByInterruptException: $out",
        out.error.isInstanceOf[ClosedByInterruptException]
      )
  }

  def assumeFifos(): Unit = {
    assumeFalse("FIFOs are not available on Windows", Platform.isWindows)
    assumeTrue("needs threads", Platform.isMultithreadingEnabled)
  }
}

class FileChannelInterruptTest {
  import FileChannelInterruptTest._

  private def opsOnRegularFile(
      ch: FileChannel
  ): List[(String, () => Any)] = List(
    "read(buf)" -> (() => ch.read(ByteBuffer.allocate(4))),
    "read(buf,pos)" -> (() => ch.read(ByteBuffer.allocate(4), 0L)),
    "read(bufs)" -> (() => ch.read(Array(ByteBuffer.allocate(4)))),
    "write(buf)" -> (() => ch.write(ByteBuffer.allocate(4))),
    "write(buf,pos)" -> (() => ch.write(ByteBuffer.allocate(4), 0L)),
    "write(bufs)" -> (() => ch.write(Array(ByteBuffer.allocate(4)))),
    "position()" -> (() => ch.position()),
    "position(5)" -> (() => ch.position(5L)),
    "size()" -> (() => ch.size()),
    "force(true)" -> (() => ch.force(true)),
    "truncate(50)" -> (() => ch.truncate(50L)),
    "transferTo" -> (() =>
      ch.transferTo(
        0L,
        4L,
        Channels.newChannel(new ByteArrayOutputStream())
      )
    ),
    "transferFrom" -> (() =>
      ch.transferFrom(
        Channels.newChannel(new ByteArrayInputStream(new Array[Byte](4))),
        0L,
        4L
      )
    ),
    "map" -> (() => ch.map(FileChannel.MapMode.READ_ONLY, 0L, 4L))
  )

  // D1
  @Test def interruptFlagSetBeforeCallClosesChannel(): Unit =
    withTemporaryDirectory { dir =>
      val f = dir.resolve("f")
      inThread {
        val names = opsOnRegularFile(null).map(_._1)
        for (name <- names) {
          Files.write(f, new Array[Byte](100))
          val ch = FileChannel.open(f, READ, WRITE)
          try {
            val op = opsOnRegularFile(ch).find(_._1 == name).get._2
            Thread.currentThread().interrupt()
            val res =
              try s"returned ${op()}"
              catch { case e: Throwable => s"threw $e" }
            val flag = Thread.interrupted()
            assertTrue(
              s"$name: $res",
              res.startsWith("threw java.nio.channels.ClosedByInterrupt")
            )
            assertTrue(s"$name: interrupt status kept", flag)
            assertFalse(s"$name: channel closed", ch.isOpen())
            val again =
              try { op(); "returned" }
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

  // D4
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

  // D2
  @Test def closeWakesBlockedRead(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      val out = blocked(
        ch.read(ByteBuffer.allocate(1)),
        _ => ch.close(),
        () => feed(fifo)
      )
      assertPrompt(out)
      assertThrew(classOf[AsynchronousCloseException], out)
      assertFalse("interrupted", out.interrupted)
    }
  }

  @Test def interruptWakesBlockedRead(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          ch.read(ByteBuffer.allocate(1)),
          _.interrupt(),
          () => feed(fifo)
        )
        assertPrompt(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }
  }

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
      assertPrompt(out)
      assertTrue(s"still blocked", out.finished)
      // A short count or AsynchronousCloseException; both are allowed.
      if (out.error != null)
        assertThrew(classOf[AsynchronousCloseException], out)
      else
        assertTrue(s"short count: $out", out.result.asInstanceOf[Int] < size)
    }
  }

  @Test def interruptWakesBlockedWrite(): Unit = {
    assumeFifos()
    withTemporaryDirectory { dir =>
      val fifo = mkfifo(dir)
      val ch = FileChannel.open(fifo, READ, WRITE)
      try {
        val out = blocked(
          ch.write(ByteBuffer.allocate(1 << 20)),
          _.interrupt(),
          () => drain(fifo)
        )
        assertPrompt(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("channel closed", ch.isOpen())
      } finally ch.close()
    }
  }

  @Test def closeWakesTransferFromBlockedSource(): Unit = {
    assumeFifos()
    assumeFalse(
      "JDK 17 rejects a FIFO source: Illegal seek",
      Platform.executingInJVMOnLowerThanJDK(21)
    )
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
        assertPrompt(out)
        assertThrew(classOf[AsynchronousCloseException], out)
      } finally dst.close()
    }
  }

  @Test def interruptWakesTransferFromBlockedSource(): Unit = {
    assumeFifos()
    assumeFalse(
      "JDK 17 rejects a FIFO source: Illegal seek",
      Platform.executingInJVMOnLowerThanJDK(21)
    )
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
        assertPrompt(out)
        assertThrew(classOf[ClosedByInterruptException], out)
        assertTrue("interrupt status kept", out.interrupted)
        assertFalse("source closed", src.isOpen())
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
      val t2 = new Thread(() => {
        try second.result = ch.read(ByteBuffer.allocate(1))
        catch { case e: Throwable => second.error = e }
        second.finished = true
      })
      t2.setDaemon(true)
      t2.start()
      val out = blocked(
        ch.read(ByteBuffer.allocate(1)),
        _ => ch.close(),
        () => feed(fifo)
      )
      t2.join(JoinMillis)
      if (t2.isAlive()) {
        val p = feed(fifo)
        try t2.join(JoinMillis)
        finally p.destroyForcibly()
      }
      assertPrompt(out)
      assertThrew(classOf[AsynchronousCloseException], out)
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

  // D4: interrupting a stream read neither wakes nor closes it.
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

  // D4b
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
      assertPrompt(out)
      assertThrew(classOf[AsynchronousCloseException], out)
    }
  }

  // D2/D3: no IOException may leak from a race with close().
  @Test def closeDuringPositionalReadsOnRegularFile(): Unit = {
    assumeTrue("needs threads", Platform.isMultithreadingEnabled)
    withTemporaryDirectory { dir =>
      val f = dir.resolve("big")
      Files.write(f, new Array[Byte](1 << 20))
      for (_ <- 0 until 50) {
        val ch = FileChannel.open(f, READ)
        val out = new Outcome
        val t = new Thread(() => {
          val b = ByteBuffer.allocate(4096)
          try {
            while ({ b.clear(); ch.read(b, 0L) >= 0 }) ()
          } catch { case e: Throwable => out.error = e }
          out.finished = true
        })
        t.setDaemon(true)
        t.start()
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
