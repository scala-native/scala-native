package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.reflect.internal.Flags._
import scalanative.util.unreachable

trait NirGenName { self: NirGenPhase =>
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
      case ObjectClass =>
        nir.Rt.Object.name
      case _ if sym.isModule =>
        genTypeName(sym.moduleClass)
      case _ =>
        val idWithSuffix =
          if (sym.isModuleClass && !nme.isImplClassName(sym.name)) {
            id + "$"
          } else {
            id
          }
        nir.Global.Top(idWithSuffix)
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
      if (sym.isSetter) {
        val id0 = sym.name.dropSetter.decoded.toString
        owner member id0 tag "extern"
      } else {
        owner member id tag "extern"
      }
    } else if (sym.name == nme.CONSTRUCTOR) {
      owner member ("init" +: mangledParams).mkString("_")
    } else {
      val mangledRetty = mangledType(tpe.resultType)
      val mangledId = id
        .replace("_", "$underscore$")
        .replace("\"", "$doublequote$")
      owner member (mangledId +: (mangledParams :+ mangledRetty))
        .mkString("_")
    }
  }

  private def mangledType(tpe: Type): String =
    genType(tpe, box = false).mangle

  private def nativeIdOf(sym: Symbol): String = {
    sym.getAnnotation(NameClass).flatMap(_.stringArg(0)).getOrElse {
      if (sym.isField) {
        val id0 = sym.name.decoded.toString
        if (id0.charAt(id0.length() - 1) != ' ') id0
        else id0.substring(0, id0.length() - 1) // strip trailing ' '
      } else if (sym.isMethod) {
        val name                = sym.name.decoded
        val isScalaHashOrEquals = name.startsWith("__scala_")
        if (sym.owner == NObjectClass) {
          name.substring(2) // strip the __
        } else if (isScalaHashOrEquals) {
          name.substring(2) // strip the __
        } else {
          name.toString
        }
      } else {
        scalanative.util.unreachable
      }
    }
  }
}
