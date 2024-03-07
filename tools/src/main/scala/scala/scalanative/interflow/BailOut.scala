package scala.scalanative
package interflow

private[interflow] final case class BailOut(val msg: String)
    extends Exception(msg)
    with scala.util.control.NoStackTrace
