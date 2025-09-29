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
// file DoubleToString.java. The java and scalanative testing environments
// differ too much for a cost-effective textual port.
//
// There is always the issue of lumpers & splitters. DoubleSuite.scala
// already exists and covers methods in addition to Double.toString.
//
// Here I have chosen to be a splitter and create a separate Suite focused
// on Double.toString(). It stays reasonably close to the ulfjack/Ry
// DoubleToString.java original. That should make re-porting from RYU easier,
// as test cases are added there.
//
// The alternative would be to add cases to the S-N DoubleSuite.scala
// original & simplify redundant cases. That would lead to the confusion of
// two sets of testing conventions being used in the same file.
// Speak French, not Franglish, whilst in Quebec and English three feet
// away in Maine.

package scala.scalanative
package runtime
package ieee754tostring.ryu

import org.junit.Assert._
import org.junit.Test

class RyuDoubleTest {

  private def assertD2sEquals(expected: String, d: scala.Double): Unit = {
    val result = d.toString
    assertTrue(
      s"result from Double.toString: $result != expected: $expected",
      expected == result
    )

    val result2 = java.lang.Double.toString(d)
    assertTrue(
      s"result from RyuDouble.doubleToChars: $result2 != expected: $expected",
      expected == result2
    )
  }

  @Test def simpleCases(): Unit = {
    assertD2sEquals("0.0", 0.0d)
    assertD2sEquals(
      "-0.0",
      java.lang.Double.longBitsToDouble(0x8000000000000000L)
    )
    assertD2sEquals("1.0", 1.0d)
    assertD2sEquals("-1.0", -1.0d)
    assertD2sEquals("NaN", Double.NaN)
    assertD2sEquals("Infinity", Double.PositiveInfinity)
    assertD2sEquals("-Infinity", Double.NegativeInfinity)
  }

  @Test def switchToSubnormal(): Unit = {
    assertD2sEquals(
      "2.2250738585072014E-308",
      java.lang.Double.longBitsToDouble(0x0010000000000000L)
    )
  }

  @Test def boundaryConditions(): Unit = {
    // x = 1.0E7
    assertD2sEquals("1.0E7", 1.0e7d)

    // x < 1.0E7
    assertD2sEquals("9999999.999999998", 9999999.999999998d)

    // x = 1.0E-3
    assertD2sEquals("0.001", 0.001d)

    // x < 1.0E-3
    assertD2sEquals("9.999999999999998E-4", 0.0009999999999999998d)
  }

  @Test def minAndMax(): Unit = {
    assertD2sEquals(
      "1.7976931348623157E308",
      java.lang.Double.longBitsToDouble(0x7fefffffffffffffL)
    )
    assertD2sEquals("4.9E-324", java.lang.Double.longBitsToDouble(1))
  }

  @Test def roundingModeConservative(): Unit = {
    assertD2sEquals("-2.1098088986959632E16", -2.109808898695963e16)
  }

  @Test def regressionTest(): Unit = {
    assertD2sEquals("4.940656E-318", 4.940656e-318d)
    assertD2sEquals("1.18575755E-316", 1.18575755e-316d)
    assertD2sEquals("2.989102097996E-312", 2.989102097996e-312d)
    assertD2sEquals("9.0608011534336E15", 9.0608011534336e15d)
    assertD2sEquals("4.708356024711512E18", 4.708356024711512e18)
    assertD2sEquals("9.409340012568248E18", 9.409340012568248e18)

    /// Comment from original RYU source.
    // This number naively requires 65 bit for the intermediate results if we
    // reduce the lookup table by half. This checks that we don't lose any
    // information in that case.
    assertD2sEquals("1.8531501765868567E21", 1.8531501765868567e21)

    assertD2sEquals("-3.347727380279489E33", -3.347727380279489e33)

    /// Comment from original RYU source.
    // Discovered by Andriy Plokhotnyuk, see #29.
    // Porting-to-Scala note:
    //   issue  #29 is at ryu master source: https://github.com/ulfjack/ryu.
    //   It is not a scalanative issue number.
    assertD2sEquals("1.9430376160308388E16", 1.9430376160308388e16)

    assertD2sEquals("-6.9741824662760956E19", -6.9741824662760956e19)
    assertD2sEquals("4.3816050601147837E18", 4.3816050601147837e18)
  }

}
