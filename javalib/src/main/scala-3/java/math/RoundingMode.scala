// Enums are not source-compatbile, make sure to sync this file with Scala 2 implementation

/*
 * Ported by Alistair Johnson from
 * https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/java/math/RoundingMode.java
 * Original license copied below:
 */

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.math

enum RoundingMode extends Enum[RoundingMode]():
  case UP extends RoundingMode
  case DOWN extends RoundingMode
  case CEILING extends RoundingMode
  case FLOOR extends RoundingMode
  case HALF_UP extends RoundingMode
  case HALF_DOWN extends RoundingMode
  case HALF_EVEN extends RoundingMode
  case UNNECESSARY extends RoundingMode
end RoundingMode

object RoundingMode:
  def valueOf(ordinal: Int): RoundingMode = {
    RoundingMode.values
      .find(_.ordinal == ordinal)
      .getOrElse {
        throw new IllegalArgumentException("Invalid rounding mode")
      }
  }
