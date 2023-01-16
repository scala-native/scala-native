package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import FilesTest.withTemporaryDirectory

class DirectoryStreamTest {

  @Test def filesNewDirectoryStreamPath(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0 = dir.resolve("f0")
      val f1 = dir.resolve("f1")
      val d0 = dir.resolve("d0")
      val f2 = d0.resolve("f2")

      Files.createDirectory(d0)
      Files.createFile(f0)
      Files.createFile(f1)
      Files.createFile(f2)
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val stream = Files.newDirectoryStream(dir)
      val expected = Set(f0, f1, d0)
      val result = scala.collection.mutable.Set.empty[Path]

      val it = stream.iterator()
      while (it.hasNext()) {
        result += it.next()
      }
      assertTrue(result == expected)
    }
  }

  @Test def filesNewDirectoryStreamPathDirectoryStreamFilterPath(): Unit = {
    withTemporaryDirectory { dirFile =>
      val dir = dirFile.toPath()
      val f0 = dir.resolve("f0")
      val f1 = dir.resolve("f1")
      val d0 = dir.resolve("d0")
      val f2 = d0.resolve("f2")

      Files.createDirectory(d0)
      Files.createFile(f0)
      Files.createFile(f1)
      Files.createFile(f2)
      assertTrue(Files.exists(d0) && Files.isDirectory(d0))
      assertTrue(Files.exists(f0) && Files.isRegularFile(f0))
      assertTrue(Files.exists(f1) && Files.isRegularFile(f1))
      assertTrue(Files.exists(f2) && Files.isRegularFile(f2))

      val filter = new DirectoryStream.Filter[Path] {
        override def accept(p: Path): Boolean = !p.toString.endsWith("f1")
      }
      val stream = Files.newDirectoryStream(dir, filter)
      val expected = Set(f0, d0)
      val result = scala.collection.mutable.Set.empty[Path]

      val it = stream.iterator()
      while (it.hasNext()) {
        result += it.next()
      }
      assertTrue(result == expected)
    }
  }

  @Test def cannotGetIteratorMoreThanOnce(): Unit = {
    val stream = Files.newDirectoryStream(Paths.get("."))
    stream.iterator()
    assertThrows(classOf[IllegalStateException], stream.iterator())
  }

  @Test def cannotGetAnIteratorAfterClose(): Unit = {
    val stream = Files.newDirectoryStream(Paths.get("."))
    stream.close()
    assertThrows(classOf[IllegalStateException], stream.iterator())
  }

  @Test def hasNextReturnsFalseAfterStreamIsClosed(): Unit = {
    val stream = Files.newDirectoryStream(Paths.get("."))
    val it = stream.iterator()
    stream.close()
    assertFalse(it.hasNext())
    assertThrows(classOf[NoSuchElementException], it.next())
  }
}
