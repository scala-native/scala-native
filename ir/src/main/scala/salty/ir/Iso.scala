package salty
package ir

import scala.collection.{mutable => mut}
import salty.ir.{Tag => T}
import salty.util.ScopedVar, ScopedVar.scoped
import Graph.{Const => C}

final case class Graph(var nodes: Seq[Graph.Node])
object Graph {
  private var lastEpoch = 0
  private def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }

  sealed abstract class Const
  object Const {
    final case object Unit extends Const
    final case object Null extends Const
    final case object True extends Const
    final case object False extends Const
    final case class I8(value: Byte) extends Const
    final case class I16(value: Short) extends Const
    final case class I32(value: Int) extends Const
    final case class I64(value: Long) extends Const
    final case class F32(value: Float) extends Const
    final case class F64(value: Double) extends Const
    final case class Str(value: String) extends Const

    sealed abstract class Name extends Const
    object Name {
      final case object No extends Name
      final case class Simple(id: String) extends Name
      final case class Nested(parent: Name, child: Name) extends Name
    }

    sealed abstract class Shape extends Const {
      def holes: Int = this match {
        case Shape.Hole         => 1
        case Shape.Ref(shape)   => shape.holes
        case Shape.Slice(shape) => shape.holes
      }
    }
    object Shape {
      final case object Hole extends Shape
      final case class Ref(of: Shape) extends Shape
      final case class Slice(of: Shape) extends Shape
      // TODO: Func(ret, args)
    }
  }

  final case class Desc(tag: Int,
                        cfIn: Int, efIn: Int, valIn: Int, refIn: Int,
                        const: C = C.Unit) {
    def totalIn: Int = cfIn + efIn + valIn + refIn
  }
  object Desc {
    val Start                       = Desc(T.Start        , 0, 0, 0, 0)
    def Label(name: C.Name, n: Int) = Desc(T.Label        , n, 0, 0, 0, name)
    val If                          = Desc(T.If           , 1, 0, 1, 0)
    val Switch                      = Desc(T.Switch       , 1, 0, 1, 0)
    val Try                         = Desc(T.Try          , 1, 0, 0, 0)
    val CaseTrue                    = Desc(T.CaseTrue     , 1, 0, 0, 0)
    val CaseFalse                   = Desc(T.CaseFalse    , 1, 0, 0, 0)
    val CaseConst                   = Desc(T.CaseConst    , 1, 0, 1, 0)
    val CaseDefault                 = Desc(T.CaseDefault  , 1, 0, 0, 0)
    val CaseException               = Desc(T.CaseException, 1, 0, 0, 0)
    def Merge(n: Int)               = Desc(T.Merge        , n, 0, 0, 0)
    val Return                      = Desc(T.Return       , 1, 1, 1, 0)
    val Throw                       = Desc(T.Throw        , 1, 1, 1, 0)
    val Undefined                   = Desc(T.Undefined    , 1, 1, 0, 0)
    def End(n: Int)                 = Desc(T.End          , n, 0, 0, 0)

    def EfPhi(n: Int) = Desc(T.EfPhi , 1, n, 0,     0)
    val Equals        = Desc(T.Equals, 0, 1, 2,     0)
    def Call(n: Int)  = Desc(T.Call  , 0, 1, n + 1, 0)
    val Load          = Desc(T.Load  , 0, 1, 1,     0)
    val Store         = Desc(T.Store , 0, 1, 2,     0)

    val Add  = Desc(T.Add , 0, 0, 2, 0)
    val Sub  = Desc(T.Sub , 0, 0, 2, 0)
    val Mul  = Desc(T.Mul , 0, 0, 2, 0)
    val Div  = Desc(T.Div , 0, 0, 2, 0)
    val Mod  = Desc(T.Mod , 0, 0, 2, 0)
    val Shl  = Desc(T.Shl , 0, 0, 2, 0)
    val Lshr = Desc(T.Lshr, 0, 0, 2, 0)
    val Ashr = Desc(T.Ashr, 0, 0, 2, 0)
    val And  = Desc(T.And , 0, 0, 2, 0)
    val Or   = Desc(T.Or  , 0, 0, 2, 0)
    val Xor  = Desc(T.Xor , 0, 0, 2, 0)
    val Eq   = Desc(T.Eq  , 0, 0, 2, 0)
    val Neq  = Desc(T.Neq , 0, 0, 2, 0)
    val Lt   = Desc(T.Lt  , 0, 0, 2, 0)
    val Lte  = Desc(T.Lte , 0, 0, 2, 0)
    val Gt   = Desc(T.Gt  , 0, 0, 2, 0)
    val Gte  = Desc(T.Gte , 0, 0, 2, 0)

    val Trunc    = Desc(T.Trunc   , 0, 0, 1, 1)
    val Zext     = Desc(T.Zext    , 0, 0, 1, 1)
    val Sext     = Desc(T.Sext    , 0, 0, 1, 1)
    val Fptrunc  = Desc(T.Fptrunc , 0, 0, 1, 1)
    val Fpext    = Desc(T.Fpext   , 0, 0, 1, 1)
    val Fptoui   = Desc(T.Fptoui  , 0, 0, 1, 1)
    val Fptosi   = Desc(T.Fptosi  , 0, 0, 1, 1)
    val Uitofp   = Desc(T.Uitofp  , 0, 0, 1, 1)
    val Sitofp   = Desc(T.Sitofp  , 0, 0, 1, 1)
    val Ptrtoint = Desc(T.Ptrtoint, 0, 0, 1, 1)
    val Inttoptr = Desc(T.Inttoptr, 0, 0, 1, 1)
    val Bitcast  = Desc(T.Bitcast , 0, 0, 1, 1)
    val Cast     = Desc(T.Cast    , 0, 0, 1, 1)
    val Box      = Desc(T.Box     , 0, 0, 1, 1)
    val Unbox    = Desc(T.Unbox   , 0, 0, 1, 1)

    def Phi(n: Int)          = Desc(T.Phi        , 1, 0, n, 0)
    val Is                   = Desc(T.Is         , 0, 0, 1, 1)
    val Alloc                = Desc(T.Alloc      , 0, 0, 0, 1)
    val Salloc               = Desc(T.Salloc     , 0, 0, 1, 1)
    val Length               = Desc(T.Length     , 0, 0, 1, 0)
    val Elem                 = Desc(T.Elem       , 0, 0, 2, 0)
    def Param(name: C.Name)  = Desc(T.Param      , 0, 0, 0, 0, name)
    val ValueOf              = Desc(T.ValueOf    , 0, 0, 0, 1)
    val ExceptionOf          = Desc(T.ExceptionOf, 1, 0, 0, 0)
    val TagOf                = Desc(T.TagOf      , 0, 0, 1, 0)
    def Const(c: C)          = Desc(T.Const      , 0, 0, 0, 0, c)
    val TagConst             = Desc(T.TagConst   , 0, 0, 0, 1)

    def Class(name: C.Name, rels: Int)                = Desc(T.Class,     0, 0, 0,      rels,        name)
    def Interface(name: C.Name, rels: Int)            = Desc(T.Interface, 0, 0, 0,      rels,        name)
    def Module(name: C.Name, rels: Int)               = Desc(T.Module,    0, 0, 0,      rels,        name)
    def Declare(name: C.Name, params: Int, rels: Int) = Desc(T.Declare,   0, 0, params, 1 + rels,    name)
    def Define(name: C.Name, params: Int, rels: Int)  = Desc(T.Define,    1, 0, params, 1 + rels,    name)
    def Field(name: C.Name, rels: Int)                = Desc(T.Field,     0, 0, 0,      1 + rels,    name)
    def Extern(name: C.Name)                          = Desc(T.Extern,    0, 0, 0,      0,           name)
    def Type(shape: C.Shape)                          = Desc(T.Type,      0, 0, 0,      shape.holes, shape)
  }

  final case class Node(id: Int, var desc: Desc, var edges: Seq[Node]) {
    private[Graph] var epoch: Int = 0
  }

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

    private implicit def node(node: ir.Node): Graph.Node =
      if (nodes.contains(node))
        nodes(node)
      else {
        lastId += 1
        val gnode = Graph.Node(lastId, null, null)
        worklist.push(node)
        gnode
      }

    private def n(desc: Desc, edges: Graph.Node*): Unit = {
      assert(desc.totalIn == edges.length)
      curNode.desc  = desc
      curNode.edges = edges
    }

    private def name(nm: ir.Name): C.Name = nm match {
      case Name.Global(s)    => C.Name.Simple(s)
      case Name.Nested(a, b) => C.Name.Nested(name(a), name(b))
    }

    private def edges(node: ir.Node): Unit = node match {
      case ty: ir.Type     => edges(ty)
      case instr: ir.Instr => edges(instr)
      case defn: ir.Defn   => edges(defn)
    }

    private def edges(ty: ir.Type): Unit = {
      var nodes = Seq.empty[Graph.Node]
      def loop(ty: ir.Type): C.Shape = ty match {
        case ir.Type.Ref(ty)   => C.Shape.Ref(loop(ty))
        case ir.Type.Slice(ty) => C.Shape.Slice(loop(ty))
        case ir.Type.Of(defn)  => nodes = node(defn) +: nodes; C.Shape.Hole
        case _                 => ???; C.Shape.Hole
      }
      n(Desc.Type(loop(ty)), nodes: _*)
    }

    private def edges(instr: ir.Instr): Unit = instr match {
      case Instr.Start()           => n(Desc.Start)
      case Instr.Label(nm, cfs)    => n(Desc.Label(name(nm), cfs.length), cfs.map(node): _*)
      case Instr.If(cf, v)         => n(Desc.If, cf, v)
      case Instr.Switch(cf, v)     => n(Desc.Switch, cf, v)
      case Instr.Try(cf)           => n(Desc.Try, cf)
      case Instr.CaseTrue(cf)      => n(Desc.CaseTrue, cf)
      case Instr.CaseFalse(cf)     => n(Desc.CaseFalse, cf)
      case Instr.CaseConst(cf, c)  => n(Desc.CaseConst, cf, c)
      case Instr.CaseDefault(cf)   => n(Desc.CaseDefault, cf)
      case Instr.CaseException(cf) => n(Desc.CaseException, cf)
      case Instr.Return(cf, ef, v) => n(Desc.Return, cf, ef, v)
      case Instr.Throw(cf, ef, v)  => n(Desc.Throw, cf, ef, v)
      case Instr.Undefined(cf, ef) => n(Desc.Undefined, cf, ef)
      case Instr.Merge(cfs)        => n(Desc.Merge(cfs.length), cfs.map(node): _*)
      case Instr.End(cfs)          => n(Desc.End(cfs.length), cfs.map(node): _*)

      case Instr.EfPhi(cf, efs)          => n(Desc.EfPhi(efs.length), (node(cf) +: efs.map(node)): _*)
      case Instr.Equals(ef, left, right) => n(Desc.Equals, ef, left, right)
      case Instr.Call(ef, ptr, args)     => n(Desc.Call(args.length), (node(ef) +: node(ptr) +: args.map(node)): _*)
      case Instr.Load(ef, ptr)           => n(Desc.Load, ef, ptr)
      case Instr.Store(ef, ptr, value)   => n(Desc.Store, ef, ptr, value)

      case Instr.Add (left, right) => n(Desc.Add,  left, right)
      case Instr.Sub (left, right) => n(Desc.Sub,  left, right)
      case Instr.Mul (left, right) => n(Desc.Mul,  left, right)
      case Instr.Div (left, right) => n(Desc.Div,  left, right)
      case Instr.Mod (left, right) => n(Desc.Mod,  left, right)
      case Instr.Shl (left, right) => n(Desc.Shl,  left, right)
      case Instr.Lshr(left, right) => n(Desc.Lshr, left, right)
      case Instr.Ashr(left, right) => n(Desc.Ashr, left, right)
      case Instr.And (left, right) => n(Desc.And,  left, right)
      case Instr.Or  (left, right) => n(Desc.Or,   left, right)
      case Instr.Xor (left, right) => n(Desc.Xor,  left, right)
      case Instr.Eq  (left, right) => n(Desc.Eq,   left, right)
      case Instr.Neq (left, right) => n(Desc.Neq,  left, right)
      case Instr.Lt  (left, right) => n(Desc.Lt,   left, right)
      case Instr.Lte (left, right) => n(Desc.Lte,  left, right)
      case Instr.Gt  (left, right) => n(Desc.Gt,   left, right)
      case Instr.Gte (left, right) => n(Desc.Gte,  left, right)

      case Instr.Trunc   (value, ty) => n(Desc.Trunc   , value, ty)
      case Instr.Zext    (value, ty) => n(Desc.Zext    , value, ty)
      case Instr.Sext    (value, ty) => n(Desc.Sext    , value, ty)
      case Instr.Fptrunc (value, ty) => n(Desc.Fptrunc , value, ty)
      case Instr.Fpext   (value, ty) => n(Desc.Fpext   , value, ty)
      case Instr.Fptoui  (value, ty) => n(Desc.Fptoui  , value, ty)
      case Instr.Fptosi  (value, ty) => n(Desc.Fptosi  , value, ty)
      case Instr.Uitofp  (value, ty) => n(Desc.Uitofp  , value, ty)
      case Instr.Sitofp  (value, ty) => n(Desc.Sitofp  , value, ty)
      case Instr.Ptrtoint(value, ty) => n(Desc.Ptrtoint, value, ty)
      case Instr.Inttoptr(value, ty) => n(Desc.Inttoptr, value, ty)
      case Instr.Bitcast (value, ty) => n(Desc.Bitcast , value, ty)
      case Instr.Cast    (value, ty) => n(Desc.Cast    , value, ty)
      case Instr.Box     (value, ty) => n(Desc.Box     , value, ty)
      case Instr.Unbox   (value, ty) => n(Desc.Unbox   , value, ty)

      case Instr.Is(value, ty)      => n(Desc.Is,                 value, ty)
      case Instr.Alloc(ty)          => n(Desc.Alloc,              ty)
      case Instr.Salloc(ty, k)      => n(Desc.Salloc,             ty, k)
      case Instr.Phi(merge, values) => n(Desc.Phi(values.length), (node(merge) +: values.map(node)): _*)
      case Instr.Length(value)      => n(Desc.Length,             value)
      case Instr.Elem(ptr, value)   => n(Desc.Elem,               ptr, value)
      case Instr.Param(nm, ty)      => n(Desc.Param(name(nm)),    ty)
      case Instr.ValueOf(defn)      => n(Desc.ValueOf,            defn)
      case Instr.ExceptionOf(cf)    => n(Desc.ExceptionOf,        cf)
      case Instr.TagOf(value)       => n(Desc.TagOf,              value)
      case c: Instr.Const           => const(c)
    }

    def const(c: ir.Instr.Const): Unit = c match {
      case Instr.Unit    => n(Desc.Const(C.Unit))
      case Instr.Null    => n(Desc.Const(C.Null))
      case Instr.True    => n(Desc.Const(C.True))
      case Instr.False   => n(Desc.Const(C.False))
      case Instr.I8(v)   => n(Desc.Const(C.I8(v)))
      case Instr.I16(v)  => n(Desc.Const(C.I16(v)))
      case Instr.I32(v)  => n(Desc.Const(C.I32(v)))
      case Instr.I64(v)  => n(Desc.Const(C.I64(v)))
      case Instr.F32(v)  => n(Desc.Const(C.F32(v)))
      case Instr.F64(v)  => n(Desc.Const(C.F64(v)))
      case Instr.Str(v)  => n(Desc.Const(C.Str(v)))
      case Instr.Tag(ty) => n(Desc.TagConst, ty)
    }

    private def edges(defn: ir.Defn): Unit = {
      val rels = defn.rels.map(rel => node(rel.defn))
      defn match {
        case Defn.Class(nm, _) =>
          n(Desc.Class(name(nm), rels.length), rels: _*)
        case Defn.Interface(nm, _) =>
          n(Desc.Interface(name(nm), rels.length), rels: _*)
        case Defn.Module(nm, _) =>
          n(Desc.Module(name(nm), rels.length), rels: _*)
        case Defn.Extern(nm, _) =>
          n(Desc.Extern(name(nm)))
        case Defn.Declare(nm, ty, in, _) =>
          val nodes = (in.map(node) :+ node(ty)) ++ rels
          n(Desc.Declare(name(nm), in.length, rels.length), nodes: _*)
        case Defn.Define(nm, ty, in, out, _) =>
          val nodes = (node(out) +: in.map(node)) ++ (node(ty) +: rels)
          n(Desc.Define(name(nm), in.length, rels.length), nodes: _*)
        case Defn.Field(nm, ty, _) =>
          n(Desc.Field(name(nm), rels.length), rels: _*)
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
          node.edges.foreach(loop)
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
