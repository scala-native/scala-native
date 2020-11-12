package scala.scalanative.runtime

/** Registry for created instances of java.lang.Class
 *  Its only purpose is to prevent the GC from collecting instances of java.lang.Class
 */
private[runtime] object ClassInstancesRegistry {
  type Bucket = scala.Array[_Class[_]]

  private final val BucketSize = 256
  private var buckets          = new scala.Array[Bucket](2)
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
      val newArr       = new scala.Array[Bucket](newSize)
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
