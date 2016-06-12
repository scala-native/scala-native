package scala.scalanative
package compiler

/** Context that pass companion can uses to instantiate passes. */
final case class Ctx(fresh: nir.Fresh,
                     entry: nir.Global,
                     top: analysis.ClassHierarchy.Top)
