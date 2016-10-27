package scala.scalanative
package optimizer
package pass

import scala.io.Source

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import analysis.ControlFlow, ControlFlow.Block
import nir._, Inst.Let

/**
 * Inline caching based on information gathered at runtime.
 * Transforms polymorphic call sites to a sequence of type tests and static
 * dispatches. Falls back to virtual dispatch if all type tests fail.
 */
class InlineCaching(dispatchInfo: Map[String, Seq[Int]],
                    maxCandidates: Int)(implicit fresh: Fresh, top: Top)
    extends Pass {
  import InlineCaching._

  /**
   * Finds the implementation of `meth` for an instance of `clss`.
   *
   * @param meth The method we're looking for inside class `clss`.
   * @param clss The type for which we want a method.
   * @return The `Global` representing the concrete implementation of `meth`
   *         that should be used for class `clss`.
   */
  private def findImpl(meth: Method, clss: Class): Option[Global] = {
    lazy val allMethods =
      clss.allmethods.filter(m => m.isConcrete && m.name.id == meth.name.id)

    // Is the method directly defined in the class we're interested in?
    lazy val direct =
      if (meth.in == clss) Some(clss.name member meth.name.id) else None

    // Is there a matching method in the class we're interested in?
    lazy val inClass = allMethods find (_.in == clss) map (_.name)

    // Did we find a single match in all the methods?
    lazy val single = allMethods match {
      case Seq(m) =>
        m.in match {
          case c: Class if c.isModule =>
            val className = c.name.id.drop("module.".length)
            Some(Global.Top(className) member m.name.id)
          case other =>
            Some(other.name member m.name.id)
        }
      case _ => None
    }

    // Lookup using the vtable
    lazy val vtable = {
      clss.vtable lift meth.vindex flatMap {
        case v: Val.Global => Some(v.name)
        case _             => None
      }
    }

    direct orElse inClass orElse single orElse vtable
  }

  /**
   * Split the block `block` at the first sequence of instructions for which
   * `test` is true.
   * The instructions are grouped using `select` and then each group is given to
   * `test`.
   *
   * @param test      The test to select where to split the block.
   * @param select    How to group all the instructions in the block (eg. use a
   *                  sliding window of length 2)
   * @param makeParam A function that generates parameters for the block coming
   *                  after the split from the instructions where we split.
   * @param block     The block to split.
   */
  private def splitAt[T <: Inst](test: Seq[Inst] => Boolean)(
      select: Seq[Inst] => Seq[Seq[Inst]])(
      makeParams: Seq[T] => Seq[Val.Local])(
      block: Block): Option[(Block, Seq[Inst], Block)] = {
    val slices = select(block.insts)
    slices span (!test(_)) match {
      case (_, Seq()) =>
        None
      case (before, (insts: Seq[T]) +: after) =>
        val merge = fresh()
        val b0    = block.copy(insts = before.map(_.head))
        val b1 =
          Block(merge,
                makeParams(insts),
                after.tail.map(_.head) ++ after.last.tail)

        Some((b0, insts, b1))
    }
  }

  /**
   * Connect the sequence of blocks `blocks` up to `last`.
   *
   * @param blocks The blocks to connect, in this order
   * @param last   The last block in the chain.
   * @return A sequence of blocks such that they are all connected using the
   *         next block's name.
   */
  private def linkBlocks(blocks: Seq[Local => Block])(
      last: Block): Seq[Block] =
    (blocks foldRight List(last)) {
      case (blk, acc) => blk(acc.head.name) :: acc
    }

  /**
   * Determines if `insts` is a virtual dispatch.
   *
   * @param inst The instructions to tests
   * @return true if `inst` is a virtual dispatch, false otherwise.
   */
  private def isVirtualDispatch(insts: Seq[Inst]): Boolean = insts match {
    case Seq(Let(n, Op.Method(_, MethodRef(_: Class, meth))),
             Let(_, Op.Call(_, Val.Local(ptr, _), _)))
        if meth.isVirtual && ptr == n =>
      true
    case _ =>
      false
  }

  /**
   * Creates a single val from the last instruction in `lets`. Useful to create
   * the parameters of a new block when splitting blocks.
   *
   * @param lets The instructions whose value we want to pass
   * @return A single `Val.Local` whose name and type are the same as the last
   *         instruction in `lets`.
   */
  private def reuseLast(lets: Seq[Let]): Seq[Val.Local] =
    Seq(Val.Local(lets.last.name, lets.last.op.resty))

  /**
   * Convert a block to the corresponding sequence of instruction
   *
   * @param block The block to convert
   * @return The sequence of instruction that corresponds to the same implicit
   *         block.
   */
  private def blockToInsts(block: Block): Seq[Inst] =
    block.label +: block.insts

  /**
   * Generates the block that retrieves statically the address of the
   * implementation of `meth` for an instance of `clss`.
   *
   * @param meth The method to retrieve
   * @param call The original method call
   * @param clss The class for which we're looking for an implementation
   * @return A function that accepts a `Local` representing the name of the
   *         block to jump to after retrieving the address of the method.
   *         The destination block must accept one parameter of type `Ptr`,
   *         which is the address of the method.
   */
  private def makeStaticBlock(meth: Method,
                              call: Op.Call,
                              clss: Class): Local => Block =
    next => {
      val blockName  = fresh()
      val impl       = findImpl(meth, clss) getOrElse ???
      val result     = fresh()

      Block(
        blockName,
        Nil,
        Seq(
          Let(result, call.copy(ptr = Val.Global(impl, Type.Ptr))),
          Inst.Jump(Next.Label(next, Seq(Val.Local(result, call.resty))))
        )
      )
    }

  /**
   * Generates a type comparison.
   *
   * @param actualType         The type that we're observing at runtime
   * @param desiredType        The type we compare against.
   * @param correspondingBlock The block to jump to if the two types are equal.
   * @return A function that accepts a `Local` representing the name of the
   *         block to jump to if the two types are different.
   */
  private def makeTypeComparison(actualType: Val,
                                 desiredType: Val,
                                 correspondingBlock: Local): Local => Block = {
    val comparison = Val.Local(fresh(), Type.Bool)

    (els: Local) =>
      Block(
        name = fresh(),
        params = Nil,
        insts = Seq(
          Let(comparison.name,
              Op.Comp(Comp.Ieq, Type.Ptr, actualType, desiredType)),
          Inst.If(comparison, Next(correspondingBlock), Next(els))
        )
      )
  }

  /**
   * Adds inline caching to virtual calls in `block`.
   *
   * @param block The block on which to add inline caching.
   * @return A block that is semantically equivalent to `block`
   */
  private def addInlineCaching(block: Block): Seq[Block] =
    splitAt(isVirtualDispatch)(_.sliding(2).toSeq)(reuseLast)(block) match {
      case Some(
          (init,
           Seq(inst @ Let(n, Op.Method(obj, MethodRef(cls: Class, meth))),
               Let(_, call @ Op.Call(resty, ptr, args))),
           merge)) =>
        val reuse: Seq[Let] => Seq[Val.Local] = lets =>
          Seq(Val.Local(lets.last.name, lets.last.op.resty))

        val instname = s"${n.scope}.${n.id}"
        val key      = s"$instname:${meth.name.id}"

        dispatchInfo getOrElse (key, Seq()) flatMap (top classWithId _) match {
          case allCandidates if allCandidates.nonEmpty =>
            // We don't inline calls to all candidates, only the most frequent for
            // performance.
            val candidates = allCandidates take maxCandidates

            val typeptr = Val.Local(fresh(), Type.Ptr)
            // Instructions to load the type id of `obj` at runtime.
            // The result is in `typeid`.
            val loadTypePtr: Seq[Let] = Seq(
              Let(typeptr.name, Op.Load(Type.Ptr, obj))
            )

            // The blocks that give the address for an inlined call
            val staticBlocks: Seq[Block] =
              candidates map (makeStaticBlock(meth, call, _)(merge.name))

            // The type comparisons. The argument is the block to go to if the
            // type test fails.
            val typeComparisons: Seq[Local => Block] =
              staticBlocks zip candidates map {
                case (block, clss) =>
                  makeTypeComparison(typeptr, clss.typeConst, block.name)
              }

            // If all type tests fail, we fallback to virtual dispatch.
            val fallback: Block = {
              val newMethod = inst.copy(name = fresh())
              val newCall   = Let(call.copy(ptr = Val.Local(newMethod.name, Type.Ptr)))
              Block(fresh(),
                    Nil,
                    Seq(
                      newMethod,
                      newCall,
                      Inst.Jump(
                        Next.Label(merge.name,
                                   Seq(Val.Local(newCall.name, call.resty))))
                    ))
            }

            // Execute start, load the typeid and jump to the first type test.
            val start: Local => Block = typeComp =>
              init.copy(
                insts = init.insts ++ loadTypePtr :+ Inst.Jump(Next(typeComp)))

            linkBlocks(start +: typeComparisons)(fallback) ++
              staticBlocks ++
              Seq(fallback) ++
              addInlineCaching(merge)

          case _ =>
            Seq(block)
        }
      case _ =>
        Seq(block)
    }

  override def preDefn = {
    case define: Defn.Define =>
      val graph     = ControlFlow.Graph(define.insts)
      val newBlocks = graph.all flatMap addInlineCaching
      Seq(define.copy(insts = newBlocks flatMap blockToInsts))
  }

}

object InlineCaching extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    config.profileDispatchInfo match {
      case Some(info) if info.exists =>
        val maxCandidates = config.inlineCachingMaxCandidates
        val dispatchInfo =
          analysis.DispatchInfoParser(Source.fromFile(info).mkString)
        new InlineCaching(dispatchInfo, maxCandidates)(top.fresh, top)

      case _ =>
        EmptyPass
    }
}
