package java.nio.file

trait Watchable {
  def register(watcher: WatchService,
               events: Array[WatchEvent.Kind[_]]): WatchKey
  def register(watcher: WatchService,
               events: Array[WatchEvent.Kind[_]],
               modifiers: Array[WatchEvent.Modifier]): WatchKey
}
