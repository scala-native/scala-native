package scala.scalanative
package linker

/** Link-time support for `Class.getEnumConstants`.
 *
 *  `scala.scalanative.runtime.LinkedEnums.valuesOf(cls)` is an intrinsic. Once
 *  it is reachable, `Reach` keeps the companion module and its `values()`
 *  method of every reached enum class (a class whose direct superclass is
 *  `java.lang.Enum`), and `Lower` replaces each call with a test of `cls`
 *  against these classes. Programs that never ask for enum constants keep no
 *  extra code.
 */
private[scalanative] object LinkedEnums {
  val EnumClass: nir.Global.Top = nir.Global.Top("java.lang.Enum")

  val Module: nir.Global.Top =
    nir.Global.Top("scala.scalanative.runtime.LinkedEnums$")

  /** Whether `name` is the `valuesOf` intrinsic. */
  def isValuesOf(name: nir.Global): Boolean = name match {
    case nir.Global.Member(Module, sig) =>
      sig.unmangled match {
        case nir.Sig.Method("valuesOf", _, _) => true
        case _                                => false
      }
    case _ => false
  }

  /** `values()` of the companion of `cls`, defined by both Scala 3 enums and
   *  the javalib enums written by hand.
   */
  def valuesMethod(cls: nir.Global.Top): nir.Global.Member =
    nir.Global
      .Top(cls.id + "$")
      .member(
        nir.Sig.Method("values", Seq(nir.Type.Array(nir.Type.Ref(cls))))
      )

  /** The enum classes whose `values()` the analysis kept, sorted by name. */
  def linked(
      analysis: ReachabilityAnalysis.Result
  ): Seq[(nir.Global.Top, nir.Global.Member)] =
    analysis.infos.valuesIterator
      .collect {
        case cls: Class
            if !cls.isModule && cls.parent.exists(_.name == EnumClass) &&
              analysis.infos
                .get(valuesMethod(cls.name))
                .exists(_.isInstanceOf[Method]) =>
          cls.name -> valuesMethod(cls.name)
      }
      .toSeq
      .sortBy(_._1.id)
}
