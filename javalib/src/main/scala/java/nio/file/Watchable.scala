package java.nio.file

trait Watchable {
  def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[?]]
  ): WatchKey
  def register(
      watcher: WatchService,
      events: Array[WatchEvent.Kind[?]],
      modifiers: Array[WatchEvent.Modifier]
  ): WatchKey
}
