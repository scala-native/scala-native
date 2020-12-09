/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package java.util

/** Make some Scala collection APIs available on Java collections. */
private[java] object ScalaOps {

  implicit class ToJavaIterableOps[A] private[ScalaOps] (
      val __self: java.lang.Iterable[A])
      extends AnyVal {
    def scalaOps: JavaIterableOps[A] = new JavaIterableOps[A](__self)
  }

  class JavaIterableOps[A] private[ScalaOps] (val __self: java.lang.Iterable[A])
      extends AnyVal {

    @inline def foreach[U](f: A => U): Unit =
      __self.iterator().scalaOps.foreach(f)

    @inline def count(f: A => Boolean): Int =
      __self.iterator().scalaOps.count(f)

    @inline def exists(f: A => Boolean): Boolean =
      __self.iterator().scalaOps.exists(f)

    @inline def forall(f: A => Boolean): Boolean =
      __self.iterator().scalaOps.forall(f)

    @inline def indexWhere(f: A => Boolean): Int =
      __self.iterator().scalaOps.indexWhere(f)

    @inline def find(f: A => Boolean): Option[A] =
      __self.iterator().scalaOps.find(f)

    @inline def foldLeft[B](z: B)(f: (B, A) => B): B =
      __self.iterator().scalaOps.foldLeft(z)(f)

    @inline def reduceLeft[B >: A](f: (B, A) => B): B =
      __self.iterator().scalaOps.reduceLeft(f)

    @inline def mkString(start: String, sep: String, end: String): String =
      __self.iterator().scalaOps.mkString(start, sep, end)

    @inline def min(comp: Comparator[_ >: A]): A =
      __self.iterator().scalaOps.min(comp)

    @inline def max(comp: Comparator[_ >: A]): A =
      __self.iterator().scalaOps.max(comp)

    @inline def headOption: Option[A] =
      __self.iterator().scalaOps.headOption

    @inline def head: A =
      __self.iterator().scalaOps.head

    @inline def lastOption: Option[A] =
      __self.iterator().scalaOps.lastOption

    @inline def last: A =
      __self.iterator().scalaOps.last

    @inline def toSeq: Seq[A] =
      __self.iterator().scalaOps.toSeq
  }

  implicit class ToJavaIteratorOps[A] private[ScalaOps] (
      val __self: Iterator[A])
      extends AnyVal {
    def scalaOps: JavaIteratorOps[A] = new JavaIteratorOps[A](__self)
  }

  class JavaIteratorOps[A] private[ScalaOps] (val __self: Iterator[A])
      extends AnyVal {

    @inline def foreach[U](f: A => U): Unit = {
      while (__self.hasNext())
        f(__self.next())
    }

    @inline def map[U](f: A => U): Iterator[U] = new Iterator[U] {
      override def hasNext(): Boolean = __self.hasNext()
      override def next(): U          = f(__self.next())
    }

    @inline def count(f: A => Boolean): Int =
      foldLeft(0)((prev, x) => if (f(x)) prev + 1 else prev)

    @inline def exists(f: A => Boolean): Boolean = {
      while (__self.hasNext()) {
        if (f(__self.next()))
          return true
      }
      false
    }

    @inline def forall(f: A => Boolean): Boolean =
      !exists(x => !f(x))

    @inline def indexWhere(f: A => Boolean): Int = {
      var i = 0
      while (__self.hasNext()) {
        if (f(__self.next()))
          return i
        i += 1
      }
      -1
    }

    @inline def find(f: A => Boolean): Option[A] = {
      while (__self.hasNext()) {
        val x = __self.next()
        if (f(x))
          return Some(x)
      }
      None
    }

    @inline def foldLeft[B](z: B)(f: (B, A) => B): B = {
      var result: B = z
      while (__self.hasNext())
        result = f(result, __self.next())
      result
    }

    @inline def reduceLeft[B >: A](f: (B, A) => B): B = {
      if (!__self.hasNext())
        throw new NoSuchElementException("collection is empty")
      foldLeft[B](__self.next())(f)
    }

    @inline def mkString(start: String, sep: String, end: String): String = {
      var result: String = start
      var first          = true
      while (__self.hasNext()) {
        if (first)
          first = false
        else
          result += sep
        result += __self.next()
      }
      result + end
    }

    @inline def headOption: Option[A] = {
      if (__self.hasNext()) Some(__self.next())
      else None
    }

    @inline def head: A = {
      if (__self.hasNext()) __self.next()
      else throw new NoSuchElementException("empty.head")
    }

    @inline def lastOption: Option[A] = {
      if (!__self.hasNext()) None
      else {
        var last: A = __self.next()
        while (__self.hasNext()) {
          last = __self.next()
        }
        Some(last)
      }
    }

    @inline def last: A =
      if (__self.hasNext()) lastOption.get
      else throw new NoSuchElementException("empty.last")

    @inline def min(comp: Comparator[_ >: A]): A =
      reduceLeft[A]((l, r) => if (comp.compare(l, r) <= 0) l else r)

    @inline def max(comp: Comparator[_ >: A]): A =
      reduceLeft[A]((l, r) => if (comp.compare(l, r) >= 0) l else r)

    @inline def toSeq: Seq[A] = {
      val buf = Seq.newBuilder[A]
      foreach(buf += _)
      buf.result()
    }
  }

  implicit class ToJavaEnumerationOps[A] private[ScalaOps] (
      val __self: Enumeration[A])
      extends AnyVal {
    def scalaOps: JavaEnumerationOps[A] = new JavaEnumerationOps[A](__self)
  }

  class JavaEnumerationOps[A] private[ScalaOps] (val __self: Enumeration[A])
      extends AnyVal {

    @inline def foreach[U](f: A => U): Unit = {
      while (__self.hasMoreElements())
        f(__self.nextElement())
    }
  }
}
