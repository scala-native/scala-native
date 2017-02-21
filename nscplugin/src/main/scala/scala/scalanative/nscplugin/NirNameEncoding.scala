package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.reflect.internal.Flags._
import scalanative.util.unreachable

trait NirNameEncoding { self: NirCodeGen =>
  import global.{Name => _, _}, definitions._
  import nirAddons.nirDefinitions._
  import SimpleType.{fromSymbol, fromType}

  def genAnonName(owner: Symbol, anon: Symbol) =
    genName(owner) member anon.fullName.toString tag "extern"

  def genName(sym: Symbol): nir.Global =
    if (sym.isType) {
      genTypeName(sym)
    } else if (sym.isMethod) {
      genMethodName(sym)
    } else if (sym.isField) {
      genFieldName(sym)
    } else {
      unreachable
    }

  def genTypeName(sym: Symbol): nir.Global = {
    val id = {
      val fullName = sym.fullName.toString
      if (fullName == "java.lang._String") "java.lang.String"
      else if (fullName == "java.lang._Object") "java.lang.Object"
      else if (fullName == "java.lang._Class") "java.lang.Class"
      else fullName
    }
    val name = sym match {
      case ObjectClass            => nir.Rt.Object.name
      case _ if sym.isModule      => genTypeName(sym.moduleClass)
      case _ if sym.isModuleClass => nir.Global.Top(id + "$")
      case _                      => nir.Global.Top(id)
    }
    name
  }

  def genFieldName(sym: Symbol): nir.Global = {
    val owner = genTypeName(sym.owner)
    val id    = nativeIdOf(sym)
    val tag   = if (sym.owner.isExternModule) "extern" else "field"
    owner member id tag tag
  }

  def genMethodSignature(sym: Symbol): String = {
    val owner = genTypeName(sym.owner)
    val id    = nativeIdOf(sym)
    val tpe   = sym.tpe.widen
    val mangledParams =
      tpe.params.map(p => mangledType(p.info))

    val mangledRetty = mangledType(tpe.resultType)
    (owner member (id.replace("_", "__") +: (mangledParams :+ mangledRetty))
      .mkString("_")).toString
  }

  def genMethodName(sym: Symbol): nir.Global = {
    val owner = genTypeName(sym.owner)
    val id    = nativeIdOf(sym)
    val tpe   = sym.tpe.widen

    val mangledParams = tpe.params.toSeq.map(p => mangledType(p.info))

    if (sym == String_+) {
      genMethodName(StringConcatMethod)
    } else if (sym.owner.isExternModule) {
      owner member id tag "extern"
    } else if (sym.name == nme.CONSTRUCTOR) {
      owner member ("init" +: mangledParams).mkString("_")
    } else {
      val mangledRetty = mangledType(tpe.resultType)
      owner member (id.replace("_", "$underscore$") +: (mangledParams :+ mangledRetty))
        .mkString("_")
    }
  }

  private def mangledType(tpe: Type): String =
    mangledTypeInternal(genType(tpe, box = false))

  private def mangledTypeInternal(ty: nir.Type): String = {
    val sb = new scalanative.util.ShowBuilder

    def printType(ty: nir.Type): Unit = ty match {
      case nir.Type.None        => sb.str("")
      case nir.Type.Void        => sb.str("void")
      case nir.Type.Vararg      => sb.str("...")
      case nir.Type.Ptr         => sb.str("ptr")
      case nir.Type.Bool        => sb.str("bool")
      case nir.Type.Char        => sb.str("char")
      case nir.Type.I(w, false) => sb.str("u"); sb.str(w)
      case nir.Type.I(w, true)  => sb.str("i"); sb.str(w)
      case nir.Type.Float       => sb.str("f32")
      case nir.Type.Double      => sb.str("f64")
      case nir.Type.Array(ty, n) =>
        sb.str("arr.")
        printType(ty)
        sb.str(".")
        sb.str(n)
      case nir.Type.Function(args, ret) =>
        sb.str("fun.")
        sb.rep(args, sep = ".")(printType)
      case nir.Type.Struct(name, _) =>
        sb.str("struct.")
        printGlobal(name)

      case nir.Type.Nothing => sb.str("nothing")
      case nir.Type.Unit    => sb.str("unit")
      case nir.Type.Class(name) =>
        sb.str("class.")
        printGlobal(name)
      case nir.Type.Trait(name) =>
        sb.str("trait.")
        printGlobal(name)
      case nir.Type.Module(name) =>
        sb.str("module.")
        printGlobal(name)
    }

    def printGlobal(global: nir.Global): Unit = global match {
      case nir.Global.None =>
        unreachable
      case nir.Global.Top(id) =>
        printId(id)
      case nir.Global.Member(n, id) =>
        printId(id)
        sb.str("..")
        printId(id)
    }

    def printId(id: String): Unit =
      sb.str(
        id.replace("scala.scalanative.runtime", "ssnr")
          .replace("scala.scalanative.native", "ssnn"))

    printType(ty)
    sb.toString
  }

  private def nativeIdOf(sym: Symbol): String = {
    sym.getAnnotation(NameClass).flatMap(_.stringArg(0)).getOrElse {
      if (sym.isField) {
        val id0 = sym.name.decoded.toString
        if (id0.charAt(id0.length() - 1) != ' ') id0
        else id0.substring(0, id0.length() - 1) // strip trailing ' '
      } else if (sym.isMethod) {
        val name = sym.name.decoded
        if (sym.owner == NObjectClass) name.substring(2) // strip the __
        else name.toString
      } else {
        scalanative.util.unreachable
      }
    }
  }
}
