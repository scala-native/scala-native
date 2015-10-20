package salty.tools.compiler

sealed abstract class Opt
object Opt {
  final case class Key[T](value: String)
  final case class Default[T](value: T)

  final case class Cp(value: ClasspathLoader) extends Opt
  object Cp {
    implicit val key:     Key[Cp]     = Key[Cp]("cp")
    implicit val default: Default[Cp] = Default(Cp(new ClasspathLoader(Seq("."))))
  }

  final case class Dot(value: Option[String]) extends Opt
  object Dot {
    implicit val key:     Key[Dot]     = Key[Dot]("dot")
    implicit val default: Default[Dot] = Default(Dot(None))
  }

  def parse(args: Seq[String]): (Map[Opt.Key[_], Opt], Seq[String]) =
    args match {
      case "-cp" :: classpath :: rest =>
        val (restopts, restrest) = parse(rest)
        (restopts + (Opt.Cp.key -> Opt.Cp(new ClasspathLoader(classpath.split(":")))), restrest)
      case "-dot" :: path :: rest =>
        val (restopts, restrest) = parse(rest)
        (restopts + (Opt.Dot.key -> Opt.Dot(Some(path))), restrest)
      case rest =>
        (Map(), rest)
    }

  def get[T: Opt.Key: Opt.Default](options: Map[Opt.Key[_], Opt]): T =
    options.get(implicitly[Opt.Key[T]]).getOrElse(implicitly[Opt.Default[T]].value).asInstanceOf[T]
}
