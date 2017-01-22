package scala.scalanative
package linker

import java.io.{File, PrintWriter}
import nir.Global

/** Linking reporters can override one of the corresponding methods to
 *  get notified whenever one of the linking events happens.
 */
trait Reporter {

  /** Gets called whenever linking starts. */
  def onStart(): Unit = ()

  /** Gets called whenever a new entry point is discovered. */
  def onEntry(global: Global): Unit = ()

  /** Gets called whenever a new definition is loaded from nir path. */
  def onResolved(global: Global): Unit = ()

  /** Gets called whenever linker fails to resolve a global. */
  def onUnresolved(globa: Global): Unit = ()

  /** Gets called whenever a new direct dependency is discovered. */
  def onDirectDependency(from: Global, to: Global): Unit = ()

  /** Gets called whenever a new conditional dependency is discovered. */
  def onConditionalDependency(from: Global, to: Global, cond: Global): Unit =
    ()

  /** Gets called whenever linking is complete. */
  def onComplete(): Unit = ()
}

object Reporter {

  /** Default no-op reporter. */
  val empty = new Reporter {}

  /** Generate dot file for observed dependency graph. */
  def toFile(file: File): Reporter = new Reporter {
    private var writer: PrintWriter = _

    private def writeStart(): Unit = {
      writer = new PrintWriter(file)
      writer.println("digraph G {")
    }

    private def writeEdge(from: Global, to: Global): Unit = {
      def quoted(s: String) = "\"" + s + "\""
      writer.print(quoted(from.show))
      writer.print("->")
      writer.print(quoted(to.show))
      writer.println(";")
    }

    private def writeEnd(): Unit = {
      writer.println("}")
      writer.close()
    }

    override def onStart(): Unit =
      writeStart()

    override def onDirectDependency(from: Global, to: Global): Unit =
      writeEdge(from, to)

    override def onConditionalDependency(from: Global,
                                         to: Global,
                                         cond: Global): Unit =
      writeEdge(from, to)

    override def onComplete(): Unit =
      writeEnd()
  }
}
