package scala.scalanative.runtime

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo.asanEnabled

/** Efficient pool of fixed-size memory pages. Allocations from underlying
 *  allocator are performed in big chunks of memory that are sliced into pages
 *  of requested size.
 *
 *  Pages and chunks are organized in an intrusive linked list way to minimise
 *  memory overhead and re-use the same nodes for the whole lifetime of the
 *  pool.
 *
 *  Memory is reclaimed back to underlying allocator once the pool is finalized.
 */
final class MemoryPool private {
  private[this] var chunkPageCount: USize = MemoryPool.MIN_PAGE_COUNT
  private[this] var chunk: MemoryPool.Chunk = null
  private[this] var page: MemoryPool.Page = null
  allocateChunk()

  /** Allocate a chunk of memory from system allocator. */
  private def allocateChunk(): Unit = {
    if (chunkPageCount < MemoryPool.MAX_PAGE_COUNT) {
      chunkPageCount *= 2.toUSize
    }
    val chunkSize = MemoryPool.PAGE_SIZE * chunkPageCount
    val start = libc.malloc(chunkSize)
    chunk = new MemoryPool.Chunk(start, 0.toUSize, chunkSize, chunk)
  }

  /** Released all claimed memory chunks */
  private[scalanative] def freeChunks(): Unit = synchronized {
    while (chunk != null) {
      libc.free(chunk.start)
      chunk = chunk.next
    }
  }

  /** Allocate a single page as a fraction of a larger chunk allocation. */
  private def allocatePage(): Unit = {
    if (chunk.offset >= chunk.size) allocateChunk()
    val start = Intrinsics.elemRawPtr(chunk.start, chunk.offset.rawSize)
    page = new MemoryPool.Page(start, 0.toUSize, page)
    chunk.offset += MemoryPool.PAGE_SIZE
  }

  /** Borrow a single unused page, to be reclaimed later. */
  def claim(): MemoryPool.Page = synchronized {
    if (page == null) allocatePage()
    val result = page
    page = result.next
    result.next = null
    result.offset = 0.toUSize
    result
  }

  /** Reclaimed a list of previously borrowed pages. */
  def reclaim(head: MemoryPool.Page, tail: MemoryPool.Page): Unit =
    synchronized {
      tail.next = page
      page = head
    }
}
object MemoryPool {
  final val PAGE_SIZE = 4096.toUSize
  final val MIN_PAGE_COUNT = 4.toUSize
  final val MAX_PAGE_COUNT = 256.toUSize

  lazy val defaultMemoryPool: MemoryPool = {
    // Release allocated chunks satisfy AdressSanitizer
    if (asanEnabled) libc.atexit { () =>
      defaultMemoryPool.freeChunks()
    }
    new MemoryPool()
  }

  private final class Chunk(
      val start: RawPtr,
      var offset: CSize,
      var size: CSize,
      var next: Chunk
  )

  final class Page(val start: RawPtr, var offset: CSize, var next: Page)
}

/** An optimized implementation of a zone that performs all allocations
 *  sequentially in pages that are claimed from memory pool. Larger allocations
 *  are allocated using the system allocator and persisted in an array buffer.
 */
final class MemoryPoolZone(private[this] val pool: MemoryPool) extends Zone {
  private[this] var tailPage = pool.claim()
  private[this] var headPage = tailPage
  private[this] var largeAllocations: scala.Array[Ptr[_]] = null
  private[this] var largeOffset = 0

  private def checkOpen(): Unit =
    if (!isOpen)
      throw new IllegalStateException("Zone {this} is already closed.")

  private def pad(addr: CSize, alignment: CSize): CSize = {
    val alignmentMask: CSize = alignment - 1.toUSize
    val padding: CSize =
      if ((addr & alignmentMask) == 0.toUSize) 0.toUSize
      else alignment - (addr & alignmentMask)
    addr + padding
  }

  override def isOpen = headPage != null

  override def isClosed = !isOpen

  override def close(): Unit = {
    checkOpen()

    // Reclaim borrowed pages to the memory pool.
    pool.reclaim(headPage, tailPage)
    headPage = null
    tailPage = null

    // Free all large allocations which were allocated with malloc.
    if (largeAllocations != null) {
      var i = 0
      while (i < largeOffset) {
        libc.free(largeAllocations(i))
        i += 1
      }
      largeAllocations = null
    }
  }

  def alloc(size: CSize): Ptr[Byte] = {
    val alignment =
      if (size >= 16.toUSize) 16.toUSize
      else if (size >= 8.toUSize) 8.toUSize
      else if (size >= 4.toUSize) 4.toUSize
      else if (size >= 2.toUSize) 2.toUSize
      else 1.toUSize

    alloc(size, alignment)
  }

  def alloc(size: CSize, alignment: CSize): Ptr[Byte] = {
    checkOpen()

    if (size <= MemoryPool.PAGE_SIZE / 2.toULong) {
      allocSmall(size, alignment)
    } else {
      allocLarge(size)
    }
  }

  private def allocSmall(size: CSize, alignment: CSize): Ptr[Byte] = {
    val currentOffset = headPage.offset
    val paddedOffset = pad(currentOffset, alignment)
    val resOffset: CSize =
      if (paddedOffset + size <= MemoryPool.PAGE_SIZE) {
        headPage.offset = paddedOffset + size
        paddedOffset
      } else {
        val newPage = pool.claim()
        newPage.next = headPage
        newPage.offset = size
        headPage = newPage
        0L.toUSize
      }

    fromRawPtr[Byte](Intrinsics.elemRawPtr(headPage.start, resOffset.rawSize))
  }

  private def allocLarge(size: CSize): Ptr[Byte] = {
    if (largeAllocations == null) {
      largeAllocations = new scala.Array[Ptr[_]](16)
    }
    if (largeOffset == largeAllocations.size) {
      val newLargeAllocations =
        new scala.Array[Ptr[_]](largeAllocations.size * 2)
      Array.copy(
        largeAllocations,
        0,
        newLargeAllocations,
        0,
        largeAllocations.size
      )
      largeAllocations = newLargeAllocations
    }
    val result = fromRawPtr[Byte](libc.malloc(size))
    largeAllocations(largeOffset) = result
    largeOffset += 1

    result
  }
}

object MemoryPoolZone {
  def open(pool: MemoryPool): MemoryPoolZone =
    new MemoryPoolZone(pool)
}
