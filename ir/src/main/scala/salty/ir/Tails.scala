package salty.ir

import scala.collection.{mutable => mut}
import salty.ir.Instr._

final case class Tails(cf: Cf, ef: Ef, value: Val) {
  assert(value != null)
  def wrap[T](f: (Cf, Ef) => T)      = f(cf, ef)
  def wrap[T](f: (Cf, Ef, Val) => T) = f(cf, ef, value)
  def withCf(newcf: Cf)              = Tails(newcf, ef, value)
  def withEf(newef: Ef)              = Tails(cf, newef, value)
  def withValue(newvalue: Val)       = Tails(cf, ef, newvalue)
  def mapCf(f: Cf => Cf)             = Tails(f(cf), ef, value)
  def mapEf(f: Ef => Val with Ef)    = { val newef = f(ef); Tails(cf, newef, newef) }
  def mapValue(f: Val => Val)        = Tails(cf, ef, f(value))
}
object Tails {
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
