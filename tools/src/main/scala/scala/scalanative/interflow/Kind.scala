package scala.scalanative
package interflow

private[interflow] sealed abstract class Kind
private[interflow] object ClassKind extends Kind
private[interflow] object ArrayKind extends Kind
private[interflow] object BoxKind extends Kind
private[interflow] object StringKind extends Kind
