package scala.scalanative
package compiler

final case class Opts(
    classpath: Seq[String],
    outpath: String,
    entry: String,
    verbose: Boolean
)
