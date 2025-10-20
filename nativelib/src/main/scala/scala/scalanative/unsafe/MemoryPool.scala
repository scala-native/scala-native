package scala.scalanative.unsafe

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import scala.scalanative.meta.LinktimeInfo.asanEnabled
import scala.scalanative.runtime.{RawPtr, fromRawPtr}
import scala.scalanative.runtime.ffi
import scala.scalanative.runtime.Intrinsics

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
private[unsafe] final class MemoryPool private {
  private var chunkPageCount: USize = MemoryPool.MIN_PAGE_COUNT
  private var chunk: MemoryPool.Chunk = null
  private var page: MemoryPool.Page = null
  allocateChunk()

  /** Allocate a chunk of memory from system allocator. */
  private def allocateChunk(): Unit = {
    if (chunkPageCount < MemoryPool.MAX_PAGE_COUNT) {
      chunkPageCount *= 2.toUSize
    }
    val chunkSize = MemoryPool.PAGE_SIZE * chunkPageCount
    val start = ffi.malloc(chunkSize)
    chunk = new MemoryPool.Chunk(start, 0.toUSize, chunkSize, chunk)
  }

  /** Released all claimed memory chunks */
  private[scalanative] def freeChunks(): Unit = synchronized {
    while (chunk != null) {
      ffi.free(chunk.start)
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
private[unsafe] object MemoryPool {
  final val PAGE_SIZE = 4096.toUSize
  final val MIN_PAGE_COUNT = 4.toUSize
  final val MAX_PAGE_COUNT = 256.toUSize

  lazy val defaultMemoryPool: MemoryPool = {
    // Release allocated chunks satisfy AdressSanitizer
    if (asanEnabled)
      try
        Runtime.getRuntime().addShutdownHook {
          val t = new Thread(() => defaultMemoryPool.freeChunks())
          t.setPriority(Thread.MIN_PRIORITY)
          t.setName("shutdown-hook:memory-pool-cleanup")
          t
        }
      catch { case ex: IllegalStateException => () } // shutdown already started
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
final class MemoryPoolZone(private val pool: MemoryPool) extends Zone {
  private var tailPage = pool.claim()
  private var headPage = tailPage
  private var largeAllocations: scala.Array[CVoidPtr] = null
  private var largeOffset = 0

  private def checkOpen(): Unit =
    if (!isOpen)
      throw new IllegalStateException("Zone {this} is already closed.")

  private def pad(addr: CSize, alignment: CSize): CSize = {
    val alignmentMask: CSize = alignment - 1.toUSize
    val padding: CSize =
      if ((addr & alignmentMask) == 0) 0.toUSize
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
        ffi.free(largeAllocations(i))
        i += 1
      }
      largeAllocations = null
    }
  }

  override def alloc(usize: CSize): Ptr[Byte] = {
    val size = usize.toInt
    val alignment =
      if (size >= 16) 16
      else if (size >= 8) 8
      else if (size >= 4) 4
      else if (size >= 2) 2
      else 1

    alloc(usize, alignment.toUSize)
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
      largeAllocations = new scala.Array[CVoidPtr](16)
    }
    if (largeOffset == largeAllocations.length) {
      val newLargeAllocations =
        new scala.Array[CVoidPtr](largeAllocations.length * 2)
      Array.copy(
        largeAllocations,
        0,
        newLargeAllocations,
        0,
        largeAllocations.length
      )
      largeAllocations = newLargeAllocations
    }
    val result = fromRawPtr[Byte](ffi.malloc(size))
    largeAllocations(largeOffset) = result
    largeOffset += 1

    result
  }
}

private[unsafe] object MemoryPoolZone {
  def open(pool: MemoryPool): MemoryPoolZone =
    new MemoryPoolZone(pool)
}
