/*
 * Copyright (c) 2001-2016 Stefan Marr
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
package havlak

class HavlakBenchmark extends benchmarks.Benchmark[(Int, Array[Int])] {

  private val innerIterations = List(15000, 1500, 150, 15, 1)
  private var i               = 0

  override def run(): (Int, Array[Int]) = {
    val iterations = innerIterations(i % innerIterations.length)
    i = i + 1
    (iterations, run(iterations))
  }

  def run(innerIterations: Int): Array[Int] =
    new LoopTesterApp().main(innerIterations, 50, 10 /* was 100 */, 10, 5)

  override def check(t: (Int, Array[Int])): Boolean =
    check(t._2, t._1)

  def check(r: Array[Int], innerIterations: Int): Boolean = {

    if (innerIterations == 15000) { return r(0) == 46602 && r(1) == 5213 }
    if (innerIterations == 1500) { return r(0) == 6102 && r(1) == 5213 }
    if (innerIterations == 150) { return r(0) == 2052 && r(1) == 5213 }
    if (innerIterations == 15) { return r(0) == 1647 && r(1) == 5213 }
    if (innerIterations == 1) { return r(0) == 1605 && r(1) == 5213 }

    System.out.println(
      "No verification result for " + innerIterations + " found")
    System.out.println("Result is: " + r(0) + ", " + r(1))
    return false
  }
}
