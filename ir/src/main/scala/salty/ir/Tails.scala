package salty.ir

import scala.collection.{mutable => mut}
import salty.ir.{Node => N}
import salty.ir.{Desc => D}, D.{Cf, Ef, Val, Termn}

final case class Tails(open: Seq[Focus], closed: Seq[Node]) {
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

  def end(wrap: (Node, Node, Node) => Node): Node =
    End(open.map { case Focus(cf, ef, v) => wrap(cf, ef, v) } ++ closed)

  def +:(focus: Focus): Tails =
    if (focus.cf.desc.isInstanceOf[Termn])
      Tails(open, focus.cf +: closed)
    else
      Tails(focus +: open, closed)

  def ++(tails: Tails): Tails =
    Tails(tails.open ++ open, tails.closed ++ closed)
}
object Tails {
  final case class NotMergeable(tails: Tails) extends Exception

  val empty = Tails(Seq(), Seq())
  def open(focus: Focus): Tails = Tails(Seq(focus), Seq())
  def termn(termn: Node): Tails = Tails(Seq(), Seq(termn))

  def flatten(tails: Seq[Tails]) = {
    val open = tails.flatMap(_.open)
    val closed = tails.flatMap(_.closed)
    Tails(open, closed)
  }
}

final case class Focus(cf: Node, ef: Node, value: Node) {
  def withCf(newcf: Node)       = copy(cf = newcf)
  def withEf(newef: Node)       = copy(ef = newef)
  def withValue(newvalue: Node) = copy(value = newvalue)
  def mapCf(f: Node => Node)    = copy(cf = f(cf))
  def mapEf(f: Node => Node)    = { val newef = f(ef); copy(ef = newef, value = newef) }
  def mapValue(f: Node => Node) = copy(value = f(value))
}
object Focus {
  def start(): Focus = {
    val start = Start()
    Focus(start, start, Unit())
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
