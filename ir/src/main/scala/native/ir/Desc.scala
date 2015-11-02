package native
package ir

import native.ir.{Schema => Sc}

// TODO: names and ids are just for debugging, store them elsewhere
sealed abstract class Desc(val schema: Schema*)
object Desc {
  sealed abstract class Plain(schema: Schema*) extends Desc(schema: _*)
  sealed abstract class Rich(schema: Schema*)  extends Desc(schema: _*)

  sealed trait Cf
  sealed trait Termn extends Cf
  sealed trait Ef
  sealed trait Val

  final case object Empty extends Plain() with Ef with Cf with Val with Defn

  final case object Dead extends Plain() with Ef with Cf with Val with Defn

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

  final case object EfPhi      extends Plain(Sc.Cf, Sc.Many(Sc.Ef)         ) with Ef
  final case object Call       extends Plain(Sc.Ef, Sc.Val, Sc.Many(Sc.Val)) with Ef with Val
  final case object Load       extends Plain(Sc.Ef, Sc.Val                 ) with Ef with Val
  final case object Store      extends Plain(Sc.Ef, Sc.Val, Sc.Val         ) with Ef with Val
  final case object Elem       extends Plain(       Sc.Val, Sc.Many(Sc.Val))         with Val
  // TODO: rename to ValueElem
  final case object StructElem extends Plain(       Sc.Val, Sc.Val         )         with Val
  // TODO: rename to PtrElem
  final case object Param      extends Plain(       Sc.Ref                 )         with Val
  final case object Phi        extends Plain(       Sc.Cf, Sc.Many(Sc.Val) )         with Val
  final case object Alloc      extends Plain(       Sc.Ref                 )         with Val
  final case object Alloca     extends Plain(       Sc.Ref                 )         with Val

  final case object Equals      extends Plain(Sc.Ef, Sc.Val, Sc.Val) with Ef with Val //scala
  final case object Hash        extends Plain(Sc.Ef, Sc.Val        ) with Ef with Val //scala
  final case object FieldElem   extends Plain(Sc.Ef, Sc.Val, Sc.Ref) with Ef with Val //scala
  final case object MethodElem  extends Plain(Sc.Ef, Sc.Val, Sc.Ref) with Ef with Val //scala
  final case object SliceElem   extends Plain(Sc.Ef, Sc.Val, Sc.Val) with Ef with Val //scala
  final case object GetClass    extends Plain(Sc.Ef, Sc.Val        ) with Ef with Val //scala
  final case object SliceLength extends Plain(Sc.Ef, Sc.Val        ) with Ef with Val //scala
  final case object ClassAlloc  extends Plain(Sc.Ef, Sc.Ref        )         with Val //scala
  final case object SliceAlloc  extends Plain(Sc.Ef, Sc.Ref, Sc.Val)         with Val //scala
  final case object Is          extends Plain(       Sc.Val, Sc.Ref)         with Val //scala
  final case object As          extends Plain(       Sc.Val, Sc.Ref)         with Val //scala
  final case object Box         extends Plain(       Sc.Val, Sc.Ref)         with Val //scala
  final case object Unbox       extends Plain(       Sc.Val, Sc.Ref)         with Val //scala

  sealed abstract trait Lit extends Val {
    def valueString: String = this match {
      case Lit.Unit   => "{}"
      case Lit.Null   => "null"
      case Lit.True   => "true"
      case Lit.False  => "false"
      case Lit.Zero   => ???
      case Lit.Size   => ???
      case Lit.I8(v)  => v.toString
      case Lit.I16(v) => v.toString
      case Lit.I32(v) => v.toString
      case Lit.I64(v) => v.toString
      case Lit.F32(v) => v.toString
      case Lit.F64(v) => v.toString
      case Lit.Struct => ???
      case Lit.Str(s) => ???
    }
    override def toString = this match {
      case Lit.Unit               => "unit"
      case Lit.Null               => "null"
      case Lit.True               => "true"
      case Lit.False              => "false"
      case Lit.Zero               => "zero"
      case Lit.Size               => "size"
      case Lit.I8(value: Byte)    => s"${value}i8"
      case Lit.I16(value: Short)  => s"${value}i16"
      case Lit.I32(value: Int)    => s"${value}i32"
      case Lit.I64(value: Long)   => s"${value}i64"
      case Lit.F32(value: Float)  => s"${value}f32"
      case Lit.F64(value: Double) => s"${value}f64"
      case Lit.Struct             => "struct"
      case Lit.Str(value: String) => "\"" + value + "\""
    }
  }
  object Lit {
    final case object Unit               extends Plain()                        with Lit
    final case object Null               extends Plain()                        with Lit
    final case object True               extends Plain()                        with Lit
    final case object False              extends Plain()                        with Lit
    final case object Zero               extends Plain(Sc.Ref)                  with Lit
    // TODO: move size out of lit
    final case object Size               extends Plain(Sc.Ref)                  with Lit
    final case object Struct             extends Plain(Sc.Ref, Sc.Many(Sc.Ref)) with Lit
    final case class  I8(value: Byte)    extends Rich()                         with Lit
    final case class  I16(value: Short)  extends Rich()                         with Lit
    final case class  I32(value: Int)    extends Rich()                         with Lit
    final case class  I64(value: Long)   extends Rich()                         with Lit
    final case class  F32(value: Float)  extends Rich()                         with Lit
    final case class  F64(value: Double) extends Rich()                         with Lit
    final case class  Str(value: String) extends Rich()                         with Lit //scala
  }

  sealed trait Prim extends Defn
  object Prim {
    final case object Unit    extends Plain() with Prim
    final case object Bool    extends Plain() with Prim
    final case object I8      extends Plain() with Prim
    final case object I16     extends Plain() with Prim
    final case object I32     extends Plain() with Prim
    final case object I64     extends Plain() with Prim
    final case object F32     extends Plain() with Prim
    final case object F64     extends Plain() with Prim
    final case object Nothing extends Plain() with Prim //scala
    final case object Null    extends Plain() with Prim //scala
  }

  sealed trait Defn
  object Defn {
    final case object Global    extends Plain(Sc.Ref, Sc.Val                ) with Defn with Val
    final case object Constant  extends Plain(Sc.Ref, Sc.Val                ) with Defn with Val
    final case object Define    extends Plain(Sc.Ref, Sc.Many(Sc.Val), Sc.Cf) with Defn with Val
    final case object Declare   extends Plain(Sc.Ref, Sc.Many(Sc.Val)       ) with Defn with Val
    final case object Extern    extends Plain(                              ) with Defn with Val
    final case object Struct    extends Plain(Sc.Many(Sc.Ref)               ) with Defn
    final case object Ptr       extends Plain(Sc.Ref                        ) with Defn
    final case object Function  extends Plain(Sc.Ref, Sc.Many(Sc.Ref)       ) with Defn

    //                                        parent, ifaces
    final case object Class     extends Plain(Sc.Ref, Sc.Many(Sc.Ref)               ) with Defn with Val //scala
    //                                        ifaces
    final case object Interface extends Plain(Sc.Many(Sc.Ref)                       ) with Defn with Val //scala
    //                                        parent, ifaces,          ctor
    final case object Module    extends Plain(Sc.Ref, Sc.Many(Sc.Ref), Sc.Ref       ) with Defn with Val //scala
    //                                        retty,  params,          cf,    owner
    final case object Method    extends Plain(Sc.Ref, Sc.Many(Sc.Val), Sc.Cf, Sc.Ref) with Defn          //scala
    //                                        ty,     owner
    final case object Field     extends Plain(Sc.Ref, Sc.Ref                        ) with Defn          //scala
    //                                        ty
    final case object Slice     extends Plain(Sc.Ref                                ) with Defn          //scala
  }
}
