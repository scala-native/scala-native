package scala.math

abstract class ScalaNumber extends java.lang.Number {
  protected def isWhole(): Boolean
  def underlying(): Object
}
