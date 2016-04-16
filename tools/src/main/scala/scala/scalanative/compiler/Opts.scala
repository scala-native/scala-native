package scala.scalanative
package compiler

final case class Opts(
  classpath: Seq[String],
  outpath: String,
  entry: String,
  debug: Boolean
)
