package salty.ir

import scala.collection.{mutable => mut}
import salty.ir.Instr._

final case class Tails(cf: Cf, ef: Ef, value: Val) {
  assert(value != null)
  def wrap[T](f: (Cf, Ef) => T)      = f(cf, ef)
  def wrap[T](f: (Cf, Ef, Val) => T) = f(cf, ef, value)
  def withCf(newcf: Cf)              = copy(cf = newcf)
  def withEf(newef: Ef)              = copy(ef = newef)
  def withValue(newvalue: Val)       = copy(value = newvalue)
  def mapCf(f: Cf => Cf)             = copy(cf = f(cf))
  def mapEf(f: Ef => Val with Ef)    = { val newef = f(ef); copy(ef = newef, value = newef) }
  def mapValue(f: Val => Val)        = copy(value = f(value))
}
object Tails {
  def start() =
    Tails(Start())
  def apply(cf: Cf with Ef): Tails =
    Tails(cf, cf, Unit)
  def apply(cf: Cf, ef: Ef): Tails =
    Tails(cf, ef, Unit)
  def merge(tails: Seq[Tails]) = {
    val cf = Merge(tails.map(_.cf))
    Tails(cf,
      EfPhi(cf, tails.map(_.ef)),
      Phi(cf, tails.map(_.value)))
  }
  def fold[T](elems: Seq[T], tails: Tails)(f: (T, Tails) => Tails): Seq[Tails] = {
    val buf = new mut.ListBuffer[Tails]
    elems.foldLeft(tails) { (etails, elem) =>
      val ntails = f(elem, etails)
      buf += ntails
      ntails
    }
    buf.toSeq
  }
}
