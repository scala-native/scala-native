package java.util

// New work for Scala Native. Based on Scala Native Optional.scala:
//   Ported from Scala.js commit SHA1: 9c79cb9 dated: 2022-03-18

import java.util.function._
import java.util.{stream => jus}

final class OptionalInt private (hasValue: Boolean, value: Int) {

  def getAsInt(): Int = {
    if (!isPresent())
      throw new NoSuchElementException()
    else
      value
  }

  @inline def isPresent(): Boolean = hasValue

  // Since: Java 11
  @inline def isEmpty(): Boolean = !hasValue

  def ifPresent(action: IntConsumer): Unit = {
    if (isPresent())
      action.accept(value)
  }

  // Since: Java 9
  def ifPresentOrElse(action: IntConsumer, emptyAction: Runnable): Unit = {
    if (isPresent())
      action.accept(value)
    else
      emptyAction.run()
  }

  def orElse(other: Int): Int =
    if (isPresent()) value
    else other

  def orElseGet(supplier: IntSupplier): Int =
    if (isPresent()) value
    else supplier.getAsInt()

  // Since: Java 10
  def orElseThrow(): Int =
    if (isPresent()) value
    else throw new NoSuchElementException()

  def orElseThrow[X <: Throwable](exceptionSupplier: Supplier[_ <: X]): Int =
    if (isPresent()) value
    else throw exceptionSupplier.get()

  // Since: Java 9
  def stream(): jus.IntStream =
    if (isPresent()) jus.IntStream.of(value)
    else jus.IntStream.empty()

  override def equals(obj: Any): Boolean = {
    obj match {
      case opt: OptionalInt =>
        (!isPresent() && !opt.isPresent()) ||
          (isPresent() && opt
            .isPresent() && Objects.equals(value, opt.getAsInt()))
      case _ => false
    }
  }

  override def hashCode(): Int = {
    if (!isPresent()) 0
    else value.hashCode()
  }

  override def toString(): String = {
    if (!isPresent()) "Optional.empty"
    else s"OptionalInt[$value]"
  }
}

object OptionalInt {
  def empty(): OptionalInt = new OptionalInt(hasValue = false, 0)

  def of(value: Int): OptionalInt = {
    new OptionalInt(hasValue = true, value)
  }
}
