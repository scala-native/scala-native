package scala.scalanative
package nscplugin

import scala.collection.mutable.Buffer
import scala.tools.nsc
import scala.tools.nsc._

/** This phase does:
 *    - Rewrite calls to scala.Enumeration.Value (include name string) (Ported
 *      from ScalaJS)
 *    - Rewrite the body `scala.util.PropertiesTrait.scalaProps` to be
 *      statically determined at compile-time.
 */
abstract class PrepNativeInterop[G <: Global with Singleton](
    override val global: G
) extends NirPhase[G](global)
    with transform.Transform {
  import PrepNativeInterop._

  import global._
  import definitions._
  import nirAddons.nirDefinitions._

  val phaseName: String = "scalanative-prepareInterop"
  override def description: String = "prepare ASTs for Native interop"

  override def newPhase(p: nsc.Phase): StdPhase = new NativeInteropPhase(p)

  class NativeInteropPhase(prev: nsc.Phase) extends Phase(prev) {
    override def name: String = phaseName
    override def description: String = PrepNativeInterop.this.description
  }

  override protected def newTransformer(unit: CompilationUnit): Transformer =
    new NativeInteropTransformer(unit)

  private object nativenme {
    val hasNext = newTermName("hasNext")
    val next = newTermName("next")
    val nextName = newTermName("nextName")
    val x = newTermName("x")
    val Value = newTermName("Value")
    val Val = newTermName("Val")
    val scalaProps = newTermName("scalaProps")
    val propFilename = newTermName("propFilename")
  }

  class NativeInteropTransformer(unit: CompilationUnit) extends Transformer {

    /** Kind of the directly enclosing (most nested) owner. */
    private var enclosingOwner: OwnerKind = OwnerKind.None

    /** Cumulative kinds of all enclosing owners. */
    private var allEnclosingOwners: OwnerKind = OwnerKind.None

    /** Nicer syntax for `allEnclosingOwners is kind`. */
    private def anyEnclosingOwner: OwnerKind = allEnclosingOwners

    /** Nicer syntax for `allEnclosingOwners isnt kind`. */
    private object noEnclosingOwner {
      @inline def is(kind: OwnerKind): Boolean =
        allEnclosingOwners isnt kind
    }

    private def enterOwner[A](kind: OwnerKind)(body: => A): A = {
      require(kind.isBaseKind, kind)
      val oldEnclosingOwner = enclosingOwner
      val oldAllEnclosingOwners = allEnclosingOwners
      enclosingOwner = kind
      allEnclosingOwners |= kind
      try {
        body
      } finally {
        enclosingOwner = oldEnclosingOwner
        allEnclosingOwners = oldAllEnclosingOwners
      }
    }

    override def transform(tree: Tree): Tree = {
      // Recursivly widen and dealias all nested types (compler dealiases only top-level)
      def widenDealiasType(tpe: Type): Type = {
        val widened = tpe.dealias.map(_.dealias)
        if (widened != tpe) widened.map(widenDealiasType(_))
        else widened
      }
      tree match {
        // Catch calls to Predef.classOf[T]. These should NEVER reach this phase
        // but unfortunately do. In normal cases, the typer phase replaces these
        // calls by a literal constant of the given type. However, when we compile
        // the scala library itself and Predef.scala is in the sources, this does
        // not happen.
        //
        // The trees reach this phase under the form:
        //
        //   scala.this.Predef.classOf[T]
        //
        // or, as of Scala 2.12.0, as:
        //
        //   scala.Predef.classOf[T]
        //
        // or so it seems, at least.
        case TypeApply(classOfTree @ Select(predef, nme.classOf), List(tpeArg))
            if predef.symbol == PredefModule =>
          // Replace call by literal constant containing type
          if (typer.checkClassTypeOrModule(tpeArg)) {
            val widenedTpe = tpeArg.tpe.dealias.widen
            println("rewriting class of for" + widenedTpe)
            typer.typed { Literal(Constant(widenedTpe)) }
          } else {
            reporter.error(tpeArg.pos, s"Type ${tpeArg} is not a class type")
            EmptyTree
          }

        // sizeOf[T] -> sizeOf(classOf[T]) + attachment
        case TypeApply(fun, List(tpeArg)) if fun.symbol == SizeOfTypeMethod =>
          val tpe = widenDealiasType(tpeArg.tpe)
          typer
            .typed {
              Apply(SizeOfMethod, Literal(Constant(tpe)))
            }
            .updateAttachment(NonErasedType(tpe))
            .setPos(tree.pos)

        // alignmentOf[T] -> alignmentOf(classOf[T]) + attachment
        case TypeApply(fun, List(tpeArg))
            if fun.symbol == AlignmentOfTypeMethod =>
          val tpe = widenDealiasType(tpeArg.tpe)
          typer
            .typed {
              Apply(AlignmentOfMethod, Literal(Constant(tpe)))
            }
            .updateAttachment(NonErasedType(tpe))
            .setPos(tree.pos)

        // Catch the definition of scala.Enumeration itself
        case cldef: ClassDef if cldef.symbol == EnumerationClass =>
          enterOwner(OwnerKind.EnumImpl) { super.transform(cldef) }

        // Catch Scala Enumerations to transform calls to scala.Enumeration.Value
        case idef: ImplDef if isScalaEnum(idef) =>
          val kind =
            if (idef.isInstanceOf[ModuleDef]) OwnerKind.EnumMod
            else OwnerKind.EnumClass
          enterOwner(kind) { super.transform(idef) }

        // Catch (Scala) ClassDefs
        case cldef: ClassDef =>
          enterOwner(OwnerKind.NonEnumScalaClass) { super.transform(cldef) }

        // Catch (Scala) ModuleDefs
        case modDef: ModuleDef =>
          enterOwner(OwnerKind.NonEnumScalaMod) { super.transform(modDef) }

        // ValOrDefDef's that are local to a block must not be transformed
        case vddef: ValOrDefDef if vddef.symbol.isLocalToBlock =>
          super.transform(tree)

        // `ValDef` that initializes `lazy val scalaProps` in trait `PropertiesTrait`
        // We rewrite the body to return a pre-propulated `Properties`.
        // - Scala 2.12
        case vddef @ ValDef(mods, name, tpt, _)
            if vddef.symbol == PropertiesTrait.info.member(
              nativenme.scalaProps
            ) =>
          val nrhs = prepopulatedScalaProperties(vddef, unit.freshTermName)
          treeCopy.ValDef(tree, mods, name, transform(tpt), nrhs)

        // Catch ValDefs in enumerations with simple calls to Value
        case ValDef(mods, name, tpt, ScalaEnumValue.NoName(optPar))
            if anyEnclosingOwner is OwnerKind.Enum =>
          val nrhs = scalaEnumValName(tree.symbol.owner, tree.symbol, optPar)
          treeCopy.ValDef(tree, mods, name, transform(tpt), nrhs)

        // Catch Select on Enumeration.Value we couldn't transform but need to
        // we ignore the implementation of scala.Enumeration itself
        case ScalaEnumValue.NoName(_)
            if noEnclosingOwner is OwnerKind.EnumImpl =>
          reporter.warning(
            tree.pos,
            """Couldn't transform call to Enumeration.Value.
              |The resulting program is unlikely to function properly as this
              |operation requires reflection.""".stripMargin
          )
          super.transform(tree)

        case ScalaEnumValue.NullName()
            if noEnclosingOwner is OwnerKind.EnumImpl =>
          reporter.warning(
            tree.pos,
            """Passing null as name to Enumeration.Value
              |requires reflection at runtime. The resulting
              |program is unlikely to function properly.""".stripMargin
          )
          super.transform(tree)

        case ScalaEnumVal.NoName(_) if noEnclosingOwner is OwnerKind.EnumImpl =>
          reporter.warning(
            tree.pos,
            """Calls to the non-string constructors of Enumeration.Val
              |require reflection at runtime. The resulting
              |program is unlikely to function properly.""".stripMargin
          )
          super.transform(tree)

        case ScalaEnumVal.NullName()
            if noEnclosingOwner is OwnerKind.EnumImpl =>
          reporter.warning(
            tree.pos,
            """Passing null as name to a constructor of Enumeration.Val
              |requires reflection at runtime. The resulting
              |program is unlikely to function properly.""".stripMargin
          )
          super.transform(tree)

        // Attach exact type information to the AST to preserve the type information
        // during the type erase phase and refer to it in the NIR generation phase.
        case Apply(fun, args) if CFuncPtrApplyMethods.contains(fun.symbol) =>
          val paramTypes =
            args.map(t => widenDealiasType(t.tpe)) :+
              widenDealiasType(tree.tpe.finalResultType)
          tree.updateAttachment(NonErasedTypes(paramTypes))

        case _ =>
          super.transform(tree)
      }
    }
  }

  private def isScalaEnum(implDef: ImplDef) =
    implDef.symbol.tpe.typeSymbol isSubClass EnumerationClass

  private abstract class ScalaEnumFctExtractors(val methSym: Symbol) {
    protected def resolve(ptpes: Symbol*) = {
      val res = methSym suchThat {
        _.tpe.params.map(_.tpe.typeSymbol) == ptpes.toList
      }
      assert(res != NoSymbol, "tried to resolve NoSymbol")
      res
    }

    protected val noArg = resolve()
    protected val nameArg = resolve(StringClass)
    protected val intArg = resolve(IntClass)
    protected val fullMeth = resolve(IntClass, StringClass)

    /** Extractor object for calls to the targeted symbol that do not have an
     *  explicit name in the parameters
     *
     *  Extracts:
     *    - `sel: Select` where sel.symbol is targeted symbol (no arg)
     *    - Apply(meth, List(param)) where meth.symbol is targeted symbol (i:
     *      Int)
     */
    object NoName {
      def unapply(t: Tree): Option[Option[Tree]] = t match {
        case sel: Select if sel.symbol == noArg =>
          Some(None)
        case Apply(meth, List(param)) if meth.symbol == intArg =>
          Some(Some(param))
        case _ =>
          None
      }
    }

    object NullName {
      def unapply(tree: Tree): Boolean = tree match {
        case Apply(meth, List(Literal(Constant(null)))) =>
          meth.symbol == nameArg
        case Apply(meth, List(_, Literal(Constant(null)))) =>
          meth.symbol == fullMeth
        case _ => false
      }
    }

  }

  private object ScalaEnumValue
      extends ScalaEnumFctExtractors(
        methSym = getMemberMethod(EnumerationClass, nativenme.Value)
      )

  private object ScalaEnumVal
      extends ScalaEnumFctExtractors(
        methSym = {
          val valSym = getMemberClass(EnumerationClass, nativenme.Val)
          valSym.tpe.member(nme.CONSTRUCTOR)
        }
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
  private def scalaEnumValName(
      thisSym: Symbol,
      nameOrig: Symbol,
      intParam: Option[Tree]
  ) = {

    val defaultName = nameOrig.asTerm.getterName.encoded

    // Construct the following tree
    //
    //   if (nextName != null && nextName.hasNext)
    //     nextName.next()
    //   else
    //     <defaultName>
    //
    val nextNameTree = Select(This(thisSym), nativenme.nextName)
    val nullCompTree =
      Apply(Select(nextNameTree, nme.NE), Literal(Constant(null)) :: Nil)
    val hasNextTree = Select(nextNameTree, nativenme.hasNext)
    val condTree = Apply(Select(nullCompTree, nme.ZAND), hasNextTree :: Nil)
    val nameTree = If(
      condTree,
      Apply(Select(nextNameTree, nativenme.next), Nil),
      Literal(Constant(defaultName))
    )
    val params = intParam.toList :+ nameTree

    typer.typed {
      Apply(Select(This(thisSym), nativenme.Value), params)
    }
  }

  /** Construct a tree that returns an instance of `java.util.Properties` that
   *  is pre-populated with the values of the `scala.util.Properties` at
   *  compile-time.
   *  @param original
   *    The original `DefDef`
   *  @param freshName
   *    A function that generates a fresh name
   *  @return
   *    The new (typed) rhs of the given `DefDef`.
   */
  private def prepopulatedScalaProperties(
      original: ValOrDefDef,
      freshName: String => TermName
  ): Tree = {
    val libraryFileName = "/library.properties"

    // Construct the following tree
    //
    //   val fresh = new java.util.Properties()
    //   fresh.put("firstKey", "firstValue")
    //   // etc.
    //   fresh
    //
    val stream = classOf[Option[_]].getResourceAsStream(libraryFileName)
    val props = new java.util.Properties()
    try props.load(stream)
    finally stream.close()

    val instanceName = freshName("properties")
    val keys = props.stringPropertyNames().iterator()
    val puts = Buffer.empty[Tree]
    while (keys.hasNext()) {
      val key = keys.next()
      val value = props.getProperty(key)
      puts += Apply(
        Select(Ident(instanceName), newTermName("put")),
        List(Literal(Constant(key)), Literal(Constant(value)))
      )
    }
    val bindTree =
      ValDef(Modifiers(), instanceName, TypeTree(), New(JavaProperties))
    val nrhs = Block(bindTree :: puts.toList, Ident(instanceName))

    typer.atOwner(original.symbol).typed(nrhs)
  }

}

object PrepNativeInterop {
  private final class OwnerKind(val baseKinds: Int) extends AnyVal {

    @inline def isBaseKind: Boolean =
      Integer.lowestOneBit(
        baseKinds
      ) == baseKinds && baseKinds != 0 // exactly 1 bit on

    @inline def |(that: OwnerKind): OwnerKind =
      new OwnerKind(this.baseKinds | that.baseKinds)

    @inline def is(that: OwnerKind): Boolean =
      (this.baseKinds & that.baseKinds) != 0

    @inline def isnt(that: OwnerKind): Boolean =
      !this.is(that)
  }

  private object OwnerKind {

    /** No owner, i.e., we are at the top-level. */
    val None = new OwnerKind(0x00)

    // Base kinds - those form a partition of all possible enclosing owners

    /** A Scala class/trait that does not extend Enumeration. */
    val NonEnumScalaClass = new OwnerKind(0x01)

    /** A Scala object that does not extend Enumeration. */
    val NonEnumScalaMod = new OwnerKind(0x02)

    /** A Scala class/trait that extends Enumeration. */
    val EnumClass = new OwnerKind(0x40)

    /** A Scala object that extends Enumeration. */
    val EnumMod = new OwnerKind(0x80)

    /** The Enumeration class itself. */
    val EnumImpl = new OwnerKind(0x100)

    // Compound kinds

    /** A Scala class/trait, possibly Enumeration-related. */
    val ScalaClass = NonEnumScalaClass | EnumClass | EnumImpl

    /** A Scala object, possibly Enumeration-related. */
    val ScalaMod = NonEnumScalaMod | EnumMod

    /** A Scala class, trait or object */
    val ScalaThing = ScalaClass | ScalaMod

    /** A Scala class/trait/object extending Enumeration, but not Enumeration
     *  itself.
     */
    val Enum = EnumClass | EnumMod

  }
}
