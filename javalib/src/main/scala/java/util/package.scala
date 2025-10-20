package java

package object util {

  private[util] implicit class CompareNullablesOps(val self: Any)
      extends AnyVal {
    @inline
    def ===(that: Any): Boolean = Objects.equals(self, that)

    @inline
    def =:=(that: Any): Boolean = {
      val aself = self.asInstanceOf[AnyRef]
      val athat = that.asInstanceOf[AnyRef]
      if (aself eq null) athat eq null
      else aself eq athat
    }
  }

  private[util] final case class Box[+K](inner: K) {
    def apply(): K = inner

    override def equals(o: Any): Boolean = {
      o match {
        case o: Box[?] => inner === o.inner
        case _         => false
      }
    }

    override def hashCode(): Int =
      if (inner == null) 0
      else inner.hashCode
  }

  private[util] final case class IdentityBox[+K](inner: K) {
    def apply(): K = inner

    override def equals(o: Any): Boolean = {
      o match {
        case o: IdentityBox[?] => inner =:= o.inner
        case _                 => false
      }
    }

    override def hashCode(): Int =
      System.identityHashCode(inner.asInstanceOf[AnyRef])

  }

  private[util] def defaultOrdering[E]: Ordering[E] = {
    new Ordering[E] {
      def compare(a: E, b: E): Int =
        a.asInstanceOf[Comparable[E]].compareTo(b)
    }
  }
}
