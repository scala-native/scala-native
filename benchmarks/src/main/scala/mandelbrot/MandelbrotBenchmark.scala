// This benchmark has been modified based on the SOM benchmark.
//
// Copyright © 2004-2013 Brent Fulgham
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//   * Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//
//   * Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//
//   * Neither the name of "The Computer Language Benchmarks Game" nor the name
//     of "The Computer Language Shootout Benchmarks" nor the names of its
//     contributors may be used to endorse or promote products derived from this
//     software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES LOSS OF USE, DATA, OR PROFITS OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// The Computer Language Benchmarks Game
// http://benchmarksgame.alioth.debian.org
//
//  contributed by Karl von Laudermann
//  modified by Jeremy Echols
//  modified by Detlef Reichl
//  modified by Joseph LaFata
//  modified by Peter Zotov

// http://benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3
package mandelbrot

import benchmarks.{BenchmarkRunningTime, VeryLongRunningTime}

class MandelbrotBenchmark extends benchmarks.Benchmark[(Int, Int)] {

  private val sizes = List(750, 500, 1)
  private var i     = 0

  override val runningTime: BenchmarkRunningTime = VeryLongRunningTime

  override def run(): (Int, Int) = {
    val size = sizes(i % sizes.length)
    i = i + 1
    (size, mandelbrot(size))
  }

  override def check(t: (Int, Int)): Boolean =
    check(t._2, t._1)

  def check(result: Int, innerIterations: Int): Boolean = {
    if (innerIterations == 500) {
      return result == 191
    }
    if (innerIterations == 750) {
      return result == 50
    }
    if (innerIterations == 1) {
      return result == 128
    }

    System.out.println(
      "No verification result for " + innerIterations + " found")
    System.out.println("Result is: " + result)
    return false
  }

  def mandelbrot(size: Int): Int = {
    var sum: Int     = 0
    var byteAcc: Int = 0
    var bitNum: Int  = 0
    var y: Int       = 0

    while (y < size) {
      var ci: Double = (2.0d * y / size) - 1.0
      var x: Int     = 0

      while (x < size) {
        var zr: Double   = 0.0d
        var zrzr: Double = 0.0d
        var zi: Double   = 0.0d
        var zizi: Double = 0.0d
        var cr: Double   = (2.0d * x / size) - 1.5d

        var z: Int           = 0
        var notDone: Boolean = true
        var escape: Int      = 0
        while (notDone && z < 50) {
          zr = zrzr - zizi + cr
          zi = 2.0 * zr * zi + ci

          // preserve recalculation
          zrzr = zr * zr
          zizi = zi * zi

          if (zrzr + zizi > 4.0) {
            notDone = false
            escape = 1
          }
          z += 1
        }

        byteAcc = (byteAcc << 1) + escape
        bitNum += 1

        // Code is very similar for these cases, but using separate blocks
        // ensures we skip the shifting when it's unnecessary, which is most cases.
        if (bitNum == 8) {
          sum ^= byteAcc
          byteAcc = 0
          bitNum = 0
        } else if (x == size - 1) {
          byteAcc <<= (8 - bitNum)
          sum ^= byteAcc
          byteAcc = 0
          bitNum = 0
        }
        x += 1
      }
      y += 1
    }
    return sum
  }
}
