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

final class LoopTesterApp {
  private final val cfg = new ControlFlowGraph()
  private final val lsg = new LoopStructureGraph()
  cfg.createNode(0)

  // Create 4 basic blocks, corresponding to and if/then/else clause
  // with a CFG that looks like a diamond
  private def buildDiamond(start: Int): Int = {
    val bb0 = start
    new BasicBlockEdge(cfg, bb0, bb0 + 1)
    new BasicBlockEdge(cfg, bb0, bb0 + 2)
    new BasicBlockEdge(cfg, bb0 + 1, bb0 + 3)
    new BasicBlockEdge(cfg, bb0 + 2, bb0 + 3)
    bb0 + 3
  }

  // Connect two existing nodes
  private def buildConnect(start: Int, end: Int): Unit =
    new BasicBlockEdge(cfg, start, end)

  // Form a straight connected sequence of n basic blocks
  private def buildStraight(start: Int, n: Int): Int = {
    (0 until n).foreach { i =>
      buildConnect(start + i, start + i + 1)
    }
    start + n
  }

  // Construct a simple loop with two diamonds in it
  private def buildBaseLoop(from: Int): Int = {
    val header   = buildStraight(from, 1)
    val diamond1 = buildDiamond(header)
    val d11      = buildStraight(diamond1, 1)
    val diamond2 = buildDiamond(d11)
    var footer   = buildStraight(diamond2, 1)
    buildConnect(diamond2, d11)
    buildConnect(diamond1, header)
    buildConnect(footer, from)
    footer = buildStraight(footer, 1)
    footer
  }

  def main(
      numDummyLoops: Int,
      findLoopIterations: Int,
      parLoops: Int,
      pparLoops: Int,
      ppparLoops: Int
  ): Array[Int] = {
    constructSimpleCFG()
    addDummyLoops(numDummyLoops)
    constructCFG(parLoops, pparLoops, ppparLoops)

    // Performing Loop Recognition, 1 Iteration, then findLoopIteration
    findLoops(lsg)
    (0 until findLoopIterations).foreach { i =>
      findLoops(new LoopStructureGraph())
    }

    lsg.calculateNestingLevel()
    Array(lsg.getNumLoops(), cfg.getNumNodes())
  }

  private def constructCFG(parLoops: Int,
                           pparLoops: Int,
                           ppparLoops: Int): Unit = {
    var n = 2

    (0 until parLoops).foreach { parlooptrees =>
      cfg.createNode(n + 1)
      buildConnect(2, n + 1)
      n += 1

      (0 until pparLoops).foreach { i =>
        val top = n
        n = buildStraight(n, 1)
        (0 until ppparLoops).foreach { j =>
          n = buildBaseLoop(n)
        }
        val bottom = buildStraight(n, 1)
        buildConnect(n, top)
        n = bottom
      }
      buildConnect(n, 1)
    }
  }

  private def addDummyLoops(numDummyLoops: Int): Unit = {
    (0 until numDummyLoops).foreach { dummyloop =>
      findLoops(lsg)
    }
  }

  private def findLoops(loopStructure: LoopStructureGraph): Unit = {
    val finder = new HavlakLoopFinder(cfg, loopStructure)
    finder.findLoops()
  }

  private def constructSimpleCFG(): Unit = {
    cfg.createNode(0)
    buildBaseLoop(0)
    cfg.createNode(1)
    new BasicBlockEdge(cfg, 0, 2)
  }
}
