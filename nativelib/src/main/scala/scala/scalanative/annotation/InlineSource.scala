package scala.scalanative.annotation

import scala.annotation.StaticAnnotation

/** Embeds a source string into the NIR representation.
 *
 * @param language identifier for the language of the string
 * @param source string to be embedded into NIR
 */
final class InlineSource(language: String, source: String)
    extends StaticAnnotation
