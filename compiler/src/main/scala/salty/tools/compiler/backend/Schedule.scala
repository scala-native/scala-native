package salty.tools.compiler.backend

import scala.collection.mutable
import salty.ir, ir._

final case class Schedule(defns: Seq[Schedule.Defn]) {
  override def toString = defns.mkString("\n")

}
object Schedule {
  final case class Defn(node: Node, ops: Seq[Op])
  final case class Op(node: Node, args: Seq[Value])
  sealed abstract class Value
  object Value {
    final case class Named(node: Node) extends Value
    final case class Struct(values: Seq[Value]) extends Value
    final case class Const(node: Node) extends Value
  }

  private class CollectDefns extends salty.ir.Pass {
    val defns = mutable.ArrayBuffer[Defn]()

    def paramTypeNames(params: Seq[Node]): Seq[Name] =
      params.map { case Param(ty) => ty.name }

    def onNode(n: Node): Unit =
      n.desc match {
        case Desc.Empty | _: Desc.Prim =>
          ()
        case _: Desc.Defn =>
          defns += Defn(n, Seq())
        case _ =>
          ()
      }
  }

  private def toValue(n: Node): (Value, Seq[Node]) = n match {
    case Lit.Struct(deps) =>
      val pairs = deps.map(toValue)
      (Value.Struct(pairs.map(_._1)), pairs.flatMap(_._2))
    case _ if n.desc.isInstanceOf[Desc.Lit] =>
      (Value.Const(n), Seq())
    case _ =>
      (Value.Named(n), Seq(n))
  }

  private def schedule(n: Node, done: Seq[Op]): Seq[Op] =
    if (done.map(_.node).contains(n) || n.desc.isInstanceOf[Desc.Defn])
      done
    else {
      val argsbuf = mutable.ArrayBuffer[Value]()
      val nodebuf = mutable.ArrayBuffer[Node]()
      n.deps.foreach { dep =>
        if (dep.isDefn || (dep eq Empty))
          ()
        else if (dep.isEf || dep.isCf)
          nodebuf += dep.dep
        else {
          val (value, named) = toValue(dep.dep)
          argsbuf += value
          nodebuf ++= named
        }
      }
      var dn = done
      nodebuf.foreach { n =>
        dn = schedule(n, dn)
      }
      if (n.desc eq Desc.End)
        dn
      else
        dn :+ Op(n, argsbuf.toSeq)
    }

  def apply(node: Node): Schedule = {
    val collectDefns = new CollectDefns
    Pass.run(collectDefns, node)
    val defns = collectDefns.defns.toSeq.map { defn =>
      defn.node match {
        case ir.Defn.Define(_, _, end) =>
          defn.copy(ops = schedule(end, Seq()))
        case _ =>
          defn
      }
    }
    Schedule(defns)
  }
}

