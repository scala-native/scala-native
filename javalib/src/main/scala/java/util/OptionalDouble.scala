package java.util

// New work for Scala Native. Based on Scala Native Optional.scala:
//   Ported from Scala.js commit SHA1: 9c79cb9 dated: 2022-03-18

import java.util.function._
import java.util.{stream => jus}

final class OptionalDouble private (hasValue: Boolean, value: Double) {

  def getAsDouble(): Double = {
    if (!isPresent())
      throw new NoSuchElementException()
    else
      value
  }

  @inline def isPresent(): Boolean = hasValue

  // Since: Java 11
  @inline def isEmpty(): Boolean = !hasValue

  def ifPresent(action: DoubleConsumer): Unit = {
    if (isPresent())
      action.accept(value)
  }

  // Since: Java 9
  def ifPresentOrElse(action: DoubleConsumer, emptyAction: Runnable): Unit = {
    if (isPresent())
      action.accept(value)
    else
      emptyAction.run()
  }

  def orElse(other: Double): Double =
    if (isPresent()) value
    else other

  def orElseGet(supplier: DoubleSupplier): Double =
    if (isPresent()) value
    else supplier.getAsDouble()

  // Since: Java 10
  def orElseThrow(): Double =
    if (isPresent()) value
    else throw new NoSuchElementException()

  def orElseThrow[X <: Throwable](exceptionSupplier: Supplier[_ <: X]): Double =
    if (isPresent()) value
    else throw exceptionSupplier.get()

  // Since: Java 9
  def stream(): jus.DoubleStream =
    if (isPresent()) jus.DoubleStream.of(value)
    else jus.DoubleStream.empty()

  override def equals(obj: Any): Boolean = {
    obj match {
      case opt: OptionalDouble =>
        (!isPresent() && !opt.isPresent()) ||
          (isPresent() && opt
            .isPresent() && Objects.equals(value, opt.getAsDouble()))
      case _ => false
    }
  }

  override def hashCode(): Int = {
    if (!isPresent()) 0
    else value.hashCode()
  }

  override def toString(): String = {
    if (!isPresent()) "Optional.empty"
    else s"OptionalDouble[$value]"
  }
}

object OptionalDouble {
  def empty(): OptionalDouble = new OptionalDouble(hasValue = false, Double.NaN)

  def of(value: Double): OptionalDouble = {
    new OptionalDouble(hasValue = true, value)
  }
}
