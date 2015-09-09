package salty
package ir

import Tag.Tag

sealed abstract class Node {
  var epoch: Int = 0
  def next: Seq[Node]
}
object Node {
  var lastEpoch = 0
  def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }
}

final case class Type(tag: Tag, ty: Option[Type], defn: Option[Defn]) extends Node {
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

final case class Instr(op: Op, nodes: Seq[Instr]) extends Node {
  def next = op.next ++ nodes
}
object Instr {
  def apply(op: Op): Instr = Instr(op, Seq())

  // Control-flow
  // TODO: Loop, Merge
  // TODO: Try, CaseTry, CaseCatch, CaseFinally
  def Start()                                    = Instr(Op.Start)
  def If(cf: Instr, value: Instr)                = Instr(Op.If, Seq(cf, value))
  def IfTrue(cf: Instr)                          = Instr(Op.IfTrue, Seq(cf))
  def IfFalse(cf: Instr)                         = Instr(Op.IfFalse, Seq(cf))
  def Switch(cf: Instr, value: Instr)            = Instr(Op.Switch, Seq(cf, value))
  def CaseValue(cf: Instr, value: Val)           = Instr(Op.CaseValue(value), Seq(cf))
  def CaseDefault(cf: Instr)                     = Instr(Op.CaseDefault, Seq(cf))
  def Throw(cf: Instr, ef: Instr, value: Instr)  = Instr(Op.Throw, Seq(cf, ef, value))
  def Undefined(cf: Instr, ef: Instr)            = Instr(Op.Undefined, Seq(cf, ef))
  def Out(cf: Instr, ef: Instr, value: Instr)    = Instr(Op.Out, Seq(cf, ef, value))
  def Return(cf: Instr, ef: Instr, value: Instr) = Instr(Op.Return, Seq(cf, ef, value))
  def End(cfs: Seq[Instr])                       = Instr(Op.End, cfs)

  // Operations
  // TODO: Catchpad
  def Bin(op: Op, left: Instr, right: Instr)        = Instr(op, Seq(left, right))
  def Conv(op: Op, left: Instr, ty: Type)           = Instr(op, Seq(left, Type(ty)))
  def Is(value: Instr, ty: Type)                    = Instr(Op.Is, Seq(value, Type(ty)))
  def Equals(ef: Instr, left: Instr, right: Instr)  = Instr(Op.Equals, Seq(ef, left, right))
  def Alloc(ef: Instr, ty: Type)                    = Instr(Op.Alloc, Seq(ef, Type(ty)))
  def Salloc(ef: Instr, ty: Type, n: Instr)         = Instr(Op.Salloc, Seq(ef, Type(ty), n))
  def Call(ef: Instr, defn: Defn, args: Seq[Instr]) = Instr(Op.Call, Defn(defn) +: args)
  def Phi(merge: Instr, values: Seq[Instr])         = Instr(Op.Phi, merge +: values)
  def Load(ef: Instr, ptr: Instr)                   = Instr(Op.Load, Seq(ef, ptr))
  def Store(ef: Instr, ptr: Instr, value: Instr)    = Instr(Op.Store, Seq(ef, ptr, value))
  def Box(value: Instr, ty: Type)                   = Instr(Op.Box, Seq(value, Type(ty)))
  def Unbox(value: Instr, ty: Type)                 = Instr(Op.Unbox, Seq(value, Type(ty)))
  def Length(value: Instr)                          = Instr(Op.Length, Seq(value))
  def Elem(ptr: Instr, value: Instr)                = Instr(Op.Elem, Seq(ptr, value))
  def Class(ty: Type)                               = Instr(Op.Class, Seq(Type(ty)))

  // Value-like instructions
  def In(ty: Type)     = Instr(Op.In, Seq(Type(ty)))
  def Val(v: Val)      = Instr(Op.Val(v))
  def Type(ty: Type)   = Instr(Op.Type(ty))
  def Defn(defn: Defn) = Instr(Op.Defn(defn))
  def Name(name: Name) = Instr(Op.Name(name))

  // Common value constants
  val Unit  = Instr.Val(ir.Val.Unit)
  val Null  = Instr.Val(ir.Val.Unit)
  val True  = Instr.Val(ir.Val.Unit)
  val False = Instr.Val(ir.Val.Unit)
}

final case class Op(tag: Tag, value: Any, mnemonic: String) {
  def next = tag match {
    case Tag.Op.Type => value.asInstanceOf[Type].next
    case Tag.Op.Defn => Seq(value.asInstanceOf[Defn])
    case _           => Seq()
  }
}
object Op {
  def apply(tag: Tag, mnemonic: String): Op = Op(tag, ir.Val.Unit, mnemonic)

  // Control-flow
  val Start             = Op(Tag.Op.Start,        "start")
  val If                = Op(Tag.Op.If,           "if")
  val IfTrue            = Op(Tag.Op.IfTrue,       "if-true")
  val IfFalse           = Op(Tag.Op.IfFalse,      "if-false")
  val Switch            = Op(Tag.Op.Switch,       "switch")
  def CaseValue(v: Val) = Op(Tag.Op.CaseValue, v, s"case-$v")
  val CaseDefault       = Op(Tag.Op.CaseDefault,  "case-default")
  val Throw             = Op(Tag.Op.Throw,        "throw")
  val Out               = Op(Tag.Op.Out,          "out")
  val Return            = Op(Tag.Op.Return,       "return")
  val Undefined         = Op(Tag.Op.Undefined,    "undefined")
  val End               = Op(Tag.Op.End,          "end")

  // Binary operations
  val Add  = Op(Tag.Op.Add,    "add"   )
  val Sub  = Op(Tag.Op.Sub,    "sub"   )
  val Mul  = Op(Tag.Op.Mul,    "mul"   )
  val Div  = Op(Tag.Op.Div,    "div"   )
  val Mod  = Op(Tag.Op.Mod,    "mod"   )
  val Shl  = Op(Tag.Op.Shl,    "shl"   )
  val Lshr = Op(Tag.Op.Lshr,   "lshr"  )
  val Ashr = Op(Tag.Op.Ashr,   "ashr"  )
  val And  = Op(Tag.Op.And,    "and"   )
  val Or   = Op(Tag.Op.Or,     "or"    )
  val Xor  = Op(Tag.Op.Xor,    "xor"   )
  val Eq   = Op(Tag.Op.Eq,     "eq"    )
  val Neq  = Op(Tag.Op.Neq,    "neq"   )
  val Lt   = Op(Tag.Op.Lt,     "lt"    )
  val Lte  = Op(Tag.Op.Lte,    "lte"   )
  val Gt   = Op(Tag.Op.Gt,     "gt"    )
  val Gte  = Op(Tag.Op.Gte,    "gte"   )

  // Conversion operations
  val Trunc    = Op(Tag.Op.Trunc,    "trunc"   )
  val Zext     = Op(Tag.Op.Zext,     "zext"    )
  val Sext     = Op(Tag.Op.Sext,     "sext"    )
  val Fptrunc  = Op(Tag.Op.Fptrunc,  "fptrunc" )
  val Fpext    = Op(Tag.Op.Fpext,    "fpext"   )
  val Fptoui   = Op(Tag.Op.Fptoui,   "fptoui"  )
  val Fptosi   = Op(Tag.Op.Fptosi,   "fptosi"  )
  val Uitofp   = Op(Tag.Op.Uitofp,   "uitofp"  )
  val Sitofp   = Op(Tag.Op.Sitofp,   "sitofp"  )
  val Ptrtoint = Op(Tag.Op.Ptrtoint, "ptrtoint")
  val Inttoptr = Op(Tag.Op.Inttoptr, "inttoptr")
  val Bitcast  = Op(Tag.Op.Bitcast,  "bitcast" )
  val Cast     = Op(Tag.Op.Cast,     "cast"    )

  // Scala-specific binary operations
  val Is     = Op(Tag.Op.Is,     "is"    )
  val Equals = Op(Tag.Op.Equals, "equals")

  // Operations
  val Alloc    = Op(Tag.Op.Alloc,    "alloc"   )
  val Salloc   = Op(Tag.Op.Salloc,   "salloc"  )
  val Call     = Op(Tag.Op.Call,     "call"    )
  val Phi      = Op(Tag.Op.Phi,      "phi"     )
  val Load     = Op(Tag.Op.Load,     "load"    )
  val Store    = Op(Tag.Op.Store,    "store"   )
  val Box      = Op(Tag.Op.Box,      "box"     )
  val Unbox    = Op(Tag.Op.Unbox,    "unbox"   )
  val Length   = Op(Tag.Op.Length,   "length"  )
  val Catchpad = Op(Tag.Op.Catchpad, "catchpad")
  val Elem     = Op(Tag.Op.Elem,     "elem"    )
  val Class    = Op(Tag.Op.Class,    "class"   )

  // Value-like ops
  def In               = Op(Tag.Op.In,           "in")
  def Val(value: Val)  = Op(Tag.Op.Val,  value, s"value-$value")
  def Type(ty: Type)   = Op(Tag.Op.Type, ty,    s"type")
  def Defn(defn: Defn) = Op(Tag.Op.Defn, defn,  s"defn")
  def Name(name: Name) = Op(Tag.Op.Name, name,  s"name-$name")
}

sealed abstract class Val
object Val {
  case object Unit extends Val
  case object Null extends Val
  case object True extends Val
  case object False extends Val
  case class I8(value: Byte) extends Val
  case class I16(value: Short) extends Val
  case class I32(value: Int) extends Val
  case class I64(value: Long) extends Val
  case class F32(value: Float) extends Val
  case class F64(value: Double) extends Val
  case class Str(value: String) extends Val
}

final case class Defn(tag: Tag, nodes: Seq[Instr], rels: Seq[Rel] = Seq()) extends Node {
  def next = nodes ++ rels
}
object Defn {
  def Class(parent: Defn, ifaces: Seq[Defn], rels: Seq[Rel] = Seq()) =
    Defn(Tag.Defn.Class, Seq(),
         Rel.Parent(parent) +: ifaces.map(Rel.Interface) ++: rels)
  def Interface(ifaces: Seq[Defn], rels: Seq[Rel] = Seq()) =
    Defn(Tag.Defn.Interface, Seq(), ifaces.map(Rel.Interface) ++ rels)
  def Module(parent: Defn, ifaces: Seq[Defn], rels: Seq[Rel] = Seq()) =
    Defn(Tag.Defn.Module, Seq(),
         Rel.Parent(parent) +: ifaces.map(Rel.Interface) ++: rels)
  def Declare(in: Seq[Instr], ty: Type, rels: Seq[Rel] = Seq()) =
    Defn(Tag.Defn.Declare, in, rels)
  def Define(in: Seq[Instr], out: Instr, rels: Seq[Rel] = Seq()) =
    Defn(Tag.Defn.Define, in :+ out, rels)
  def Field(ty: Type, rels: Seq[Rel] = Seq()) =
    Defn(Tag.Defn.Field, Seq(Instr.Type(ty)), rels)
  def Extern(name: Name, rels: Seq[Rel] = Seq()) =
    Defn(Tag.Defn.Extern, Seq(Instr.Name(name)), rels)
}

final case class Rel(tag: Tag, defns: Seq[Defn]) extends Node {
  def next = defns
}
object Rel {
  def Parent(defn: Defn) =
    Rel(Tag.Rel.Parent, Seq(defn))
  def Interface(defn: Defn) =
    Rel(Tag.Rel.Interface, Seq(defn))
  def Overrides(defn: Defn) =
    Rel(Tag.Rel.Overrides, Seq(defn))
  def Belongs(defn: Defn) =
    Rel(Tag.Rel.Belongs, Seq(defn))
}

sealed abstract class Name
object Name {
  final case class Global(id: String) extends Name
  final case class Nested(parent: Name, child: Name) extends Name
}

final case class Scope(entries: Map[Name, Defn])
