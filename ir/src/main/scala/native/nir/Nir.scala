package native
package nir

final case class Block(name: Name, params: Seq[Param], instrs: Seq[Instr])
final case class Instr(name: Name, op: Op, ty: Type)
final case class Param(name: Name, ty: Defn)
final case class Next(value: Val, name: Name, args: Seq[Val])

sealed trait Op
object Op {
  final case object Undefined                                                extends Op
  final case class Ret    (value: Val)                                       extends Op
  final case class Throw  (value: Val)                                       extends Op
  final case class Br     (value: Val, thenp: Next, elsep: Next)             extends Op
  final case class Switch (value: Val, default: Next, cases: Seq[Next])      extends Op
  final case class Invoke (ptr: Val, args: Seq[Val], succ: Next, fail: Next) extends Op
  final case class Call   (ptr: Val, args: Seq[Val])                         extends Op
  final case class Load   (ty: Type, ptr: Val)                               extends Op
  final case class Store  (ty: Type, ptr: Val, value: Val)                   extends Op
  final case class Elem   (ptr: Val, indexes: Seq[Val])                      extends Op
  final case class Extract(aggr: Val, index: Val)                            extends Op
  final case class Insert (aggr: Val, value: Val, index: Val)                extends Op
  final case class Alloc  (ty: Type)                                         extends Op
  final case class Alloca (ty: Type)                                         extends Op
  final case class Size   (ty: Type)                                         extends Op
  final case class Builtin(builtin: nir.Builtin, targs: Seq[Type],
                           args: Seq[Val]) extends Op

  //scala
  final case class FieldElem   (name: Name, value: Val) extends Op
  final case class MethodElem  (name: Name, value: Val) extends Op
  final case class AllocClass  (ty: Type)               extends Op
  final case class AllocArray  (ty: Type, length: Val)  extends Op
  final case class Equals      (left: Val, right: Val)  extends Op
  final case class HashCode    (value: Val)             extends Op
  final case class GetClass    (value: Val)             extends Op
  final case class ClassOf     (ty: Type)               extends Op
  final case class AsInstanceOf(value: Val, ty: Type)   extends Op
  final case class IsInstanceOf(value: Val, ty: Type)   extends Op
  final case class ArrayLength (value: Val)             extends Op
  final case class ArrayElem   (value: Val, index: Val) extends Op
  final case class Box         (value: Val, to: Type)   extends Op
  final case class Unbox       (value: Val, to: Type)   extends Op
}

sealed trait Builtin
object Builtin {
  final case object Add  extends Builtin
  final case object Sub  extends Builtin
  final case object Mul  extends Builtin
  final case object Div  extends Builtin
  final case object Mod  extends Builtin
  final case object Shl  extends Builtin
  final case object Lshr extends Builtin
  final case object Ashr extends Builtin
  final case object And  extends Builtin
  final case object Or   extends Builtin
  final case object Xor  extends Builtin
  final case object Eq   extends Builtin
  final case object Neq  extends Builtin
  final case object Lt   extends Builtin
  final case object Lte  extends Builtin
  final case object Gt   extends Builtin
  final case object Gte  extends Builtin

  final case object Trunc    extends Builtin
  final case object Zext     extends Builtin
  final case object Sext     extends Builtin
  final case object Fptrunc  extends Builtin
  final case object Fpext    extends Builtin
  final case object Fptoui   extends Builtin
  final case object Fptosi   extends Builtin
  final case object Uitofp   extends Builtin
  final case object Sitofp   extends Builtin
  final case object Ptrtoint extends Builtin
  final case object Inttoptr extends Builtin
  final case object Bitcast  extends Builtin
}

sealed trait Val
object Val {
  final case object None                    extends Val
  final case object Zero                    extends Val
  final case object True                    extends Val
  final case object False                   extends Val
  final case class I8    (value: Byte)      extends Val
  final case class I16   (value: Short)     extends Val
  final case class I32   (value: Int)       extends Val
  final case class I64   (value: Long)      extends Val
  final case class F32   (value: Float)     extends Val
  final case class F64   (value: Double)    extends Val
  final case class Struct(values: Seq[Val]) extends Val
  final case class Array (values: Seq[Val]) extends Val
  final case class Name  (name: nir.Name)   extends Val

  //scala
  final case object Null extends Val
  final case object Unit extends Val
}

sealed trait Defn { def name: Name }
object Defn {
  final case class Extern  (name: Name)                               extends Defn
  final case class Var     (name: Name, ty: Type, value: Val)         extends Defn
  final case class Declare (name: Name, ty: Type)                     extends Defn
  final case class Define  (name: Name, ty: Type, blocks: Seq[Block]) extends Defn
  final case class Struct  (name: Name, fields: Seq[Defn])            extends Defn

  // scala
  final case class Interface(name: Name,
                             interfaces: Seq[Name],
                             members: Seq[Defn]) extends Defn
  final case class Class(name: Name,
                         parent: Name,
                         interfaces: Seq[Name],
                         members: Seq[Defn]) extends Defn
  final case class Module(name: Name,
                          parent: Name,
                          interfaces: Seq[Name],
                          members: Seq[Defn]) extends Defn
}

sealed trait Type
object Type {
  final case object None                                extends Type
  final case object Void                                extends Type
  final case object Bool                                extends Type
  final case object I8                                  extends Type
  final case object I16                                 extends Type
  final case object I32                                 extends Type
  final case object I64                                 extends Type
  final case object F32                                 extends Type
  final case object F64                                 extends Type
  final case class Array   (ty: Defn, n: Int)           extends Type
  final case class Ptr     (ty: Defn)                   extends Type
  final case class Function(ret: Defn, args: Seq[Defn]) extends Type
  final case class Struct  (name: Name)                 extends Type

  // scala
  final case object Unit                extends Type
  final case object Nothing             extends Type
  final case object Null                extends Type
  final case class Class(name: Name)    extends Type
  final case class ArrayClass(ty: Type) extends Type
}

sealed trait Name
object Name {
  final case object None                                               extends Name
  final case class Fresh      (id: Int)                                extends Name
  final case class Local      (id: String)                             extends Name
  final case class Extern     (id: String)                             extends Name
  final case class Nested     (owner: Name, member: Name)              extends Name
  final case class Class      (id: String)                             extends Name
  final case class Module     (id: String)                             extends Name
  final case class Interface  (id: String)                             extends Name
  final case class Field      (id: String)                             extends Name
  final case class Constructor(args: Seq[Name])                        extends Name
  final case class Method     (id: String, args: Seq[Name], ret: Name) extends Name
  final case class Accessor   (owner: Name)                            extends Name
  final case class Data       (owner: Name)                            extends Name
  final case class Vtable     (owner: Name)                            extends Name
  final case class Array      (name: Name)                             extends Name
}
