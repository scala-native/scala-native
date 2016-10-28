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
 * A simple class simulating the concept of
 * a control flow graph.
 *
 * CFG maintains a list of nodes, plus a start node.
 * That's it.
 *
 * @author rhundt
 */
final class ControlFlowGraph {
  private var startNode: BasicBlock = null
  private val basicBlockMap         = new Vector[BasicBlock]
  private val edgeList              = new Vector[BasicBlockEdge]

  def createNode(name: Int): BasicBlock = {
    val node =
      if (basicBlockMap.at(name) != null) {
        basicBlockMap.at(name)
      } else {
        val bb = new BasicBlock(name)
        basicBlockMap.atPut(name, bb)
        bb
      }

    if (getNumNodes() == 1) {
      startNode = node
    }

    node
  }

  def addEdge(edge: BasicBlockEdge): Unit =
    edgeList.append(edge)

  def getNumNodes(): Int =
    basicBlockMap.size()

  def getStartBasicBlock(): BasicBlock =
    startNode

  def getBasicBlocks(): Vector[BasicBlock] =
    basicBlockMap
}
