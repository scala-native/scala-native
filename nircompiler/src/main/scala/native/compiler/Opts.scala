package native
package compiler

final case class Opts(
  classpath: Seq[String],
  outpath: String,
  entry: String
)
object Opts {
  lazy val empty =
    Opts(
      classpath = Seq(""),
      outpath = "",
      entry = ""
    )

  def fromArgs(args: Seq[String], base: Opts = Opts.empty): Opts =
    args match {
      case Seq() =>
        base
      case "-cp" +: classpath +: rest =>
        fromArgs(rest, base = base.copy(classpath = classpath.split(":")))
      case "-o" +: outpath +: rest =>
        fromArgs(rest, base = base.copy(outpath = outpath))
      case "-e" +: entry +: rest =>
        fromArgs(rest, base = base.copy(entry = entry))
      case opt +: rest =>
        throw new Exception(s"unrecognized option $opt")
    }
}
