package scala.scalanative
package util

import scala.annotation.nowarn

import language.implicitConversions

class ScopedVar[A] {
  import ScopedVar.Assignment

  private var init = false
  @nowarn("msg=`= _` has been deprecated")
  private var value: A = _

  def get: A = if (!init) throw ScopedVar.Unitialized() else value
  def :=(newValue: A): Assignment[A] = new Assignment(this, newValue)
  def isInitialized: Boolean = init
}

object ScopedVar {
  case class Unitialized() extends Exception

  class Assignment[T](scVar: ScopedVar[T], value: T) {
    private[ScopedVar] def push(): AssignmentStackElement[T] = {
      val stack = new AssignmentStackElement(scVar, scVar.init, scVar.value)
      scVar.init = true
      scVar.value = value
      stack
    }
  }

  private class AssignmentStackElement[T](
      scVar: ScopedVar[T],
      oldInit: Boolean,
      oldValue: T
  ) {
    private[ScopedVar] def pop(): Unit = {
      scVar.init = oldInit
      scVar.value = oldValue
    }
  }

  implicit def toValue[T](scVar: ScopedVar[T]): T = scVar.get

  @nowarn("msg=`_` is deprecated for wildcard arguments of types")
  def scoped[T](ass: Assignment[_]*)(body: => T): T = {
    val stack = ass.map(_.push())
    try body
    finally stack.reverse.foreach(_.pop())
  }
  // @nowarn("msg=The syntax `x: _\\*` is no longer supported for vararg splices")
  // @nowarn("msg=`_` is deprecated for wildcard arguments of types")
  @nowarn() // Cannot define multiple @nowarn annottations in Scala 2.12
  def scopedPushIf[T](
      shouldPushAssignments: Boolean
  )(lazyAssignments: => Seq[Assignment[_]])(body: => T): T = {
    if (shouldPushAssignments) scoped(lazyAssignments: _*)(body)
    else body
  }
}
