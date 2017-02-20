package scala.scalanative
package optimizer

import java.io.File
import java.nio.file.Paths
import scalanative.nir.Defn
import scalanative.nir.serialization.serializeText
import scalanative.io.{withScratchBuffer, VirtualDirectory}

trait Reporter {

  /** Gets called whenever optimizations starts. */
  def onStart(batchId: Int, batchDefns: Seq[Defn]): Unit = ()

  /** Gets called right after pass transforms the batchDefns. */
  def onPass(batchId: Int,
             passId: Int,
             pass: Pass,
             batchDefns: Seq[nir.Defn]): Unit = ()

  /** Gets called with final result of optimization. */
  def onComplete(batchId: Int, batchDefns: Seq[Defn]): Unit = ()
}

object Reporter {

  /** Default no-op reporter. */
  val empty: Reporter = new Reporter {}

  /** Dump textual NIR after every pass to given directory. */
  def toDirectory(file: File): Reporter = new Reporter {
    lazy val dir = VirtualDirectory.local(file)

    private def debug(batchDefns: Seq[Defn], suffix: String) =
      withScratchBuffer { buffer =>
        serializeText(batchDefns, buffer)
        buffer.flip
        dir.write(Paths.get(s"out.$suffix.hnir"), buffer)
      }

    private def padded(value: Int): String =
      if (value < 10) "0" + value else "" + value

    override def onStart(batchId: Int, batchDefns: Seq[Defn]): Unit =
      debug(batchDefns, padded(batchId) + ".00")

    override def onPass(batchId: Int,
                        passId: Int,
                        pass: Pass,
                        batchDefns: Seq[nir.Defn]): Unit =
      debug(
        batchDefns,
        padded(batchId) + "." + padded(passId + 1) + "-" + pass.getClass.getSimpleName)
  }
}
