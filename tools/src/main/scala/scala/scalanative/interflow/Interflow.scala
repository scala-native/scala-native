package scala.scalanative
package interflow

import scala.collection.mutable
import scala.scalanative.codegen.TargetInfo
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

  /** The target machine for which code is being compiled. */
  implicit val target: TargetInfo = TargetInfo(config)

  /** A map from symbol to its original definition. */
  private val originals = {
    val out = mutable.Map.empty[nir.Global, nir.Defn]
    analysis.defns.foreach { defn => out(defn.name) = defn }
    out
  }

  /** The set of definitions to process. */
  private val todo = mutable.Queue.empty[nir.Global.Member]

  /** A map from symbol to its optimized definition. */
  private val done = mutable.Map.empty[nir.Global.Member, nir.Defn.Define]

  /** A set with the symbols for which Interflow has started. */
  private val started = mutable.Set.empty[nir.Global.Member]

  /** A set with the symbols that shouldn't be processed. */
  private val denylist = mutable.Set.empty[nir.Global.Member]

  /** A set with the symbols that are accessible from code roots. */
  private val reached = mutable.HashSet.empty[nir.Global.Member]

  /** A map indicatting whether a particular symbol is pure. */
  private val modulePurity = mutable.Map.empty[nir.Global.Top, Boolean]

  /** Returns the current scope. */
  def currentFreshScope = freshScopeTl.get()

  private val freshScopeTl =
    ThreadLocal.withInitial(() => new ScopedVar[nir.Fresh])

  /** Returns the current lexical scopes. */
  def currentLexicalScopes = lexicalScopesTl.get()

  private val lexicalScopesTl = ThreadLocal.withInitial(() =>
    new ScopedVar[mutable.UnrolledBuffer[DebugInfo.LexicalScope]]
  )

  private val contextTl =
    ThreadLocal.withInitial(() => List.empty[String])

  private val mergeProcessorTl =
    ThreadLocal.withInitial(() => List.empty[MergeProcessor])

  private val blockFreshTl =
    ThreadLocal.withInitial(() => List.empty[nir.Fresh])

  /** Returns `true` iff `name` has an original, unmodified definition. */
  def hasOriginal(name: nir.Global.Member): Boolean =
    originals.contains(name) && originals(name).isInstanceOf[nir.Defn.Define]

  /** Returns the original, unmodified definition of `name`.
   *
   *    - Requires: `name` has an original definition.
   */
  def getOriginal(name: nir.Global.Member): nir.Defn.Define =
    originals(name).asInstanceOf[nir.Defn.Define]

  /** Returns the original, unmodified definition of `name` if it has one. */
  def maybeOriginal(name: nir.Global.Member): Option[nir.Defn.Define] =
    originals.get(name).collect { case defn: nir.Defn.Define => defn }

  /** Thread-safely returns the next symbol to process, or `Global.None` if
   *  there aren't any.
   */
  def popTodo(): nir.Global =
    todo.synchronized {
      if (todo.isEmpty) {
        nir.Global.None
      } else {
        todo.dequeue()
      }
    }

  /** Thread-safely adds a symbol to process. */
  def pushTodo(name: nir.Global.Member): Unit =
    todo.synchronized {
      if (!reached.contains(name)) {
        todo.enqueue(name)
        reached += name
      }
    }

  /** Thread-safely accesses the list of symbols to process. */
  def allTodo(): Seq[nir.Global.Member] =
    todo.synchronized {
      todo.toSeq
    }

  /** Returns `true` iff `name` has been processed. */
  def hasOptimizedDefinition(name: nir.Global.Member): Boolean =
    done.synchronized {
      done.contains(name)
    }

  /** Sets `value` as the optimized form of `name`. */
  def setOptimizedDefinition(name: nir.Global.Member, value: nir.Defn.Define) =
    done.synchronized {
      done(name) = value
    }

  /** Returns the optimized form of `name`.
   *
   *    - Requires: `name` has been processed.
   */
  def getOptimizedDefinition(name: nir.Global.Member): nir.Defn.Define =
    done.synchronized {
      done(name)
    }

  /** Returns the optimized form of `name` if there is one. */
  def maybeDone(name: nir.Global.Member): Option[nir.Defn.Define] =
    done.synchronized {
      done.get(name)
    }

  /** Returns the optimized form of `name` if there is one. */
  def hasStarted(name: nir.Global.Member): Boolean =
    started.synchronized {
      started.contains(name)
    }

  /** Register the fact that interflow analysis has started for `name`. */
  def markStarted(name: nir.Global.Member): Unit =
    started.synchronized {
      started += name
    }

  /** Returns `true` iff `name` should not be processed. */
  def isDenylisted(name: nir.Global.Member): Boolean =
    denylist.synchronized {
      denylist.contains(name)
    }

  /** Register the fact that `name` should not be processed. */
  def markDenylisted(name: nir.Global.Member): Unit =
    denylist.synchronized {
      denylist += name
    }

  /** `true` iff the purity of `name` is defined.
   *
   *  An object is pure iff it is statically reachable, none of its fields
   *  requires instantiation, and its constructor is empty. In that case, a
   *  non-null instance can be compiled as a constant value.
   */
  def hasModulePurity(name: nir.Global.Top): Boolean =
    modulePurity.synchronized {
      modulePurity.contains(name)
    }

  /** Defines the purity of `name` as `value`. */
  def setModulePurity(name: nir.Global.Top, value: Boolean): Unit =
    modulePurity.synchronized {
      modulePurity(name) = value
    }

  /** `true` iff `name` does not contain any field requiring instantiation.
   *
   *  `this.hasModulePurity(name)` must be `true`.
   */
  def getModulePurity(name: nir.Global.Top): Boolean =
    modulePurity.synchronized {
      modulePurity(name)
    }

  /** The size of the backtrace (i.e., a log of operations). */
  def contextDepth(): Int =
    contextTl.get.size

  /** `true` iff the algorithm has a backtrace.
   *
   *  Contexts are used for logging and for detecting that some operation is
   *  being done recursively.
   */
  def hasContext(value: String): Boolean =
    contextTl.get.contains(value)

  /** Adds an operation on the backtrace. */
  def pushContext(value: String): Unit =
    contextTl.set(value :: contextTl.get)

  /** Removes the operation on the top of the backtrace. */
  def popContext(): Unit =
    contextTl.set(contextTl.get.tail)

  /** The current inlining preprocessor. */
  def mergeProcessor: MergeProcessor =
    mergeProcessorTl.get.head

  /** Adds a new inlining preprocessor on top of the current one. */
  def pushMergeProcessor(value: MergeProcessor): Unit =
    mergeProcessorTl.set(value :: mergeProcessorTl.get)

  /** Removes current inlining preprocessor. */
  def popMergeProcessor(): Unit =
    mergeProcessorTl.set(mergeProcessorTl.get.tail)

  /** The current NIR block in which instructions are inserted. */
  def blockFresh: nir.Fresh =
    blockFreshTl.get.head

  /** Adds a new NIR block for inserting instructions. */
  def pushBlockFresh(value: nir.Fresh): Unit =
    blockFreshTl.set(value :: blockFreshTl.get)

  /** Removes the current NIR block. */
  def popBlockFresh(): Unit =
    blockFreshTl.set(blockFreshTl.get.tail)

  /** Returns a collection with the optimized forms of all definitions. */
  def result(): Seq[nir.Defn] = {
    val optimized = originals.clone()
    optimized ++= done
    optimized.values.toSeq
  }

  /** Returns the mode (debug or release) in which compilation takes place. */
  protected def mode: build.Mode = config.compilerConfig.mode

}

object Interflow {

  /** Runs Interflow with the given `config`. */
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

  /** The symbols required by Interflow. */
  val dependencies: Seq[nir.Global] = Seq(
    LLVMIntrinsics.StackSave.name,
    LLVMIntrinsics.StackRestore.name
  )

}
