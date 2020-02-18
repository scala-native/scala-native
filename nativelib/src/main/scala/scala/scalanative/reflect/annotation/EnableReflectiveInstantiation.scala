package scala.scalanative
package reflect.annotation

/** Enables reflective instantiation for the annotated class, trait or object,
 *  and all its descendants.
 *
 *  Affected classes can be identified at run-time through methods of
 *  [[scala.scalanative.reflect.Reflect]].
 */
class EnableReflectiveInstantiation extends scala.annotation.StaticAnnotation
