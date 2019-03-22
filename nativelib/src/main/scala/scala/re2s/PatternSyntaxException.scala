// Copyright 2010 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package scala.re2s

/**
 * An exception thrown by the parser if the pattern was invalid.
 *
 * Following {@code java.util.regex.PatternSyntaxException}, this is an
 * unchecked exception.
 */
class PatternSyntaxException(error: String, input: String, index: Int)
    extends RuntimeException(
      "error parsing regexp: " + error + ": `" + input + "`") {
  def this(error: String, input: String) = this(error, input, 0)
  def this(error: String) = this(error, "", 0)

  /**
   * Retrieves the error index.
   *
   * @return  The approximate index in the pattern of the error,
   *         or <tt>-1</tt> if the index is not known
   */
  def getIndex(): Int = index

  /**
   * Retrieves the description of the error.
   *
   * @return  The description of the error
   */
  def getDescription(): String = error

  override def getMessage(): String = {
    s"""$error near index $index
       |$getPattern
       |${" " * getIndex() + "^"}""".stripMargin
  }

  /**
   * Retrieves the erroneous regular-expression pattern.
   *
   * @return  The erroneous pattern
   */
  def getPattern(): String = input
}
