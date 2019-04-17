package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._
import scalanative.util.ScopedVar
import java.util.function.Supplier

class Interflow(val mode: build.Mode)(implicit val linked: linker.Result)
    extends Visit
    with Opt
    with NoOpt
    with Eval
    with Combine
    with Inline
    with PolyInline
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

  private var contextTl = ThreadLocal.withInitial(new Supplier[List[String]] {
    def get() = Nil
  })
  private val mergeProcessorTl =
    ThreadLocal.withInitial(new Supplier[List[MergeProcessor]] {
      def get() = Nil
    })
  private val blockFreshTl = ThreadLocal.withInitial(new Supplier[List[Fresh]] {
    def get() = Nil
  })

  def hasOriginal(name: Global): Boolean =
    originals.contains(name) && originals(name).isInstanceOf[Defn.Define]
  def getOriginal(name: Global): Defn.Define =
    originals(name).asInstanceOf[Defn.Define]
  def maybeOriginal(name: Global): Option[Defn.Define] =
    originals.get(name).collect { case defn: Defn.Define => defn }

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

  def contextDepth(): Int =
    contextTl.get.size
  def hasContext(value: String): Boolean =
    contextTl.get.contains(value)
  def pushContext(value: String): Unit =
    contextTl.set(value :: contextTl.get)
  def popContext(): Unit =
    contextTl.set(contextTl.get.tail)

  def mergeProcessor: MergeProcessor =
    mergeProcessorTl.get.head
  def pushMergeProcessor(value: MergeProcessor): Unit =
    mergeProcessorTl.set(value :: mergeProcessorTl.get)
  def popMergeProcessor(): Unit =
    mergeProcessorTl.set(mergeProcessorTl.get.tail)

  def blockFresh: Fresh =
    blockFreshTl.get.head
  def pushBlockFresh(value: Fresh): Unit =
    blockFreshTl.set(value :: blockFreshTl.get)
  def popBlockFresh(): Unit =
    blockFreshTl.set(blockFreshTl.get.tail)

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
