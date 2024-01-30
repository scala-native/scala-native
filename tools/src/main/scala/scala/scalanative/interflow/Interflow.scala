package scala.scalanative
package interflow

import scala.collection.mutable
import scala.scalanative.codegen.PlatformInfo
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.linker._
import scala.scalanative.util.ScopedVar
import java.util.function.Supplier
import scala.concurrent._

class Interflow(val config: build.Config)(implicit
    val analysis: ReachabilityAnalysis.Result
) extends Visit
    with Opt
    with NoOpt
    with Eval
    with Combine
    with Inline
    with PolyInline
    with Intrinsics
    with Log {
  implicit val platform: PlatformInfo = PlatformInfo(config)

  private val originals = {
    val out = mutable.Map.empty[nir.Global, nir.Defn]
    analysis.defns.foreach { defn => out(defn.name) = defn }
    out
  }

  private val todo = mutable.Queue.empty[nir.Global.Member]
  private val done = mutable.Map.empty[nir.Global.Member, nir.Defn.Define]
  private val started = mutable.Set.empty[nir.Global.Member]
  private val denylist = mutable.Set.empty[nir.Global.Member]
  private val reached = mutable.HashSet.empty[nir.Global.Member]
  private val modulePurity = mutable.Map.empty[nir.Global.Top, Boolean]

  def currentFreshScope = freshScopeTl.get()
  private val freshScopeTl =
    ThreadLocal.withInitial(() => new ScopedVar[nir.Fresh])

  def currentLexicalScopes = lexicalScopesTl.get()
  private val lexicalScopesTl = ThreadLocal.withInitial(() =>
    new ScopedVar[mutable.UnrolledBuffer[DebugInfo.LexicalScope]]
  )

  // Not thread-safe, each thread shall contain it's own stack
  protected class SymbolsStack {
    private var state: List[nir.Global.Member] = Nil
    private var cachedSize = 0

    def tracked[T](symbol: nir.Global.Member)(block: => T): T = {
      push(symbol)
      try block
      finally pop()
    }
    def size = cachedSize
    def contains(symbol: nir.Global.Member): Boolean = state.contains(symbol)
    def push(symbol: nir.Global.Member): Unit = {
      state = symbol :: state
      cachedSize += 1
    }
    def pop(): nir.Global.Member = {
      require(state.nonEmpty, "Cannot pop empty stack")
      val head :: tail = state: @unchecked
      state = tail
      cachedSize -= 1
      head
    }
  }
  private val inliningBacktraceTl =
    ThreadLocal.withInitial(() => new SymbolsStack())
  private val contextTl =
    ThreadLocal.withInitial(() => List.empty[String])
  private val mergeProcessorTl =
    ThreadLocal.withInitial(() => List.empty[MergeProcessor])
  private val blockFreshTl =
    ThreadLocal.withInitial(() => List.empty[nir.Fresh])

  def hasOriginal(name: nir.Global.Member): Boolean =
    originals.contains(name) && originals(name).isInstanceOf[nir.Defn.Define]
  def getOriginal(name: nir.Global.Member): nir.Defn.Define =
    originals(name).asInstanceOf[nir.Defn.Define]
  def maybeOriginal(name: nir.Global.Member): Option[nir.Defn.Define] =
    originals.get(name).collect { case defn: nir.Defn.Define => defn }

  def popTodo(): nir.Global =
    todo.synchronized {
      if (todo.isEmpty) {
        nir.Global.None
      } else {
        todo.dequeue()
      }
    }
  def pushTodo(name: nir.Global.Member): Unit =
    todo.synchronized {
      if (!reached.contains(name)) {
        todo.enqueue(name)
        reached += name
      }
    }
  def allTodo(): Seq[nir.Global.Member] =
    todo.synchronized {
      todo.toSeq
    }

  def isDone(name: nir.Global.Member): Boolean =
    done.synchronized {
      done.contains(name)
    }
  def setDone(name: nir.Global.Member, value: nir.Defn.Define) =
    done.synchronized {
      done(name) = value
    }
  def getDone(name: nir.Global.Member): nir.Defn.Define =
    done.synchronized {
      done(name)
    }
  def maybeDone(name: nir.Global.Member): Option[nir.Defn.Define] =
    done.synchronized {
      done.get(name)
    }

  def hasStarted(name: nir.Global.Member): Boolean =
    started.synchronized {
      started.contains(name)
    }
  def markStarted(name: nir.Global.Member): Unit =
    started.synchronized {
      started += name
    }

  def isDenylisted(name: nir.Global.Member): Boolean =
    denylist.synchronized {
      denylist.contains(name)
    }
  def markDenylisted(name: nir.Global.Member): Unit =
    denylist.synchronized {
      denylist += name
    }

  def hasModulePurity(name: nir.Global.Top): Boolean =
    modulePurity.synchronized {
      modulePurity.contains(name)
    }
  def setModulePurity(name: nir.Global.Top, value: Boolean): Unit =
    modulePurity.synchronized {
      modulePurity(name) = value
    }
  def getModulePurity(name: nir.Global.Top): Boolean =
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

  def inliningBacktrace: SymbolsStack =
    inliningBacktraceTl.get

  def mergeProcessor: MergeProcessor =
    mergeProcessorTl.get.head
  def pushMergeProcessor(value: MergeProcessor): Unit =
    mergeProcessorTl.set(value :: mergeProcessorTl.get)
  def popMergeProcessor(): Unit =
    mergeProcessorTl.set(mergeProcessorTl.get.tail)

  def blockFresh: nir.Fresh =
    blockFreshTl.get.head
  def pushBlockFresh(value: nir.Fresh): Unit =
    blockFreshTl.set(value :: blockFreshTl.get)
  def popBlockFresh(): Unit =
    blockFreshTl.set(blockFreshTl.get.tail)

  def result(): Seq[nir.Defn] = {
    val optimized = originals.clone()
    optimized ++= done
    optimized.values.toSeq
  }

  protected def mode: build.Mode = config.compilerConfig.mode

}

object Interflow {

  def optimize(config: build.Config, analysis: ReachabilityAnalysis.Result)(
      implicit ec: ExecutionContext
  ): Future[Seq[nir.Defn]] = {
    val interflow = new Interflow(config)(analysis)
    interflow.visitEntries()
    interflow
      .visitLoop()
      .map(_ => interflow.result())
  }

  object LLVMIntrinsics {
    private val externAttrs = nir.Attrs(isExtern = true)
    private val LLVMI =
      nir.Global.Top("scala.scalanative.runtime.LLVMIntrinsics$")
    private def llvmIntrinsic(id: String) =
      nir.Val.Global(LLVMI.member(nir.Sig.Extern(id)), nir.Type.Ptr)

    val StackSave = llvmIntrinsic("llvm.stacksave")
    val StackSaveSig = nir.Type.Function(Nil, nir.Type.Ptr)

    val StackRestore = llvmIntrinsic("llvm.stackrestore")
    val StackRestoreSig = nir.Type.Function(Seq(nir.Type.Ptr), nir.Type.Unit)
  }

  val depends: Seq[nir.Global] = Seq(
    LLVMIntrinsics.StackSave.name,
    LLVMIntrinsics.StackRestore.name
  )

}
