package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._

sealed abstract class Info {
  def name: Global
}
sealed abstract class ScopeInfo extends Info {
  val members = mutable.UnrolledBuffer.empty[MemberInfo]
  val calls   = mutable.Set.empty[String]
}
sealed abstract class MemberInfo extends Info {
  def owner: ScopeInfo
}
final class Class(val name: Global,
                  val parent: Option[Class],
                  val traits: Seq[Trait],
                  val isModule: Boolean)
    extends ScopeInfo {
  val subclasses = mutable.Set.empty[Class]
  val responds   = mutable.Map.empty[String, Global]
}
final class Trait(val name: Global, val traits: Seq[Trait]) extends ScopeInfo {
  val implementors = mutable.Set.empty[Class]
}
final class Method(val owner: ScopeInfo,
                   val name: Global,
                   val isConcrete: Boolean)
    extends MemberInfo
final class Field(val owner: ScopeInfo, val name: Global, val isConst: Boolean)
    extends MemberInfo
