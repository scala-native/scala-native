package scala.scalanative
package interflow

sealed abstract class Kind
object ClassKind extends Kind
object ArrayKind extends Kind
object BoxKind extends Kind
object StringKind extends Kind
