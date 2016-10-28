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
 * A simple class simulating the concept of Basic Blocks
 *
 * BasicBlock only maintains a vector of in-edges and
 * a vector of out-edges.
 *
 * @author rhundt
 */
final class BasicBlock(name: Int) extends CustomHash {
  private val inEdges: Vector[BasicBlock]  = new Vector[BasicBlock](2)
  private val outEdges: Vector[BasicBlock] = new Vector[BasicBlock](2)

  def getInEdges(): Vector[BasicBlock]  = inEdges
  def getOutEdges(): Vector[BasicBlock] = outEdges
  def getNumPred(): Int                 = inEdges.size()

  def addOutEdge(to: BasicBlock): Unit  = outEdges.append(to)
  def addInEdge(from: BasicBlock): Unit = inEdges.append(from)

  override def customHash(): Int = name
}
