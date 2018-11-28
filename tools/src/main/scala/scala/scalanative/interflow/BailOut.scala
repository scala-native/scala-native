package scala.scalanative
package interflow

final case class BailOut(val msg: String) extends Exception(msg)
