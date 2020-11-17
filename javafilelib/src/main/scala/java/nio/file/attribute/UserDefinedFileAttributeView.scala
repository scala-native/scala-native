package java.nio.file.attribute

import java.nio.ByteBuffer
import java.util.List

trait UserDefinedFileAttributeView extends FileAttributeView {
  def delete(name: String): Unit
  def list(): List[String]
  def read(name: String, dst: ByteBuffer): Int
  def size(name: String): Int
  def write(name: String, src: ByteBuffer): Int
}
