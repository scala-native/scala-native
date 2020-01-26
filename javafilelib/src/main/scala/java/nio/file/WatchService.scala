package java.nio.file

import java.io.Closeable
import java.util.concurrent.TimeUnit

trait WatchService extends Closeable {
  def close(): Unit
  def poll(): WatchKey
  def poll(timeout: Long, unit: TimeUnit): WatchKey
  def take(): WatchKey
}
