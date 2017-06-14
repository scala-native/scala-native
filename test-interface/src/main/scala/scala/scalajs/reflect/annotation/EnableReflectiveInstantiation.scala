package scala.scalajs.reflect.annotation

// Ported from Scala JS

/** Enables reflective instantiation for the annotated class, trait or object,
 *  and all its descendants.
 *
 *  Affected classes can be identified at run-time through methods of
 *  [[scala.scalajs.reflect.Reflect]].
 */
class EnableReflectiveInstantiation extends scala.annotation.StaticAnnotation
