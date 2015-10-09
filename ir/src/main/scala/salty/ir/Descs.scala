package salty.ir

import salty.ir.{Schema => Sc}

sealed abstract class Schema
object Schema {
  final case object Val              extends Schema
  final case object Cf               extends Schema
  final case object Ef               extends Schema
  final case object Ref              extends Schema
  final case class  Many(of: Schema) extends Schema
}

sealed abstract class Desc(val schema: Schema*)
object Desc {
  sealed trait Plain extends Desc
  sealed trait Cf
  sealed trait Termn extends Cf
  sealed trait Ef
  sealed trait Val
  sealed trait Const extends Val
  sealed trait Defn

  final case object Empty extends Desc() with Plain with Ef with Cf with Val with Defn

  final case class  Label(id: String) extends Desc(Sc.Many(Sc.Cf)      )            with Cf
  final case object If                extends Desc(Sc.Cf, Sc.Val       ) with Plain with Cf
  final case object Switch            extends Desc(Sc.Cf, Sc.Val       ) with Plain with Cf
  final case object Try               extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseTrue          extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseFalse         extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseConst         extends Desc(Sc.Cf, Sc.Val       ) with Plain with Cf
  final case object CaseDefault       extends Desc(Sc.Cf               ) with Plain with Cf
  final case object CaseException     extends Desc(Sc.Cf               ) with Plain with Cf with Val
  final case object Merge             extends Desc(Sc.Many(Sc.Cf)      ) with Plain with Cf
  final case object Return            extends Desc(Sc.Cf, Sc.Ef, Sc.Val) with Plain with Termn
  final case object Throw             extends Desc(Sc.Cf, Sc.Ef, Sc.Val) with Plain with Termn
  final case object Undefined         extends Desc(Sc.Cf, Sc.Ef        ) with Plain with Termn
  final case object End               extends Desc(Sc.Many(Sc.Cf)      ) with Plain with Cf

  final case object Add  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Sub  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Mul  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Div  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Mod  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Shl  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Lshr extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Ashr extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object And  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Or   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Xor  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Eq   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Neq  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Lt   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Lte  extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Gt   extends Desc(Sc.Val, Sc.Val) with Plain with Val
  final case object Gte  extends Desc(Sc.Val, Sc.Val) with Plain with Val

  final case object Trunc    extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Zext     extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Sext     extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fptrunc  extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fpext    extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fptoui   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Fptosi   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Uitofp   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Sitofp   extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Ptrtoint extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Inttoptr extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object Bitcast  extends Desc(Sc.Val, Sc.Ref) with Plain with Val
  final case object As       extends Desc(Sc.Val, Sc.Ref) with Plain with Val //scala
  final case object Box      extends Desc(Sc.Val, Sc.Ref) with Plain with Val //scala
  final case object Unbox    extends Desc(Sc.Val, Sc.Ref) with Plain with Val //scala

  final case object EfPhi             extends Desc(Sc.Cf, Sc.Many(Sc.Ef))          with Plain with Ef
  final case object Call              extends Desc(Sc.Ef, Sc.Val, Sc.Many(Sc.Val)) with Plain with Ef with Val
  final case object Load              extends Desc(Sc.Ef, Sc.Val)                  with Plain with Ef with Val
  final case object Store             extends Desc(Sc.Ef, Sc.Val, Sc.Val)          with Plain with Ef with Val
  final case object Equals            extends Desc(Sc.Ef, Sc.Val, Sc.Val)          with Plain with Ef with Val //scala
  final case object Hash              extends Desc(Sc.Ef, Sc.Val)                  with Plain with Ef with Val //scala

  final case object Phi               extends Desc(Sc.Cf, Sc.Many(Sc.Val))         with Plain with Val
  final case class  Param(id: String) extends Desc(Sc.Ref)                                    with Val
  final case object Alloc             extends Desc(Sc.Ref)                         with Plain with Val
  final case object Alloca            extends Desc(Sc.Ref)                         with Plain with Val
  final case object Allocs            extends Desc(Sc.Ref, Sc.Val)                 with Plain with Val //scala
  final case object Is                extends Desc(Sc.Val, Sc.Ref)                 with Plain with Val //scala
  final case object Length            extends Desc(Sc.Val)                         with Plain with Val //scala
  final case object Elem              extends Desc(Sc.Val, Sc.Val)                 with Plain with Val //scala?
  final case object GetClass          extends Desc(Sc.Val)                         with Plain with Val //scala

  final case object Unit                extends Desc() with Plain with Const
  final case object Null                extends Desc() with Plain with Const
  final case object True                extends Desc() with Plain with Const
  final case object False               extends Desc() with Plain with Const
  final case class  I8(value: Byte)     extends Desc()            with Const
  final case class  I16(value: Short)   extends Desc()            with Const
  final case class  I32(value: Int)     extends Desc()            with Const
  final case class  I64(value: Long)    extends Desc()            with Const
  final case class  F32(value: Float)   extends Desc()            with Const
  final case class  F64(value: Double)  extends Desc()            with Const
  final case class  Str(value: String)  extends Desc()            with Const //scala

  final case class Class(name: Name)     extends Desc(Sc.Many(Sc.Ref)                                ) with Defn with Val //scala
  final case class Interface(name: Name) extends Desc(Sc.Many(Sc.Ref)                                ) with Defn with Val //scala
  final case class Module(name: Name)    extends Desc(Sc.Many(Sc.Ref)                                ) with Defn with Val //scala
  final case class Declare(name: Name)   extends Desc(Sc.Ref, Sc.Many(Sc.Val), Sc.Many(Sc.Ref)       ) with Defn with Val
  final case class Define(name: Name)    extends Desc(Sc.Ref, Sc.Many(Sc.Val), Sc.Cf, Sc.Many(Sc.Ref)) with Defn with Val
  final case class Field(name: Name)     extends Desc(Sc.Ref, Sc.Many(Sc.Ref)                        ) with Defn with Val
  final case class Extern(name: Name)    extends Desc(                                               ) with Defn
  final case class Type(shape: Shape)    extends Desc(Sc.Many(Sc.Ref)                                ) with Defn
  final case class Primitive(name: Name) extends Desc(                                               ) with Defn
}
