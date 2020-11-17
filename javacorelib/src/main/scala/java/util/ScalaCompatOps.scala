package java.util

import scala.collection.mutable

private[util] object ScalaCompatOps {

  implicit class ToScalaMutableSortedSetCompatOps[A] private[ScalaCompatOps] (
      private val self: mutable.SortedSet[A])
      extends AnyVal {
    def compatOps: ScalaMutableSetCompatOps[A] =
      new ScalaMutableSetCompatOps[A](self)
  }

  class ScalaMutableSetCompatOps[A] private[ScalaCompatOps] (
      private val self: mutable.SortedSet[A])
      extends AnyVal {

    def rangeUntil(until: A): mutable.SortedSet[A] =
      self.rangeImpl(None, Some(until))

    def rangeFrom(from: A): mutable.SortedSet[A] =
      self.rangeImpl(Some(from), None)

    def rangeTo(to: A): mutable.SortedSet[A] = {
      val i = rangeFrom(to).iterator
      if (i.isEmpty) self
      else {
        val next = i.next()
        if (defaultOrdering.compare(next, to) == 0)
          if (i.isEmpty) self
          else rangeUntil(i.next())
        else
          rangeUntil(next)
      }
    }
  }
}
