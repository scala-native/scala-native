package salty.ir

object Start {
  def apply(): Node = ???
  def unapply(n: Node): Boolean = ???
}
object Label {
  def apply(name: Name, cfs: Seq[Node]): Node = ???
}
object If {
  def apply(cf: Node, value: Node): Node = ???
}
object Switch {
  def apply(cf: Node, value: Node): Node = ???
}
object Try {
  def apply(cf: Node): Node = ???
}
object CaseTrue {
  def apply(cf: Node): Node = ???
}
object CaseFalse {
  def apply(cf: Node): Node = ???
}
object CaseConst {
  def apply(cf: Node, const: Node): Node = ???
}
object CaseDefault {
  def apply(cf: Node): Node = ???
}
object CaseException {
  def apply(cf: Node): Node = ???
}
object Merge {
  def apply(cfs: Seq[Node]): Node = ???
}
object Return {
  def apply(cf: Node, ef: Node, value: Node): Node = ???
  def unapply(node: Node): Option[(Node, Node, Node)] = ???
}
object Throw {
  def apply(cf: Node, ef: Node, value: Node): Node = ???
  def unapply(node: Node): Option[(Node, Node, Node)] = ???
}
object Undefined {
  def apply(cf: Node, ef: Node): Node = ???
  def unapply(node: Node): Option[(Node, Node)] = ???
}
object End {
  def apply(cfs: Seq[Node]): Node = ???
}

object EfPhi {
  def apply(cf: Node, efs: Seq[Node]): Node = ???
}
object Equals {
  def apply(ef: Node, left: Node, right: Node): Node = ???
}
object Call {
  def apply(ef: Node, funptr: Node, args: Seq[Node]): Node = ???
}
object Load {
  def apply(ef: Node, ptr: Node): Node = ???
}
object Store {
  def apply(ef: Node, ptr: Node, value: Node): Node = ???
}

object Add  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Sub  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Mul  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Div  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Mod  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Shl  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Lshr extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Ashr extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object And  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Or   extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Xor  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Eq   extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Neq  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Lt   extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Lte  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Gt   extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }
object Gte  extends ((Node, Node) => Node) { def apply(left: Node, right: Node): Node = ??? }

object Trunc    extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Zext     extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Sext     extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Fptrunc  extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Fpext    extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Fptoui   extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Fptosi   extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Uitofp   extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Sitofp   extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Ptrtoint extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Inttoptr extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Bitcast  extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Cast     extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Box      extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }
object Unbox    extends ((Node, Node) => Node) { def apply(value: Node, ty: Node): Node = ??? }

object Phi         { def apply(cf: Node, values: Seq[Node]): Node = ??? }
object Is          { def apply(value: Node, ty: Node): Node = ??? }
object Alloc       { def apply(ty: Node): Node = ??? }
object Salloc      { def apply(ty: Node, n: Node): Node = ??? }
object Length      { def apply(value: Node): Node = ??? }
object Elem        { def apply(value: Node, index: Node): Node = ??? }
object Param       { def apply(name: Name, ty: Node): Node = ??? }
object ValueOf     { def apply(defn: Node): Node = ??? }
object ExceptionOf { def apply(cf: Node): Node = ??? }
object TagOf       { def apply(defn: Node): Node = ??? }
object ConstOf     { def apply(const: Const): Node = ??? }
object TagConst    { def apply(defn: Node): Node = ??? }

object Null  { def apply(): Node = ??? }
object Unit  { def apply(): Node = ??? }
object True  { def apply(): Node = ??? }
object False { def apply(): Node = ??? }
object I8    { def apply(v: Byte): Node = ??? }
object I16   { def apply(v: Short): Node = ??? }
object I32   { def apply(v: Int): Node = ??? }
object I64   { def apply(v: Long): Node = ??? }
object F32   { def apply(v: Float): Node = ??? }
object F64   { def apply(v: Double): Node = ??? }
object Str   { def apply(v: String): Node = ??? }

object Class     { def apply(name: Name, rels: Seq[Node]): Node = ??? }
object Interface { def apply(name: Name, rels: Seq[Node]): Node = ??? }
object Module    { def apply(name: Name, rels: Seq[Node]): Node = ??? }
object Declare   { def apply(name: Name, ty: Node, params: Seq[Node], rels: Seq[Node]): Node = ??? }
object Define    { def apply(name: Name, ty: Node, params: Seq[Node], end: Node, rels: Seq[Node]): Node = ??? }
object Field     { def apply(name: Name, ty: Node, rels: Seq[Node]): Node = ??? }
object Extern    { def apply(name: Name): Node = ??? }
object Type      { def apply(shape: Shape, holes: Seq[Node]): Node = ??? }
