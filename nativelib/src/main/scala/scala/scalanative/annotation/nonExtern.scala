package scala.scalanative
package annotation

/** Internal annotation used in compiler plugin to exclude subset of extern type
 *  definitions from of being treated as extern
 */
private final abstract class nonExtern()
    extends scala.annotation.StaticAnnotation
