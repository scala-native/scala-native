package scala.scalanative
package native

sealed abstract class Nat

object Nat {
  sealed abstract class Base extends Nat
  final abstract class _0    extends Base
  final abstract class _1    extends Base
  final abstract class _2    extends Base
  final abstract class _3    extends Base
  final abstract class _4    extends Base
  final abstract class _5    extends Base
  final abstract class _6    extends Base
  final abstract class _7    extends Base
  final abstract class _8    extends Base
  final abstract class _9    extends Base

  final abstract class Digit[N <: Nat.Base, M <: Nat] extends Nat
}
