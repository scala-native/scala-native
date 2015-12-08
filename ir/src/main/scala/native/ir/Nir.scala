package native
package nir

final case class Block(params: Seq[Param], ops: Seq[Op])
final case class Param(value: Val, ty: Defn)
final case class Next(value: Val, block: Block, values: Seq[Val])

sealed trait Op
object Op {
  final case object Undefined                                                extends Op
  final case class Return(value: Val)                                        extends Op
  final case class Throw (value: Val)                                        extends Op
  final case class If    (cond: Val, thenp: Next, elsep: Next)               extends Op
  final case class Switch(scrut: Val, default: Next, cases: Seq[Next])       extends Op
  final case class Invoke(func: Val, args: Seq[Val], succ: Next, fail: Next) extends Op

  final case class Add (left: Val, right: Val) extends Op
  final case class Sub (left: Val, right: Val) extends Op
  final case class Mul (left: Val, right: Val) extends Op
  final case class Div (left: Val, right: Val) extends Op
  final case class Mod (left: Val, right: Val) extends Op
  final case class Shl (left: Val, right: Val) extends Op
  final case class Lshr(left: Val, right: Val) extends Op
  final case class Ashr(left: Val, right: Val) extends Op
  final case class And (left: Val, right: Val) extends Op
  final case class Or  (left: Val, right: Val) extends Op
  final case class Xor (left: Val, right: Val) extends Op
  final case class Eq  (left: Val, right: Val) extends Op
  final case class Neq (left: Val, right: Val) extends Op
  final case class Lt  (left: Val, right: Val) extends Op
  final case class Lte (left: Val, right: Val) extends Op
  final case class Gt  (left: Val, right: Val) extends Op
  final case class Gte (left: Val, right: Val) extends Op

  final case class Trunc   (value: Val, to: Defn) extends Op
  final case class Zext    (value: Val, to: Defn) extends Op
  final case class Sext    (value: Val, to: Defn) extends Op
  final case class Fptrunc (value: Val, to: Defn) extends Op
  final case class Fpext   (value: Val, to: Defn) extends Op
  final case class Fptoui  (value: Val, to: Defn) extends Op
  final case class Fptosi  (value: Val, to: Defn) extends Op
  final case class Uitofp  (value: Val, to: Defn) extends Op
  final case class Sitofp  (value: Val, to: Defn) extends Op
  final case class Ptrtoint(value: Val, to: Defn) extends Op
  final case class Inttoptr(value: Val, to: Defn) extends Op
  final case class Bitcast (value: Val, to: Defn) extends Op

  final case class Load   (ptr: Val)                    extends Op
  final case class Store  (ptr: Val, value: Val)        extends Op
  final case class Elem   (ptr: Val, indexes: Seq[Val]) extends Op
  final case class Extract(aggr: Val, index: Val)       extends Op
  final case class Alloc  (defn: Defn)                  extends Op
  final case class Alloca (defn: Defn)                  extends Op
  final case class Size   (defn: Defn)                  extends Op

  //scala
  final case class Equals          (left: Val, right: Val)       extends Op
  final case class Hash            (value: Val)                  extends Op
  final case class FieldElem       (instance: Val, field: Defn)  extends Op
  final case class MethodElem      (instance: Val, method: Defn) extends Op
  final case class GetClass        (instance: Val)               extends Op
  final case class ClassAlloc      (defn: Defn)                  extends Op
  final case class ArrayClassElem  (instance: Val)               extends Op
  final case class ArrayClassLength(instance: Val)               extends Op
  final case class ArrayClassAlloc (defn: Defn, length: Val)     extends Op
  final case class Is              (value: Val, defn: Defn)      extends Op
  final case class As              (value: Val, defn: Defn)      extends Op
  final case class Box             (value: Val, defn: Defn)      extends Op
  final case class Unbox           (value: Val, defn: Defn)      extends Op
}

sealed trait Val
object Val {
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
  final case class Defn  (defn: Defn)       extends Val
  final case class Op    (op: Op)           extends Val

  //scala
  final case object Null extends Val
  final case object Unit extends Val
  final case class Str(value: String) extends Val
}

sealed trait Defn { def name: Name }
object Defn {
  final case class Global  (name: Name, of: Type, value: Val) extends Defn
  final case class Constant(name: Name, of: Type, value: Val) extends Defn
  final case class Define  (name: Name, entry: Block)         extends Defn
  final case class Declare (name: Name, params: Seq[Param])   extends Defn
  final case class Extern  (name: Name)                       extends Defn
  final case class Struct  (name: Name, fields: Seq[Defn])    extends Defn

  // scala
  final case class Class(name: Name,
                         parent: Defn,
                         interfaces: Seq[Defn],
                         members: Seq[Defn]) extends Defn
  final case class Interface(name: Name,
                             interfaces: Seq[Defn],
                             members: Seq[Defn]) extends Defn
  final case class Module(name: Name,
                          parent: Defn,
                          interfaces: Seq[Defn],
                          members: Seq[Defn]) extends Defn
  final case class Method(name: Name, entry: Block) extends Defn
  final case class Field(name: Name, of: Defn) extends Defn
}

sealed trait Type
object Type {
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
  final case class Struct  (defn: Defn.Struct)          extends Type

  // scala
  final case object Unit                   extends Type
  final case object Nothing                extends Type
  final case object Null                   extends Type
  final case class Class(defn: Defn.Class) extends Type
  final case class ArrayClass(of: Type)    extends Type
}

sealed trait Name
object Name {
  final case object None                                               extends Name
  final case class Local      (id: String)                             extends Name
  final case class Extern     (id: String)                             extends Name
  final case class Nested     (owner: Name, member: Name)              extends Name
  final case class Class      (id: String)                             extends Name
  final case class Module     (id: String)                             extends Name
  final case class Interface  (id: String)                             extends Name
  final case class Constructor(args: Seq[Name])                        extends Name
  final case class Field      (id: String)                             extends Name
  final case class Method     (id: String, args: Seq[Name], ret: Name) extends Name
  final case class Accessor   (owner: Name)                            extends Name
  final case class Data       (owner: Name)                            extends Name
  final case class Vtable     (owner: Name)                            extends Name
  final case class Array      (name: Name)                             extends Name
}
