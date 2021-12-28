package scala.scalanative.nscplugin

import dotty.tools.dotc.ast.tpd._
import dotty.tools.dotc.core
import core.Contexts._
import core.Symbols._
import core.Flags._
import core.StdNames._
import dotty.tools.dotc.transform.SymUtils._
import dotty.tools.backend.jvm.DottyBackendInterface.symExtensions
import scalanative.util.unreachable
import scala.scalanative.nir
import scala.scalanative.nir._
import scala.language.implicitConversions

trait NirGenName(using Context) {
  self: NirCodeGen =>

  def genName(sym: Symbol): nir.Global =
    if (sym.isType) genTypeName(sym)
    else if (sym.is(Method)) genMethodName(sym)
    else genFieldName(sym)

  def genTypeName(sym: Symbol): nir.Global.Top = {
    val sym1 =
      if (sym.isAllOf(ModuleClass | JavaDefined)) sym.linkedClass
      else sym

    if (sym1 == defn.ObjectClass) nir.Rt.Object.name.top
    else {
      val id = {
        val fullName = sym1.javaClassName
        NirGenName.MappedNames.getOrElse(fullName, fullName)
      }
      nir.Global.Top(id)
    }
  }

  def genModuleName(sym: Symbol): nir.Global.Top = {
    val typeName = genTypeName(sym)
    if (typeName.id.endsWith("$")) typeName
    else Global.Top(typeName.id + "$")
  }

  def genFieldName(sym: Symbol): nir.Global = {
    val owner =
      if (sym.isScalaStatic) genModuleName(sym.owner)
      else genTypeName(sym.owner)
    val id = nativeIdOf(sym)
    val scope = {
      /* Variables are internally private, but with public setter/getter.
       * Removing this check would cause problems with reachability
       */
      if (sym.isPrivate && !sym.is(Mutable))
        nir.Sig.Scope.Private(owner)
      else nir.Sig.Scope.Public
    }

    owner.member {
      if (sym.isExtern) nir.Sig.Extern(id)
      else nir.Sig.Field(id, scope)
    }
  }

  def genMethodName(sym: Symbol): nir.Global = {
    val owner = genTypeName(sym.owner)
    val id = nativeIdOf(sym)
    val scope =
      if (sym.isPrivate)
        if (sym.isStaticMethod) nir.Sig.Scope.PrivateStatic(owner)
        else nir.Sig.Scope.Private(owner)
      else if (sym.isStaticMethod) nir.Sig.Scope.PublicStatic
      else nir.Sig.Scope.Public

    val paramTypes = sym.info.paramInfoss.flatten
      .map(fromType)
      .map(genType)

    if (sym == defn.`String_+`) genMethodName(defnNir.String_concat)
    else if (sym.isExtern)
      if (sym.isSetter)
        val id = nativeIdOf(sym.getter)
        owner.member(nir.Sig.Extern(id))
      else owner.member(nir.Sig.Extern(id))
    else if (sym.isClassConstructor) owner.member(nir.Sig.Ctor(paramTypes))
    else if (sym.isStaticConstructor) owner.member(nir.Sig.Clinit())
    else if (sym.name == nme.TRAIT_CONSTRUCTOR)
      owner.member(nir.Sig.Method(id, Seq(nir.Type.Unit), scope))
    else
      val retType = genType(fromType(sym.info.resultType))
      owner.member(nir.Sig.Method(id, paramTypes :+ retType, scope))
  }

  def genStaticMemberName(sym: Symbol): Global = {
    val owner = {
      // Methods defined as static inside module cannot have static forwarders
      // Make sure to use refer to their original owner
      val typeName = genTypeName(sym.owner)
      val ownerIsScalaModule = sym.owner.is(Module, butNot = JavaDefined)
      def haveNoForwarders = sym.isOneOf(ExcludedForwarder, butNot = Enum)
      if (ownerIsScalaModule && haveNoForwarders) typeName
      else Global.Top(typeName.id.stripSuffix("$"))
    }
    val sig = genStaticMemberSig(sym)
    owner.member(sig)
  }

  def genStaticMemberSig(sym: Symbol): nir.Sig = {
    val id = nativeIdOf(sym)
    val scope =
      if (sym.isPrivate) nir.Sig.Scope.PrivateStatic(genTypeName(sym.owner))
      else nir.Sig.Scope.PublicStatic

    val paramTypes = sym.info.paramInfoss.flatten
      .map(fromType)
      .map(genType)
    val retType = genType(fromType(sym.info.resultType))

    val name = sym.name
    val sig = nir.Sig.Method(id, paramTypes :+ retType, scope)
    sig
  }

  private def nativeIdOf(sym: Symbol): String = {
    sym
      .getAnnotation(defnNir.NameClass)
      .flatMap(_.argumentConstantString(0))
      .getOrElse {
        val name = sym.javaSimpleName
        val id: String =
          // Don't use encoded names for externs
          // LLVM intrinisc methods are using dots
          if (sym.isExtern) sym.name.decode.toString
          else if (sym.isField) name
          else if (sym.is(Method))
            val isScalaHashOrEquals = name.startsWith("__scala_")
            if (sym.owner == defnNir.NObjectClass || isScalaHashOrEquals)
              name.substring(2) // strip the __
            else name
          else scalanative.util.unreachable

        /*
         * Double quoted identifiers are not allowed in CLang.
         * We're replacing them with unicode to allow distinction between x / `x` and `"x"`.
         * It follows Scala JVM naming convention.
         */
        id.replace("\"", "$u0022")
      }
  }
}

object NirGenName {
  private val MappedNames = Map(
    "java.lang._Class" -> "java.lang.Class",
    "java.lang._Cloneable" -> "java.lang.Cloneable",
    "java.lang._Comparable" -> "java.lang.Comparable",
    "java.lang._Enum" -> "java.lang.Enum",
    "java.lang._NullPointerException" -> "java.lang.NullPointerException",
    "java.lang._Object" -> "java.lang.Object",
    "java.lang._String" -> "java.lang.String",
    "java.lang.annotation._Retention" -> "java.lang.annotation.Retention",
    "java.io._Serializable" -> "java.io.Serializable"
  ).flatMap {
    case classEntry @ (nativeName, javaName) =>
      List(
        classEntry,
        (nativeName + "$", javaName + "$")
      )
  }
}
