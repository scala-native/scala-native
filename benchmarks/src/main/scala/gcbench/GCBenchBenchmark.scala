// Ported from https://www.hboehm.info/gc/gc_bench/GCBench.java
//
//   This is adapted from a benchmark written by John Ellis and Pete Kovac
//   of Post Communications.
//   It was modified by Hans Boehm of Silicon Graphics.
//
//   	This is no substitute for real applications.  No actual application
//  	is likely to behave in exactly this way.  However, this benchmark was
//  	designed to be more representative of real applications than other
//  	Java GC benchmarks of which we are aware.
//  	It attempts to model those properties of allocation requests that
//  	are important to current GC techniques.
//  	It is designed to be used either to obtain a single overall performance
//  	number, or to give a more detailed estimate of how collector
//  	performance varies with object lifetimes.  It prints the time
//  	required to allocate and collect balanced binary trees of various
//  	sizes.  Smaller trees result in shorter object lifetimes.  Each cycle
//  	allocates roughly the same amount of memory.
//  	Two data structures are kept around during the entire process, so
//  	that the measured performance is representative of applications
//  	that maintain some live in-memory data.  One of these is a tree
//  	containing many pointers.  The other is a large array containing
//  	double precision floating point numbers.  Both should be of comparable
//  	size.
//
//  	The results are only really meaningful together with a specification
//  	of how much memory was used.  It is possible to trade memory for
//  	better time performance.  This benchmark should be run in a 32 MB
//  	heap, though we don't currently know how to enforce that uniformly.
//
//  	Unlike the original Ellis and Kovac benchmark, we do not attempt
//   	measure pause times.  This facility should eventually be added back
//  	in.  There are several reasons for omitting it for now.  The original
//  	implementation depended on assumptions about the thread scheduler
//  	that don't hold uniformly.  The results really measure both the
//  	scheduler and GC.  Pause time measurements tend to not fit well with
//  	current benchmark suites.  As far as we know, none of the current
//  	commercial Java implementations seriously attempt to minimize GC pause
//  	times.

package gcbench

import benchmarks.{BenchmarkRunningTime, VeryLongRunningTime}

class GCBenchBenchmark extends benchmarks.Benchmark[(Node, Array[Double])] {
  override val runningTime: BenchmarkRunningTime = VeryLongRunningTime

  override def run(): (Node, Array[Double]) = GCBenchBenchmark.start()

  override def check(result: (Node, Array[Double])): Boolean =
    result._1 != null && result._2(1000) == 1.0 / 1000

}

class Node(var left: Node, var right: Node, var i: Int, var j: Int)

object GCBenchBenchmark {
  val kStretchTreeDepth: Int   = 18 // about 16Mb
  val kLongLivedTreeDepth: Int = 16 // about 4Mb
  val kArraySize: Int          = 500000 // about 4Mb
  val kMinTreeDepth: Int       = 4
  val kMaxTreeDepth: Int       = 16

  // Nodes used by a tree of a given size
  def treeSize(i: Int): Int = {
    return ((1 << (i + 1)) - 1)
  }

  // Number of iterations to use for a given tree depth
  def numIters(i: Int): Int =
    2 * treeSize(kStretchTreeDepth) / treeSize(i)

  // Build tree top down, assigning to older objects.
  def populate(iDepth: Int, thisNode: Node): Unit =
    if (iDepth > 0) {
      thisNode.left = new Node(null, null, 0, 0)
      thisNode.right = new Node(null, null, 0, 0)
      populate(iDepth - 1, thisNode.left)
      populate(iDepth - 1, thisNode.right)
    }

  // Build tree bottom-up
  def makeTree(iDepth: Int): Node =
    if (iDepth <= 0) {
      new Node(null, null, 0, 0)
    } else {
      new Node(makeTree(iDepth - 1), makeTree(iDepth - 1), 0, 0)
    }

  def construction(depth: Int): Unit = {
    var root: Node     = null
    var tempTree: Node = null
    val iNumIter: Int  = numIters(depth)

    var i = 0;
    while (i < iNumIter) {
      tempTree = new Node(null, null, 0, 0)
      populate(depth, tempTree)
      tempTree = null
      i += 1
    }

    i = 0
    while (i < iNumIter) {
      tempTree = makeTree(depth)
      tempTree = null
      i += 1
    }
  }

  def start(): (Node, Array[Double]) = {
    var root: Node          = null
    var longLivedTree: Node = null
    var tempTree: Node      = null

    // Stretch the memory space quickly
    tempTree = makeTree(kStretchTreeDepth)
    tempTree = null;

    // Create a long lived object
    longLivedTree = new Node(null, null, 0, 0);
    populate(kLongLivedTreeDepth, longLivedTree)

    // Create long-lived array, filling half of it
    val array = new Array[Double](kArraySize)
    var i     = 0
    while (i < kArraySize / 2) {
      array(i) = 1.0 / i
      i += 1
    }

    i = kMinTreeDepth
    while (i <= kMaxTreeDepth) {
      construction(i)
      i += 2
    }

    (longLivedTree, array)
  }
}
