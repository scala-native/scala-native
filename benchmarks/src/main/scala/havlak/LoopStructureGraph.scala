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
 * LoopStructureGraph
 *
 * Maintain loop structure for a given CFG.
 *
 * Two values are maintained for this loop graph, depth, and nesting level.
 * For example:
 *
 * loop        nesting level    depth
 *----------------------------------------
 * loop-0      2                0
 *   loop-1    1                1
 *   loop-3    1                1
 *     loop-2  0                2
 *
 * @author rhundt
 */
final class LoopStructureGraph {
  private val root        = new SimpleLoop(null, true)
  private val loops       = new Vector[SimpleLoop]()
  private var loopCounter = 0
  root.setNestingLevel(0)
  root.setCounter(loopCounter)
  loopCounter += 1
  loops.append(root)

  def createNewLoop(bb: BasicBlock, isReducible: Boolean): SimpleLoop = {
    val loop = new SimpleLoop(bb, isReducible)
    loop.setCounter(loopCounter)
    loopCounter += 1
    loops.append(loop)
    loop
  }

  def calculateNestingLevel(): Unit = {
    // link up all 1st level loops to artificial root node.
    loops.forEach { liter =>
      if (!liter.isRoot()) {
        if (liter.getParent() == null) {
          liter.setParent(root)
        }
      }
    }

    // recursively traverse the tree and assign levels.
    calculateNestingLevelRec(root, 0)
  }

  def calculateNestingLevelRec(loop: SimpleLoop, depth: Int): Unit = {
    loop.setDepthLevel(depth)
    loop.getChildren().forEach { liter =>
      calculateNestingLevelRec(liter, depth + 1)

      loop.setNestingLevel(
        Math.max(loop.getNestingLevel(), 1 + liter.getNestingLevel()))
    }
  }

  def getNumLoops(): Int = loops.size()
}
