// format: off

// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// See nativelib runtime/Arrays.scala.gyb for details.

package scala.scalanative
package unsafe

sealed abstract class Nat

object Nat {
  sealed abstract class Base extends Nat

  final abstract class _0 extends Base

  final abstract class _1 extends Base

  final abstract class _2 extends Base

  final abstract class _3 extends Base

  final abstract class _4 extends Base

  final abstract class _5 extends Base

  final abstract class _6 extends Base

  final abstract class _7 extends Base

  final abstract class _8 extends Base

  final abstract class _9 extends Base

  final abstract class Digit2[N1 <: Nat.Base, N2 <: Nat.Base] extends Nat

  final abstract class Digit3[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base] extends Nat

  final abstract class Digit4[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base, N4 <: Nat.Base] extends Nat

  final abstract class Digit5[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base, N4 <: Nat.Base, N5 <: Nat.Base] extends Nat

  final abstract class Digit6[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base, N4 <: Nat.Base, N5 <: Nat.Base, N6 <: Nat.Base] extends Nat

  final abstract class Digit7[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base, N4 <: Nat.Base, N5 <: Nat.Base, N6 <: Nat.Base, N7 <: Nat.Base] extends Nat

  final abstract class Digit8[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base, N4 <: Nat.Base, N5 <: Nat.Base, N6 <: Nat.Base, N7 <: Nat.Base, N8 <: Nat.Base] extends Nat

  final abstract class Digit9[N1 <: Nat.Base, N2 <: Nat.Base, N3 <: Nat.Base, N4 <: Nat.Base, N5 <: Nat.Base, N6 <: Nat.Base, N7 <: Nat.Base, N8 <: Nat.Base, N9 <: Nat.Base] extends Nat

}
