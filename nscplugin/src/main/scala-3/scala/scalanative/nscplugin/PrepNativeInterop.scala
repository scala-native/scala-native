package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools._
import dotc._
import dotc.ast.tpd._
import dotc.transform.SymUtils.setter
import core.Contexts._
import core.Definitions
import core.Names._
import core.Symbols._
import core.Types._
import core.StdNames._
import core.Constants.Constant
import NirGenUtil.ContextCached

/** This phase does:
 *    - Rewrite calls to scala.Enumeration.Value (include name string) (Ported
 *      from ScalaJS and Scala 2 Native compiler plugin)
 */
object PrepNativeInterop {
  val name = "scalanative-prepareInterop"
}

class PrepNativeInterop extends PluginPhase {
  override val runsAfter = Set(transform.PostTyper.name)
  override val runsBefore = Set(transform.Pickler.name)
  val phaseName = PrepNativeInterop.name
  override def description: String = "prepare ASTs for Native interop"

  def defn(using Context): Definitions = ctx.definitions
  def defnNir(using Context): NirDefinitions = NirDefinitions.get

  private def isTopLevelExtern(dd: ValOrDefDef)(using Context) = {
    dd.rhs.symbol == defnNir.UnsafePackage_extern &&
    dd.symbol.isWrappedToplevelDef
  }

  override def transformTypeApply(tree: TypeApply)(using Context): Tree = {
    val TypeApply(fun, tArgs) = tree
    if fun.symbol == defnNir.Intrinsics_sizeOfType then
      // sizeOf[T] -> sizeOf(classOf[T])
      cpy.Apply(tree)(
        ref(defnNir.Intrinsics_sizeOf),
        tArgs.map(tpe => Literal(Constant(tpe.tpe.finalResultType)))
      )
    else tree
  }

  override def transformDefDef(dd: DefDef)(using Context): Tree = {
    // Set `@extern` annotation for top-level extern functions
    if (isTopLevelExtern(dd) && !dd.symbol.hasAnnotation(defnNir.ExternClass)) {
      dd.symbol.addAnnotation(defnNir.ExternClass)
    }
    dd
  }

  override def transformValDef(vd: ValDef)(using Context): Tree = {
    val enumsCtx = EnumerationsContext.get
    import enumsCtx._
    val sym = vd.symbol
    vd match {
      case ValDef(_, tpt, ScalaEnumValue.NoName(optIntParam)) =>
        val nrhs = scalaEnumValName(sym.owner.asClass, sym, optIntParam)
        cpy.ValDef(vd)(tpt = transformAllDeep(tpt), nrhs)

      case ValDef(_, tpt, ScalaEnumValue.NullName(optIntParam)) =>
        val nrhs = scalaEnumValName(sym.owner.asClass, sym, optIntParam)
        cpy.ValDef(vd)(tpt = transformAllDeep(tpt), nrhs)

      case _ =>
        // Set `@extern` annotation for top-level extern variables
        if (isTopLevelExtern(vd) &&
            !sym.hasAnnotation(defnNir.ExternClass)) {
          sym.addAnnotation(defnNir.ExternClass)
          sym.setter.addAnnotation(defnNir.ExternClass)
        }

        vd
    }
  }

  private object EnumerationsContext {
    private val cached = ContextCached(EnumerationsContext())
    def get(using Context): EnumerationsContext = cached.get
  }
  private class EnumerationsContext(using Context) {
    abstract class ScalaEnumFctExtractors(
        owner: ClassSymbol,
        methodName: TermName
    ) {
      private def resolve(argTypes: Type*)(owner: ClassSymbol): Symbol = {
        val res = owner.denot.info
          .member(methodName)
          .filterWithPredicate(
            _.info.paramInfoss.flatten.corresponds(argTypes)(_ =:= _)
          )
          .symbol
        assert(res.exists, "tried to resolve NoSymbol")
        res
      }

      private val noArgDef = resolve()(_)
      private val nameArgDef = resolve(defn.StringType)(_)
      private val intArgDef = resolve(defn.IntType)(_)
      private val fullMethDef = resolve(defn.IntType, defn.StringType)(_)

      val NoArg = noArgDef(owner)
      def noArg(owner: ClassSymbol) = noArgDef(owner)

      val NameArg = nameArgDef(owner)
      def nameArg(owner: ClassSymbol) = nameArgDef(owner)

      val IntArg = intArgDef(owner)
      def intArg(owner: ClassSymbol) = intArgDef(owner)

      val FullMethod = fullMethDef(owner)
      def fullMethod(owner: ClassSymbol) = fullMethDef(owner)

      /** Extractor object for calls to the targeted symbol that do not have an
       *  explicit name in the parameters
       *
       *  Extracts:
       *    - `sel: Select` where sel.symbol is targeted symbol (no arg)
       *    - Apply(meth, List(param)) where meth.symbol is targeted symbol (i:
       *      Int)
       */
      object NoName {
        def unapply(tree: LazyTree): Option[Option[Tree]] =
          tree.asInstanceOf[Tree] match {
            case t: RefTree if t.symbol == NoArg => Some(None)
            case Apply(method, List(param)) if method.symbol == IntArg =>
              Some(Some(param))
            case _ => None
          }
      }

      object NullName {
        def unapply(tree: LazyTree): Option[Option[Tree]] =
          tree.asInstanceOf[Tree] match {
            case Apply(meth, List(Literal(Constant(null))))
                if meth.symbol == NameArg =>
              Some(None)
            case Apply(meth, List(param, Literal(Constant(null))))
                if meth.symbol == FullMethod =>
              Some(Some(param))
            case _ => None
          }
      }
    }

    object ScalaEnumValue
        extends ScalaEnumFctExtractors(
          owner = defnNative.EnumerationClass,
          methodName = nmeNative.Value
        )

    object ScalaEnumVal
        extends ScalaEnumFctExtractors(
          owner = defnNative.EnumerationClass.requiredClass(nmeNative.Val),
          methodName = nme.CONSTRUCTOR
        )

    /** Construct a call to Enumeration.Value
     *  @param thisSym
     *    ClassSymbol of enclosing class
     *  @param nameOrig
     *    Symbol of ValDef where this call will be placed (determines the string
     *    passed to Value)
     *  @param intParam
     *    Optional tree with Int passed to Value
     *  @return
     *    Typed tree with appropriate call to Value
     */
    def scalaEnumValName(
        thisSym: ClassSymbol,
        nameOrig: Symbol,
        intParam: Option[Tree]
    ) = {
      val defaultName: String = nameOrig.asTerm.accessedFieldOrGetter.name.show

      // Construct the following tree
      //
      //   if (nextName != null && nextName.hasNext)
      //     nextName.next()
      //   else
      //     <defaultName>
      //
      val nextNameTree = Select(This(thisSym), nmeNative.nextName)
      val nullCompTree =
        Apply(Select(nextNameTree, nme.NE), Literal(Constant(null)) :: Nil)
      val hasNextTree = Select(nextNameTree, nmeNative.hasNext)
      val condTree = Apply(Select(nullCompTree, nme.ZAND), hasNextTree :: Nil)
      val nameTree = If(
        condTree,
        Apply(Select(nextNameTree, nmeNative.next), Nil),
        Literal(Constant(defaultName))
      )
      val (method, params) = intParam match {
        case Some(int) =>
          ScalaEnumValue.fullMethod(thisSym) -> List(int, nameTree)
        case _ => ScalaEnumValue.nameArg(thisSym) -> List(nameTree)
      }

      ctx.typer.typed {
        Apply(Ident(method.namedType), params)
      }
    }

    private object defnNative {
      val EnumerationClass = requiredClassRef(
        "scala.Enumeration"
      ).symbol.asClass
    }

    private object nmeNative {
      val hasNext = termName("hasNext")
      val next = termName("next")
      val nextName = termName("nextName")
      val Value = termName("Value")
      val Val = termName("Val")
    }
  }

}
