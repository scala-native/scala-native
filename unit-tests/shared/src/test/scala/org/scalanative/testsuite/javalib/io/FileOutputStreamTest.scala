package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.Platform.isWindows
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class FileOutputStreamTest {
  def withTempFile(f: File => Unit): Unit = {
    val tmpfile = File.createTempFile("scala-native-test", null)
    try {
      f(tmpfile)
    } finally {
      tmpfile.delete()
    }
  }

  def withTempDirectory(f: File => Unit): Unit = {
    import java.nio.file._
    import attribute._
    val tmpdir = Files.createTempDirectory("scala-native-test")
    try {
      f(tmpdir.toFile())
    } finally {
      Files.walkFileTree(
        tmpdir,
        new SimpleFileVisitor[Path]() {
          override def visitFile(
              file: Path,
              attrs: BasicFileAttributes
          ): FileVisitResult = {
            Files.delete(file)
            FileVisitResult.CONTINUE
          }
          override def postVisitDirectory(
              dir: Path,
              exc: IOException
          ): FileVisitResult = {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          }
        }
      )
    }
  }

  @Test def writeNull(): Unit = {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      assertThrows(classOf[NullPointerException], fos.write(null))
      assertThrows(classOf[NullPointerException], fos.write(null, 0, 0))
      fos.close()
    }
  }

  @Test def writeOutOfBoundsNegativeCount(): Unit = {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, 0, -1))
      fos.close()
    }
  }

  @Test def writeOutOfBoundsNegativeOffset(): Unit = {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, -1, 0))
      fos.close()
    }
  }

  @Test def writeOutOfBoundsArrayTooSmall(): Unit = {
    withTempFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, 0, 16))
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, 4, 8))
      fos.close()
    }
  }

  @Test def attemptToOpenReadonlyRegularFile(): Unit = {
    withTempFile { ro =>
      ro.setReadOnly()
      assertThrows(classOf[FileNotFoundException], new FileOutputStream(ro))
    }
  }

  @Test def attemptToOpenDirectory(): Unit = {
    withTempDirectory { dir =>
      assertThrows(classOf[FileNotFoundException], new FileOutputStream(dir))
    }
  }

  @Test def attemptToCreateFileInReadonlyDirectory(): Unit = {
    assumeFalse(
      "Setting directory read only in Windows does not have affect on creating new files",
      isWindows
    )
    withTempDirectory { ro =>
      ro.setReadOnly()
      assertThrows(
        classOf[FileNotFoundException],
        new FileOutputStream(new File(ro, "child"))
      )
    }

  }

  @Test def truncateFileOnInitializationIfAppendFalse(): Unit = {
    val nonEmpty = File.createTempFile("scala-native-unit-test", null)
    try {
      // prepares a non-empty file
      locally {
        val fos = new FileOutputStream(nonEmpty)
        try {
          fos.write(0x20)
        } finally {
          fos.close()
        }
      }
      // re-opens the file with append=false so that it is truncated
      locally {
        val fos = new FileOutputStream(nonEmpty)
        fos.close()
      }
      // checks the content
      locally {
        val fin = new FileInputStream(nonEmpty)
        try {
          assertEquals(-1, fin.read())
        } finally {
          fin.close()
        }
      }
    } finally {
      nonEmpty.delete()
    }
  }

  @Test def doNotTruncateFileOnInitializationIfAppendTrue(): Unit = {
    val nonEmpty = File.createTempFile("scala-native-unit-test", null)
    try {
      val written = 0x20
      // prepares a non-empty file
      locally {
        val fos = new FileOutputStream(nonEmpty)
        try {
          fos.write(written)
        } finally {
          fos.close()
        }
      }
      // re-opens the file with append=true
      locally {
        val fos = new FileOutputStream(nonEmpty, true)
        fos.close()
      }
      // checks the content
      locally {
        val fin = new FileInputStream(nonEmpty)
        try {
          assertEquals(written, fin.read())
          assertEquals(-1, fin.read())
        } finally {
          fin.close()
        }
      }
    } finally {
      nonEmpty.delete()
    }
  }
}
