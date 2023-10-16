object Main {
  def main(args: Array[String]): Unit = {
    List(
      "/LICENSE",
      "/NOTICE",
      "/library.properties",
      "/rootdoc.txt",
      "/META-INF/MANIFEST.MF",
      "/scala-native/stdlib.c" // /scala-native/ files should be excluded by default
    ).foreach(file =>
      assert(
        getClass().getResourceAsStream(file) == null,
        s"$file should not be embedded because they are not explicitly listed in the resource list"
      )
    )
  }
}
