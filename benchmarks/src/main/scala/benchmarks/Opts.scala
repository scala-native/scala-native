package benchmarks

final class Opts(val format: Format = TextFormat,
                 val test: Boolean = false) {
  private def copy(format: Format = this.format,
                   test: Boolean = this.test) = new Opts(format, test)
  def withFormat(value: Format) = copy(format = value)
  def withTest(value: Boolean) = copy(test = value)
}

object Opts {
  def apply(args: Array[String]): Opts = {
    def loop(opts: Opts, args: List[String]): Opts = args match {
      case "--format" :: format :: rest =>
        loop(opts.copy(format = Format(format)), rest)
      case "--test" :: rest =>
        loop(opts.copy(test = true), rest)
      case other :: _ =>
        throw new Exception("unrecognized option: " + other)
      case Nil =>
        opts
    }
    loop(new Opts(), args.toList)
  }
}


