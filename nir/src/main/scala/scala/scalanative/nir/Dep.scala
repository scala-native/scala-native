package scala.scalanative
package nir

sealed abstract class Dep
object Dep {
  final case class Direct(dep: Global)                    extends Dep
  final case class Conditional(dep: Global, cond: Global) extends Dep
  final case class Weak(dep: Global)                      extends Dep
}
