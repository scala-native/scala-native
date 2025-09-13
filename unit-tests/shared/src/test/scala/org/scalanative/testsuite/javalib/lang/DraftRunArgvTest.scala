package org.scalanative.testsuite.javalib.lang

/* This is a DEBUGGING DRAFT to see if we can come up with
 * a drop-in 1-to-1 replacement for Scalafmt's "runArgv()"
 * method which does not hang on Scala Native.
 */

import org.junit.Test
import org.junit.Assert._

import scala.Option
import scala.sys.process.{BasicIO => ScalaBasicIO}
import scala.util.{Try, Success}

import java.{lang => jl}
import java.{util => ju}
import java.nio.file.Path

import org.scalanative.testsuite.utils.Platform

class DraftRunArgvTest {

  // Signature of Scalafmt original.
  def runArgv(cmd: Seq[String], cwd: Option[Path]): scala.util.Try[String] = {

    // Work around Scala 2.12 & 2.13 differences in conversion method names.
    val javaCmd = new ju.ArrayList[String](cmd.length)

    cmd.foreach(c =>
      javaCmd.add(
        // Change to PlatformCompat for scalafmt usage. here and in import.
        if (!Platform.isWindows) c
        else c
      )
    )

    val pb = new jl.ProcessBuilder(javaCmd)
      // To match 2024-01-01 exactly remove/comment_out, See Note 1 below.
      .redirectErrorStream(true) // merge child stderr into stdout

    cwd.map(wd => pb.directory(wd.toFile))

    val proc = pb.start()

    val is = proc.getInputStream()

    val bldr = new jl.StringBuilder()

    /* Scala BasicIO.processFully() has had years of exercise to shake out bugs
     * Removes any concerns with Scala Versions deprecating 'streamLines'.
     */
    val processor = ScalaBasicIO.processFully(s => bldr.append(s))

    // Wait here until entire stream 'is' is consumed, thereby  closing it.
    processor(is)

    Success(bldr.toString())

    /* Note 1:
     *   Scalafmt 2024-01-01 code effectively discards any stderr coming from
     *   the child process. Long analysis about "effectively discards"
     *   available.
     * 
     *   redirectErrorStream(true) merges the child's stderr & stdout, which
     *   is probably what one wants but differs from the 2024-01-01 behavior.
     *
     *   Java 9 introduces a Redirect.DISCARD, which would match the
     *   2024-01-01 behavior. I believe that Scalafmt supports a minimal
     *   Java version of 8 and that Redirect is not implemented in
     *   Scala Native.
     *
     *   I do not know if Scalafmt has an idiom similar to Scala Native's
     *   Platform.isWindows(). If such is available and if one __really__
     *   wants to discard the child's stderr, one could create a
     *   java.io.file.File for "/dev/null" or "NUL" depending on the
     *   operative architecture.
     */
  }

  @Test def testRunArgv_A_1(): Unit = {

    val argv = Seq("git", "init", "-b", "main")

    val r = runArgv(argv, None)

    val response = r.get // As desired, will throw Exception if not Success.

    assertTrue(
      s"process response: '${response}'",
      response.startsWith("Initialized empty Git repository in") ||
        response.startsWith("warning: re-init: ignored --initial-branch=")
    )
  }
}
