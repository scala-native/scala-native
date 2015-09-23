package salty
package ir

import salty.ir.{Tags => T}

sealed class Node(var desc: Desc, var deps: Seq[Node]) {
  private[ir] var epoch: Int = 0
}

sealed abstract class Prim(name: Name) extends Node(Desc.Primitive(name), Seq())
object Prim {
  final case object Null    extends Prim(Name.Simple("null"))
  final case object Nothing extends Prim(Name.Simple("nothing"))
  final case object Unit    extends Prim(Name.Simple("unit"))
  final case object Bool    extends Prim(Name.Simple("bool"))

  sealed abstract case class I(width: Int) extends Prim(Name.Simple(s"i$width"))
  final object I8  extends I(8)
  final object I16 extends I(16)
  final object I32 extends I(32)
  final object I64 extends I(64)

  sealed abstract case class F(width: Int) extends Prim(Name.Simple(s"f$width"))
  final object F32 extends F(32)
  final object F64 extends F(64)
}

sealed abstract class Desc(tag: Int, cfIn: Int, efIn: Int, valIn: Int, refIn: Int) {
  def totalIn: Int = cfIn + efIn + valIn + refIn
}
object Desc {
  sealed trait Cf
  sealed trait Termn extends Cf
  sealed trait Ef
  sealed trait Val
  sealed trait Defn

  final case object Start                     extends Desc(T.Start        , 0, 0, 0, 0) with Cf with Ef
  final case class  Label(name: Name, n: Int) extends Desc(T.Label        , n, 0, 0, 0) with Cf
  final case object If                        extends Desc(T.If           , 1, 0, 1, 0) with Cf
  final case object Switch                    extends Desc(T.Switch       , 1, 0, 1, 0) with Cf
  final case object Try                       extends Desc(T.Try          , 1, 0, 0, 0) with Cf
  final case object CaseTrue                  extends Desc(T.CaseTrue     , 1, 0, 0, 0) with Cf
  final case object CaseFalse                 extends Desc(T.CaseFalse    , 1, 0, 0, 0) with Cf
  final case object CaseConst                 extends Desc(T.CaseConst    , 1, 0, 1, 0) with Cf
  final case object CaseDefault               extends Desc(T.CaseDefault  , 1, 0, 0, 0) with Cf
  final case object CaseException             extends Desc(T.CaseException, 1, 0, 0, 0) with Cf
  final case class  Merge(n: Int)             extends Desc(T.Merge        , n, 0, 0, 0) with Cf
  final case object Return                    extends Desc(T.Return       , 1, 1, 1, 0) with Termn
  final case object Throw                     extends Desc(T.Throw        , 1, 1, 1, 0) with Termn
  final case object Undefined                 extends Desc(T.Undefined    , 1, 1, 0, 0) with Termn
  final case class  End(n: Int)               extends Desc(T.End          , n, 0, 0, 0) with Cf

  final case class  EfPhi(n: Int) extends Desc(T.EfPhi , 1, n, 0,     0) with Ef
  final case object Equals        extends Desc(T.Equals, 0, 1, 2,     0) with Ef with Val
  final case class  Call(n: Int)  extends Desc(T.Call  , 0, 1, n + 1, 0) with Ef with Val
  final case object Load          extends Desc(T.Load  , 0, 1, 1,     0) with Ef with Val
  final case object Store         extends Desc(T.Store , 0, 1, 2,     0) with Ef with Val

  final case object Add  extends Desc(T.Add , 0, 0, 2, 0) with Val
  final case object Sub  extends Desc(T.Sub , 0, 0, 2, 0) with Val
  final case object Mul  extends Desc(T.Mul , 0, 0, 2, 0) with Val
  final case object Div  extends Desc(T.Div , 0, 0, 2, 0) with Val
  final case object Mod  extends Desc(T.Mod , 0, 0, 2, 0) with Val
  final case object Shl  extends Desc(T.Shl , 0, 0, 2, 0) with Val
  final case object Lshr extends Desc(T.Lshr, 0, 0, 2, 0) with Val
  final case object Ashr extends Desc(T.Ashr, 0, 0, 2, 0) with Val
  final case object And  extends Desc(T.And , 0, 0, 2, 0) with Val
  final case object Or   extends Desc(T.Or  , 0, 0, 2, 0) with Val
  final case object Xor  extends Desc(T.Xor , 0, 0, 2, 0) with Val
  final case object Eq   extends Desc(T.Eq  , 0, 0, 2, 0) with Val
  final case object Neq  extends Desc(T.Neq , 0, 0, 2, 0) with Val
  final case object Lt   extends Desc(T.Lt  , 0, 0, 2, 0) with Val
  final case object Lte  extends Desc(T.Lte , 0, 0, 2, 0) with Val
  final case object Gt   extends Desc(T.Gt  , 0, 0, 2, 0) with Val
  final case object Gte  extends Desc(T.Gte , 0, 0, 2, 0) with Val

  final case object Trunc    extends Desc(T.Trunc   , 0, 0, 1, 1) with Val
  final case object Zext     extends Desc(T.Zext    , 0, 0, 1, 1) with Val
  final case object Sext     extends Desc(T.Sext    , 0, 0, 1, 1) with Val
  final case object Fptrunc  extends Desc(T.Fptrunc , 0, 0, 1, 1) with Val
  final case object Fpext    extends Desc(T.Fpext   , 0, 0, 1, 1) with Val
  final case object Fptoui   extends Desc(T.Fptoui  , 0, 0, 1, 1) with Val
  final case object Fptosi   extends Desc(T.Fptosi  , 0, 0, 1, 1) with Val
  final case object Uitofp   extends Desc(T.Uitofp  , 0, 0, 1, 1) with Val
  final case object Sitofp   extends Desc(T.Sitofp  , 0, 0, 1, 1) with Val
  final case object Ptrtoint extends Desc(T.Ptrtoint, 0, 0, 1, 1) with Val
  final case object Inttoptr extends Desc(T.Inttoptr, 0, 0, 1, 1) with Val
  final case object Bitcast  extends Desc(T.Bitcast , 0, 0, 1, 1) with Val
  final case object Cast     extends Desc(T.Cast    , 0, 0, 1, 1) with Val
  final case object Box      extends Desc(T.Box     , 0, 0, 1, 1) with Val
  final case object Unbox    extends Desc(T.Unbox   , 0, 0, 1, 1) with Val

  final case class  Phi(n: Int)         extends Desc(T.Phi        , 1, 0, n, 0) with Val
  final case object Is                  extends Desc(T.Is         , 0, 0, 1, 1) with Val
  final case object Alloc               extends Desc(T.Alloc      , 0, 0, 0, 1) with Val
  final case object Salloc              extends Desc(T.Salloc     , 0, 0, 1, 1) with Val
  final case object Length              extends Desc(T.Length     , 0, 0, 1, 0) with Val
  final case object Elem                extends Desc(T.Elem       , 0, 0, 2, 0) with Val
  final case class  Param(name: Name)   extends Desc(T.Param      , 0, 0, 0, 1) with Val
  final case object ValueOf             extends Desc(T.ValueOf    , 0, 0, 0, 1) with Val
  final case object ExceptionOf         extends Desc(T.ExceptionOf, 1, 0, 0, 0) with Val
  final case object TagOf               extends Desc(T.TagOf      , 0, 0, 1, 0) with Val
  final case class  Const(const: Const) extends Desc(T.Const      , 0, 0, 0, 0) with Val
  final case object TagConst            extends Desc(T.TagConst   , 0, 0, 0, 1) with Val

  final case class Class(name: Name, rels: Int)                extends Desc(T.Class,     0, 0, 0,      rels)        with Defn
  final case class Interface(name: Name, rels: Int)            extends Desc(T.Interface, 0, 0, 0,      rels)        with Defn
  final case class Module(name: Name, rels: Int)               extends Desc(T.Module,    0, 0, 0,      rels)        with Defn
  final case class Declare(name: Name, params: Int, rels: Int) extends Desc(T.Declare,   0, 0, params, 1 + rels)    with Defn
  final case class Define(name: Name, params: Int, rels: Int)  extends Desc(T.Define,    1, 0, params, 1 + rels)    with Defn
  final case class Field(name: Name, rels: Int)                extends Desc(T.Field,     0, 0, 0,      1 + rels)    with Defn
  final case class Extern(name: Name)                          extends Desc(T.Extern,    0, 0, 0,      0)           with Defn
  final case class Type(shape: Shape)                          extends Desc(T.Type,      0, 0, 0,      shape.holes) with Defn
  final case class Primitive(name: Name)                       extends Desc(T.Primitive, 0, 0, 0,      0)           with Defn
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
}

sealed abstract class Name
object Name {
  final case object No extends Name
  final case class Simple(id: String) extends Name
  final case class Nested(parent: Name, child: Name) extends Name
}

sealed abstract class Shape {
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
  // TODO: Struct(fields)
  // TODO: Array(t, n)
}

final case class Scope(entries: Map[Name, Node])
