// Copyright 2018 Ulf Adams
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Credits:
//
//    1) This work is a heavily modified derivation of the original work by
//       Ulf Adams at URL: https://github.com/ulfjack/ryu.
//       As such, it inherits the Apache license of the original work.
//       Thank you Ulf Adams.
//
//    2) The original java sources were converted by hand. This reduced
//       or eliminated the mismatch between the testing framework used
//       by RYU and that used by Scala Native.
//
//       This additional work, including introduced bugs, is an original
//       contribution to Scala Native development.

// 2019-02-25
// Lee Tibbert
//
// This is a semantic rather than textual port of the RYU GitHub
// file FloatToString.java. The java and scalanative testing environments
// differ too much for a cost effective textual port.
//
// There is always the issue of lumpers & splitters. FloatSuite.scala
// already exists and covers methods in addition to Float.toString.
//
// Here I have chosen to be a splitter and create a separate Suite focused
// on Float.toString(). It stays reasonably close to the ulfjack/Ry
// FloatToString.java original. That should make re-porting from RYU easier,
// as test cases are added there.
//
// The alternative would be to add cases to the S-N FloatSuite.scala
// original & simplify redundant cases. That would lead to the confusion of
// two sets of testing conventions being used in the same file.
// Speak French, not Franglish, whilst in Quebec and English three feet
// away in Maine.

package scala.scalanative
package runtime
package ieee754tostring.ryu

object RyuFloatSuite extends tests.Suite {

  private def assertF2sEquals(expected: String, f: scala.Float): Unit = {
    val result = f.toString
    assert(expected == result, s"result: $result != expected: $expected")
  }

  test("Simple cases") {
    assertF2sEquals("0.0", 0.0f)
    assertF2sEquals("-0.0", java.lang.Float.intBitsToFloat(0x80000000))
    assertF2sEquals("1.0", 1.0f)
    assertF2sEquals("-1.0", -1.0f)
    assertF2sEquals("NaN", Float.NaN)
    assertF2sEquals("Infinity", Float.PositiveInfinity)
    assertF2sEquals("-Infinity", Float.NegativeInfinity)
  }

  test("Switch to subnormal") {
    assertF2sEquals("1.1754944E-38", java.lang.Float.intBitsToFloat(0x00800000))
  }

  test("Boundary conditions") {
    // x = 1.0E7
    assertF2sEquals("1.0E7", 1.0E7f)

    // x < 1.0E7
    assertF2sEquals("9999999.0", 9999999.0f)

    // x = 1.0E-3
    assertF2sEquals("0.001", 0.001f)

    // x < 1.0E-3
    assertF2sEquals("9.999999E-4", 0.0009999999f)
  }

  test("Min and Max") {
    assertF2sEquals("3.4028235E38", java.lang.Float.intBitsToFloat(0x7f7fffff))
    assertF2sEquals("1.4E-45", java.lang.Float.intBitsToFloat(0x00000001))
  }

  test("roundingMode CONSERVATIVE") {
    assertF2sEquals("3.3554448E7", 3.3554448E7f)
    assertF2sEquals("8.999999E9", 8.999999E9f)
    assertF2sEquals("3.4366718E10", 3.4366717E10f)
    assertF2sEquals("3.3554448E7", 3.3554448E7f)
    assertF2sEquals("8.999999E9", 8.999999E9f)
    assertF2sEquals("3.4366718E10", 3.4366717E10f)
  }

  test("roundingEvenIfTied") {
    assertF2sEquals("0.33007812", 0.33007812f)
  }

  test("looksLikePow5") {
    /// Comment from original RYU source.
    // These are all floating point numbers where the mantissa is a power of 5,
    // and the exponent is in the range such that q = 10.
    assertF2sEquals("6.7108864E17", java.lang.Float.intBitsToFloat(0x5D1502F9))
    assertF2sEquals("1.3421773E18", java.lang.Float.intBitsToFloat(0x5D9502F9))
    assertF2sEquals("2.6843546E18", java.lang.Float.intBitsToFloat(0x5E1502F9))
  }

  test("Regression test") {
    assertF2sEquals("4.7223665E21", 4.7223665E21f)
    assertF2sEquals("8388608.0", 8388608.0f)
    assertF2sEquals("1.6777216E7", 1.6777216E7f)
    assertF2sEquals("3.3554436E7", 3.3554436E7f)
    assertF2sEquals("6.7131496E7", 6.7131496E7f)
    assertF2sEquals("1.9310392E-38", 1.9310392E-38f)
    assertF2sEquals("-2.47E-43", -2.47E-43f)
    assertF2sEquals("1.993244E-38", 1.993244E-38f)
    assertF2sEquals("4103.9004", 4103.9003f)
    assertF2sEquals("5.3399997E9", 5.3399997E9f)
    assertF2sEquals("6.0898E-39", 6.0898E-39f)
    assertF2sEquals("0.0010310042", 0.0010310042f)
    assertF2sEquals("2.882326E17", 2.8823261E17f)

    // Porting Note (LeeT):
    // scala versions <= 2.12.2 appear to have a bug where scalac
    // does not correctly convert the ""7.038531E-26f" in the source
    // to the proper bit pattern. I had a jolly time finding that out.
    // S-N currently uses scalac versions which have the bug.
    // Use the correct explicit bit pattern here to test the Ryu code.
    // The bit pattern works for both buggy & fixed scala versions, so
    // it suits my purpose here.

    // assertF2sEquals("7.038531E-26", 7.038531E-26f) // Ryu original
    assertF2sEquals("7.038531E-26", java.lang.Float.intBitsToFloat(0x15ae43fd))

    assertF2sEquals("9.223404E17", 9.2234038E17f)
    assertF2sEquals("6.710887E7", 6.7108872E7f)
    assertF2sEquals("1.0E-44", 1.0E-44f)
    assertF2sEquals("2.816025E14", 2.816025E14f)
    assertF2sEquals("9.223372E18", 9.223372E18f)
    assertF2sEquals("1.5846086E29", 1.5846085E29f)
    assertF2sEquals("1.1811161E19", 1.1811161E19f)
    assertF2sEquals("5.368709E18", 5.368709E18f)
    assertF2sEquals("4.6143166E18", 4.6143165E18f)
    assertF2sEquals("0.007812537", 0.007812537f)
    assertF2sEquals("1.4E-45", 1.4E-45f)
    assertF2sEquals("1.18697725E20", 1.18697724E20f)
    assertF2sEquals("1.00014165E-36", 1.00014165E-36f)
    assertF2sEquals("200.0", 200f)
    assertF2sEquals("3.3554432E7", 3.3554432E7f)
  }

}
