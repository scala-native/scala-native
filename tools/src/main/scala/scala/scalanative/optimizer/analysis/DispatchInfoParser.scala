package scala.scalanative
package optimizer
package analysis

import scala.util.parsing.combinator._
import scala.util.matching.Regex

object DispatchInfoParser extends JavaTokenParsers {

  def apply(in: String): Map[String, Seq[Int]] =
    parseAll(dispatchInfo, in) match {
      case Success(info, _) =>
        info
      case Failure(msg, next) =>
        throw new Exception(msg + "\n" + next.source.toString)
      case Error(msg, next) =>
        throw new Exception(msg)
    }

  def dispatchType: Parser[String] =
    rep1sep(ident, ".") ^^ { _ mkString "." }

  def dispatchHeader: Parser[String] =
    "=" ~> ident ~ "." ~ wholeNumber ~ ":" ~ dispatchType ~ ":" ^^ {
      case scp ~ "." ~ id ~ ":" ~ names ~ ":" =>
        s"""$scp.$id:$names"""
    }

  def dispatchMethod: Parser[(String, Seq[Int])] =
    dispatchHeader ~ rep1(wholeNumber) ^^ {
      case header ~ tpes =>
        (header, tpes map (_.toInt))
    }


  def dispatchInfo: Parser[Map[String, Seq[Int]]] =
    rep(dispatchMethod) ^^ (_.toMap)

}
