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

import scalanative.annotation.alwaysinline

private[unsafe] abstract class NatValue[T <: Nat] {
  @alwaysinline def value: Int
}

private[unsafe] object NatValue {
  abstract class NatBaseValue[T <: Nat.Base] extends NatValue[T] {
    @alwaysinline def value: Int
  }

  object Nat0 extends NatBaseValue[unsafe.Nat._0] {
    @alwaysinline def value: Int = 0
  }

  object Nat1 extends NatBaseValue[unsafe.Nat._1] {
    @alwaysinline def value: Int = 1
  }

  object Nat2 extends NatBaseValue[unsafe.Nat._2] {
    @alwaysinline def value: Int = 2
  }

  object Nat3 extends NatBaseValue[unsafe.Nat._3] {
    @alwaysinline def value: Int = 3
  }

  object Nat4 extends NatBaseValue[unsafe.Nat._4] {
    @alwaysinline def value: Int = 4
  }

  object Nat5 extends NatBaseValue[unsafe.Nat._5] {
    @alwaysinline def value: Int = 5
  }

  object Nat6 extends NatBaseValue[unsafe.Nat._6] {
    @alwaysinline def value: Int = 6
  }

  object Nat7 extends NatBaseValue[unsafe.Nat._7] {
    @alwaysinline def value: Int = 7
  }

  object Nat8 extends NatBaseValue[unsafe.Nat._8] {
    @alwaysinline def value: Int = 8
  }

  object Nat9 extends NatBaseValue[unsafe.Nat._9] {
    @alwaysinline def value: Int = 9
  }


  @alwaysinline implicit def materializeNat0NatBaseValue: NatBaseValue[unsafe.Nat._0] =
    Nat0
  @alwaysinline implicit def materializeNat1NatBaseValue: NatBaseValue[unsafe.Nat._1] =
    Nat1
  @alwaysinline implicit def materializeNat2NatBaseValue: NatBaseValue[unsafe.Nat._2] =
    Nat2
  @alwaysinline implicit def materializeNat3NatBaseValue: NatBaseValue[unsafe.Nat._3] =
    Nat3
  @alwaysinline implicit def materializeNat4NatBaseValue: NatBaseValue[unsafe.Nat._4] =
    Nat4
  @alwaysinline implicit def materializeNat5NatBaseValue: NatBaseValue[unsafe.Nat._5] =
    Nat5
  @alwaysinline implicit def materializeNat6NatBaseValue: NatBaseValue[unsafe.Nat._6] =
    Nat6
  @alwaysinline implicit def materializeNat7NatBaseValue: NatBaseValue[unsafe.Nat._7] =
    Nat7
  @alwaysinline implicit def materializeNat8NatBaseValue: NatBaseValue[unsafe.Nat._8] =
    Nat8
  @alwaysinline implicit def materializeNat9NatBaseValue: NatBaseValue[unsafe.Nat._9] =
    Nat9

  @alwaysinline implicit def materializeNatDigit2NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit2[N1, N2]] =
    new NatValue[Nat.Digit2[N1, N2]] {
      @alwaysinline def value: Int = {
        10 * implicitly[NatBaseValue[N1]].value +
          implicitly[NatBaseValue[N2]].value
      }
    }
  @alwaysinline implicit def materializeNatDigit3NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue, N3 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit3[N1, N2, N3]] =
    new NatValue[Nat.Digit3[N1, N2, N3]] {
      @alwaysinline def value: Int = {
        100 * implicitly[NatBaseValue[N1]].value +
          10 * implicitly[NatBaseValue[N2]].value +
          implicitly[NatBaseValue[N3]].value
      }
    }
  @alwaysinline implicit def materializeNatDigit4NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue, N3 <: Nat.Base : NatBaseValue, N4 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit4[N1, N2, N3, N4]] =
    new NatValue[Nat.Digit4[N1, N2, N3, N4]] {
      @alwaysinline def value: Int = {
        1000 * implicitly[NatBaseValue[N1]].value +
          100 * implicitly[NatBaseValue[N2]].value +
          10 * implicitly[NatBaseValue[N3]].value +
          implicitly[NatBaseValue[N4]].value
      }
    }
  @alwaysinline implicit def materializeNatDigit5NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue, N3 <: Nat.Base : NatBaseValue, N4 <: Nat.Base : NatBaseValue, N5 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit5[N1, N2, N3, N4, N5]] =
    new NatValue[Nat.Digit5[N1, N2, N3, N4, N5]] {
      @alwaysinline def value: Int = {
        10000 * implicitly[NatBaseValue[N1]].value +
          1000 * implicitly[NatBaseValue[N2]].value +
          100 * implicitly[NatBaseValue[N3]].value +
          10 * implicitly[NatBaseValue[N4]].value +
          implicitly[NatBaseValue[N5]].value
      }
    }
  @alwaysinline implicit def materializeNatDigit6NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue, N3 <: Nat.Base : NatBaseValue, N4 <: Nat.Base : NatBaseValue, N5 <: Nat.Base : NatBaseValue, N6 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit6[N1, N2, N3, N4, N5, N6]] =
    new NatValue[Nat.Digit6[N1, N2, N3, N4, N5, N6]] {
      @alwaysinline def value: Int = {
        100000 * implicitly[NatBaseValue[N1]].value +
          10000 * implicitly[NatBaseValue[N2]].value +
          1000 * implicitly[NatBaseValue[N3]].value +
          100 * implicitly[NatBaseValue[N4]].value +
          10 * implicitly[NatBaseValue[N5]].value +
          implicitly[NatBaseValue[N6]].value
      }
    }
  @alwaysinline implicit def materializeNatDigit7NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue, N3 <: Nat.Base : NatBaseValue, N4 <: Nat.Base : NatBaseValue, N5 <: Nat.Base : NatBaseValue, N6 <: Nat.Base : NatBaseValue, N7 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit7[N1, N2, N3, N4, N5, N6, N7]] =
    new NatValue[Nat.Digit7[N1, N2, N3, N4, N5, N6, N7]] {
      @alwaysinline def value: Int = {
        1000000 * implicitly[NatBaseValue[N1]].value +
          100000 * implicitly[NatBaseValue[N2]].value +
          10000 * implicitly[NatBaseValue[N3]].value +
          1000 * implicitly[NatBaseValue[N4]].value +
          100 * implicitly[NatBaseValue[N5]].value +
          10 * implicitly[NatBaseValue[N6]].value +
          implicitly[NatBaseValue[N7]].value
      }
    }
  @alwaysinline implicit def materializeNatDigit8NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue, N3 <: Nat.Base : NatBaseValue, N4 <: Nat.Base : NatBaseValue, N5 <: Nat.Base : NatBaseValue, N6 <: Nat.Base : NatBaseValue, N7 <: Nat.Base : NatBaseValue, N8 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit8[N1, N2, N3, N4, N5, N6, N7, N8]] =
    new NatValue[Nat.Digit8[N1, N2, N3, N4, N5, N6, N7, N8]] {
      @alwaysinline def value: Int = {
        10000000 * implicitly[NatBaseValue[N1]].value +
          1000000 * implicitly[NatBaseValue[N2]].value +
          100000 * implicitly[NatBaseValue[N3]].value +
          10000 * implicitly[NatBaseValue[N4]].value +
          1000 * implicitly[NatBaseValue[N5]].value +
          100 * implicitly[NatBaseValue[N6]].value +
          10 * implicitly[NatBaseValue[N7]].value +
          implicitly[NatBaseValue[N8]].value
      }
    }
  @alwaysinline implicit def materializeNatDigit9NatValue[N1 <: Nat.Base : NatBaseValue, N2 <: Nat.Base : NatBaseValue, N3 <: Nat.Base : NatBaseValue, N4 <: Nat.Base : NatBaseValue, N5 <: Nat.Base : NatBaseValue, N6 <: Nat.Base : NatBaseValue, N7 <: Nat.Base : NatBaseValue, N8 <: Nat.Base : NatBaseValue, N9 <: Nat.Base : NatBaseValue]: NatValue[Nat.Digit9[N1, N2, N3, N4, N5, N6, N7, N8, N9]] =
    new NatValue[Nat.Digit9[N1, N2, N3, N4, N5, N6, N7, N8, N9]] {
      @alwaysinline def value: Int = {
        100000000 * implicitly[NatBaseValue[N1]].value +
          10000000 * implicitly[NatBaseValue[N2]].value +
          1000000 * implicitly[NatBaseValue[N3]].value +
          100000 * implicitly[NatBaseValue[N4]].value +
          10000 * implicitly[NatBaseValue[N5]].value +
          1000 * implicitly[NatBaseValue[N6]].value +
          100 * implicitly[NatBaseValue[N7]].value +
          10 * implicitly[NatBaseValue[N8]].value +
          implicitly[NatBaseValue[N9]].value
      }
    }
}
