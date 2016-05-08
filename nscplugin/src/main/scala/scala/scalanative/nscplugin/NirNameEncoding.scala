package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.reflect.internal.Flags._
import scalanative.nir.Shows.{showType => _, showGlobal => _, _}
import scalanative.util.{sh, Show}, Show.{Repeat => r}

trait NirNameEncoding { self: NirCodeGen =>
  import global.{Name => _, _}, definitions._
  import nirAddons.nirDefinitions._

  private lazy val StringClassName  = nir.Global.Type("java.lang.String")
  private lazy val StringModuleName = nir.Global.Val("java.lang.String")

  def genTypeName(sym: Symbol): nir.Global = {
    val id = {
      val fullName = sym.fullName.toString
      if (fullName == "java.lang._String") "java.lang.String"
      else if (fullName == "java.lang._Object") "java.lang.Object"
      else if (fullName == "java.lang._Class") "java.lang.Class"
      else fullName
    }
    val name = sym match {
      case ObjectClass                               => nir.Rt.Object.name
      case _ if sym.isModule                         => genTypeName(sym.moduleClass)
      case _ if sym.isModuleClass || sym.isImplClass => nir.Global.Val(id)
      case _                                         => nir.Global.Type(id)
    }
    name
  }

  def genFieldName(sym: Symbol): nir.Global = {
    val owner = genTypeName(sym.owner)
    val id0   = sym.name.decoded.toString
    val id =
      if (id0.charAt(id0.length() - 1) != ' ') id0
      else id0.substring(0, id0.length() - 1)

    if (isExternModule(sym.owner)) owner member id tag "extern"
    else owner member id tag "field"
  }

  def genMethodName(sym: Symbol): nir.Global = {
    val owner   = sym.owner
    val ownerId = genTypeName(sym.owner)
    val id = {
      val name = sym.name.decoded
      if (owner == NObjectClass) name.substring(1) // skip the _
      else name.toString
    }
    val tpe = sym.tpe.widen
    val mangledParams =
      tpe.params.toSeq.map(p => mangledType(p.info, retty = false))

    if (sym == String_+) {
      genMethodName(StringConcatMethod)
    } else if (isExternModule(owner)) {
      ownerId member id
    } else if (sym.name == nme.CONSTRUCTOR) {
      ownerId member ("init" +: mangledParams).mkString("_")
    } else {
      val mangledRetty = mangledType(tpe.resultType, retty = true)

      ownerId member (id +: (mangledParams :+ mangledRetty)).mkString("_")
    }
  }

  private def mangledType(tpe: Type, retty: Boolean): String =
    mangledTypeInternal(genType(tpe, retty))

  private def mangledTypeInternal(ty: nir.Type): String = {
    implicit lazy val showMangledType: Show[nir.Type] = Show {
      case nir.Type.None                => ""
      case nir.Type.Void                => "void"
      case nir.Type.Label               => "label"
      case nir.Type.Vararg              => "..."
      case nir.Type.Ptr                 => "ptr"
      case nir.Type.Bool                => "bool"
      case nir.Type.I8                  => "i8"
      case nir.Type.I16                 => "i16"
      case nir.Type.I32                 => "i32"
      case nir.Type.I64                 => "i64"
      case nir.Type.F32                 => "f32"
      case nir.Type.F64                 => "f64"
      case nir.Type.Array(ty, n)        => sh"arr.$ty.$n"
      case nir.Type.Function(args, ret) => sh"fun.${r(args :+ ret, sep = ".")}"
      case nir.Type.Struct(name, _)     => sh"struct.$name"

      case nir.Type.Nothing      => "nothing"
      case nir.Type.Unit         => "unit"
      case nir.Type.Class(name)  => sh"class.$name"
      case nir.Type.Trait(name)  => sh"trait.$name"
      case nir.Type.Module(name) => sh"module.$name"
    }

    implicit lazy val showMangledGlobal: Show[nir.Global] = Show {
      case nir.Global.Val(id)       => showId(id)
      case nir.Global.Type(id)      => showId(id)
      case nir.Global.Member(n, id) => showId(id) + ".." + showId(id)
    }

    def showId(id: String): String =
      id.replace("scala.scalanative.runtime", "ssnr")
        .replace("scala.scalanative.native", "ssnn")

    sh"$ty".toString
  }
}
