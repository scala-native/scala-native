package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._

class Interflow(val mode: build.Mode)(implicit val linked: linker.Result)
    extends Visit
    with Eval
    with Combine
    with Inline
    with Intrinsics
    with Log {
  private val originals = {
    val out = mutable.Map.empty[Global, Defn]
    linked.defns.foreach { defn =>
      out(defn.name) = defn
    }
    out
  }

  private val todo         = mutable.Queue.empty[Global]
  private val done         = mutable.Map.empty[Global, Defn.Define]
  private val started      = mutable.Set.empty[Global]
  private val blacklist    = mutable.Set.empty[Global]
  private val modulePurity = mutable.Map.empty[Global, Boolean]

  private var contextTl        = new ThreadLocal[List[String]]
  private val mergeProcessorTl = new ThreadLocal[MergeProcessor]
  private val blockFreshTl     = new ThreadLocal[Fresh]

  def hasOriginal(name: Global): Boolean =
    originals.contains(name) && originals(name).isInstanceOf[Defn.Define]
  def getOriginal(name: Global): Defn.Define =
    originals(name).asInstanceOf[Defn.Define]

  def popTodo(): Global =
    todo.synchronized {
      if (todo.isEmpty) {
        Global.None
      } else {
        todo.dequeue()
      }
    }
  def pushTodo(name: Global): Unit =
    todo.synchronized {
      assert(name ne Global.None)
      todo.enqueue(name)
    }
  def allTodo(): Seq[Global] =
    todo.synchronized {
      todo.toSeq
    }

  def isDone(name: Global): Boolean =
    done.synchronized {
      done.contains(name)
    }
  def setDone(name: Global, value: Defn.Define) =
    done.synchronized {
      done(name) = value
    }
  def getDone(name: Global): Defn.Define =
    done.synchronized {
      done(name)
    }
  def maybeDone(name: Global): Option[Defn.Define] =
    done.synchronized {
      done.get(name)
    }

  def hasStarted(name: Global): Boolean =
    started.synchronized {
      started.contains(name)
    }
  def markStarted(name: Global): Unit =
    started.synchronized {
      started += name
    }

  def isBlacklisted(name: Global): Boolean =
    blacklist.synchronized {
      blacklist.contains(name)
    }
  def markBlacklisted(name: Global): Unit =
    blacklist.synchronized {
      blacklist += name
    }

  def hasModulePurity(name: Global): Boolean =
    modulePurity.synchronized {
      modulePurity.contains(name)
    }
  def setModulePurity(name: Global, value: Boolean): Unit =
    modulePurity.synchronized {
      modulePurity(name) = value
    }
  def getModulePurity(name: Global): Boolean =
    modulePurity.synchronized {
      modulePurity(name)
    }

  def context: List[String] =
    contextTl.get
  def context_=(value: List[String]) =
    contextTl.set(value)

  def mergeProcessor: MergeProcessor =
    mergeProcessorTl.get
  def mergeProcessor_=(value: MergeProcessor) =
    mergeProcessorTl.set(value)

  def blockFresh: Fresh =
    blockFreshTl.get
  def blockFresh_=(value: Fresh) =
    blockFreshTl.set(value)

  def result(): Seq[Defn] = {
    val optimized = originals.clone()
    optimized ++= done
    optimized.values.toSeq.sortBy(_.name)
  }
}

object Interflow {
  def apply(config: build.Config, linked: linker.Result): Seq[Defn] = {
    val interflow = new Interflow(config.mode)(linked)
    interflow.visitEntries()
    interflow.visitLoop()
    interflow.result()
  }
}
