package salty.ir

import scala.collection.{mutable => mut}
import salty.ir.Instr._

final case class Tails private[ir] (open: Seq[Focus], closed: Seq[Termn]) {
  override def toString =
    s"Tails(${open.map(_.cf.getClass)}, ${closed.map(_.getClass)})"

  def merge: (Focus, Tails) = {
    val foc = open match {
      case Seq()    =>
        throw Tails.NotMergeable(this)
      case Seq(foc) =>
        foc
      case focs     =>
        val cf    = Merge(focs.map(_.cf))
        val ef    = EfPhi(cf, focs.map(_.ef))
        val value = Phi(cf, focs.map(_.value))
        Focus(cf, ef, value)
    }
    (foc, Tails(Seq(), closed))
  }
  def end: End =
    End(open.map { case Focus(cf, ef, v) => Return(cf, ef, v) } ++ closed)

  def +:(focus: Focus): Tails = focus.cf match {
    case termn: Termn => Tails(open, termn +: closed)
    case _            => Tails(focus +: open, closed)
  }

  def ++(tails: Tails): Tails =
    Tails(tails.open ++ open, tails.closed ++ closed)
}
object Tails {
  final case class NotMergeable(tails: Tails) extends Exception

  val empty = Tails(Seq(), Seq())
  def open(focus: Focus): Tails = Tails(Seq(focus), Seq())
  def termn(termn: Termn): Tails = Tails(Seq(), Seq(termn))

  def flatten(tails: Seq[Tails]) = {
    val open = tails.flatMap(_.open)
    val closed = tails.flatMap(_.closed)
    Tails(open, closed)
  }
}

final case class Focus(cf: Cf, ef: Ef, value: Val) {
  def withCf(newcf: Cf)              = copy(cf = newcf)
  def withEf(newef: Ef)              = copy(ef = newef)
  def withValue(newvalue: Val)       = copy(value = newvalue)
  def mapCf(f: Cf => Cf)             = copy(cf = f(cf))
  def mapEf(f: Ef => Ef with Val)    = { val newef = f(ef); copy(ef = newef, value = newef) }
  def mapValue(f: Val => Val)        = copy(value = f(value))
}
object Focus {
  def start(): Focus = {
    val start = Start()
    Focus(start, start, Unit)
  }

  def sequenced[T](elems: Seq[T], foc: Focus)
                  (f: (T, Focus) => Tails): (Seq[Focus], Tails) = {
    val focs = new mut.ListBuffer[Focus]
    val tails = new mut.ListBuffer[Tails]
    elems.foldLeft(foc) { (accfoc, elem) =>
      val (efoc, etails) = f(elem, accfoc).merge
      focs += efoc
      tails += etails
      efoc
    }
    (focs.toSeq, Tails.flatten(tails.toSeq))
  }
}
