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
package bounce

import benchmarks.{BenchmarkRunningTime, ShortRunningTime}
import som.Random

class BounceBenchmark extends benchmarks.Benchmark[Int] {
  private class Ball(random: Random) {
    private var x: Int    = random.next()  % 500
    private var y: Int    = random.next()  % 500
    private var xVel: Int = (random.next() % 300) - 150
    private var yVel: Int = (random.next() % 300) - 150

    def bounce(): Boolean = {
      val xLimit: Int = 500
      val yLimit: Int = 500
      var bounced     = false

      x += xVel;
      y += yVel;
      if (x > xLimit) {
        x = xLimit; xVel = 0 - Math.abs(xVel); bounced = true;
      }
      if (x < 0) { x = 0; xVel = Math.abs(xVel); bounced = true; }
      if (y > yLimit) {
        y = yLimit; yVel = 0 - Math.abs(yVel); bounced = true;
      }
      if (y < 0) { y = 0; yVel = Math.abs(yVel); bounced = true; }

      bounced
    }
  }

  override val runningTime: BenchmarkRunningTime = ShortRunningTime

  override def run(): Int = {
    val random = new Random()

    val ballCount = 100
    var bounces   = 0
    val balls     = Array.fill(ballCount)(new Ball(random))

    (0 to 49).foreach { i =>
      balls.foreach { ball =>
        if (ball.bounce()) {
          bounces += 1
        }
      }
    }

    bounces
  }

  override def check(result: Int): Boolean =
    result == 1331
}
