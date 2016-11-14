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
 * A simple class simulating the concept of Edges
 * between Basic Blocks.
 *
 * @author rhundt
 */
final class BasicBlockEdge(cfg: ControlFlowGraph, fromName: Int, toName: Int) {
  private val from: BasicBlock = cfg.createNode(fromName)
  private val to: BasicBlock   = cfg.createNode(toName)

  from.addOutEdge(to)
  to.addInEdge(from)
  cfg.addEdge(this)
}
