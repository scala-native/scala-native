// Ported from Scala.js commit: 35c2061 dated: 2026-02-16
// Note: this file has differences noted below

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

import java.util.function._

/** Make some Scala collection APIs available on Java collections. */
private[java] object ScalaOps {

  /* The following should be left commented out until the point where
   * we can run the javalib with -Yno-predef
   * See: https://github.com/scala-native/scala-native/issues/2885
   * Note 1: Removed scalastyle comments
   * Note 2: Adapted mkstring for SN and added additional mkString methods
   */

  // implicit class IntScalaOps private[ScalaOps] (val __self: Int) extends AnyVal {
  //   @inline def until(end: Int): SimpleRange =
  //     new SimpleRange(__self, end)

  //   @inline def to(end: Int): SimpleInclusiveRange =
  //     new SimpleInclusiveRange(__self, end)
  // }

  // @inline
  // final class SimpleRange(start: Int, end: Int) {
  //   @inline
  //   def foreach[U](f: IntConsumer): Unit = {
  //     var i = start
  //     while (i < end) {
  //       f.accept(i)
  //       i += 1
  //     }
  //   }
  // }

  // @inline
  // final class SimpleInclusiveRange(start: Int, end: Int) {
  //   @inline
  //   def foreach[U](f: IntConsumer): Unit = {
  //     var i = start
  //     while (i <= end) {
  //       f.accept(i)
  //       i += 1
  //     }
  //   }
  // }

  implicit class ToJavaIterableOps[A] private[ScalaOps] (
      val __self: java.lang.Iterable[A]
  ) extends AnyVal {
    def scalaOps: JavaIterableOps[A] = new JavaIterableOps[A](__self)
  }

  class JavaIterableOps[A] private[ScalaOps] (val __self: java.lang.Iterable[A])
      extends AnyVal {

    @inline def foreach(f: Consumer[A]): Unit =
      __self.iterator().scalaOps.foreach(f)

    @inline def count(f: Predicate[A]): Int =
      __self.iterator().scalaOps.count(f)

    @inline def exists(f: Predicate[A]): Boolean =
      __self.iterator().scalaOps.exists(f)

    @inline def forall(f: Predicate[A]): Boolean =
      __self.iterator().scalaOps.forall(f)

    @inline def indexWhere(f: Predicate[A]): Int =
      __self.iterator().scalaOps.indexWhere(f)

    @inline def findFold[B](f: Predicate[A])(default: Supplier[B])(
        g: Function[A, B]
    ): B =
      __self.iterator().scalaOps.findFold(f)(default)(g)

    @inline def foldLeft[B](z: B)(f: BiFunction[B, A, B]): B =
      __self.iterator().scalaOps.foldLeft(z)(f)

    @inline def reduceLeft[B >: A](f: BiFunction[B, A, B]): B =
      __self.iterator().scalaOps.reduceLeft(f)

    @inline def mkString(start: String, sep: String, end: String): String =
      __self.iterator().scalaOps.mkString(start, sep, end)

    @inline def mkString(sep: String): String = mkString("", sep, "")

    @inline def mkString: String = mkString("", "", "")
  }

  implicit class ToJavaIteratorOps[A] private[ScalaOps] (
      val __self: Iterator[A]
  ) extends AnyVal {
    def scalaOps: JavaIteratorOps[A] = new JavaIteratorOps[A](__self)
  }

  class JavaIteratorOps[A] private[ScalaOps] (val __self: Iterator[A])
      extends AnyVal {

    @inline def foreach(f: Consumer[A]): Unit = {
      while (__self.hasNext())
        f.accept(__self.next())
    }

    @inline def count(f: Predicate[A]): Int =
      foldLeft(0)((prev, x) => if (f.test(x)) prev + 1 else prev)

    @inline def exists(f: Predicate[A]): Boolean = {
      while (__self.hasNext()) {
        if (f.test(__self.next()))
          return true
      }
      false
    }

    @inline def forall(f: Predicate[A]): Boolean =
      !exists(x => !f.test(x))

    @inline def indexWhere(f: Predicate[A]): Int = {
      var i = 0
      while (__self.hasNext()) {
        if (f.test(__self.next()))
          return i
        i += 1
      }
      -1
    }

    @inline def findFold[B](
        f: Predicate[A]
    )(default: Supplier[B])(g: Function[A, B]): B = {
      while (__self.hasNext()) {
        val x = __self.next()
        if (f.test(x))
          return g(x)
      }
      default.get()
    }

    @inline def foldLeft[B](z: B)(f: BiFunction[B, A, B]): B = {
      var result: B = z
      while (__self.hasNext())
        result = f(result, __self.next())
      result
    }

    @inline def reduceLeft[B >: A](f: BiFunction[B, A, B]): B = {
      if (!__self.hasNext())
        throw new NoSuchElementException("collection is empty")
      foldLeft[B](__self.next())(f)
    }

    /* Scala.js Strings are treated as primitive types so we use
     * java.lang.StringBuilder for Scala Native
     */
    @inline def mkString(start: String, sep: String, end: String): String = {
      val sb = new java.lang.StringBuilder(start)
      var first = true
      while (__self.hasNext()) {
        if (first)
          first = false
        else
          sb.append(sep)
        sb.append(__self.next().asInstanceOf[Object])
      }
      sb.append(end)
      sb.toString
    }

    @inline def mkString(sep: String): String = mkString("", sep, "")

    @inline def mkString: String = mkString("", "", "")
  }

  implicit class ToJavaEnumerationOps[A] private[ScalaOps] (
      val __self: Enumeration[A]
  ) extends AnyVal {
    def scalaOps: JavaEnumerationOps[A] = new JavaEnumerationOps[A](__self)
  }

  class JavaEnumerationOps[A] private[ScalaOps] (val __self: Enumeration[A])
      extends AnyVal {

    @inline def foreach(f: Consumer[A]): Unit = {
      while (__self.hasMoreElements())
        f.accept(__self.nextElement())
    }
  }

}
