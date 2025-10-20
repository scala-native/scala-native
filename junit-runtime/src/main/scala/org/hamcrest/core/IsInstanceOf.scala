/*
 * Ported from https://github.com/hamcrest/JavaHamcrest/
 */
package org.hamcrest.core

import org.hamcrest.{Description, DiagnosingMatcher, Matcher}

class IsInstanceOf private (expectedClass: Class[?], matchableClass: Class[?])
    extends DiagnosingMatcher[AnyRef] {

  def this(expectedClass: Class[?]) =
    this(expectedClass, IsInstanceOf.matchableClass(expectedClass))

  override protected def matches(
      item: AnyRef,
      mismatch: Description
  ): Boolean = {
    if (null == item) {
      mismatch.appendText("null")
      false
    } else if (!matchableClass.isInstance(item)) {
      mismatch.appendValue(item).appendText(" is a " + item.getClass.getName)
      false
    } else true
  }

  override def describeTo(description: Description): Unit =
    description.appendText("an instance of ").appendText(expectedClass.getName)
}

object IsInstanceOf {

  private[IsInstanceOf] def matchableClass(
      expectedClass: Class[?]
  ): Class[?] = {
    expectedClass match {
      case java.lang.Byte.TYPE      => classOf[java.lang.Byte]
      case java.lang.Boolean.TYPE   => classOf[java.lang.Boolean]
      case java.lang.Integer.TYPE   => classOf[java.lang.Integer]
      case java.lang.Long.TYPE      => classOf[java.lang.Long]
      case java.lang.Character.TYPE => classOf[java.lang.Character]
      case java.lang.Short.TYPE     => classOf[java.lang.Character]
      case java.lang.Float.TYPE     => classOf[java.lang.Float]
      case java.lang.Double.TYPE    => classOf[java.lang.Double]
      case _                        => expectedClass
    }
  }

  // @SuppressWarnings("unchecked")
  def instanceOf[T](typ: Class[?]): Matcher[T] =
    new IsInstanceOf(typ).asInstanceOf[Matcher[T]]

  // @SuppressWarnings("unchecked")
  def any[T](typ: Class[?]): Matcher[T] =
    new IsInstanceOf(typ).asInstanceOf[Matcher[T]]
}
