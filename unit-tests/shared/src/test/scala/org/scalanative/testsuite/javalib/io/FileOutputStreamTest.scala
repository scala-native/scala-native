package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.javalib.io.IoTestHelpers._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

import scala.scalanative.junit.utils.AssumesHelper._

class FileOutputStreamTest {

  @Test def writeNull(): Unit = {
    withTemporaryFile { file =>
      val fos = new FileOutputStream(file)
      assertThrows(classOf[NullPointerException], fos.write(null))
      assertThrows(classOf[NullPointerException], fos.write(null, 0, 0))
      fos.close()
    }
  }

  @Test def writeOutOfBoundsNegativeCount(): Unit = {
    withTemporaryFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, 0, -1))
      fos.close()
    }
  }

  @Test def writeOutOfBoundsNegativeOffset(): Unit = {
    withTemporaryFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, -1, 0))
      fos.close()
    }
  }

  @Test def writeOutOfBoundsArrayTooSmall(): Unit = {
    withTemporaryFile { file =>
      val fos = new FileOutputStream(file)
      val arr = new Array[Byte](8)
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, 0, 16))
      assertThrows(classOf[IndexOutOfBoundsException], fos.write(arr, 4, 8))
      fos.close()
    }
  }

  @Test def attemptToOpenReadonlyRegularFile(): Unit = {
    assumeNotRoot()
    withTemporaryFile { ro =>
      ro.setReadOnly()
      assertThrows(classOf[FileNotFoundException], new FileOutputStream(ro))
    }
  }

  @Test def attemptToOpenDirectory(): Unit = {
    withTemporaryDirectory { dir =>
      assertThrows(classOf[FileNotFoundException], new FileOutputStream(dir))
    }
  }

  @Test def attemptToCreateFileInReadonlyDirectory(): Unit = {
    assumeFalse(
      "Setting directory read only in Windows does not have affect on creating new files",
      isWindows
    )
    assumeNotRoot()
    withTemporaryDirectory { ro =>
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
