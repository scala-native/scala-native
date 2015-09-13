package salty
package ir

sealed abstract class Node {
  var epoch: Int = 0
}
object Node {
  var lastEpoch = 0
  def nextEpoch = {
    lastEpoch += 1
    lastEpoch
  }
}

sealed abstract class Type extends Node
object Type {
  final case object Null           extends Type
  final case object Nothing        extends Type
  final case object Unit           extends Type
  final case object Bool           extends Type
  final case object I8             extends Type
  final case object I16            extends Type
  final case object I32            extends Type
  final case object I64            extends Type
  final case object F32            extends Type
  final case object F64            extends Type
  final case class Ref(ty: Type)   extends Type
  final case class Slice(ty: Type) extends Type
  final case class Of(defn: Defn)  extends Type

  object I {
    def unapply(ty: Type) = ty match {
      case Type.I8  => Some(32)
      case Type.I16 => Some(32)
      case Type.I32 => Some(32)
      case Type.I64 => Some(64)
      case _        => None
    }
  }

  object F {
    def unapply(ty: Type) = ty match {
      case Type.F32 => Some(32)
      case Type.F64 => Some(64)
      case _        => None
    }
  }
}

sealed abstract class Instr extends Node
object Instr {
  sealed trait Cf    extends Instr
  sealed trait Ef    extends Instr
  sealed trait Val   extends Instr
  sealed trait Const extends Val

  // Control-flow
  // TODO: Loop
  // TODO: Try, CaseTry, CaseCatch, CaseFinally
  final case class Start()                            extends Cf with Ef
  final case class If(cf: Cf, ef: Ef, value: Val)     extends Cf
  final case class Switch(cf: Cf, ef: Ef, value: Val) extends Cf
  final case class CaseTrue(cf: Cf)                   extends Cf with Ef
  final case class CaseFalse(cf: Cf)                  extends Cf with Ef
  final case class CaseConst(cf: Cf, const: Const)    extends Cf with Ef
  final case class CaseDefault(cf: Cf)                extends Cf with Ef
  final case class CaseEnd(cf: Cf, ef: Ef)            extends Cf
  final case class Return(cf: Cf, ef: Ef, value: Val) extends Cf
  final case class Throw(cf: Cf, ef: Ef, value: Val)  extends Cf
  final case class Undefined(cf: Cf, ef: Ef)          extends Cf
  final case class Merge(cfs: Seq[Cf])                extends Cf
  final case class End(cfs: Seq[Cf])                  extends Cf

  // Effectful
  final case class Equals(ef: Ef, left: Val, right: Val)  extends Val with Ef
  final case class Call(ef: Ef, ptr: Val, args: Seq[Val]) extends Val with Ef
  final case class Load(ef: Ef, ptr: Val)                 extends Val with Ef
  final case class Store(ef: Ef, ptr: Val, value: Val)    extends Val with Ef

  // Pure binary
  final case class Add (left: Val, right: Val) extends Val
  final case class Sub (left: Val, right: Val) extends Val
  final case class Mul (left: Val, right: Val) extends Val
  final case class Div (left: Val, right: Val) extends Val
  final case class Mod (left: Val, right: Val) extends Val
  final case class Shl (left: Val, right: Val) extends Val
  final case class Lshr(left: Val, right: Val) extends Val
  final case class Ashr(left: Val, right: Val) extends Val
  final case class And (left: Val, right: Val) extends Val
  final case class Or  (left: Val, right: Val) extends Val
  final case class Xor (left: Val, right: Val) extends Val
  final case class Eq  (left: Val, right: Val) extends Val
  final case class Neq (left: Val, right: Val) extends Val
  final case class Lt  (left: Val, right: Val) extends Val
  final case class Lte (left: Val, right: Val) extends Val
  final case class Gt  (left: Val, right: Val) extends Val
  final case class Gte (left: Val, right: Val) extends Val

  // Pure conversions
  final case class Trunc   (value: Val, ty: Type) extends Val
  final case class Zext    (value: Val, ty: Type) extends Val
  final case class Sext    (value: Val, ty: Type) extends Val
  final case class Fptrunc (value: Val, ty: Type) extends Val
  final case class Fpext   (value: Val, ty: Type) extends Val
  final case class Fptoui  (value: Val, ty: Type) extends Val
  final case class Fptosi  (value: Val, ty: Type) extends Val
  final case class Uitofp  (value: Val, ty: Type) extends Val
  final case class Sitofp  (value: Val, ty: Type) extends Val
  final case class Ptrtoint(value: Val, ty: Type) extends Val
  final case class Inttoptr(value: Val, ty: Type) extends Val
  final case class Bitcast (value: Val, ty: Type) extends Val
  final case class Cast    (value: Val, ty: Type) extends Val

  // Pure rest
  final case class Is(value: Val, ty: Type)         extends Val
  final case class Alloc(ty: Type)                  extends Val
  final case class Salloc(ty: Type, n: Val)         extends Val
  final case class Phi(merge: Cf, values: Seq[Val]) extends Val
  final case class Box(value: Val, ty: Type)        extends Val
  final case class Unbox(value: Val, ty: Type)      extends Val
  final case class Length(value: Val)               extends Val
  final case class Elem(ptr: Val, value: Val)       extends Val
  final case class Class(ty: Type)                  extends Val
  final case class In(ty: Type)                     extends Val
  final case class ValueOf(defn: Defn)              extends Val

  // Constants
  final case object Unit              extends Const
  final case object Null              extends Const
  final case object True              extends Const
  final case object False             extends Const
  final case class I8(value: Byte)    extends Const
  final case class I16(value: Short)  extends Const
  final case class I32(value: Int)    extends Const
  final case class I64(value: Long)   extends Const
  final case class F32(value: Float)  extends Const
  final case class F64(value: Double) extends Const
  final case class Str(value: String) extends Const
}

sealed abstract class Defn extends Node {
  def rels: Seq[Rel]
}
object Defn {
  final case class Class(rels: Seq[Rel] = Seq()) extends Defn
  final case class Interface(rels: Seq[Rel] = Seq()) extends Defn
  final case class Module(rels: Seq[Rel] = Seq()) extends Defn
  final case class Declare(ty: Type, in: Seq[Instr.In],
                           rels: Seq[Rel] = Seq()) extends Defn
  final case class Define(ty: Type, in: Seq[Instr.In], out: Instr.End,
                          rels: Seq[Rel] = Seq()) extends Defn
  final case class Field(ty: Type, rels: Seq[Rel] = Seq()) extends Defn
  final case class Extern(name: Name, rels: Seq[Rel] = Seq()) extends Defn
}

sealed abstract class Rel { def defn: Defn }
object Rel {
  final case class Child(defn: Defn)      extends Rel
  final case class Implements(defn: Defn) extends Rel
  final case class Overrides(defn: Defn)  extends Rel
  final case class Belongs(defn: Defn)    extends Rel
}

sealed abstract class Name
object Name {
  final case class Global(id: String) extends Name
  final case class Nested(parent: Name, child: Name) extends Name
}

final case class Scope(entries: Map[Name, Defn])
