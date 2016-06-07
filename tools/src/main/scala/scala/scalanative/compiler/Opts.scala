package scala.scalanative
package compiler

final case class Opts(classpath: Seq[String],
                      outpath: String,
                      dotpath: Option[String],
                      entry: String,
                      verbose: Boolean)
