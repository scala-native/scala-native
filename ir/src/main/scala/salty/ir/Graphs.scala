package salty.ir

import Tag.Tag

sealed abstract class Root{
  var epoch: Int = 0
  def next: Seq[Root]
}
object Root {
  var lastEpoch = 0
  def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }
}

final case class Type(tag: Tag, ty: Option[Type], defn: Option[Defn]) extends Root {
  def next = ty.toSeq ++ defn.toSeq
}
object Type {
  val Null            = Type(Tag.Type.Null,    None,     None      )
  val Nothing         = Type(Tag.Type.Nothing, None,     None      )
  val Unit            = Type(Tag.Type.Unit,    None,     None      )
  val Bool            = Type(Tag.Type.Bool,    None,     None      )
  val I8              = Type(Tag.Type.I8,      None,     None      )
  val I16             = Type(Tag.Type.I16,     None,     None      )
  val I32             = Type(Tag.Type.I32,     None,     None      )
  val I64             = Type(Tag.Type.I64,     None,     None      )
  val F32             = Type(Tag.Type.F32,     None,     None      )
  val F64             = Type(Tag.Type.F64,     None,     None      )
  def Ref(ty: Type)   = Type(Tag.Type.Ref,     Some(ty), None      )
  def Slice(ty: Type) = Type(Tag.Type.Slice,   Some(ty), None      )
  def Of(defn: Defn)  = Type(Tag.Type.Of,      None,     Some(defn))

  object I {
    def unapply(ty: Type) = ty.tag match {
      case Tag.Type.I8  => Some(32)
      case Tag.Type.I16 => Some(32)
      case Tag.Type.I32 => Some(32)
      case Tag.Type.I64 => Some(64)
      case _            => None
    }
  }

  object F {
    def unapply(ty: Type) = ty.tag match {
      case Tag.Type.F32 => Some(32)
      case Tag.Type.F64 => Some(64)
      case _            => None
    }
  }
}

final case class Node(op: Op, nodes: Seq[Node], types: Seq[Type]) extends Root {
  def next = nodes ++ types
}
object Node {
  // Control-flow
  val Undefined =
    Node(Op.Undefined, Seq(), Seq())
  def In(ty: Type) =
    Node(Op.In, Seq(), Seq(ty))
  def Out(value: Node, effects: Seq[Node] = Seq()) =
    Node(Op.Out, value +: effects, Seq())
  def Return(value: Node) =
    Node(Op.Return, Seq(value), Seq())
  def Throw(value: Node) =
    Node(Op.Throw, Seq(value), Seq())
  def If(cond: Node, thenp: Node, elsep: Node) =
    Node(Op.If, Seq(thenp, elsep), Seq())
  def Switch(on: Node, default: Node, branches: Seq[Node]) =
    Node(Op.Switch, default +: branches, Seq())
  def Try(body: Node, catchb: Option[Node], finallyb: Option[Node]) =
    Node(Op.Try, catchb.getOrElse(Unit) +: finallyb.getOrElse(Unit) +: Seq(), Seq())
  def End(nodes: Seq[Node], ty: Type) =
    Node(Op.End, nodes, Seq(ty))

  // Operations
  def Bin(op: Op, left: Node, right: Node) =
    Node(op, Seq(left, right), Seq())
  def Conv(op: Op, left: Node, to: Type) =
    Node(op, Seq(left), Seq(to))
  def Is(value: Node, ty: Type) =
    Node(Op.Is, Seq(value), Seq(ty))
  def Alloc(ty: Type, elements: Option[Node] = None) =
    Node(Op.Alloc, elements.toSeq, Seq(ty))
  def Call(defn: Defn, args: Seq[Node]) =
    Node(Op.Call, args, Seq(Type.Of(defn)))
  def Phi(branches: Seq[Node]) =
    Node(Op.Phi, branches, Seq())
  def Load(ptr: Node) =
    Node(Op.Load, Seq(ptr), Seq())
  def Store(ptr: Node, value: Node) =
    Node(Op.Store, Seq(ptr, value), Seq())
  def Box(value: Node, ty: Type) =
    Node(Op.Box, Seq(value), Seq(ty))
  def Unbox(value: Node, ty: Type) =
    Node(Op.Unbox, Seq(value), Seq(ty))
  def Length(value: Node) =
    Node(Op.Length, Seq(value), Seq())
  val Catchpad =
    Node(Op.Catchpad, Seq(), Seq())
  def Elem(ptr: Node, value: Node) =
    Node(Op.Elem, Seq(ptr, value), Seq())
  def Class(ty: Type) =
    Node(Op.Class, Seq(), Seq(ty))
  def Of(defn: Defn) =
    Node(Op.Of, Seq(), Seq(Type.Of(defn)))

  // Constants
  val Null               = Node(Op.Null,        Seq(), Seq())
  val Unit               = Node(Op.Unit,        Seq(), Seq())
  val True               = Node(Op.True,        Seq(), Seq())
  val False              = Node(Op.False,       Seq(), Seq())
  def I8(value: Byte)    = Node(Op.I8(value),   Seq(), Seq())
  def I16(value: Short)  = Node(Op.I16(value),  Seq(), Seq())
  def I32(value: Int)    = Node(Op.I32(value),  Seq(), Seq())
  def I64(value: Long)   = Node(Op.I64(value),  Seq(), Seq())
  def F32(value: Float)  = Node(Op.F32(value),  Seq(), Seq())
  def F64(value: Double) = Node(Op.F64(value),  Seq(), Seq())
  def Str(value: String) = Node(Op.Str(value),  Seq(), Seq())
  def Name(value: Name)  = Node(Op.Name(value), Seq(), Seq())
}

final case class Op(tag: Tag, value: Any, mnemonic: String)
object Op {
  // Control-flow
  val Undefined = Op(Tag.Op.Undefined, (), "undefined")
  def In        = Op(Tag.Op.In,        (), "in")
  def Out       = Op(Tag.Op.Out,       (), "out")
  def Return    = Op(Tag.Op.Return,    (), "return")
  def Throw     = Op(Tag.Op.Throw,     (), "throw")
  def Jump      = Op(Tag.Op.Jump,      (), "jump")
  def If        = Op(Tag.Op.If,        (), "if")
  def Switch    = Op(Tag.Op.Switch,    (), "switch")
  def Try       = Op(Tag.Op.Try,       (), "try")
  def End       = Op(Tag.Op.End,       (), "end")

  // Binary operations
  val Add    = Op(Tag.Op.Add,    (), "add"   )
  val Sub    = Op(Tag.Op.Sub,    (), "sub"   )
  val Mul    = Op(Tag.Op.Mul,    (), "mul"   )
  val Div    = Op(Tag.Op.Div,    (), "div"   )
  val Mod    = Op(Tag.Op.Mod,    (), "mod"   )
  val Shl    = Op(Tag.Op.Shl,    (), "shl"   )
  val Lshr   = Op(Tag.Op.Lshr,   (), "lshr"  )
  val Ashr   = Op(Tag.Op.Ashr,   (), "ashr"  )
  val And    = Op(Tag.Op.And,    (), "and"   )
  val Or     = Op(Tag.Op.Or,     (), "or"    )
  val Xor    = Op(Tag.Op.Xor,    (), "xor"   )
  val Eq     = Op(Tag.Op.Eq,     (), "eq"    )
  val Equals = Op(Tag.Op.Equals, (), "equals")
  val Neq    = Op(Tag.Op.Neq,    (), "neq"   )
  val Lt     = Op(Tag.Op.Lt,     (), "lt"    )
  val Lte    = Op(Tag.Op.Lte,    (), "lte"   )
  val Gt     = Op(Tag.Op.Gt,     (), "gt"    )
  val Gte    = Op(Tag.Op.Gte,    (), "gte"   )

  // Conversion operations
  val Trunc    = Op(Tag.Op.Trunc,    (), "trunc"   )
  val Zext     = Op(Tag.Op.Zext,     (), "zext"    )
  val Sext     = Op(Tag.Op.Sext,     (), "sext"    )
  val Fptrunc  = Op(Tag.Op.Fptrunc,  (), "fptrunc" )
  val Fpext    = Op(Tag.Op.Fpext,    (), "fpext"   )
  val Fptoui   = Op(Tag.Op.Fptoui,   (), "fptoui"  )
  val Fptosi   = Op(Tag.Op.Fptosi,   (), "fptosi"  )
  val Uitofp   = Op(Tag.Op.Uitofp,   (), "uitofp"  )
  val Sitofp   = Op(Tag.Op.Sitofp,   (), "sitofp"  )
  val Ptrtoint = Op(Tag.Op.Ptrtoint, (), "ptrtoint")
  val Inttoptr = Op(Tag.Op.Inttoptr, (), "inttoptr")
  val Bitcast  = Op(Tag.Op.Bitcast,  (), "bitcast" )
  val Cast     = Op(Tag.Op.Cast,     (), "cast"    )

  // Operations
  val Is       = Op(Tag.Op.Is,       (), "is"      )
  val Alloc    = Op(Tag.Op.Alloc,    (), "alloc"   )
  val Call     = Op(Tag.Op.Call,     (), "call"    )
  val Phi      = Op(Tag.Op.Phi,      (), "phi"     )
  val Load     = Op(Tag.Op.Load,     (), "load"    )
  val Store    = Op(Tag.Op.Store,    (), "store"   )
  val Box      = Op(Tag.Op.Box,      (), "box"     )
  val Unbox    = Op(Tag.Op.Unbox,    (), "unbox"   )
  val Length   = Op(Tag.Op.Length,   (), "length"  )
  val Catchpad = Op(Tag.Op.Catchpad, (), "catchpad")
  val Elem     = Op(Tag.Op.Elem,     (), "elem"    )
  val Class    = Op(Tag.Op.Class,    (), "class"   )
  val Of       = Op(Tag.Op.Of,       (), "of"      )

  // Constants
  val Unit               = Op(Tag.Op.Unit,   (),    "unit")
  val Null               = Op(Tag.Op.Null,   null,  "null")
  val True               = Op(Tag.Op.True,   true,  "true")
  val False              = Op(Tag.Op.False,  false, "false")
  def I8(value: Byte)    = Op(Tag.Op.I8,     value, s"${value}i8")
  def I16(value: Short)  = Op(Tag.Op.I16,    value, s"${value}i16")
  def I32(value: Int)    = Op(Tag.Op.I32,    value, s"${value}i32")
  def I64(value: Long)   = Op(Tag.Op.I64,    value, s"${value}i64")
  def F32(value: Float)  = Op(Tag.Op.F32,    value, s"${value}f32")
  def F64(value: Double) = Op(Tag.Op.F64,    value, s"${value}f64")
  def Str(value: String) = Op(Tag.Op.String, value, "\"" + value + "\"")
  def Name(value: Name)  = Op(Tag.Op.Name,   value, s"name $value")
}

final case class Defn(tag: Tag, nodes: Seq[Node], meta: Seq[Meta] = Seq()) extends Root {
  def next = nodes ++ meta
}
object Defn {
  def Class(parent: Defn, ifaces: Seq[Defn], meta: Seq[Meta] = Seq()) =
    Defn(Tag.Defn.Class, Seq(),
         Meta.Parent(parent) +: ifaces.map(Meta.Interface) ++: meta)
  def Interface(ifaces: Seq[Defn], meta: Seq[Meta] = Seq()) =
    Defn(Tag.Defn.Interface, Seq(), ifaces.map(Meta.Interface) ++ meta)
  def Module(parent: Defn, ifaces: Seq[Defn], meta: Seq[Meta] = Seq()) =
    Defn(Tag.Defn.Module, Seq(),
         Meta.Parent(parent) +: ifaces.map(Meta.Interface) ++: meta)
  def Declare(in: Seq[Node], ty: Type, meta: Seq[Meta] = Seq()) =
    Defn(Tag.Defn.Declare, in :+ Node.End(Seq(Node.Undefined), ty),  meta)
  def Define(in: Seq[Node], out: Node, meta: Seq[Meta] = Seq()) =
    Defn(Tag.Defn.Define, in :+ out, meta)
  def Field(ty: Type, meta: Seq[Meta] = Seq()) =
    Defn(Tag.Defn.Field, Seq(Node.Class(ty)), meta)
  def Extern(name: Name, meta: Seq[Meta] = Seq()) =
    Defn(Tag.Defn.Extern, Seq(Node.Name(name)), meta)
}

final case class Meta(tag: Tag, defns: Seq[Defn]) extends Root {
  def next = defns
}
object Meta {
  def Parent(defn: Defn) =
    Meta(Tag.Meta.Parent, Seq(defn))
  def Interface(defn: Defn) =
    Meta(Tag.Meta.Interface, Seq(defn))
  def Overrides(defn: Defn) =
    Meta(Tag.Meta.Overrides, Seq(defn))
  def Belongs(defn: Defn) =
    Meta(Tag.Meta.Belongs, Seq(defn))
}

sealed abstract class Name
object Name {
  final case class Global(id: String) extends Name
  final case class Nested(parent: Name, child: Name) extends Name
}

final case class Scope(entries: Map[Name, Defn])
