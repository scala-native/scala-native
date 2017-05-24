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
package towers

import benchmarks.{BenchmarkRunningTime, ShortRunningTime}

class TowersBenchmark extends benchmarks.Benchmark[Int] {
  final class TowersDisk(val size: Int, var next: TowersDisk = null)

  var piles: Array[TowersDisk] = _
  var movesDone: Int           = _

  def pushDisk(disk: TowersDisk, pile: Int): Unit = {
    val top = piles(pile)
    if (!(top == null) && (disk.size >= top.size)) {
      throw new RuntimeException("Cannot put a big disk on a smaller one")
    }

    disk.next = top
    piles(pile) = disk
  }

  def popDiskFrom(pile: Int): TowersDisk = {
    val top = piles(pile)
    if (top == null) {
      throw new RuntimeException(
        "Attempting to remove a disk from an empty pile")
    }

    piles(pile) = top.next
    top.next = null
    top
  }

  def moveTopDisk(fromPile: Int, toPile: Int): Unit = {
    pushDisk(popDiskFrom(fromPile), toPile)
    movesDone += 1
  }

  def buildTowerAt(pile: Int, disks: Int): Unit = {
    var i = disks
    while (i >= 0) {
      pushDisk(new TowersDisk(i), pile)
      i -= 1
    }
  }

  def moveDisks(disks: Int, fromPile: Int, toPile: Int) {
    if (disks == 1) {
      moveTopDisk(fromPile, toPile)
    } else {
      val otherPile = (3 - fromPile) - toPile
      moveDisks(disks - 1, fromPile, otherPile)
      moveTopDisk(fromPile, toPile)
      moveDisks(disks - 1, otherPile, toPile)
    }
  }

  override val runningTime: BenchmarkRunningTime = ShortRunningTime

  override def run(): Int = {
    piles = new Array[TowersDisk](3)
    buildTowerAt(0, 13)
    movesDone = 0
    moveDisks(13, 0, 1)
    movesDone
  }

  override def check(result: Int): Boolean =
    result == 8191
}
