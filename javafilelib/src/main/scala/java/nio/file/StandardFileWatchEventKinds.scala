package java.nio.file

object StandardWatchEventKinds {
  val ENTRY_CREATE =
    new WatchEvent.Kind[Path] {
      override val name   = "ENTRY_CREATE"
      override val `type` = classOf[Path]
    }

  val ENTRY_DELETE =
    new WatchEvent.Kind[Path] {
      override val name   = "ENTRY_DELETE"
      override val `type` = classOf[Path]
    }

  val ENTRY_MODIFY =
    new WatchEvent.Kind[Path] {
      override val name   = "ENTRY_MODIFY"
      override val `type` = classOf[Path]
    }

  val OVERFLOW =
    new WatchEvent.Kind[Object] {
      override val name   = "OVERFLOW"
      override val `type` = classOf[Object]
    }
}
