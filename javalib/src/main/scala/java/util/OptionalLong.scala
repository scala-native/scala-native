package java.util

// New work for Scala Native. Based on Scala Native Optional.scala:
//   Ported from Scala.js commit SHA1: 9c79cb9 dated: 2022-03-18

import java.util.function._
import java.util.{stream => jus}

final class OptionalLong private (hasValue: Boolean, value: Long) {

  def getAsLong(): Long = {
    if (!isPresent())
      throw new NoSuchElementException()
    else
      value
  }

  @inline def isPresent(): Boolean = hasValue

  // Since: Java 11
  @inline def isEmpty(): Boolean = !hasValue

  def ifPresent(action: LongConsumer): Unit = {
    if (isPresent())
      action.accept(value)
  }

  // Since: Java 9
  def ifPresentOrElse(action: LongConsumer, emptyAction: Runnable): Unit = {
    if (isPresent())
      action.accept(value)
    else
      emptyAction.run()
  }

  def orElse(other: Long): Long =
    if (isPresent()) value
    else other

  def orElseGet(supplier: LongSupplier): Long =
    if (isPresent()) value
    else supplier.getAsLong()

  // Since: Java 10
  def orElseThrow(): Long =
    if (isPresent()) value
    else throw new NoSuchElementException()

  def orElseThrow[X <: Throwable](exceptionSupplier: Supplier[_ <: X]): Long =
    if (isPresent()) value
    else throw exceptionSupplier.get()

  // Since: Java 9
  def stream(): jus.LongStream =
    if (isPresent()) jus.LongStream.of(value)
    else jus.LongStream.empty()

  override def equals(obj: Any): Boolean = {
    obj match {
      case opt: OptionalLong =>
        (!isPresent() && !opt.isPresent()) ||
          (isPresent() && opt
            .isPresent() && Objects.equals(value, opt.getAsLong()))
      case _ => false
    }
  }

  override def hashCode(): Int = {
    if (!isPresent()) 0
    else value.hashCode()
  }

  override def toString(): String = {
    if (!isPresent()) "Optional.empty"
    else s"OptionalLong[$value]"
  }
}

object OptionalLong {
  def empty(): OptionalLong = new OptionalLong(hasValue = false, 0L)

  def of(value: Long): OptionalLong = {
    new OptionalLong(hasValue = true, value)
  }
}
