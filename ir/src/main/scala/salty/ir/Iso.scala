package salty
package ir

import scala.collection.{mutable => mut}
import salty.ir.{Tag => T}
import salty.util.ScopedVar, ScopedVar.scoped

final case class Graph(var nodes: Seq[Graph.Node])
object Graph {
  private var lastEpoch = 0
  private def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }

  final case class Node(id: Int, var tag: Int, var edges: Seq[Graph.Edge]) {
    private[Graph] var epoch: Int = 0
  }
  final case class Edge(tag: Int, to: Graph.Node)

  final class To {
    private val nodes    = mut.Map.empty[ir.Node, Graph.Node]
    private var worklist = new mut.Stack[ir.Node]
    private val curNode  = new salty.util.ScopedVar[Graph.Node]
    private var lastId   = 0

    def +=(n: ir.Node): Unit = {
      node(n)
      while (worklist.nonEmpty) {
        val n = worklist.pop
        scoped (
          curNode := nodes(n)
        ) {
          edges(n)
        }
      }
    }

    def graph: Graph = Graph(nodes.values.toSeq)

    private def node(node: ir.Node): Graph.Node =
      if (nodes.contains(node))
        nodes(node)
      else {
        lastId += 1
        val gnode = Graph.Node(lastId, 0, mut.Seq.empty)
        worklist.push(node)
        gnode
      }

    private def edges(node: ir.Node): Seq[Edge] = node match {
      case ty: ir.Type     => edges(ty)
      case instr: ir.Instr => edges(instr)
      case defn: ir.Defn   => edges(defn)
    }

    private def t(tag: Int) =
      curNode.tag = tag
    private def e(tag: Int, to: Graph.Node): Unit = {
      val node = curNode.get
      node.edges = node.edges :+ Edge(tag, to)
    }

    private def type_(to: ir.Node)         = e(T.Type,      node(to))
    private def ref_(to: ir.Node)          = e(T.Ref,       node(to))
    private def cf_(to: ir.Node)           = e(T.Cf,        node(to))
    private def ef_(to: ir.Node)           = e(T.Ef,        node(to))
    private def val_(i: Int, to: ir.Node)  = e(T.Val + i,   node(to))
    private def instr(i: Int, to: ir.Node) = e(T.Instr + i, node(to))

    private def vals(base: Int, vals: Seq[ir.Instr.Val]) =
      vals.zipWithIndex.foreach { case (v, i) => val_(base + i, v) }
    private def instrs(base: Int, instrs: Seq[ir.Instr]) =
      instrs.zipWithIndex.foreach { case (v, i) => instr(base + i, v) }

    private def edges(ty: ir.Type): Unit = ty match {
      case ir.Type.Ref(nty)   => type_(nty)
      case ir.Type.Slice(nty) => type_(nty)
      case ir.Type.Of(defn)   => ref_(defn)
      case _                  => ()
    }

    private def edges(instr: ir.Instr): Unit = instr match {
      case Instr.Start()           => t(T.Start)
      case Instr.Label(_, cfs)     => t(T.Label); cfs.foreach(cf_)
      case Instr.If(cf, v)         => t(T.If); cf_(cf); val_(0, v)
      case Instr.Switch(cf, v)     => t(T.Switch); cf_(cf); val_(0, v)
      case Instr.Try(cf)           => t(T.Try); cf_(cf)
      case Instr.CaseTrue(cf)      => t(T.CaseTrue); cf_(cf)
      case Instr.CaseFalse(cf)     => t(T.CaseFalse); cf_(cf)
      case Instr.CaseConst(cf, c)  => t(T.CaseConst); cf_(cf); val_(0, c)
      case Instr.CaseDefault(cf)   => t(T.CaseDefault); cf_(cf)
      case Instr.CaseException(cf) => t(T.CaseException); cf_(cf)
      case Instr.Return(cf, ef, v) => t(T.Return); cf_(cf); ef_(ef); val_(0, v)
      case Instr.Throw(cf, ef, v)  => t(T.Throw); cf_(cf); ef_(ef); val_(0, v)
      case Instr.Undefined(cf, ef) => t(T.Undefined); cf_(cf); ef_(ef)
      case Instr.Merge(cfs)        => t(T.Merge); cfs.foreach(cf_)
      case Instr.End(cfs)          => t(T.End); cfs.foreach(cf_)

      case Instr.EfPhi(cf, efs)          => t(T.EfPhi); cf_(cf); efs.foreach(ef_)
      case Instr.Equals(ef, left, right) => t(T.Equals); ef_(ef); val_(0, left); val_(1, right)
      case Instr.Call(ef, ptr, args)     => t(T.Call); ef_(ef); val_(0, ptr); vals(1, args)
      case Instr.Load(ef, ptr)           => t(T.Load); ef_(ef); val_(0, ptr)
      case Instr.Store(ef, ptr, value)   => t(T.Store); ef_(ef); val_(0, ptr); val_(1, value)

      case Instr.Add (left, right) => t(T.Add ); val_(0, left); val_(1, right)
      case Instr.Sub (left, right) => t(T.Sub ); val_(0, left); val_(1, right)
      case Instr.Mul (left, right) => t(T.Mul ); val_(0, left); val_(1, right)
      case Instr.Div (left, right) => t(T.Div ); val_(0, left); val_(1, right)
      case Instr.Mod (left, right) => t(T.Mod ); val_(0, left); val_(1, right)
      case Instr.Shl (left, right) => t(T.Shl ); val_(0, left); val_(1, right)
      case Instr.Lshr(left, right) => t(T.Lshr); val_(0, left); val_(1, right)
      case Instr.Ashr(left, right) => t(T.Ashr); val_(0, left); val_(1, right)
      case Instr.And (left, right) => t(T.And ); val_(0, left); val_(1, right)
      case Instr.Or  (left, right) => t(T.Or  ); val_(0, left); val_(1, right)
      case Instr.Xor (left, right) => t(T.Xor ); val_(0, left); val_(1, right)
      case Instr.Eq  (left, right) => t(T.Eq  ); val_(0, left); val_(1, right)
      case Instr.Neq (left, right) => t(T.Neq ); val_(0, left); val_(1, right)
      case Instr.Lt  (left, right) => t(T.Lt  ); val_(0, left); val_(1, right)
      case Instr.Lte (left, right) => t(T.Lte ); val_(0, left); val_(1, right)
      case Instr.Gt  (left, right) => t(T.Gt  ); val_(0, left); val_(1, right)
      case Instr.Gte (left, right) => t(T.Gte ); val_(0, left); val_(1, right)

      case T.Trunc => Instr.Trunc(val_(0), type_(ty))

      case Instr.Trunc   (value, ty) => t(T.Trunc   ); val_(0, value); type_(ty)
      case Instr.Zext    (value, ty) => t(T.Zext    ); val_(0, value); type_(ty)
      case Instr.Sext    (value, ty) => t(T.Sext    ); val_(0, value); type_(ty)
      case Instr.Fptrunc (value, ty) => t(T.Fptrunc ); val_(0, value); type_(ty)
      case Instr.Fpext   (value, ty) => t(T.Fpext   ); val_(0, value); type_(ty)
      case Instr.Fptoui  (value, ty) => t(T.Fptoui  ); val_(0, value); type_(ty)
      case Instr.Fptosi  (value, ty) => t(T.Fptosi  ); val_(0, value); type_(ty)
      case Instr.Uitofp  (value, ty) => t(T.Uitofp  ); val_(0, value); type_(ty)
      case Instr.Sitofp  (value, ty) => t(T.Sitofp  ); val_(0, value); type_(ty)
      case Instr.Ptrtoint(value, ty) => t(T.Ptrtoint); val_(0, value); type_(ty)
      case Instr.Inttoptr(value, ty) => t(T.Inttoptr); val_(0, value); type_(ty)
      case Instr.Bitcast (value, ty) => t(T.Bitcast ); val_(0, value); type_(ty)
      case Instr.Cast    (value, ty) => t(T.Cast    ); val_(0, value); type_(ty)
      case Instr.Box     (value, ty) => t(T.Box     ); val_(0, value); type_(ty)
      case Instr.Unbox   (value, ty) => t(T.Unbox   ); val_(0, value); type_(ty)

      case Instr.Is(value, ty)      => t(T.Is); val_(0, value); type_(ty)
      case Instr.Alloc(ty)          => t(T.Alloc); type_(ty)
      case Instr.Salloc(ty, n)      => t(T.Salloc); type_(ty); val_(0, n)
      case Instr.Phi(merge, values) => t(T.Phi); cf_(merge); vals(0, values)
      case Instr.Length(value)      => t(T.Length); val_(0, value)
      case Instr.Elem(ptr, value)   => t(T.Elem); val_(0, ptr); val_(1, value)
      case Instr.Param(_, ty)       => t(T.Param); type_(ty)
      case Instr.ValueOf(defn)      => t(T.ValueOf); ref_(defn)
      case Instr.ExceptionOf(cf)    => t(T.ExceptionOf); cf_(cf)
      case Instr.TagOf(value)       => t(T.TagOf); val_(0, value)

      case Instr.Tag(ty)           => type_(ty)
      case _: Instr.Const          => () // TODO: store values somewhere
    }

    private def edges(defn: ir.Defn): Unit = {
      defn.rels.foreach {
        case Rel.Child(to)      => e(T.Child,      node(to))
        case Rel.Implements(to) => e(T.Implements, node(to))
        case Rel.Overrides(to)  => e(T.Overrides,  node(to))
        case Rel.Belongs(to)    => e(T.Belongs,    node(to))
      }
      defn match {
        case _: Defn.Class                  => t(T.Class)
        case _: Defn.Interface              => t(T.Interface)
        case _: Defn.Module                 => t(T.Module)
        case _: Defn.Extern                 => t(T.Extern)
        case Defn.Declare(_, ty, in, _)     => t(T.Declare); type_(ty); instrs(0, in)
        case Defn.Define(_, ty, in, out, _) => t(T.Define); type_(ty); instrs(0, in); instr(in.length, out)
        case Defn.Field(_, ty, _)           => t(T.Field); type_(ty)
      }
    }
  }
  object To {
    def apply(entries: Seq[ir.Node]): Graph = {
      val to = new To
      entries.foreach(to += _)
      to.graph
    }
  }

  abstract class Pass {
    def onEntry(node: Graph.Node): Unit
    def onNode(node: Graph.Node): Unit
  }
  object Pass {
    def run(pass: Pass, entry: Graph.Node) = {
      val epoch = nextEpoch
      def loop(node: Graph.Node): Unit =
        if (node.epoch < epoch) {
          node.epoch = epoch
          pass.onNode(node)
          node.edges.foreach(e => loop(e.to))
        }
      pass.onEntry(entry)
      loop(entry)
    }
  }

  final class From(g: Graph) extends Pass{
    var entries = Seq.empty[ir.Node]
    def onEntry(node: Node): Unit = ???
    def onNode(node: Node): Unit = ???
  }
  object From {
    def apply(g: Graph, entries: Seq[Graph.Node]) = {
      val pass = new From(g)
      entries.foreach(Pass.run(pass, _))
      pass.entries
    }
  }
}
