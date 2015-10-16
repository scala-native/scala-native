package salty.tools.compiler

import salty.ir._
import salty.ir.serialization._
import salty.tools.compiler.reductions._

object Main extends App {
  def abort(msg: String): Nothing = {
    println(msg)
    System.exit(1)
    throw new Exception("unreachable")
  }
  val (opts, id) = Opt.parse(args.toList) match {
    case (opts, id :: Nil) =>
      (opts, id)
    case (opts, _ ) =>
      abort("Compiler takes a single entry point.")
  }
  val classpath = Opt.get[Opt.Cp](opts).value
  def resolve(n: Name) =
    classpath.resolve(n).getOrElse {
      abort(s"Couldn't resolve $n")
    }
  val main = {
    val moduleName = Name.Module(id)
    val methodName =
      Name.Method(moduleName, "main",
        Vector(Name.Slice(Name.Class("java.lang.String"))),
        Name.Primitive("unit"))
    val module = resolve(moduleName)
    val method = resolve(methodName)
    val elem = MethodElem(Empty, module, method)
    val call = Call(elem, elem, Seq())
    val end = End(Seq(Return(Empty, call, Unit())))
    Define(Prim.Unit, Seq(), end, name = Name.Main)
  }
  Reduction.run(ModuleLowering, main)
  Opt.get[Opt.Dot](opts).value.foreach { path =>
    val scope = Scope(Map(Name.Main -> main))
    serializeDotFile(scope, path)
  }
}
