package java.nio.file

import java.util.List

trait WatchKey {
  def cancel(): Unit
  def isValid(): Boolean
  def pollEvents(): List[WatchEvent[_]]
  def reset(): Boolean
  def watchable(): Watchable
}
