package java.nio.channels

trait InterruptibleChannel extends Channel {
  override def close(): Unit
}
