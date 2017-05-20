/* This code is based on the SOM class library.
 *
 * Copyright (c) 2001-2016 see AUTHORS.md file
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package queens

import benchmarks.{BenchmarkRunningTime, MediumRunningTime}

class QueensBenchmark extends benchmarks.Benchmark[Boolean] {
  var freeMaxs: Array[Boolean] = _
  var freeRows: Array[Boolean] = _
  var freeMins: Array[Boolean] = _
  var queenRows: Array[Int]    = _

  override val runningTime: BenchmarkRunningTime = MediumRunningTime

  override def run(): Boolean = {
    var result = true
    (0 until 10).foreach { i =>
      result = result && queens()
    }
    result
  }

  def queens(): Boolean = {
    freeRows = Array.fill(8)(true)
    freeMaxs = Array.fill(16)(true)
    freeMins = Array.fill(16)(true)
    queenRows = Array.fill(8)(-1)

    placeQueen(0)
  }

  def placeQueen(c: Int): Boolean = {
    (0 until 8).foreach { r =>
      if (getRowColumn(r, c)) {
        queenRows(r) = c
        setRowColumn(r, c, false)

        if (c == 7) {
          return true
        }

        if (placeQueen(c + 1)) {
          return true
        }
        setRowColumn(r, c, true)
      }
    }
    false
  }

  def getRowColumn(r: Int, c: Int): Boolean =
    freeRows(r) && freeMaxs(c + r) && freeMins(c - r + 7)

  def setRowColumn(r: Int, c: Int, v: Boolean): Unit = {
    freeRows(r) = v
    freeMaxs(c + r) = v
    freeMins(c - r + 7) = v
  }

  override def check(result: Boolean) = result
}
