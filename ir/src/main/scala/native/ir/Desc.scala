package native
package ir

// TODO: names and ids are just for debugging, store them elsewhere
sealed abstract class Desc(val schema: Desc.Schema*)
object Desc {
  sealed abstract class Plain(schema: Schema*) extends Desc(schema: _*)
  sealed abstract class Rich (schema: Schema*) extends Desc(schema: _*)

  sealed trait Cf
  sealed trait Termn extends Cf
  sealed trait Ef
  sealed trait Val

  sealed abstract class Schema
  final case object Val              extends Schema
  final case object Cf               extends Schema
  final case object Ef               extends Schema
  final case object Ref              extends Schema
  final case class  Many(of: Schema) extends Schema

  final case object Empty extends Plain() with Ef with Cf with Val with Defn
  final case object Dead  extends Plain() with Ef with Cf with Val with Defn

  final case object Label         extends Plain(Many(Cf))    with Cf
  final case object If            extends Plain(Cf, Val)     with Cf
  final case object Switch        extends Plain(Cf, Val)     with Cf
  final case object Try           extends Plain(Cf)          with Cf
  final case object CaseTrue      extends Plain(Cf)          with Cf
  final case object CaseFalse     extends Plain(Cf)          with Cf
  final case object CaseConst     extends Plain(Cf, Val)     with Cf
  final case object CaseDefault   extends Plain(Cf)          with Cf
  final case object CaseException extends Plain(Cf)          with Cf with Val
  final case object Merge         extends Plain(Many(Cf))    with Cf
  final case object Return        extends Plain(Cf, Ef, Val) with Termn
  final case object Throw         extends Plain(Cf, Ef, Val) with Termn
  final case object Undefined     extends Plain(Cf, Ef)      with Termn
  final case object End           extends Plain(Many(Cf))    with Cf

  final case object Add  extends Plain(Val, Val) with Val // add & fadd
  final case object Sub  extends Plain(Val, Val) with Val // sub & fsub
  final case object Mul  extends Plain(Val, Val) with Val // mul & fmul
  final case object Div  extends Plain(Val, Val) with Val // div & fdiv
  final case object Mod  extends Plain(Val, Val) with Val // rem & frem
  final case object Shl  extends Plain(Val, Val) with Val // shl
  final case object Lshr extends Plain(Val, Val) with Val // lshr
  final case object Ashr extends Plain(Val, Val) with Val // ashr
  final case object And  extends Plain(Val, Val) with Val // and
  final case object Or   extends Plain(Val, Val) with Val // or
  final case object Xor  extends Plain(Val, Val) with Val // xor
  final case object Eq   extends Plain(Val, Val) with Val // icmp eq  & fcmp ???
  final case object Neq  extends Plain(Val, Val) with Val // icmp ne  & fcmp ???
  final case object Lt   extends Plain(Val, Val) with Val // icmp slt & fcmp ???
  final case object Lte  extends Plain(Val, Val) with Val // icmp sle & fcmp ???
  final case object Gt   extends Plain(Val, Val) with Val // icmp sgt & fcmp ???
  final case object Gte  extends Plain(Val, Val) with Val // icmp sge & fcmp ???

  final case object Trunc    extends Plain(Val, Ref) with Val
  final case object Zext     extends Plain(Val, Ref) with Val
  final case object Sext     extends Plain(Val, Ref) with Val
  final case object Fptrunc  extends Plain(Val, Ref) with Val
  final case object Fpext    extends Plain(Val, Ref) with Val
  final case object Fptoui   extends Plain(Val, Ref) with Val
  final case object Fptosi   extends Plain(Val, Ref) with Val
  final case object Uitofp   extends Plain(Val, Ref) with Val
  final case object Sitofp   extends Plain(Val, Ref) with Val
  final case object Ptrtoint extends Plain(Val, Ref) with Val
  final case object Inttoptr extends Plain(Val, Ref) with Val
  final case object Bitcast  extends Plain(Val, Ref) with Val

  final case object EfPhi      extends Plain(Cf, Many(Ef)      ) with Ef
  final case object Call       extends Plain(Ef, Val, Many(Val)) with Ef with Val
  final case object Load       extends Plain(Ef, Val           ) with Ef with Val
  final case object Store      extends Plain(Ef, Val, Val      ) with Ef with Val
  final case object Elem       extends Plain(    Val, Many(Val))            with Val
  // TODO: rename to Extract
  final case object StructElem extends Plain(    Val, Val      )         with Val
  final case object Param      extends Plain(    Ref           )         with Val
  final case object Phi        extends Plain(    Cf, Many(Val) )         with Val
  final case object Alloc      extends Plain(    Ref           )         with Val
  final case object Alloca     extends Plain(    Ref           )         with Val
  final case object Size       extends Plain(    Ref           )         with Val

  final case object Equals           extends Plain(Ef, Val, Val) with Ef with Val //scala
  final case object Hash             extends Plain(Ef, Val     ) with Ef with Val //scala
  final case object FieldElem        extends Plain(Ef, Val, Ref) with Ef with Val //scala
  final case object MethodElem       extends Plain(Ef, Val, Ref) with Ef with Val //scala
  final case object ArrayClassElem   extends Plain(Ef, Val, Val) with Ef with Val //scala
  final case object GetClass         extends Plain(Ef, Val     ) with Ef with Val //scala
  final case object ArrayClassLength extends Plain(Ef, Val     ) with Ef with Val //scala
  final case object ClassAlloc       extends Plain(Ef, Ref     )         with Val //scala
  final case object ArrayClassAlloc  extends Plain(Ef, Ref, Val)         with Val //scala
  final case object Is               extends Plain(    Val, Ref)         with Val //scala
  final case object As               extends Plain(    Val, Ref)         with Val //scala
  final case object Box              extends Plain(    Val, Ref)         with Val //scala
  final case object Unbox            extends Plain(    Val, Ref)         with Val //scala

  sealed abstract trait Lit extends Val {
    def valueString: String = this match {
      case Lit.Unit   => "{}"
      case Lit.Null   => "null"
      case Lit.True   => "true"
      case Lit.False  => "false"
      case Lit.Zero   => "zeroinitilizer"
      case Lit.I8(v)  => v.toString
      case Lit.I16(v) => v.toString
      case Lit.I32(v) => v.toString
      case Lit.I64(v) => v.toString
      case Lit.F32(v) => v.toString
      case Lit.F64(v) => v.toString
      case Lit.Struct => "struct_lit"
      case Lit.Array  => "array_lit"
      case Lit.Str(s) => s
    }
    override def toString = this match {
      case Lit.Unit               => "unit"
      case Lit.Null               => "null"
      case Lit.True               => "true"
      case Lit.False              => "false"
      case Lit.Zero               => "zero"
      case Lit.I8(value: Byte)    => s"${value}i8"
      case Lit.I16(value: Short)  => s"${value}i16"
      case Lit.I32(value: Int)    => s"${value}i32"
      case Lit.I64(value: Long)   => s"${value}i64"
      case Lit.F32(value: Float)  => s"${value}f32"
      case Lit.F64(value: Double) => s"${value}f64"
      case Lit.Struct             => "struct_lit"
      case Lit.Array              => "array_lit"
      case Lit.Str(value: String) => "\"" + value + "\""
    }
  }
  object Lit {
    final case object Unit               extends Plain()               with Lit
    final case object Null               extends Plain()               with Lit
    final case object True               extends Plain()               with Lit
    final case object False              extends Plain()               with Lit
    final case object Zero               extends Plain(Ref)            with Lit
    final case object Struct             extends Plain(Ref, Many(Ref)) with Lit
    final case object Array              extends Plain(Many(Val))      with Lit
    final case class  I8(value: Byte)    extends Rich()                with Lit
    final case class  I16(value: Short)  extends Rich()                with Lit
    final case class  I32(value: Int)    extends Rich()                with Lit
    final case class  I64(value: Long)   extends Rich()                with Lit
    final case class  F32(value: Float)  extends Rich()                with Lit
    final case class  F64(value: Double) extends Rich()                with Lit
    final case class  Str(value: String) extends Rich()                with Lit //scala
  }

  sealed trait Prim extends Defn
  object Prim {
    final case object Void    extends Plain() with Prim
    final case object Bool    extends Plain() with Prim
    final case object I8      extends Plain() with Prim
    final case object I16     extends Plain() with Prim
    final case object I32     extends Plain() with Prim
    final case object I64     extends Plain() with Prim
    final case object F32     extends Plain() with Prim
    final case object F64     extends Plain() with Prim
    final case object Unit    extends Plain() with Prim //scala
    final case object Nothing extends Plain() with Prim //scala
    final case object Null    extends Plain() with Prim //scala
  }

  sealed trait Defn
  object Defn {
    final case object Global        extends Plain(Ref, Val)           with Defn with Val
    final case object Constant      extends Plain(Ref, Val)           with Defn with Val
    final case object Define        extends Plain(Ref, Many(Val), Cf) with Defn with Val
    final case object Declare       extends Plain(Ref, Many(Val))     with Defn with Val
    final case object Extern        extends Plain()                   with Defn with Val
    final case object Struct        extends Plain(Many(Ref))          with Defn
    final case class  Array(n: Int) extends Rich(Ref)                 with Defn
    final case object Ptr           extends Plain(Ref)                with Defn
    final case object Function      extends Plain(Ref, Many(Ref))     with Defn

    //                                         parent, ifaces
    final case object Class      extends Plain(Ref, Many(Ref))          with Defn with Val //scala
    //                                         ifaces
    final case object Interface  extends Plain(Many(Ref))               with Defn with Val //scala
    //                                         parent, ifaces, ctor
    final case object Module     extends Plain(Ref, Many(Ref), Ref)     with Defn with Val //scala
    //                                         retty, params, cf, owner
    final case object Method     extends Plain(Ref, Many(Val), Cf, Ref) with Defn          //scala
    //                                         ty,  owner
    final case object Field      extends Plain(Ref, Ref)                with Defn          //scala
    //                                         ty
    final case object ArrayClass extends Plain(Ref)                     with Defn          //scala
  }
}
