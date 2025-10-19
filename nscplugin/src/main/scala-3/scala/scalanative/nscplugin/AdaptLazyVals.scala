package scala.scalanative.nscplugin

import dotty.tools._
import dotc._
import dotc.ast.tpd._
import core.Contexts._
import core.Names._
import core.Symbols._
import core.StdNames._
import core.Constants.{Constant, ClazzTag}
import scala.annotation.{threadUnsafe => tu}
import dotty.tools.dotc.config.*

// This helper class is responsible for rewriting calls to scala.runtime.LazyVals with
// its scala native specific counter-part. This is needed, because LazyVals are
// using JVM unsafe API and static class constructors which are not supported
// in Scala Native.
// In theory it could be defined as separate compilation phase (it was in the past), but
// it would lead to the problems when using macros - rewritten lazy fields method would
// be inlined and evaluated at compile time, however modified AST contains Scala Native
// specific calls to Intrinsic methods. This would lead to throwing exception while compiling.
class AdaptLazyVals(defnNir: NirDefinitions) {
  def defn(using Context) = LazyValsDefns.get

  private val compilerUsesVarHandles = ScalaVersion.current match {
    // bug in 3.8.0-RC1 nightlies, no version property set
    case AnyScalaVersion => true
    case version => version >= SpecificScalaVersion(3, 8, 0, ScalaBuild.Final)
  }

  private def isLazyFieldOffset(name: Name) =
    name.startsWith(nme.LAZY_FIELD_OFFSET.toString)

  private def isLazyFieldHandle(name: Name) =
    name.is(CompilerCompat.LazyValHandleNameCompat)

  private def isLazyFieldStore(name: Name) =
    if compilerUsesVarHandles then isLazyFieldHandle(name)
    else isLazyFieldOffset(name)

  private def hasLazyFields(sym: Symbol)(using Context) =
    sym.denot.info.fields
      .exists(f => isLazyFieldStore(f.name))

  // Map of field symbols for LazyVals offsets and literals
  // with the name of referenced bitmap fields within given TypeDef
  private val lazyFieldProxies = collection.mutable.Map.empty[Symbol, Literal]

  def clean(): Unit = {
    lazyFieldProxies.clear()
  }

  // Collect information about offset fields
  def prepareForTypeDef(td: TypeDef)(using Context): Unit = {
    val sym = td.symbol

    if (hasLazyFields(sym)) {
      val template @ Template(_, _, _, _) = td.rhs: @unchecked
      lazyFieldProxies ++= template.body.collect {
        case vd: ValDef if isLazyFieldStore(vd.name) =>
          import LazyValsNames.*
          val fieldname = vd.rhs match {
            // Scala 3.1.x
            case Apply(
                  Select(_, GetOffset),
                  List(cls: Literal, fieldname: Literal)
                ) =>
              fieldname
            // Scala 3.2.x
            case Apply(
                  Select(_, GetOffsetStatic),
                  List(
                    Apply(Select(_, GetDeclaredField), List(fieldname: Literal))
                  )
                ) =>
              fieldname
            // Scala 3.2.x + -Ylightweight-lazy-vals
            case Apply(
                  Select(_, GetStaticFieldOffset),
                  List(
                    Apply(Select(_, GetDeclaredField), List(fieldname: Literal))
                  )
                ) =>
              fieldname
            // Scala 3.8.x
            case Apply(
                  Select(_, FindVarHandle),
                  List(_, fieldname: Literal, _)
                ) =>
              fieldname
          }
          vd.symbol -> fieldname
      }.toMap
    }
  }

  def transformDefDef(dd: DefDef)(using Context): DefDef | Thicket = {
    val hasLazyFields = this.hasLazyFields(dd.symbol.owner)

    // Remove LazyVals Offset fields assignments from static constructors,
    // as they're leading to reachability problems
    // Drop static constructor if empty after filtering
    if (hasLazyFields && dd.symbol.isStaticConstructor) {
      val DefDef(_, _, _, b @ Block(stats, expr)) = dd: @unchecked
      val newBlock = cpy.Block(b.asInstanceOf[Tree])(
        stats = b.stats
          .filter {
            case Assign(lhs, rhs) => !isLazyFieldStore(lhs.symbol.name)
            case _                => true
          }
          .asInstanceOf[List[Tree]],
        expr = expr.asInstanceOf[Tree]
      )
      if (newBlock.stats.isEmpty) EmptyTree
      else cpy.DefDef(dd)(dd.name, dd.paramss, dd.tpt, newBlock)
    } else dd
  }

  // Replace all usages of all unsupported LazyVals methods with their
  // Scala Native specific implementation (taking Ptr instead of object + offset)
  def transformApply(tree: Apply)(using Context): Apply = {
    // Create call to SN intrinsic methods returning pointer to bitmap field
    def classFieldPtr(target: Tree, fieldRef: Tree): Tree = {
      val fieldName = lazyFieldProxies(fieldRef.symbol)
      cpy.Apply(tree)(
        fun = ref(defnNir.Intrinsics_classFieldRawPtr),
        args = List(target, fieldName)
      )
    }
    val Apply(fun, args) = tree
    val sym = fun.symbol

    if lazyFieldProxies.isEmpty then tree // No LazyVals in TypeDef, fast path
    else if sym == defn.LazyVals_get then
      val List(target, fieldRef) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_get),
        args = List(classFieldPtr(target, fieldRef))
      )
    else if sym == defn.LazyVals_setFlag then
      val List(target, fieldRef, value, ord) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_setFlag),
        args = List(classFieldPtr(target, fieldRef), value, ord)
      )
    else if sym.name == defn.VarHandleCASName then
      fun match {
        case Select(fieldRef, _) if isLazyFieldHandle(fieldRef.symbol.name) =>
          val List(target, expected, value) = args
          cpy.Apply(tree)(
            fun = ref(defn.NativeLazyVals_objCAS),
            args = List(classFieldPtr(target, fieldRef), expected, value)
          )
        case _ => tree
      }
    else if defn.LazyVals_objCAS.contains(sym) then
      val List(targetTree, fieldRef, expected, value) = args
      val target = targetTree match {
        case Literal(c: Constant) if c.tag == ClazzTag =>
          ref(c.typeValue.classSymbol.companionModule)
        case _ => targetTree
      }
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_objCAS),
        args = List(classFieldPtr(target, fieldRef), expected, value)
      )
    else if sym == defn.LazyVals_CAS then
      val List(target, fieldRef, expected, value, ord) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_CAS),
        args = List(classFieldPtr(target, fieldRef), expected, value, ord)
      )
    else if sym == defn.LazyVals_wait4Notification then
      val List(target, fieldRef, value, ord) = args
      cpy.Apply(tree)(
        fun = ref(defn.NativeLazyVals_wait4Notification),
        args = List(classFieldPtr(target, fieldRef), value, ord)
      )
    else tree
  }
  object LazyValsNames {
    val LazyVals = typeName("LazyVals")
    val GetOffset = termName("getOffset")
    val GetOffsetStatic = termName("getOffsetStatic")
    val GetStaticFieldOffset = termName("getStaticFieldOffset")
    val GetDeclaredField = termName("getDeclaredField")
    val FindVarHandle = termName("findVarHandle")
  }

  object LazyValsDefns {
    private val cached = NirGenUtil.ContextCached(LazyValsDefns())
    def get(using Context): LazyValsDefns = cached.get
  }
  class LazyValsDefns(using Context) {
    @tu lazy val NativeLazyValsModule = requiredModule(
      "scala.scalanative.runtime.LazyVals"
    )
    @tu lazy val NativeLazyVals_get = NativeLazyValsModule.requiredMethod("get")
    @tu lazy val NativeLazyVals_setFlag =
      NativeLazyValsModule.requiredMethod("setFlag")
    @tu lazy val NativeLazyVals_CAS = NativeLazyValsModule.requiredMethod("CAS")
    @tu lazy val NativeLazyVals_objCAS =
      NativeLazyValsModule.requiredMethod("objCAS")
    @tu lazy val NativeLazyVals_wait4Notification =
      NativeLazyValsModule.requiredMethod("wait4Notification")

    @tu lazy val LazyValsModule = requiredModule("scala.runtime.LazyVals")
    @tu lazy val LazyVals_get = LazyValsModule.requiredMethod("get")
    @tu lazy val LazyVals_setFlag = LazyValsModule.requiredMethod("setFlag")
    @tu lazy val LazyVals_CAS = LazyValsModule.requiredMethod("CAS")
    @tu lazy val LazyVals_wait4Notification =
      LazyValsModule.requiredMethod("wait4Notification")
    // Since 3.2.2 as experimental
    @tu lazy val LazyVals_objCAS: Option[TermSymbol] =
      Option(LazyValsModule.info.member(termName("objCAS")).symbol)
        .filter(_ != NoSymbol)
        .map(_.asTerm)

    final val VarHandleCASName = termName("compareAndSet")
    @tu lazy val VarHandleCAS: Option[TermSymbol] =
      scala.util
        .Try {
          requiredClass(termName("java.lang.invoke.VarHandle"))
        }
        .toOption
        .map(_.requiredMethod(VarHandleCASName))
  }

}
