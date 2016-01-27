package native
package compiler

final case class Opts(
  classpath: Seq[String],
  outpath: String,
  entry: String,
  gen: codegen.Gen
)
object Opts {
  lazy val empty =
    Opts(
      classpath = Seq(""),
      outpath = "out",
      entry = "",
      gen = codegen.GenTextualLLVM
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
      case "-g" +: genname +: rest =>
        val gen = genname match {
          case "nir" => codegen.GenTextualNIR
          case "ll"  => codegen.GenTextualLLVM
        }
        fromArgs(rest, base = base.copy(gen = gen))
      case opt +: rest =>
        throw new Exception(s"unrecognized option $opt")
    }
}
