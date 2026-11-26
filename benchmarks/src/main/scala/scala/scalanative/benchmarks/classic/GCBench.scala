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

package scala.scalanative.benchmarks.classic

import java.util.concurrent.TimeUnit

import microbench.*

/** GCBench (Ellis, Kovac, Boehm): binary trees of varying lifetimes built top
 *  down and bottom up, next to long-lived data. Allocation and GC throughput.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class GCBench:
  @Setup(Level.Trial) def verify(): Unit =
    val result = run()
    if result != true then
      throw new AssertionError(s"expected true, got $result")

  @Benchmark def run(): Boolean =
    val (node, doubles) = GCBenchImpl.start()
    node != null && doubles(1000) == 1.0 / 1000

private class GCNode(
    var left: GCNode,
    var right: GCNode,
    var i: Int,
    var j: Int
)

private object GCBenchImpl {

  val kStretchTreeDepth: Int = 18 // about 16Mb
  val kLongLivedTreeDepth: Int = 16 // about 4Mb
  val kArraySize: Int = 500000 // about 4Mb
  val kMinTreeDepth: Int = 4
  val kMaxTreeDepth: Int = 16

  // Nodes used by a tree of a given size
  def treeSize(i: Int): Int = {
    return ((1 << (i + 1)) - 1)
  }

  // Number of iterations to use for a given tree depth
  def numIters(i: Int): Int =
    2 * treeSize(kStretchTreeDepth) / treeSize(i)

  // Build tree top down, assigning to older objects.
  def populate(iDepth: Int, thisNode: GCNode): Unit =
    if (iDepth > 0) {
      thisNode.left = new GCNode(null, null, 0, 0)
      thisNode.right = new GCNode(null, null, 0, 0)
      populate(iDepth - 1, thisNode.left)
      populate(iDepth - 1, thisNode.right)
    }

  // Build tree bottom-up
  def makeTree(iDepth: Int): GCNode =
    if (iDepth <= 0) {
      new GCNode(null, null, 0, 0)
    } else {
      new GCNode(makeTree(iDepth - 1), makeTree(iDepth - 1), 0, 0)
    }

  def construction(depth: Int): Unit = {
    var root: GCNode = null
    var tempTree: GCNode = null
    val iNumIter: Int = numIters(depth)

    var i = 0;
    while (i < iNumIter) {
      tempTree = new GCNode(null, null, 0, 0)
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

  def start(): (GCNode, Array[Double]) = {
    var root: GCNode = null
    var longLivedTree: GCNode = null
    var tempTree: GCNode = null

    // Stretch the memory space quickly
    tempTree = makeTree(kStretchTreeDepth)
    tempTree = null;

    // Create a long lived object
    longLivedTree = new GCNode(null, null, 0, 0);
    populate(kLongLivedTreeDepth, longLivedTree)

    // Create long-lived array, filling half of it
    val array = new Array[Double](kArraySize)
    var i = 0
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
