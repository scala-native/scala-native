package scala.scalanative
package optimizer
package analysis

import fastparse.WhitespaceApi
import fastparse.noApi._

object DispatchInfoParser {

  private val IgnoreWhitespace = WhitespaceApi.Wrapper {
    import fastparse.all._
    NoTrace(CharIn(Seq(' ', '\t', '\n')).rep)
  }
  import IgnoreWhitespace._

  val number: P[Int] = P(CharIn('0' to '9').rep(1).!.map(_.toInt))

  val dispatchHeader: P[String] =
    P("=" ~ CharsWhile(_ != ':').! ~ ":" ~ CharsWhile(_ != ':').! ~ ":") map {
      case (src, name) => s"$src:$name"
    }

  val dispatchMethod: P[(String, Seq[Int])] =
    dispatchHeader ~ (number ~ "(" ~ number ~ ")").rep(1) map {
      case (header, tpes) => (header, tpes.sortBy(_._2).map(_._1).reverse)
    }

  val dispatchInfo: P[Map[String, Seq[Int]]] =
    dispatchMethod.rep ~ End map (_.toMap)

  def apply(in: String): Map[String, Seq[Int]] =
    dispatchInfo.parse(in) match {
      case Parsed.Success(info, _) => info
      case Parsed.Failure(_, _, _) => Map.empty
    }
}
