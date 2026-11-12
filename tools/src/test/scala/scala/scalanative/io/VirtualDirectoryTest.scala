package scala.scalanative.io

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.util.Scope

class VirtualDirectoryTest {

  private def withJar(entries: (String, String)*)(fn: Path => Unit): Unit = {
    val jar = Files.createTempFile("scala-native-vdir-test", ".jar")
    try {
      val out = new ZipOutputStream(Files.newOutputStream(jar))
      try
        entries.foreach {
          case (name, content) =>
            out.putNextEntry(new ZipEntry(name))
            out.write(content.getBytes(StandardCharsets.UTF_8))
            out.closeEntry()
        }
      finally out.close()
      fn(jar)
    } finally Files.deleteIfExists(jar)
  }

  private def readString(dir: VirtualDirectory, path: String): String = {
    val file = dir.files
      .find(_.toString == path)
      .getOrElse(throw new AssertionError(s"$path not found in ${dir.files}"))
    val buf = dir.read(file)
    val bytes = new Array[Byte](buf.remaining())
    buf.get(bytes)
    new String(bytes, StandardCharsets.UTF_8)
  }

  @Test def jarStaysReadableWhenAnotherScopeOverItCloses(): Unit =
    withJar("a/b.txt" -> "hello", "c.txt" -> "world") { jar =>
      val first = Scope.unsafe()
      val second = Scope.unsafe()
      try {
        val dir1 = VirtualDirectory.real(jar)(first)
        val dir2 = VirtualDirectory.real(jar)(second)
        assertEquals("hello", readString(dir1, "/a/b.txt"))
        assertEquals("hello", readString(dir2, "/a/b.txt"))

        first.close()

        assertEquals("hello", readString(dir2, "/a/b.txt"))
        assertEquals("world", readString(dir2, "/c.txt"))
      } finally {
        first.close()
        second.close()
      }
    }
}
