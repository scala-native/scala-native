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

//======================================================
// Scaffold Code
//======================================================

/**
 * The Havlak loop finding algorithm.
 *
 * @author rhundt
 */
package havlak

import som._

/**
 * class SimpleLoop
 *
 * Basic representation of loops, a loop has an entry point,
 * one or more exit edges, a set of basic blocks, and potentially
 * an outer loop - a "parent" loop.
 *
 * Furthermore, it can have any set of properties, e.g.,
 * it can be an irreducible loop, have control flow, be
 * a candidate for transformations, and what not.
 */
final class SimpleLoop(header: BasicBlock, isReducible: Boolean) {
  private val basicBlocks        = new IdentitySet[BasicBlock]()
  private val children           = new IdentitySet[SimpleLoop]()
  private var parent: SimpleLoop = null
  private var _isRoot: Boolean   = false
  private var nestingLevel: Int  = 0
  private var counter: Int       = 0
  private var depthLevel: Int    = 0

  if (header != null) {
    basicBlocks.add(header)
  }

  def addNode(bb: BasicBlock): Unit =
    basicBlocks.add(bb)

  private def addChildLoop(loop: SimpleLoop): Unit =
    children.add(loop)

  // Getters/Setters
  def getChildren() = children

  def getParent() = parent

  def getNestingLevel() = nestingLevel

  def isRoot() = _isRoot

  def setParent(parent: SimpleLoop): Unit = {
    this.parent = parent
    this.parent.addChildLoop(this)
  }

  def setIsRoot(): Unit = {
    _isRoot = true
  }

  def setCounter(value: Int): Unit = {
    counter = value
  }

  def setNestingLevel(level: Int): Unit = {
    nestingLevel = level
    if (level == 0) {
      setIsRoot()
    }
  }

  def setDepthLevel(level: Int): Unit = {
    depthLevel = level
  }
}
