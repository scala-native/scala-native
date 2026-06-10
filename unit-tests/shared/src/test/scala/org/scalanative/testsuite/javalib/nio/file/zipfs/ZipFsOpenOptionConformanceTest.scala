package org.scalanative.testsuite.javalib.nio.file.zipfs

import java.nio.ByteBuffer
import java.nio.file.{
  FileSystems, Files, OpenOption, Path, StandardOpenOption
}
import java.util.HashMap

import org.junit.Assert._
import org.junit.{After, Before, Test}

class ZipFsOpenOptionConformanceTest {
  import ZipFileSystemTest._

  private var tempDir: Path = _

  @Before def setUp(): Unit = {
    tempDir = Files.createTempDirectory("zipfs-open-options")
  }

  @After def tearDown(): Unit = {
    try {
      val it = Files.walk(tempDir).iterator()
      val all = new java.util.ArrayList[Path]()
      while (it.hasNext()) all.add(it.next())
      for (i <- (all.size() - 1) to 0 by -1) {
        try Files.deleteIfExists(all.get(i))
        catch { case _: Throwable => () }
      }
    } catch { case _: Throwable => () }
  }

  private def writeEnv(): java.util.Map[String, Object] = {
    val m = new HashMap[String, Object]()
    m.put("create", "true")
    m
  }

  private def writeString(path: Path, value: String): Unit = {
    val out = Files.newOutputStream(path, Array.empty[OpenOption]: _*)
    try out.write(value.getBytes("UTF-8"))
    finally out.close()
  }

  private def assertText(path: Path, expected: String): Unit =
    assertEquals(expected, new String(Files.readAllBytes(path), "UTF-8"))

  @Test def newByteChannelEmptyOptionsReadExistingEntry(): Unit = {
    val jar = tempDir.resolve("read-default.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val p = fs.getPath("/entry.txt")
      writeString(p, "abcdef")

      val ch = Files.newByteChannel(p, Array.empty[OpenOption]: _*)
      try {
        val buf = ByteBuffer.allocate(8)
        assertEquals(6, ch.read(buf))
        buf.flip()
        val bytes = new Array[Byte](buf.remaining())
        buf.get(bytes)
        assertEquals("abcdef", new String(bytes, "UTF-8"))
      } finally ch.close()
    } finally fs.close()
  }

  @Test def writeWithoutCreateDoesNotCreateMissingEntry(): Unit = {
    val jar = tempDir.resolve("write-no-create.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      try {
        Files.newByteChannel(
          fs.getPath("/missing.txt"),
          Array[OpenOption](StandardOpenOption.WRITE): _*
        )
        fail("expected NoSuchFileException")
      } catch { case _: java.nio.file.NoSuchFileException => () }
    } finally fs.close()
  }

  @Test def writeExistingEntryTruncates(): Unit = {
    val jar = tempDir.resolve("write-no-truncate.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val p = fs.getPath("/entry.txt")
      writeString(p, "abcdef")

      val ch = Files.newByteChannel(
        p,
        Array[OpenOption](StandardOpenOption.WRITE): _*
      )
      try assertEquals(2, ch.write(ByteBuffer.wrap("XY".getBytes("UTF-8"))))
      finally ch.close()

      assertText(p, "XY")
    } finally fs.close()
  }

  @Test def appendExtendsExistingEntry(): Unit = {
    val jar = tempDir.resolve("append.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val p = fs.getPath("/entry.txt")
      writeString(p, "abc")

      val ch = Files.newByteChannel(
        p,
        Array[OpenOption](StandardOpenOption.APPEND): _*
      )
      try assertEquals(3, ch.write(ByteBuffer.wrap("def".getBytes("UTF-8"))))
      finally ch.close()

      assertText(p, "abcdef")
    } finally fs.close()
  }

  @Test def writeSideOptionsWithoutWriteDoNotCreateMissingEntry(): Unit = {
    // jdk.zipfs treats write-side options without WRITE/APPEND as a
    // read-only open, so missing entries surface as NoSuchFileException
    // rather than IllegalArgumentException (despite what JDK
    // FileChannel.toOpenOptions does for the default FS).
    val jar = tempDir.resolve("write-side-options.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val cases = Array[OpenOption](
        StandardOpenOption.CREATE,
        StandardOpenOption.CREATE_NEW,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.DELETE_ON_CLOSE
      )
      var i = 0
      while (i < cases.length) {
        try {
          Files.newByteChannel(
            fs.getPath("/entry-" + i + ".txt"),
            Array[OpenOption](cases(i)): _*
          )
          fail("expected NoSuchFileException for " + cases(i))
        } catch { case _: java.nio.file.NoSuchFileException => () }
        i += 1
      }
    } finally fs.close()
  }

  @Test def appendWithReadStillAppends(): Unit = {
    val jar = tempDir.resolve("append-conflicts.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val p = fs.getPath("/entry.txt")
      writeString(p, "abc")

      val appendRead = Files.newByteChannel(
        p,
        Array[OpenOption](
          StandardOpenOption.APPEND,
          StandardOpenOption.READ
        ): _*
      )
      try appendRead.write(ByteBuffer.wrap("def".getBytes("UTF-8")))
      finally appendRead.close()
      assertText(p, "abcdef")

    } finally fs.close()
  }

  @Test def appendConflictsWithTruncateExisting(): Unit = {
    val jar = tempDir.resolve("append-truncate-conflict.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val p = fs.getPath("/entry.txt")
      writeString(p, "abc")

      try {
        Files.newByteChannel(
          p,
          Array[OpenOption](
            StandardOpenOption.APPEND,
            StandardOpenOption.TRUNCATE_EXISTING
          ): _*
        )
        fail("expected IllegalArgumentException")
      } catch { case _: IllegalArgumentException => () }
    } finally fs.close()
  }

  @Test def createNewWithTruncateExistingCreatesFreshEntry(): Unit = {
    val jar = tempDir.resolve("create-new-truncate.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      val p = fs.getPath("/entry.txt")
      val ch = Files.newByteChannel(
        p,
        Array[OpenOption](
          StandardOpenOption.CREATE_NEW,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE
        ): _*
      )
      try assertEquals(3, ch.write(ByteBuffer.wrap("abc".getBytes("UTF-8"))))
      finally ch.close()

      assertText(p, "abc")
    } finally fs.close()
  }

  @Test def newOutputStreamRejectsReadOption(): Unit = {
    val jar = tempDir.resolve("output-stream-read.jar")
    val fs = FileSystems.newFileSystem(jarUri(jar), writeEnv())
    try {
      try {
        Files.newOutputStream(
          fs.getPath("/entry.txt"),
          Array[OpenOption](StandardOpenOption.READ): _*
        )
        fail("expected IllegalArgumentException")
      } catch { case _: IllegalArgumentException => () }
    } finally fs.close()
  }
}
