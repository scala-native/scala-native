/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Ported from Apache Harmony

package java.util

/** Last-in-first-out stack of objects, backed by [[Vector]]. */
@SerialVersionUID(1224463164541339165L)
class Stack[E <: AnyRef] extends Vector[E] {

  def empty(): Boolean = isEmpty()

  def peek(): E = synchronized {
    if (elementCount == 0) throw new EmptyStackException()
    elementData(elementCount - 1)
  }

  def pop(): E = synchronized {
    val obj = peek()
    removeElementAt(elementCount - 1)
    obj
  }

  def push(item: E): E = {
    addElement(item)
    item
  }

  /** 1-based distance from the top of the stack, or -1 if absent. */
  def search(o: Any): Int = synchronized {
    val i = lastIndexOf(o, elementCount - 1)
    if (i >= 0) elementCount - i else -1
  }
}
