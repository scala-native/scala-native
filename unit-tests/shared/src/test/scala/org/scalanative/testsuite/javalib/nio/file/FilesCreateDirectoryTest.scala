package org.scalanative.testsuite.javalib.nio.file

import java.io.IOException
import java.nio.file._
import java.util.concurrent.CountDownLatch

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class FilesCreateDirectoryTest {

  @Test def createDirectoryOnExistingEmptyDirectoryThrowsFileAlreadyExists()
      : Unit = {
    withTempDirectory("create-directory-empty") { root =>
      val dir = Files.createDirectory(root.resolve("empty"))
      val thrown = assertThrows(
        classOf[FileAlreadyExistsException],
        Files.createDirectory(dir)
      )
      assertTrue(thrown.getMessage.contains(dir.toString))
    }
  }

  @Test def createDirectoryOnExistingNonEmptyDirectoryThrowsFileAlreadyExists()
      : Unit = {
    withTempDirectory("create-directory-nonempty") { root =>
      val dir = Files.createDirectory(root.resolve("filled"))
      Files.createFile(dir.resolve("child"))
      assertThrows(
        classOf[FileAlreadyExistsException],
        Files.createDirectory(dir)
      )
    }
  }

  @Test def createDirectoryOnExistingFileThrowsFileAlreadyExists(): Unit = {
    withTempDirectory("create-directory-file") { root =>
      val file = Files.createFile(root.resolve("plain"))
      assertThrows(
        classOf[FileAlreadyExistsException],
        Files.createDirectory(file)
      )
    }
  }

  @Test def createDirectoryWithoutParentNamesThePath(): Unit = {
    withTempDirectory("create-directory-missing-parent") { root =>
      val child = root.resolve("missing").resolve("child")
      val thrown =
        assertThrows(classOf[IOException], Files.createDirectory(child))
      val message = thrown.getMessage
      assertTrue(message != null && message.contains(child.toString))
    }
  }

  @Test def createDirectoriesOnExistingDirectoryReturnsIt(): Unit = {
    withTempDirectory("create-directories-exists") { root =>
      val dir = Files.createDirectory(root.resolve("exists"))
      assertSame(dir, Files.createDirectories(dir))
      assertTrue(Files.isDirectory(dir))
    }
  }

  @Test def createDirectoriesOnExistingFileThrowsFileAlreadyExists(): Unit = {
    withTempDirectory("create-directories-file") { root =>
      val file = Files.createFile(root.resolve("plain"))
      assertThrows(
        classOf[FileAlreadyExistsException],
        Files.createDirectories(file)
      )
    }
  }

  @Test def createDirectoriesSucceedsWhenAnotherThreadCreatesTheDirectory()
      : Unit = {
    withTempDirectory("create-directories-race") { root =>
      val threads = 8
      val rounds = 100
      var round = 0
      while (round < rounds) {
        val target = root.resolve(s"r$round").resolve("a/b/c")
        val ready = new CountDownLatch(threads)
        val gate = new CountDownLatch(1)
        val failures = new Array[Throwable](threads)
        val workers = (0 until threads).toArray.map { index =>
          val thread = new Thread {
            override def run(): Unit = {
              ready.countDown()
              try {
                gate.await()
                val created = Files.createDirectories(target)
                if (created != target || !Files.isDirectory(target)) {
                  failures(index) = new AssertionError(
                    s"round $round created '$created', directory=${Files.isDirectory(target)}"
                  )
                }
              } catch {
                case thrown: Throwable =>
                  failures(index) = thrown
              }
            }
          }
          thread.start()
          thread
        }
        ready.await()
        gate.countDown()
        var joined = 0
        while (joined < workers.length) {
          workers(joined).join()
          joined += 1
        }
        val messages = failures.flatMap { thrown =>
          if (thrown == null) None
          else
            Some(
              thrown.getClass.getName +
                (if (thrown.getMessage == null) ""
                 else ": " + thrown.getMessage)
            )
        }
        assertTrue(
          s"round $round: ${messages.mkString("; ")}",
          messages.isEmpty
        )
        assertTrue(Files.isDirectory(target))
        round += 1
      }
    }
  }

  private def withTempDirectory(prefix: String)(use: Path => Unit): Unit = {
    val root = Files.createTempDirectory(prefix)
    try use(root)
    finally remove(root)
  }

  private def remove(path: Path): Unit =
    if (Files.exists(path)) {
      if (Files.isDirectory(path)) {
        val children = Files.list(path)
        try {
          val it = children.iterator()
          while (it.hasNext())
            remove(it.next())
        } finally children.close()
      }
      Files.delete(path)
    }
}
