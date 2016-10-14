package scala.scalanative
package optimizer
package pass

import scala.io.Source

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import analysis.ControlFlow, ControlFlow.Block
import scalanative.util.unreachable
import nir._, Inst.Let

/**
 * Inline caching based on information gathered at runtime.
 */
class InlineCaching(dispatchInfo: Map[String, Seq[Int]],
                    maxCandidates: Int)(implicit fresh: Fresh, top: Top)
    extends Pass {
  import InlineCaching._

  println("#" * 181)
  println("Dispatch info:")
  println(dispatchInfo)
  println("#" * 181)

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

    lazy val vtable = {
      clss.vtable lift meth.vindex flatMap {
        case v: Val.Global => Some(v.name)
        case _             => None
      }
    }

    direct orElse inClass orElse single orElse vtable
  }

  /**
   * Split the block `block` at the first instruction for which `fn` is true.
   *
   * @param block The block to split. It must contain an instruction for which
   *              `fn` evaluates to true.
   * @param fn    The function that determines where to split the block.
   * @return A triplet consisting of:
   *           - a block whose instructions are those of `block` until one where
   *             `fn` evaluates to true is found.
   *           - the first instruction for which `fn` evaluates to true
   *           - a block consisting of all the instructions coming after. It has
   *             one parameter whose name is the same as that of the instruction
   *             where we split.
   *
   */
  private def splitAt(fn: Inst => Boolean)(
      block: Block): (Block, Let, Block) = {
    assert(block.insts exists fn)
    val (l0, (x @ Let(name, op)) +: rest) = block.insts span (!fn(_))

    val merge = fresh()
    val b0    = block.copy(insts = l0)
    val b1    = Block(merge, Seq(Val.Local(x.name, op.resty)), rest :+ block.insts.last)

    (b0, x, b1)
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
   * Determines if `inst` is a virtual dispatch
   *
   * @param inst The instruction to tests
   * @return true if `inst` is a virtual dispatch, false otherwise.
   */
  private def isVirtualDispatch(inst: Inst): Boolean =
    inst match {
      case Let(_, Op.Method(_, MethodRef(_: Class, meth)))
        if meth.isVirtual =>
        true
      case _ =>
        false
    }

  private def blockToInsts(block: Block): Seq[Inst] =
    block.label +: block.insts

  /**
   * Generates the block that retrieves statically the address of the
   * implementation of `meth` for an instance of `clss`.
   *
   * @param meth The method to retrieve
   * @param clss The class for which we're looking for an implementation
   * @return A function that accepts a `Local` representing the name of the
   *         block to jump to after retrieving the address of the method.
   *         The destination block must accept one parameter of type `Ptr`,
   *         which is the address of the method.
   */
  private def makeStaticBlock(meth: Method, clss: Class): Local => Block =
    next => {
      val blockName  = fresh()
      val impl       = findImpl(meth, clss) getOrElse ???
      val staticCall = Let(Op.Copy(Val.Global(impl, Type.Ptr)))
      Block(
          blockName,
          Nil,
          Seq(
            staticCall,
            Inst.Jump(Next.Label(next, Seq(Val.Local(staticCall.name, Type.Ptr))))
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
                                 desiredType: Int,
                                 correspondingBlock: Local): Local => Block = {
    val comparison = Val.Local(fresh(), Type.Bool)

    (els: Local) =>
      Block(
          name = fresh(),
          params = Nil,
          insts = Seq(
              Let(comparison.name,
                   Op.Comp(Comp.Ieq,
                           Type.I32,
                           Val.I32(desiredType),
                           actualType)),
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
  private def addInlineCaching(block: Block): Seq[Block] = {
    if (block.insts exists isVirtualDispatch) {
      val (init,
           inst @ Let(n, Op.Method(obj, MethodRef(cls: Class, meth))),
           merge) = splitAt(isVirtualDispatch)(block)

      val instname = s"${n.scope}.${n.id}"
      val key      = s"$instname:${meth.name.id}"

      dispatchInfo getOrElse (key, Seq()) flatMap (top classWithId _) match {
        case allCandidates if allCandidates.nonEmpty =>
          // We don't inline calls to all candidates, only the most frequent for
          // performance.
          val candidates = allCandidates take maxCandidates

          val tpe     = Val.Local(fresh(), cls.typeStruct)
          val typeptr = Val.Local(fresh(), Type.Ptr)
          val typeid  = Val.Local(fresh(), Type.I32)

          // Instructions to load the type id of `obj` at runtime.
          // The result is in `typeid`.
          val loadTypeId: Seq[Let] = Seq(
              Let(typeptr.name, Op.Load(Type.Ptr, obj)),
              Let(tpe.name, Op.Load(cls.typeStruct, typeptr)),
              Let(typeid.name, Op.Extract(tpe, Seq(0)))
          )

          // The blocks that give the address for an inlined call
          val staticBlocks: Seq[Block] =
            candidates map (makeStaticBlock(meth, _)(merge.name))

          // The type comparisons. The argument is the block to go to if the
          // type test fails.
          val typeComparisons: Seq[Local => Block] =
            staticBlocks zip candidates map {
              case (block, clss) =>
                makeTypeComparison(typeid, clss.id, block.name)
            }

          // If all type tests fail, we fallback to virtual dispatch.
          val fallback: Block = {
            val newInstName = fresh()
            val newInst     = inst.copy(name = newInstName)
            Block(fresh(),
                  Nil,
                  Seq(
                    newInst,
                    Inst.Jump(
                        Next.Label(merge.name,
                                   Seq(Val.Local(newInstName, inst.op.resty))))
                  )
            )
          }

          // Execute start, load the typeid and jump to the first type test.
          val start: Local => Block = typeComp =>
            init.copy(insts = init.insts ++ loadTypeId :+ Inst.Jump(Next(typeComp)))

          linkBlocks(start +: typeComparisons)(fallback) ++
          staticBlocks ++
          Seq(fallback) ++
          addInlineCaching(merge)

        case _ =>
          Seq(block)
      }
    } else {
      Seq(block)
    }
  }

  override def preDefn = {
    case define: Defn.Define =>
      val graph = ControlFlow.Graph(define.insts)
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
