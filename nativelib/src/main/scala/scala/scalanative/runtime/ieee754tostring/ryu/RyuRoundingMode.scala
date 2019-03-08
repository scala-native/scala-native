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
//    2) The original java sources were converted to a rough draft of
//       scala code using the service at URL: javatoscala.com.
//
//       The raw conversion did not compile and contained bugs due
//       to the handling of break and return statements, but it saved
//       days, if not weeks, of effort.
//
//       Thank you javatoscala.com.
//
//    3) All additional work, including introduced bugs,  is an original
//       contribution to Scala Native development.

package scala.scalanative
package runtime
package ieee754tostring.ryu

abstract class RyuRoundingMode {
  def acceptUpperBound(even: Boolean): Boolean
  def acceptLowerBound(even: Boolean): Boolean
}

object RyuRoundingMode {
  object Conservative extends RyuRoundingMode {
    def acceptUpperBound(even: Boolean): Boolean = false
    def acceptLowerBound(even: Boolean): Boolean = false
  }

  object RoundEven extends RyuRoundingMode {
    def acceptUpperBound(even: Boolean): Boolean = even
    def acceptLowerBound(even: Boolean): Boolean = even
  }
}
