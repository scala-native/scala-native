# FAQ

## How do I make the resulting executable smaller?

Try to use `-O2` and then https://upx.github.io/

## How do I specify which native library an `@extern` object should link to?

Use the `@link("libraryname")` annotation to automatically link with
a native library whenever an annotated extern object is used. (e.g. `@link("png"`) to link with "libpng"):

```
@link("telldus-core")
@extern object TelldusCore {
  def tdInit(): Unit = extern
  [...]
}
```
