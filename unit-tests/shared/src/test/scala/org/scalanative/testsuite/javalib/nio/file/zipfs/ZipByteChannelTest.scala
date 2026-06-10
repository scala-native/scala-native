package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.{NonWritableChannelException, SeekableByteChannel}
import java.nio.charset.StandardCharsets
import java.nio.file._
import java.util.HashMap

import org.junit.Assert._
import org.junit.Assume._
import org.junit._

import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVM

/** `newByteChannel` in read mode.
 *
 *  Drives the prebuilt `io-fixture.jar`. The channel is exercised both via
 *  `Files.newBufferedReader` (high-level, routes through providers) and
 *  directly through `provider.newByteChannel` (low-level position /
 *  partial-read).
 */
object ZipByteChannelTest {
  private val fixtureResource = "io-fixture.jar"
  private def sourceFixturePath: Path = {
    val root = if (Platform.executingInJVM) "../.." else "unit-tests"
    Paths.get(s"$root/shared/src/test/resources/zipfs/$fixtureResource")
  }
}

class ZipByteChannelTest {
  import ZipByteChannelTest._

  private var tempDir: Path = _
  private var fs: FileSystem = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-bc")
    val jar = tempDir.resolve("bc.jar")
    Files.copy(sourceFixturePath, jar)
    // accessMode=readOnly so both platforms expose the same read-only
    // contract (jdk.zipfs would otherwise mount writable by default).
    fs = FileSystems.newFileSystem(
      new URI("jar:" + jar.toUri.toString),
      ZipFileSystemTest.readOnlyEnv()
    )
  }

  @After def tearDown(): Unit = {
    try fs.close()
    catch { case _: Throwable => () }
    try {
      val it = Files.walk(tempDir).iterator()
      val all = new java.util.ArrayList[Path]()
      while (it.hasNext()) all.add(it.next())
      var i = all.size() - 1
      while (i >= 0) {
        try Files.deleteIfExists(all.get(i))
        catch { case _: Throwable => () }
        i -= 1
      }
    } catch { case _: Throwable => () }
  }

  private def openRead(name: String): SeekableByteChannel = {
    val opts = new java.util.HashSet[OpenOption]()
    opts.add(StandardOpenOption.READ)
    fs.provider().newByteChannel(fs.getPath(name), opts)
  }

  private def readAll(ch: SeekableByteChannel): String = {
    val buf = ByteBuffer.allocate(64)
    val out = new java.io.ByteArrayOutputStream()
    var r = ch.read(buf)
    while (r >= 0) {
      buf.flip()
      val arr = new Array[Byte](buf.remaining())
      buf.get(arr)
      out.write(arr)
      buf.clear()
      r = ch.read(buf)
    }
    new String(out.toByteArray(), StandardCharsets.UTF_8)
  }

  // --- via Files.* high-level entry points --------------------------------

  @Test def newBufferedReaderReadsLines(): Unit = {
    val br = Files.newBufferedReader(fs.getPath("/META-INF/MANIFEST.MF"))
    try {
      assertEquals("Manifest-Version: 1.0", br.readLine())
      assertNull(br.readLine())
    } finally br.close()
  }

  @Test def newInputStreamStillWorks(): Unit = {
    // Sanity: the input-stream path still routes correctly when the
    // channel impl is also in place.
    val is = Files.newInputStream(fs.getPath("/hello.txt"))
    try {
      val buf = new Array[Byte](16)
      val n = is.read(buf)
      assertEquals(5, n)
      assertEquals("hello", new String(buf, 0, n, StandardCharsets.UTF_8))
    } finally is.close()
  }

  // --- channel-level semantics --------------------------------------------

  @Test def channelSizeAndInitialPosition(): Unit = {
    val ch = openRead("/hello.txt")
    try {
      assertEquals(5L, ch.size())
      assertEquals(0L, ch.position())
      assertTrue(ch.isOpen())
    } finally ch.close()
  }

  @Test def channelReadEntireEntry(): Unit = {
    val ch = openRead("/hello.txt")
    try assertEquals("hello", readAll(ch))
    finally ch.close()
  }

  @Test def channelEmptyOptionsDefaultsToRead(): Unit = {
    // Per the Files.newByteChannel contract, an empty option set opens
    // for reading.
    val ch =
      Files.newByteChannel(fs.getPath("/hello.txt"), Array.empty[OpenOption]: _*)
    try assertEquals("hello", readAll(ch))
    finally ch.close()
  }

  @Test def channelPartialReads(): Unit = {
    val ch = openRead("/META-INF/MANIFEST.MF")
    try {
      val buf = ByteBuffer.allocate(8)
      val n1 = ch.read(buf)
      assertEquals(8, n1)
      assertEquals(8L, ch.position())
      buf.clear()
      val n2 = ch.read(buf)
      assertEquals(8, n2)
      buf.flip()
      val bytes = new Array[Byte](8)
      buf.get(bytes)
      // "Manifest-Version: 1.0\n" — bytes 8..15.
      assertEquals("-Version", new String(bytes, StandardCharsets.UTF_8))
    } finally ch.close()
  }

  @Test def channelPositionSeekBackAndRead(): Unit = {
    val ch = openRead("/META-INF/MANIFEST.MF")
    try {
      ch.position(10L)
      assertEquals(10L, ch.position())
      val buf = ByteBuffer.allocate(4)
      val n = ch.read(buf)
      assertEquals(4, n)
      buf.flip()
      val bytes = new Array[Byte](4)
      buf.get(bytes)
      // bytes 10..13 of "Manifest-Version: 1.0\n".
      assertEquals("ersi", new String(bytes, StandardCharsets.UTF_8))

      // Seek back to start and re-read first byte.
      ch.position(0L)
      val one = ByteBuffer.allocate(1)
      assertEquals(1, ch.read(one))
      one.flip()
      assertEquals('M'.toByte, one.get())
    } finally ch.close()
  }

  @Test def channelSeekPastEndIsLegal(): Unit = {
    // SeekableByteChannel spec: setting position > size() is legal, must
    // not change size(), and subsequent read() must return -1 (EOF).
    // jdk.zipfs's ByteArrayChannel clamps to size — that is a JDK
    // divergence from the spec, not something we want to mirror.
    assumeTrue(
      "jdk.zipfs clamps position to size; Native follows the spec",
      !executingInJVM
    )
    val ch = openRead("/hello.txt") // size 5
    try {
      ch.position(100L)
      assertEquals(100L, ch.position())
      assertEquals(5L, ch.size())
      val buf = ByteBuffer.allocate(8)
      assertEquals(-1, ch.read(buf))
      // Seek back to a valid position and read normally.
      ch.position(1L)
      val one = ByteBuffer.allocate(1)
      assertEquals(1, ch.read(one))
      one.flip()
      assertEquals('e'.toByte, one.get())
    } finally ch.close()
  }

  @Test def channelZeroLengthReadAtEofReturnsZero(): Unit = {
    // Channel semantics: a read into a buffer with no room returns 0
    // immediately — even at EOF, where a non-empty read returns -1.
    // jdk.zipfs's ByteArrayChannel reports -1 here as well; another spec
    // divergence we intentionally don't mirror.
    assumeTrue(
      "jdk.zipfs returns -1 for zero-length read at EOF; Native follows the spec",
      !executingInJVM
    )
    val ch = openRead("/hello.txt")
    try {
      ch.position(ch.size())
      assertEquals(0, ch.read(ByteBuffer.allocate(0)))
      // Sanity: the non-empty read at the same position still reports EOF.
      assertEquals(-1, ch.read(ByteBuffer.allocate(4)))
    } finally ch.close()
  }

  @Test def channelClosedWriteAndTruncateThrow(): Unit = {
    // After close, write/truncate must report ClosedChannelException, not
    // NonWritableChannelException — the closed state wins.
    // jdk.zipfs's ByteArrayChannel.write checks read-only first, before
    // closed-state, so it throws NonWritableChannelException on JVM —
    // another deviation from the channel-precedence contract.
    assumeTrue(
      "jdk.zipfs orders read-only check before closed-state; Native follows the spec",
      !executingInJVM
    )
    val ch = openRead("/hello.txt")
    ch.close()
    try {
      ch.write(ByteBuffer.wrap("x".getBytes()))
      fail("expected ClosedChannelException")
    } catch { case _: java.nio.channels.ClosedChannelException => () }
    try {
      ch.truncate(0L)
      fail("expected ClosedChannelException")
    } catch { case _: java.nio.channels.ClosedChannelException => () }
  }

  @Test def channelReadAtEndReturnsMinusOne(): Unit = {
    val ch = openRead("/hello.txt")
    try {
      ch.position(ch.size())
      val buf = ByteBuffer.allocate(8)
      assertEquals(-1, ch.read(buf))
    } finally ch.close()
  }

  @Test def channelWriteThrows(): Unit = {
    val ch = openRead("/hello.txt")
    try {
      ch.write(ByteBuffer.wrap("x".getBytes()))
      fail("expected NonWritableChannelException")
    } catch { case _: NonWritableChannelException => () }
    finally ch.close()
  }

  @Test def channelTruncateThrows(): Unit = {
    val ch = openRead("/hello.txt")
    try {
      ch.truncate(0L)
      fail("expected NonWritableChannelException")
    } catch { case _: NonWritableChannelException => () }
    finally ch.close()
  }

  @Test def channelNegativePositionThrows(): Unit = {
    val ch = openRead("/hello.txt")
    try {
      ch.position(-1L)
      fail("expected IllegalArgumentException")
    } catch { case _: IllegalArgumentException => () }
    finally ch.close()
  }

  @Test def channelMissingFileThrows(): Unit = {
    try {
      val ch = openRead("/no/such")
      try ch.close()
      catch { case _: Throwable => () }
      fail("expected NoSuchFileException")
    } catch { case _: NoSuchFileException => () }
  }

  @Test def channelOnDirectoryThrows(): Unit = {
    try {
      val ch = openRead("/META-INF")
      try ch.close()
      catch { case _: Throwable => () }
      fail("expected IOException for opening a directory as a channel")
    } catch { case _: java.io.IOException => () }
  }

  @Test def channelRejectsWriteOption(): Unit = {
    // The mount is read-only (accessMode=readOnly), so opening for write
    // must fail. jdk.zipfs honours accessMode since JDK 23.
    assumeFalse(
      "jdk.zipfs supports accessMode only since JDK 23",
      Platform.executingInJVMOnLowerThanJDK(23)
    )
    val opts = new java.util.HashSet[OpenOption]()
    opts.add(StandardOpenOption.WRITE)
    try {
      fs.provider().newByteChannel(fs.getPath("/hello.txt"), opts)
      fail("expected ReadOnlyFileSystemException")
    } catch { case _: ReadOnlyFileSystemException => () }
  }

  @Test def channelClosedReadThrows(): Unit = {
    val ch = openRead("/hello.txt")
    ch.close()
    try {
      ch.read(ByteBuffer.allocate(4))
      fail("expected ClosedChannelException")
    } catch { case _: java.nio.channels.ClosedChannelException => () }
  }

  @Test def channelNullOptionThrowsNpe(): Unit = {
    // NIO convention: null options are programmer error (NPE), not UOE.
    val opts = new java.util.HashSet[OpenOption]()
    opts.add(null.asInstanceOf[OpenOption])
    try {
      fs.provider().newByteChannel(fs.getPath("/hello.txt"), opts)
      fail("expected NullPointerException")
    } catch { case _: NullPointerException => () }
  }

  @Test def newInputStreamNullOptionThrows(): Unit = {
    // Native ZipFS routes through validateReadOption → NPE.
    // jdk.zipfs's ZipPath.newInputStream rejects null with UOE("'null'
    // not allowed") — different exception but the same "null is illegal"
    // contract. Accept either, since the test exists to pin "doesn't
    // silently succeed", not to fix the choice of exception class.
    try {
      Files.newInputStream(
        fs.getPath("/hello.txt"),
        null.asInstanceOf[OpenOption]
      )
      fail("expected NullPointerException or UnsupportedOperationException")
    } catch {
      case _: NullPointerException          => ()
      case _: UnsupportedOperationException => ()
    }
  }

  @Test def channelNonAsciiEntry(): Unit = {
    val ch = openRead("/Тест.nir")
    try assertEquals("russian", readAll(ch))
    finally ch.close()
  }
}
