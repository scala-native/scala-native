package scala.scalanative
package nir

/** The identifier of a type or method (aka a symbol). */
sealed abstract class Global {

  /** Returns the owner of `this`. */
  def top: Global.Top

  /** Returns a member of `this` with the given signature.
   *
   *    - Requires: `this` is a top-level symbol.
   */
  def member(sig: Sig): Global.Member

  /** Returns a member of `this` with the given signature.
   *
   *    - Requires: `this` is a top-level symbol.
   */
  def member(sig: Sig.Unmangled): Global.Member =
    member(sig.mangled)

  /** Returns `true` iff `this` is a top-level symbol. */
  final def isTop: Boolean =
    this.isInstanceOf[Global.Top]

  /** Returns a textual representation of `this`. */
  final def show: String =
    Show(this)

  /** Returns the mangled representation of `this`. */
  final def mangle: String =
    Mangle(this)

}

object Global {

  /** A stub to introduce `null`s.
   *
   *  Instances of this class are never emitted from actual code. Instead, they
   *  can be used as intermediate placeholders during code generation or markers
   *  of erroneous code paths. In particular, they can be used to skip null
   *  checks.
   */
  case object None extends Global {

    override def top: Global.Top =
      throw new Exception("None doesn't have a top.")

    override def member(sig: Sig) =
      throw new Exception("Global.None can't have any members.")

  }

  /** A top-level symbol.
   *
   *  Top-level symbols describe types (i.e., classes and traits). Note that
   *  type aliases are not preserved in NIR.
   */
  final case class Top(val id: String) extends Global {

    override def top: Global.Top =
      this

    override def member(sig: Sig): Global.Member =
      Global.Member(this, sig)

  }

  /** A member of some top-level symbol having its own signature.
   *
   *  Member symbols describe methods and fields, including duplicates generated
   *  by interflow. A can only be described based on their "owner" symbol, which
   *  is always `Global.Top`; members shall not have other members.
   */
  final case class Member(val owner: Top, val sig: Sig) extends Global {

    override def top: Global.Top =
      owner

    override def member(sig: Sig): Global.Member =
      throw new Exception("Global.Member can't have any members.")
  }

  /** The order between global symbols. */
  implicit val globalOrdering: Ordering[Global] =
    Ordering.by[Global, (String, String)] {
      case Global.Member(Global.Top(id), sig) =>
        (id, sig.mangle)
      case Global.Top(id) =>
        (id, "")
      case _ =>
        ("", "")
    }

}
