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
package list

class ListBenchmark extends benchmarks.Benchmark[Int] {
  final class Element(var value: Any, var next: Element = null) {
    def length(): Int = {
      if (next == null) {
        return 1
      } else {
        return 1 + next.length()
      }
    }
  }

  override def run(): Int = {
    val result = tail(makeList(15), makeList(10), makeList(6))
    result.length()
  }

  def makeList(length: Int): Element = {
    if (length == 0) { return null } else {
      val e = new Element(length)
      e.next = makeList(length - 1)
      return e
    }
  }

  def isShorterThan(x: Element, y: Element): Boolean = {
    var xTail = x
    var yTail = y

    while (yTail != null) {
      if (xTail == null) { return true }
      xTail = xTail.next
      yTail = yTail.next
    }

    false
  }

  def tail(x: Element, y: Element, z: Element): Element = {
    if (isShorterThan(y, x)) {
      tail(tail(x.next, y, z), tail(y.next, z, x), tail(z.next, x, y))
    } else {
      z
    }
  }

  override def check(result: Int): Boolean =
    result == 10
}
