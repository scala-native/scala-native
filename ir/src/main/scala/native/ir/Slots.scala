package native
package ir

sealed trait Context {
  def isVal:  Boolean
  def isCf:   Boolean
  def isEf:   Boolean
  def isDefn: Boolean
}

sealed trait Use extends Context {
  def use: Node
}
object Use {
  def unapply(use: Use): Some[Node] = Some(use.use)
}

sealed trait Dep extends Context {
  def dep: Node
}
object Dep {
  def unapply(dep: Dep): Some[Node] = Some(dep.dep)
}

final class MultiDep(val node: Node, val length: Int, val offset: Int) {
  def apply(index: Int): Node =
    node._slots(offset + index).dep

  def deps: Seq[Node] = {
    var i = offset
    Seq.fill(length) {
      val n = node._slots(i).dep
      i += 1
      n
    }
  }
}

// TODO: compute schema dynamically
private[ir] final class Slot private[ir] (
  val schema: Schema,
  val use:    Node,
  var dep:    Node
) extends java.lang.Cloneable with Use with Dep {
  def isVal:  Boolean = schema == Schema.Val
  def isCf:   Boolean = schema == Schema.Cf
  def isEf:   Boolean = schema == Schema.Ef
  def isDefn: Boolean = schema == Schema.Ref

  dep._uses += this

  def :=(value: Node): Unit = {
    dep._uses -= this
    dep = value
    dep._uses += this
  }

  override def toString = s"Slot(use = $use, dep = $dep)"
}


