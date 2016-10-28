// Copyright 2011 Google Inc.
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
package havlak

import som._

/**
 * The Havlak loop finding algorithm.
 *
 * This class encapsulates the complete finder algorithm
 *
 * @author rhundt
 */
final class HavlakLoopFinder(
    private val cfg: ControlFlowGraph,
    private val lsg: LoopStructureGraph
) {
  import HavlakLoopFinder._

  private val nonBackPreds                   = new Vector[Set[Integer]]()
  private val backPreds                      = new Vector[Vector[Integer]]()
  private val number                         = new IdentityDictionary[BasicBlock, Integer]()
  private var maxSize: Int                   = 0
  private var header: Array[Int]             = null
  private var type_ : Array[BasicBlockClass] = null
  private var last: Array[Int]               = null
  private var nodes: Array[UnionFindNode]    = null

  //
  // IsAncestor
  //
  // As described in the paper, determine whether a node 'w' is a
  // "true" ancestor for node 'v'.
  //
  // Dominance can be tested quickly using a pre-order trick
  // for depth-first spanning trees. This is why DFS is the first
  // thing we run below.
  //
  private def isAncestor(w: Int, v: Int): Boolean =
    w <= v && v <= last(w)

  //
  // DFS - Depth-First-Search
  //
  // DESCRIPTION:
  // Simple depth first traversal along out edges with node numbering.
  //
  private def doDFS(currentNode: BasicBlock, current: Int): Int = {
    nodes(current).initNode(currentNode, current)
    number.atPut(currentNode, current)

    var lastId      = current
    val outerBlocks = currentNode.getOutEdges()

    (0 until outerBlocks.size).foreach { i =>
      val target = outerBlocks.at(i)
      if (number.at(target) == UNVISITED) {
        lastId = doDFS(target, lastId + 1)
      }
    }

    last(current) = lastId
    lastId
  }

  private def initAllNodes(): Unit = {
    // Step a:
    //   - initialize all nodes as unvisited.
    //   - depth-first traversal and numbering.
    //   - unreached BB's are marked as dead.
    //
    cfg.getBasicBlocks().forEach(bb => number.atPut(bb, UNVISITED))

    doDFS(cfg.getStartBasicBlock(), 0)
  }

  private def identifyEdges(size: Int): Unit = {
    // Step b:
    //   - iterate over all nodes.
    //
    //   A backedge comes from a descendant in the DFS tree, and non-backedges
    //   from non-descendants (following Tarjan).
    //
    //   - check incoming edges 'v' and add them to either
    //     - the list of backedges (backPreds) or
    //     - the list of non-backedges (nonBackPreds)
    //
    (0 until size).foreach { w =>
      header(w) = 0
      type_(w) = BasicBlockClass.BB_NONHEADER

      val nodeW = nodes(w).getBb()
      if (nodeW == null) {
        type_(w) = BasicBlockClass.BB_DEAD
      } else {
        processEdges(nodeW, w)
      }
    }
  }

  private def processEdges(nodeW: BasicBlock, w: Int): Unit = {
    if (nodeW.getNumPred() > 0) {
      nodeW.getInEdges().forEach { nodeV =>
        val v = number.at(nodeV)
        if (v != UNVISITED) {
          if (isAncestor(w, v)) {
            backPreds.at(w).append(v)
          } else {
            nonBackPreds.at(w).add(v)
          }
        }
      }
    }
  }

  //
  // findLoops
  //
  // Find loops and build loop forest using Havlak's algorithm, which
  // is derived from Tarjan. Variable names and step numbering has
  // been chosen to be identical to the nomenclature in Havlak's
  // paper (which, in turn, is similar to the one used by Tarjan).
  //
  def findLoops(): Unit = {
    if (cfg.getStartBasicBlock() == null) {
      return
    }

    val size = cfg.getNumNodes()

    nonBackPreds.removeAll()
    backPreds.removeAll()
    number.removeAll()
    if (size > maxSize) {
      header = new Array[Int](size)
      type_ = new Array[BasicBlockClass](size)
      last = new Array[Int](size)
      nodes = new Array[UnionFindNode](size)
      maxSize = size
    }

    (0 until size).foreach { i =>
      nonBackPreds.append(new Set())
      backPreds.append(new Vector())
      nodes(i) = new UnionFindNode()
    }

    initAllNodes()
    identifyEdges(size)

    // Start node is root of all other loops.
    header(0) = 0

    // Step c:
    //
    // The outer loop, unchanged from Tarjan. It does nothing except
    // for those nodes which are the destinations of backedges.
    // For a header node w, we chase backward from the sources of the
    // backedges adding nodes to the set P, representing the body of
    // the loop headed by w.
    //
    // By running through the nodes in reverse of the DFST preorder,
    // we ensure that inner loop headers will be processed before the
    // headers for surrounding loops.
    //
    var w = size - 1
    while (w >= 0) {
      // this is 'P' in Havlak's paper
      val nodePool = new Vector[UnionFindNode]()

      val nodeW = nodes(w).getBb()
      if (nodeW != null) {
        stepD(w, nodePool)

        // Copy nodePool to workList.
        //
        val workList = new Vector[UnionFindNode]()
        nodePool.forEach(niter => workList.append(niter))

        if (nodePool.size() != 0) {
          type_(w) = BasicBlockClass.BB_REDUCIBLE
        }

        // work the list...
        //
        while (!workList.isEmpty()) {
          val x = workList.removeFirst()

          // Step e:
          //
          // Step e represents the main difference from Tarjan's method.
          // Chasing upwards from the sources of a node w's backedges. If
          // there is a node y' that is not a descendant of w, w is marked
          // the header of an irreducible loop, there is another entry
          // into this loop that avoids w.
          //

          // The algorithm has degenerated. Break and
          // return in this case.
          //
          val nonBackSize = nonBackPreds.at(x.getDfsNumber()).size()
          if (nonBackSize > MAXNONBACKPREDS) {
            return
          }
          stepEProcessNonBackPreds(w, nodePool, workList, x)
        }

        // Collapse/Unionize nodes in a SCC to a single node
        // For every SCC found, create a loop descriptor and link it in.
        //
        if ((nodePool.size() > 0) || (type_(w) == BasicBlockClass.BB_SELF)) {
          val loop = lsg
            .createNewLoop(nodeW, type_(w) != BasicBlockClass.BB_IRREDUCIBLE)
          setLoopAttributes(w, nodePool, loop)
        }
      }

      w -= 1
    } // Step c
  } // findLoops

  private def stepEProcessNonBackPreds(w: Int,
                                       nodePool: Vector[UnionFindNode],
                                       workList: Vector[UnionFindNode],
                                       x: UnionFindNode): Unit = {
    nonBackPreds.at(x.getDfsNumber()).forEach { iter =>
      val y     = nodes(iter)
      val ydash = y.findSet()

      if (!isAncestor(w, ydash.getDfsNumber())) {
        type_(w) = BasicBlockClass.BB_IRREDUCIBLE
        nonBackPreds.at(w).add(ydash.getDfsNumber())
      } else {
        if (ydash.getDfsNumber() != w) {
          if (!nodePool.hasSome(e => e == ydash)) {
            workList.append(ydash)
            nodePool.append(ydash)
          }
        }
      }
    }
  }

  private def setLoopAttributes(w: Int,
                                nodePool: Vector[UnionFindNode],
                                loop: SimpleLoop): Unit = {
    // At this point, one can set attributes to the loop, such as:
    //
    // the bottom node:
    //    iter  = backPreds[w].begin()
    //    loop bottom is: nodes[iter].node)
    //
    // the number of backedges:
    //    backPreds[w].size()
    //
    // whether this loop is reducible:
    //    type_[w] != BasicBlockClass.BB_IRREDUCIBLE
    //
    nodes(w).setLoop(loop)

    nodePool.forEach { node =>
      // Add nodes to loop descriptor.
      header(node.getDfsNumber()) = w
      node.union(nodes(w))

      // Nested loops are not added, but linked together.
      if (node.getLoop() != null) {
        node.getLoop().setParent(loop)
      } else {
        loop.addNode(node.getBb())
      }
    }
  }

  private def stepD(w: Int, nodePool: Vector[UnionFindNode]): Unit = {
    backPreds.at(w).forEach { v =>
      if (v != w) {
        nodePool.append(nodes(v).findSet())
      } else {
        type_(w) = BasicBlockClass.BB_SELF
      }
    }
  }
}

object HavlakLoopFinder {
  // Marker for uninitialized nodes.
  final val UNVISITED = Integer.MAX_VALUE

  // Safeguard against pathological algorithm behavior.
  final val MAXNONBACKPREDS = (32 * 1024)

  /**
   * enum BasicBlockClass
   *
   * Basic Blocks and Loops are being classified as regular, irreducible,
   * and so on. This enum contains a symbolic name for all these classifications
   */
  type BasicBlockClass = Int
  object BasicBlockClass {
    final val BB_TOP         = 0 // uninitialized
    final val BB_NONHEADER   = 1 // a regular BB
    final val BB_REDUCIBLE   = 2 // reducible loop
    final val BB_SELF        = 3 // single BB loop
    final val BB_IRREDUCIBLE = 4 // irreducible loop
    final val BB_DEAD        = 5 // a dead BB
    final val BB_LAST        = 6 // Sentinel
  }
}
