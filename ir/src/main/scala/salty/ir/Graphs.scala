package salty
package ir

sealed abstract class Node

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
      case Type.I8  => Some(8)
      case Type.I16 => Some(16)
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
  sealed trait Termn extends Cf
  sealed trait Ef    extends Instr
  sealed trait Val   extends Instr
  sealed trait Const extends Val

  // Control-flow
  // TODO: Loop
  // TODO: Try, CaseTry, CaseCatch, CaseFinally
  final case class Start()                                         extends Cf with Ef
  final case class Label(name: Name, var cfs: Seq[Cf])             extends Cf
  final case class If(var cf: Cf, var value: Val)                  extends Cf
  final case class Switch(var cf: Cf, var value: Val)              extends Cf
  final case class Try(var cf: Cf)                                 extends Cf
  final case class CaseTrue(var cf: Cf)                            extends Cf
  final case class CaseFalse(var cf: Cf)                           extends Cf
  final case class CaseConst(var cf: Cf, var const: Const)         extends Cf
  final case class CaseDefault(var cf: Cf)                         extends Cf
  final case class CaseException(var cf: Cf)                       extends Cf
  final case class Merge(var cfs: Seq[Cf])                         extends Cf
  final case class Return(var cf: Cf, var ef: Ef, var value: Val)  extends Termn
  final case class Throw(var cf: Cf, var ef: Ef, var value: Val)   extends Termn
  final case class Undefined(var cf: Cf, var ef: Ef)               extends Termn
  final case class End(var cfs: Seq[Termn])                        extends Cf

  // Effectful
  final case class EfPhi(var cf: Cf, var efs: Seq[Ef])                extends Ef
  final case class Equals(var ef: Ef, var left: Val, var right: Val)  extends Val with Ef
  final case class Call(var ef: Ef, var ptr: Val, var args: Seq[Val]) extends Val with Ef
  final case class Load(var ef: Ef, var ptr: Val)                     extends Val with Ef
  final case class Store(var ef: Ef, var ptr: Val, var value: Val)    extends Val with Ef

  // Pure binary
  final case class Add (var left: Val, var right: Val) extends Val
  final case class Sub (var left: Val, var right: Val) extends Val
  final case class Mul (var left: Val, var right: Val) extends Val
  final case class Div (var left: Val, var right: Val) extends Val
  final case class Mod (var left: Val, var right: Val) extends Val
  final case class Shl (var left: Val, var right: Val) extends Val
  final case class Lshr(var left: Val, var right: Val) extends Val
  final case class Ashr(var left: Val, var right: Val) extends Val
  final case class And (var left: Val, var right: Val) extends Val
  final case class Or  (var left: Val, var right: Val) extends Val
  final case class Xor (var left: Val, var right: Val) extends Val
  final case class Eq  (var left: Val, var right: Val) extends Val
  final case class Neq (var left: Val, var right: Val) extends Val
  final case class Lt  (var left: Val, var right: Val) extends Val
  final case class Lte (var left: Val, var right: Val) extends Val
  final case class Gt  (var left: Val, var right: Val) extends Val
  final case class Gte (var left: Val, var right: Val) extends Val

  // Pure conversions
  final case class Trunc   (var value: Val, var ty: Type) extends Val
  final case class Zext    (var value: Val, var ty: Type) extends Val
  final case class Sext    (var value: Val, var ty: Type) extends Val
  final case class Fptrunc (var value: Val, var ty: Type) extends Val
  final case class Fpext   (var value: Val, var ty: Type) extends Val
  final case class Fptoui  (var value: Val, var ty: Type) extends Val
  final case class Fptosi  (var value: Val, var ty: Type) extends Val
  final case class Uitofp  (var value: Val, var ty: Type) extends Val
  final case class Sitofp  (var value: Val, var ty: Type) extends Val
  final case class Ptrtoint(var value: Val, var ty: Type) extends Val
  final case class Inttoptr(var value: Val, var ty: Type) extends Val
  final case class Bitcast (var value: Val, var ty: Type) extends Val
  final case class Cast    (var value: Val, var ty: Type) extends Val
  final case class Box     (var value: Val, var ty: Type) extends Val
  final case class Unbox   (var value: Val, var ty: Type) extends Val

  // Pure resft
  final case class Phi(var merge: Cf, var values: Seq[Val]) extends Val
  final case class Is(var value: Val, var ty: Type)         extends Val
  final case class Alloc(var ty: Type)                      extends Val
  final case class Salloc(var ty: Type, var n: Val)         extends Val
  final case class Length(var value: Val)                   extends Val
  final case class Elem(var ptr: Val, var value: Val)       extends Val
  final case class Param(name: Name, var ty: Type)          extends Val
  final case class ValueOf(var defn: Defn)                  extends Val
  final case class ExceptionOf(var cf: Cf)                  extends Val
  final case class TagOf(var value: Val)                    extends Val

  // Constants
  final case object Unit              extends Const { override def toString = "unit" }
  final case object Null              extends Const { override def toString = "null" }
  final case object True              extends Const { override def toString = "true" }
  final case object False             extends Const { override def toString = "false" }
  final case class I8(value: Byte)    extends Const { override def toString = s"${value}i8" }
  final case class I16(value: Short)  extends Const { override def toString = s"${value}i16" }
  final case class I32(value: Int)    extends Const { override def toString = s"${value}i32" }
  final case class I64(value: Long)   extends Const { override def toString = s"${value}i64" }
  final case class F32(value: Float)  extends Const { override def toString = s"${value}f32" }
  final case class F64(value: Double) extends Const { override def toString = s"${value}f64" }
  final case class Str(value: String) extends Const { override def toString = "\"" + value + "\"" }
  final case class Tag(ty: Type)      extends Const
}

sealed abstract class Defn extends Node {
  def name: Name
  def rels: Seq[Rel]
}
object Defn {
  final case class Class(name: Name, var rels: Seq[Rel] = Seq()) extends Defn
  final case class Interface(name: Name, var rels: Seq[Rel] = Seq()) extends Defn
  final case class Module(name: Name, var rels: Seq[Rel] = Seq()) extends Defn
  final case class Declare(name: Name, var ty: Type, var params: Seq[Instr.Param],
                           var rels: Seq[Rel] = Seq()) extends Defn
  final case class Define(name: Name, var ty: Type, var params: Seq[Instr.Param],
                          var end: Instr.End, rels: Seq[Rel] = Seq()) extends Defn
  final case class Field(name: Name, var ty: Type, rels: Seq[Rel] = Seq()) extends Defn
  final case class Extern(name: Name, var rels: Seq[Rel] = Seq()) extends Defn
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
  final case class Global(id: String) extends Name {
    override def toString = id
  }
  final case class Nested(parent: Name, child: Name) extends Name {
    override def toString = s"$parent::$child"
  }
}

final case class Scope(entries: Map[Name, Defn])
