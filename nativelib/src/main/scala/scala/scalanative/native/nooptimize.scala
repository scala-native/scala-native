package scala.scalanative
package native

/** Don't optimize annotated method at all.
 *
 *  This annotation implies noinline and nospecialize.
 *  In case any inline annotations are provided,
 *  nooptimize wins over them (i.e. inline nooptimize is
 *  the same as nooptimize).
 */
final class nooptimize extends scala.annotation.StaticAnnotation
