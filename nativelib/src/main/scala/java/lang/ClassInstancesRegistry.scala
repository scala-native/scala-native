package java.lang

/** Registry for created instances of java.lang.Class
 * It's only purpose is to prevent GC from collecting instances of java.lang.Class
 **/
object ClassInstancesRegistry {
  type Arr[T] = scala.Array[T]
  type Bucket = Arr[_Class[_]]

  final val BucketSize = 256
  private var buckets  = new Arr[Bucket](2)
  private var lastId   = -1
  private var bucketId = -1
  private var itemId   = -1

  @inline def nextIds(): Unit = {
    lastId += 1;
    bucketId = lastId / BucketSize
    itemId = lastId % BucketSize
  }

  def add(cls: _Class[_]): _Class[_] = {
    nextIds()
    if (bucketId >= buckets.length) {
      val newSize: Int = buckets.length * 2
      val newArr       = new Arr[Bucket](newSize)
      Array.copy(buckets, 0, newArr, 0, buckets.length)
      buckets = newArr
    }
    if (buckets(bucketId) == null) {
      buckets(bucketId) = new Bucket(BucketSize)
    }
    buckets(bucketId)(itemId) = cls
    cls
  }
}
