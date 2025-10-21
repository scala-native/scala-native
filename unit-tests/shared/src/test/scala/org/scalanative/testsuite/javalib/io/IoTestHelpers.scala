package org.scalanative.testsuite.javalib.io

import java.io.{File, IOException}

/* Note on Technical Debt:
 *
 * The good news about Technical Debt is that it means that a project has
 * endured long enough to accumulate it.  The bad news is that there
 * is accumulated Technical Debt.
 *
 * Specifically, the "withTemporaryDirectory" concept and implementations of it
 * and close kin, exist in a number of places) in javalib unit-tests.
 * They tend to have slight variations, which make both development &
 * maintenance both annoying and error prone.
 * Someday they should be unified into a "Single Point of Truth".
 *
 * This file allows the FileInputStream#available test introduced in PR 3333
 * to share the "withTemporaryDirectory" previously used in FileOutputStream.
 * By doing so, it avoids introducing new Technical Debt. It makes no attempt
 * to solve the whole "withTemporaryDirectory" problem.
 *
 * The impediment to solving the larger problem is probably determining a
 * proper place in the directory tree for the common file and gaining
 * consensus.
 *
 * Perhaps inspiration or enlightenment will strike the next time someone
 * implements or maintains a unit-test requiring "withTemporaryDirectory".
 */

object IoTestHelpers {
  def withTemporaryFile(f: File => Unit): Unit = {
    val tmpfile = File.createTempFile("scala-native-test", null)
    try {
      f(tmpfile)
    } finally {
      tmpfile.delete()
    }
  }

  // This variant takes "Temporary" to mean: clean up after yourself.
  def withTemporaryDirectory(f: File => Unit): Unit = {
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

}
