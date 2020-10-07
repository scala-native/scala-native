package java.lang

/** Registry for created instances of java.lang.Class
 * Its only purpose is to prevent the GC from collecting instances of java.lang.Class
 **/
private[lang] object ClassInstancesRegistry {
  type Bucket = Array[_Class[_]]

  private final val BucketSize = 256
  private var buckets          = new Array[Bucket](2)
  private var lastId           = -1
  private var bucketId         = -1
  private var itemId           = -1

  @inline def nextIds(): Unit = {
    lastId += 1
    bucketId = lastId / BucketSize
    itemId = lastId % BucketSize
  }

  def add(cls: _Class[_]): _Class[_] = {
    nextIds()
    if (bucketId >= buckets.length) {
      val newSize: Int = buckets.length * 2
      val newArr       = new Array[Bucket](newSize)
      System.arraycopy(buckets, 0, newArr, 0, buckets.length)
      buckets = newArr
    }
    if (buckets(bucketId) == null) {
      buckets(bucketId) = new Bucket(BucketSize)
    }
    buckets(bucketId)(itemId) = cls
    cls
  }
}
