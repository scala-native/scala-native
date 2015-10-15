package salty.ir

import salty.ir.{Schema => Sc}

// TODO: names and ids are just for debugging, store them elsewhere
sealed abstract class Desc(val schema: Schema*)
object Desc {
  sealed abstract class Plain(schema: Schema*) extends Desc(schema: _*)
  sealed abstract class Rich(schema: Schema*)  extends Desc(schema: _*)

  sealed trait Cf
  sealed trait Termn extends Cf
  sealed trait Ef
  sealed trait Val
  sealed trait Defn

  final case object Empty extends Plain() with Ef with Cf with Val with Defn

  final case object Label         extends Plain(Sc.Many(Sc.Cf)      ) with Cf
  final case object If            extends Plain(Sc.Cf, Sc.Val       ) with Cf
  final case object Switch        extends Plain(Sc.Cf, Sc.Val       ) with Cf
  final case object Try           extends Plain(Sc.Cf               ) with Cf
  final case object CaseTrue      extends Plain(Sc.Cf               ) with Cf
  final case object CaseFalse     extends Plain(Sc.Cf               ) with Cf
  final case object CaseConst     extends Plain(Sc.Cf, Sc.Val       ) with Cf
  final case object CaseDefault   extends Plain(Sc.Cf               ) with Cf
  final case object CaseException extends Plain(Sc.Cf               ) with Cf with Val
  final case object Merge         extends Plain(Sc.Many(Sc.Cf)      ) with Cf
  final case object Return        extends Plain(Sc.Cf, Sc.Ef, Sc.Val) with Termn
  final case object Throw         extends Plain(Sc.Cf, Sc.Ef, Sc.Val) with Termn
  final case object Undefined     extends Plain(Sc.Cf, Sc.Ef        ) with Termn
  final case object End           extends Plain(Sc.Many(Sc.Cf)      ) with Cf

  final case object Add  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Sub  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Mul  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Div  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Mod  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Shl  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Lshr extends Plain(Sc.Val, Sc.Val) with Val
  final case object Ashr extends Plain(Sc.Val, Sc.Val) with Val
  final case object And  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Or   extends Plain(Sc.Val, Sc.Val) with Val
  final case object Xor  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Eq   extends Plain(Sc.Val, Sc.Val) with Val
  final case object Neq  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Lt   extends Plain(Sc.Val, Sc.Val) with Val
  final case object Lte  extends Plain(Sc.Val, Sc.Val) with Val
  final case object Gt   extends Plain(Sc.Val, Sc.Val) with Val
  final case object Gte  extends Plain(Sc.Val, Sc.Val) with Val

  final case object Trunc    extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Zext     extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Sext     extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Fptrunc  extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Fpext    extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Fptoui   extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Fptosi   extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Uitofp   extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Sitofp   extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Ptrtoint extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Inttoptr extends Plain(Sc.Val, Sc.Ref) with Val
  final case object Bitcast  extends Plain(Sc.Val, Sc.Ref) with Val

  final case object EfPhi  extends Plain(Sc.Cf, Sc.Many(Sc.Ef)         ) with Ef
  final case object Call   extends Plain(Sc.Ef, Sc.Val, Sc.Many(Sc.Val)) with Ef with Val
  final case object Load   extends Plain(Sc.Ef, Sc.Val                 ) with Ef with Val
  final case object Store  extends Plain(Sc.Ef, Sc.Val, Sc.Val         ) with Ef with Val
  final case object Param  extends Plain(       Sc.Ref                 )         with Val
  final case object Phi    extends Plain(       Sc.Cf, Sc.Many(Sc.Val) )         with Val
  final case object Alloc  extends Plain(       Sc.Ref                 )         with Val
  final case object Alloca extends Plain(       Sc.Ref                 )         with Val

  final case object Equals     extends Plain(Sc.Ef, Sc.Val, Sc.Val) with Ef with Val //scala
  final case object Hash       extends Plain(Sc.Ef, Sc.Val        ) with Ef with Val //scala
  final case object FieldElem  extends Plain(Sc.Ef, Sc.Val, Sc.Ref) with Ef with Val //scala
  final case object MethodElem extends Plain(Sc.Ef, Sc.Val, Sc.Ref) with Ef with Val //scala
  final case object SliceElem  extends Plain(Sc.Ef, Sc.Val, Sc.Val) with Ef with Val //scala
  final case object GetClass   extends Plain(Sc.Ef, Sc.Val        ) with Ef with Val //scala
  final case object Length     extends Plain(Sc.Ef, Sc.Val        ) with Ef with Val //scala
  final case object Is         extends Plain(       Sc.Val, Sc.Ref)         with Val //scala
  final case object As         extends Plain(       Sc.Val, Sc.Ref)         with Val //scala
  final case object Box        extends Plain(       Sc.Val, Sc.Ref)         with Val //scala
  final case object Unbox      extends Plain(       Sc.Val, Sc.Ref)         with Val //scala
  final case object Allocs     extends Plain(       Sc.Ref, Sc.Val)         with Val //scala

  final case object Unit               extends Plain() with Val
  final case object Null               extends Plain() with Val
  final case object True               extends Plain() with Val
  final case object False              extends Plain() with Val
  final case class  I8(value: Byte)    extends Rich()  with Val
  final case class  I16(value: Short)  extends Rich()  with Val
  final case class  I32(value: Int)    extends Rich()  with Val
  final case class  I64(value: Long)   extends Rich()  with Val
  final case class  F32(value: Float)  extends Rich()  with Val
  final case class  F64(value: Double) extends Rich()  with Val
  final case class  Str(value: String) extends Rich()  with Val //scala

  final case object Primitive          extends Plain(                              ) with Defn
  final case object Global             extends Plain(Sc.Ref, Sc.Val                ) with Defn with Val
  final case object Define             extends Plain(Sc.Ref, Sc.Many(Sc.Val), Sc.Cf) with Defn with Val
  final case object Declare            extends Plain(Sc.Ref, Sc.Many(Sc.Val)       ) with Defn with Val
  final case object Extern             extends Plain(                              ) with Defn with Val
  final case class  Type(shape: Shape) extends Rich(Sc.Many(Sc.Ref)                ) with Defn

  final case object Class     extends Plain(Sc.Ref, Sc.Many(Sc.Ref)               ) with Defn with Val //scala
  final case object Interface extends Plain(Sc.Many(Sc.Ref)                       ) with Defn with Val //scala
  final case object Module    extends Plain(Sc.Ref, Sc.Many(Sc.Ref), Sc.Ref       ) with Defn with Val //scala
  final case object Method    extends Plain(Sc.Ref, Sc.Many(Sc.Val), Sc.Cf, Sc.Ref) with Defn          //scala
  final case object Field     extends Plain(Sc.Ref, Sc.Ref                        ) with Defn          //scala

}
