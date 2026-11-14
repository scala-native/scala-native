package org.scalanative.testsuite.javalib.nio.file

import java.io.{File, IOException}
import java.nio.file._
import java.util.concurrent.{ConcurrentHashMap, CountDownLatch}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class FilesCreateTempTest {

  private def withTempDirectory(name: String)(body: Path => Unit): Unit = {
    val root = Files.createTempDirectory(name)
    try body(root)
    finally {
      val children = root.toFile().listFiles()
      if (children != null) children.foreach(_.delete())
      Files.deleteIfExists(root)
    }
  }

  private def createConcurrently(threads: Int, perThread: Int)(
      create: () => Path
  ): java.util.Set[Path] = {
    val created = ConcurrentHashMap.newKeySet[Path]()
    val failures = new ConcurrentHashMap[Throwable, Boolean]()
    val start = new CountDownLatch(1)
    val task: Runnable = () => {
      start.await()
      try
        for (_ <- 0 until perThread)
          assertTrue("duplicate temp name", created.add(create()))
      catch { case t: Throwable => failures.put(t, true) }
      ()
    }
    val workers = Array.fill(threads)(new Thread(task))
    workers.foreach(_.start())
    start.countDown()
    workers.foreach(_.join())
    if (!failures.isEmpty()) throw failures.keys().nextElement()
    created
  }

  @Test def createTempFileInParallelYieldsDistinctNewFiles(): Unit = {
    assumeTrue("needs threads", Platform.isMultithreadingEnabled)
    withTempDirectory("temp-file-parallel") { root =>
      val created = createConcurrently(threads = 8, perThread = 100) { () =>
        Files.createTempFile(root, "p", ".tmp")
      }
      assertEquals(800, created.size())
      assertEquals(800, root.toFile().list().length)
      created.forEach(p => assertTrue(Files.isRegularFile(p)))
    }
  }

  @Test def fileCreateTempFileInParallelYieldsDistinctNewFiles(): Unit = {
    assumeTrue("needs threads", Platform.isMultithreadingEnabled)
    withTempDirectory("io-temp-file-parallel") { root =>
      val created = createConcurrently(threads = 8, perThread = 100) { () =>
        File.createTempFile("pre", null, root.toFile()).toPath()
      }
      assertEquals(800, created.size())
      assertEquals(800, root.toFile().list().length)
    }
  }

  @Test def createTempDirectoryInParallelYieldsDistinctNewDirs(): Unit = {
    assumeTrue("needs threads", Platform.isMultithreadingEnabled)
    withTempDirectory("temp-dir-parallel") { root =>
      val created = createConcurrently(threads = 8, perThread = 50) { () =>
        Files.createTempDirectory(root, "d")
      }
      assertEquals(400, created.size())
      created.forEach(p => assertTrue(Files.isDirectory(p)))
    }
  }

  @Test def createdTempFileIsEmpty(): Unit = {
    withTempDirectory("temp-file-empty") { root =>
      val f = Files.createTempFile(root, "e", null)
      assertEquals(0L, Files.size(f))
      assertTrue(f.getFileName().toString.endsWith(".tmp"))
    }
  }

  @Test def createTempFileInMissingDirectoryThrows(): Unit = {
    withTempDirectory("temp-file-missing") { root =>
      val missing = root.resolve("does-not-exist")
      assertThrows(
        classOf[IOException],
        Files.createTempFile(missing, "m", ".tmp")
      )
      assertThrows(
        classOf[IOException],
        File.createTempFile("mmm", ".tmp", missing.toFile())
      )
    }
  }
}
