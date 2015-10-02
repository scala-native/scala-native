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
    case (opts, s :: Nil) =>
      s.split(" ") match {
        case Array(kind, id) =>
          kind match {
            case "class"     => (opts, Name.Class(id))
            case "module"    => (opts, Name.Module(id))
            case "interface" => (opts, Name.Interface(id))
            case _           => abort("Only classes, modules and interfaces qualify as entry points.")
          }
        case _ =>
          abort("Entry point must consist of kind and id.")
      }
    case (opts, _       ) => abort("Compiler takes a single entry point.")
  }
  val classpath = Opt.get[Opt.Cp](opts).value
  val deserializer = classpath.deserializer(entry).getOrElse {
    abort(s"Couldn't resolve entry point ${entry.fullString}")
  }
  val scope = deserializer.scope
  Opt.get[Opt.Dot](opts).value.foreach { path =>
    serializeDotFile(scope, path)
  }
}
