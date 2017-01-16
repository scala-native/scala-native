package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.reflect.internal.Flags._
import scalanative.nir.Shows.{showType => _, showGlobal => _, _}
import scalanative.util.{unreachable, sh, Show}, Show.{Repeat => r}

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
      owner member (id.replace("_", "__") +: (mangledParams :+ mangledRetty))
        .mkString("_")
    }
  }

  private def mangledType(tpe: Type): String =
    mangledTypeInternal(genType(tpe, box = false))

  private def mangledTypeInternal(ty: nir.Type): String = {
    implicit lazy val showMangledType: Show[nir.Type] = Show {
      case nir.Type.None         => ""
      case nir.Type.Void         => "void"
      case nir.Type.Vararg       => "..."
      case nir.Type.Ptr          => "ptr"
      case nir.Type.Bool         => "bool"
      case nir.Type.I(src)       => s"i${src.width.v}"
      case nir.Type.F32          => "f32"
      case nir.Type.F64          => "f64"
      case nir.Type.Array(ty, n) => sh"arr.$ty.$n"
      case nir.Type.Function(args, ret) =>
        sh"fun.${r(args.map(_.ty) :+ ret, sep = ".")}"
      case nir.Type.Struct(name, _) => sh"struct.$name"

      case nir.Type.Nothing      => "nothing"
      case nir.Type.Unit         => "unit"
      case nir.Type.Class(name)  => sh"class.$name"
      case nir.Type.Trait(name)  => sh"trait.$name"
      case nir.Type.Module(name) => sh"module.$name"
    }

    implicit lazy val showMangledGlobal: Show[nir.Global] = Show {
      case nir.Global.None          => unreachable
      case nir.Global.Top(id)       => showId(id)
      case nir.Global.Member(n, id) => showId(id) + ".." + showId(id)
    }

    def showId(id: String): String =
      id.replace("scala.scalanative.runtime", "ssnr")
        .replace("scala.scalanative.native", "ssnn")

    sh"$ty".toString
  }

  private def nativeIdOf(sym: Symbol): String = {
    sym.getAnnotation(NameClass).flatMap(_.stringArg(0)).getOrElse {
      if (sym.isField) {
        val id0 = sym.name.decoded.toString
        if (id0.charAt(id0.length() - 1) != ' ') id0
        else id0.substring(0, id0.length() - 1) // strip trailing ' '
      } else if (sym.isMethod) {
        val name = sym.name.decoded
        if (sym.owner == NObjectClass) name.substring(1) // strip the _
        else name.toString
      } else {
        scalanative.util.unreachable
      }
    }
  }
}
