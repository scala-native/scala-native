package scala.scalanative
package interflow

sealed abstract class Kind
final object ClassKind  extends Kind
final object ArrayKind  extends Kind
final object BoxKind    extends Kind
final object StringKind extends Kind
