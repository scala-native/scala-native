package salty.tools.compiler

import salty.ir._
import salty.ir.serialization._

object Main extends App {
  def abort(msg: String): Nothing = {
    println(msg)
    System.exit(1)
    throw new Exception("unreachable")
  }

  val (opts, entry) = Opt.parse(args.toList) match {
    case (opts, id :: Nil) =>
      val entry =
        Name.Method(Name.Module(id), "main",
          Vector(Name.Slice(Name.Class("java.lang.String"))),
          Name.Primitive("unit"))
      (opts, entry)
    case (opts, _ ) =>
      abort("Compiler takes a single entry point.")
  }
  val classpath = Opt.get[Opt.Cp](opts).value
  val node = classpath.resolve(entry).getOrElse {
    abort(s"Couldn't resolve entry point ${entry.fullString}")
  }
  Opt.get[Opt.Dot](opts).value.foreach { path =>
    val scope = Scope(Map(entry -> node))
    serializeDotFile(scope, path)
  }
}
