package scala.scalanative
package io

import java.nio.ByteBuffer
import java.nio.file.{Files, Path}

sealed trait VirtualFile {

  /** Parent virtual directory. */
  def directory: VirtualDirectory

  /** Relative location of a file in virtual directory. */
  def path: Path

  /** Read cached contents of the virtual file. */
  def contents: java.nio.ByteBuffer

  /** Write contents to the file. */
  def contents_=(value: java.nio.ByteBuffer): Unit
}

object VirtualFile {

  /** A memory cached file-like object contained in some member of a classpath. */
  private final class Impl(val directory: VirtualDirectory, val path: Path)
      extends VirtualFile {
    def contents: java.nio.ByteBuffer =
      directory.read(path)

    def contents_=(value: java.nio.ByteBuffer): Unit =
      directory.write(path, value)
  }

  /** Create a virtual file from given path. */
  def apply(directory: VirtualDirectory, path: Path): VirtualFile =
    new Impl(directory, path)
}
