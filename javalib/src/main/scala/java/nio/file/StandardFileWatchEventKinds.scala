package java.nio.file

object StandardWatchEventKinds {
  val ENTRY_CREATE =
    new WatchEvent.Kind[Path] {
      override def name() = "ENTRY_CREATE"
      override def `type`() = classOf[Path]
    }

  val ENTRY_DELETE =
    new WatchEvent.Kind[Path] {
      override def name() = "ENTRY_DELETE"
      override def `type`() = classOf[Path]
    }

  val ENTRY_MODIFY =
    new WatchEvent.Kind[Path] {
      override def name() = "ENTRY_MODIFY"
      override def `type`() = classOf[Path]
    }

  val OVERFLOW =
    new WatchEvent.Kind[Object] {
      override def name() = "OVERFLOW"
      override def `type`() = classOf[Object]
    }
}
