/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package json

import som.Vector

/**
 * Represents a JSON object, a set of name/value pairs, where the names are strings and the values
 * are JSON values.
 * <p>
 * Members can be added using the <code>add(String, ...)</code> methods which accept instances of
 * {@link JsonValue}, strings, primitive numbers, and boolean values. To modify certain values of an
 * object, use the <code>set(String, ...)</code> methods. Please note that the <code>add</code>
 * methods are faster than <code>set</code> as they do not search for existing members. On the other
 * hand, the <code>add</code> methods do not prevent adding multiple members with the same name.
 * Duplicate names are discouraged but not prohibited by JSON.
 * </p>
 * <p>
 * Members can be accessed by their name using {@link #get(String)}.
 * <p>
 * This class is <strong>not supposed to be extended</strong> by clients.
 * </p>
 */
final class JsonObject extends JsonValue {
  private val names  = new Vector[String]()
  private val values = new Vector[JsonValue]()
  private var table  = new HashIndexTable()

  /**
   * Appends a new member to the end of this object, with the specified name and the specified JSON
   * value.
   * <p>
   * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name
   * that already exists in the object will append another member with the same name. In order to
   * replace existing members, use the method <code>set(name, value)</code> instead. However,
   * <strong> <em>add</em> is much faster than <em>set</em></strong> (because it does not need to
   * search for existing members). Therefore <em>add</em> should be preferred when constructing new
   * objects.
   * </p>
   *
   * @param name
   *          the name of the member to add
   * @param value
   *          the value of the member to add, must not be <code>null</code>
   * @return the object itself, to enable method chaining
   */
  def add(name: String, value: JsonValue): JsonObject = {
    if (name == null) {
      throw new NullPointerException("name is null")
    }
    if (value == null) {
      throw new NullPointerException("value is null")
    }
    table.add(name, names.size())
    names.append(name)
    values.append(value)
    return this
  }

  /**
   * Returns the value of the member with the specified name in this object. If this object contains
   * multiple members with the given name, this method will return the last one.
   *
   * @param name
   *          the name of the member whose value is to be returned
   * @return the value of the last member with the specified name, or <code>null</code> if this
   *         object does not contain a member with that name
   */
  def get(name: String): JsonValue = {
    if (name == null) {
      throw new NullPointerException("name is null")
    }
    val index = indexOf(name)

    if (index == -1) null else values.at(index)
  }

  /**
   * Returns the number of members (name/value pairs) in this object.
   *
   * @return the number of members in this object
   */
  def size() = names.size()

  /**
   * Returns <code>true</code> if this object contains no members.
   *
   * @return <code>true</code> if this object contains no members
   */
  def isEmpty() = names.isEmpty()

  override def isObject() = true

  override def asObject() = this

  def indexOf(name: String): Int = {
    val index = table.get(name)
    if (index != -1 && name.equals(names.at(index))) {
      return index
    }
    ??? // Not needed for benchmark
  }

  private class HashIndexTable {
    private val hashTable = new Array[Int](32)

    def add(name: String, index: Int): Unit = {
      val slot = hashSlotFor(name)
      if (index < 0xff) {
        // increment by 1, 0 stands for empty
        hashTable(slot) = (index + 1) & 0xff
      } else {
        hashTable(slot) = 0
      }
    }

    def get(name: String): Int = {
      val slot = hashSlotFor(name)
      // subtract 1, 0 stands for empty
      (hashTable(slot) & 0xff) - 1
    }

    def stringHash(s: String): Int =
      // this is not a proper hash, but sufficient for the benchmark,
      // and very portable!
      s.length() * 1402589

    def hashSlotFor(element: String): Int =
      stringHash(element) & hashTable.length - 1
  }
}
