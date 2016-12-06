package scala.scalanative
package optimizer
package pass

import scala.collection.mutable.{ Map => MutableMap }

import analysis.ClassHierarchy.Top
import analysis.ControlFlow.{ Block, Graph }
import nir.{ Defn, Inst, Local, Op, Val }
import tools.Config

/** Performs common subexpression elimination */
class CommonSubexpressionElimination extends Pass {

  /**
   * Is this Op stable? That is, does it always return the same result given the
   * same arguments?
   *
   * @param op The Op to test
   * @return true, if and only if the given Op is stable.
   */
  private def stable(op: Op): Boolean = op match {
    case _: Op.Call       => false
    case _: Op.Load       => false
    case _: Op.Store      => true
    case _: Op.Elem       => true
    case _: Op.Extract    => false
    case _: Op.Insert     => true
    case _: Op.Stackalloc => false
    case _: Op.Bin        => true
    case _: Op.Comp       => true
    case _: Op.Conv       => true
    case _: Op.Select     => true
    case _: Op.Classalloc => false
    case _                => false
  }

  /**
   * Is this Op pure? That is, does it have any side effect?
   *
   * @param op The Op to test
   * @return true, if and onyl if the given Op is pure.
   */
  private def pure(op: Op): Boolean = op match {
    case _: Op.Pure => true
    case _          => false
  }

  /**
   * Collect all the expressions that are available at the entry of `block`.
   *
   * @param block The block for which to determine the available `Op`s.
   * @return A map from Op to Local, whose pairs (op, local) mean that `op` is
   *         available to `block`
   *         under the name `local`.
   */
  private def availableBefore(block: Block): Map[Op, Local] = {
    val before = block.pred.map(availableAfter(_).toSet)
    if (before.isEmpty) Map.empty
    else before.reduce(_ intersect _).toMap
  }

  /**
   * Collect all the expressions that are available right after the last
   * expression of the block.
   *
   * @param block The block for which to determine the available `Op`s.
   * @return A map from Op to Local, whose pairs (op, local) mean that `op` is
   *         available after `block` under the name `local`.
   */
  private def availableAfter(block: Block): Map[Op, Local] = {
    val createdInBlock = block.insts.collect {
      case Inst.Let(name, op) if stable(op) && pure(op) =>
        op -> name
    }.toMap
    availableBefore(block) ++ createdInBlock
  }

  /**
   * Collect all the expressions that are available to `inst`.
   *
   * @param inst    The instruction for which to determine the available `Op`s.
   * @param inBlock The block that contains instruction `inst`.
   * @return A map from Op to Local, whose pairs (op, local) mean that `op` is
   *         available to `inst` under the name `local`.
   */
  private def available(inst: Inst, inBlock: Block): Map[Op, Local] = {
    val truncatedBlock = inBlock.copy(insts = inBlock.insts.takeWhile(_ != inst))
    availableAfter(truncatedBlock)
  }

  /**
   * Collect all the expressions that are available to `inst`.
   *
   * @param inst  The instruction for which to determine the available `Op`s.
   * @param graph The control flow graph that contains `inst`.
   * @return A map from Op to Local, whose pairs (op, local) mean that `op` is
   *         available to `inst` under the name `local`.
   */
  private def available(i: Inst, graph: Graph): Map[Op, Local] = {
    val containingBlock = graph.all.find(_.insts contains i)
    containingBlock map (available(i, _)) getOrElse Map.empty
  }

  /**
   * Perform common subexpression elimination in defn.
   *
   * @param defn The definition in which to perform CSE.
   * @return A new Defn where all redundant computations have been replaced by
   *         the previously computed value.
   */
  private def eliminate(defn: Defn): Defn = defn match {
    case d: Defn.Define =>
      val graph = Graph(d.insts)
      val newInsts = d.insts map {
        case inst @ Inst.Let(name, op) =>
          available(inst, graph) get op match {
            case Some(value) =>
              Inst.Let(name, Op.Copy(Val.Local(value, op.resty)))
            case None =>
              inst
          }
        case other =>
          other
      }
      d.copy(insts = newInsts)

    case other =>
      other
  }

  override def preAssembly = {
    case assembly =>
      assembly map eliminate
  }

}

object CommonSubexpressionElimination extends PassCompanion {
  override def apply(config: Config, top: Top) =
    new CommonSubexpressionElimination
}

