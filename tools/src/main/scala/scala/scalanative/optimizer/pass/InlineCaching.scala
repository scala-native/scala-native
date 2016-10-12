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
class InlineCaching(dispatchInfo: Map[String, Seq[Int]], maxCandidates: Int)(implicit fresh: Fresh, top: Top) extends Pass {
  import InlineCaching._

  println("#" * 181)
  println("Dispatch info:")
  println(dispatchInfo)
  println("#" * 181)

  private def findImpl(meth: Method, clss: Class): Global =
    if (meth.in == clss) {
      assert(meth.isConcrete, s"Method ${meth.name.id} belongs to a type observed at runtime. The method should be concrete!")
      Global.Member(Global.Top(clss.name.id), meth.name.id)
    }
    else {
      clss.allmethods.filter(m => m.isConcrete && m.name.id == meth.name.id) match {
        case Seq() =>
          ???
        case Seq(m) =>
            m.in match {
              case c: Class if c.isModule =>
                val className = c.name.id.drop("module.".length)
                Global.Member(Global.Top(className), m.name.id)
              case other =>
                Global.Member(Global.Top(m.in.name.id), m.name.id)
            }
        case many =>
          many find (_.in == clss) match {
            case Some(m) =>
              Global.Member(Global.Top(clss.name.id), m.name.id)
            case None =>
              ???
          }
      }
    }


  def splitAtVirtualCall(block: Block): (Block, Let, Block) = {
    assert(block.insts exists isVirtualCall)
    block.insts span (!isVirtualCall(_)) match {
      case (l0, (x @ Let(_, Op.Method(_, _))) +: rest) =>
        val merge = fresh()
        val b0 = block.copy(insts = l0)
        val b1 = Block(merge, Seq(Val.Local(x.name, Type.Ptr)), rest :+ block.insts.last)
        (b0, x, b1)
      case _ =>
        unreachable
    }
  }


  private def isVirtualCall(inst: Inst): Boolean =
    inst match {
      case Let(_, Op.Method(_, MethodRef(_: Class, meth)))
        if meth.isVirtual =>
        true
      case _ =>
        false
    }

  private def blockToInsts(block: Block): Seq[Inst] =
    block.label +: block.insts

  private def splitBlock(block: Block): Seq[Block] =
    if (block.insts exists isVirtualCall) {
      val (b0, inst @ Let(n, Op.Method(obj, MethodRef(cls: Class, meth))), b1) =
        splitAtVirtualCall(block)

      val instname = s"${n.scope}.${n.id}"
      val key = s"$instname:${meth.name.id}"

      dispatchInfo get key getOrElse Seq() flatMap (top classWithId _) match {
        case candidates if 1 to maxCandidates contains candidates.length =>

          // load type String
          val tpe        = Val.Local(fresh(), cls.typeStruct)
          val typeptr    = Val.Local(fresh(), Type.Ptr)
          val typeid     = Val.Local(fresh(), Type.I32)

          val loadTpeNameInsts = Seq(
            Let(typeptr.name, Op.Load(Type.Ptr, obj)),
            Let(tpe.name, Op.Load(cls.typeStruct, typeptr)),
            Let(typeid.name, Op.Extract(tpe, Seq(0)))
          )

          val individualBlocks: Seq[(Class, Local, Block)] = candidates map { clss =>
            val blockName = fresh()
            val impl = findImpl(meth, clss)
            val staticCall = Let(fresh(), Op.Copy(Val.Global(impl, Type.Ptr)))
            (clss, blockName, Block(blockName, Nil, Seq(staticCall, Inst.Jump(Next.Label(b1.name, Seq(Val.Local(staticCall.name, Type.Ptr)))))))
          }

          val fallback = {
            val instName = fresh()
            val inst2 = inst.copy(name = instName)
            Block(fresh(), Nil, Seq(inst2, Inst.Jump(Next.Label(b1.name, Seq(Val.Local(instName, Type.Ptr))))))
          }

          val makeTypeComparison =
            (tpeName: Int, thn: Local) => {

              val cmpTpe     = Val.Local(fresh(), Type.Bool)

              val typeComp = Seq(
                Let(cmpTpe.name, Op.Comp(Comp.Ieq, Type.I32, Val.I32(tpeName), typeid))
              )

              // val cmpTpe = cmpTpeInst(tpeName)
              (els: Local) =>
                Block(
                  name = fresh(),
                  params = Nil,
                  insts = typeComp :+  Inst.If(Val.Local(cmpTpe.name, Type.Bool), Next(thn), Next(els))
                )
            }

          val typeComparisons: Seq[Local => Block] = individualBlocks map { case (clss, _, block) =>
            makeTypeComparison(clss.id, block.name)
          }

          val finallyTypeComparisons: Seq[Block] =
            (typeComparisons foldRight List(fallback)) {
              case (blk, acc) => blk(acc.head.name) :: acc
            }

          val b01 = b0.copy(
            insts = b0.insts ++ loadTpeNameInsts :+ Inst.Jump(Next(finallyTypeComparisons.head.name)))

          Seq(b01) ++
            finallyTypeComparisons ++
            individualBlocks.map(_._3) ++
            Seq(fallback) ++
            splitBlock(b1)

        case _ =>
          Seq(block)
      }
    } else {
      Seq(block)
    }

  override def preDefn = {
    case define: Defn.Define =>
      val graph = ControlFlow.Graph(define.insts)
      val newBlocks = graph.all flatMap splitBlock
      Seq(define.copy(insts = newBlocks flatMap blockToInsts))
  }


}

object InlineCaching extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    config.profileDispatchInfo match {
      case Some(info) if info.exists =>
        val dispatchInfo =
          analysis.DispatchInfoParser(Source.fromFile(info).mkString)
        new InlineCaching(dispatchInfo, 5)(top.fresh, top)

      case _ =>
        EmptyPass
    }
}
