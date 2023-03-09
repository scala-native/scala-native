package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

import scalanative.unsigned._

class TagTest {

  @Test def tagSize(): Unit = {
    assertTrue(tagof[Ptr[_]].size == sizeof[Size])
    assertTrue(tagof[Object].size == sizeof[Size])
    assertTrue(tagof[Array[Any]].size == sizeof[Size])
    assertTrue(tagof[Unit].size == sizeof[Size])
    assertTrue(tagof[Boolean].size == 1.toULong)
    assertTrue(tagof[Char].size == 2.toULong)
    assertTrue(tagof[Byte].size == 1.toULong)
    assertTrue(tagof[UByte].size == 1.toULong)
    assertTrue(tagof[Short].size == 2.toULong)
    assertTrue(tagof[UShort].size == 2.toULong)
    assertTrue(tagof[Int].size == 4.toULong)
    assertTrue(tagof[UInt].size == 4.toULong)
    assertTrue(tagof[Long].size == 8.toULong)
    assertTrue(tagof[ULong].size == 8.toULong)
    assertTrue(tagof[Float].size == 4.toULong)
    assertTrue(tagof[Double].size == 8.toULong)
    assertTrue(tagof[CArray[Int, Nat._0]].size == 0.toULong)
    assertTrue(tagof[CArray[Int, Nat._3]].size == (4 * 3).toULong)
    assertTrue(tagof[CArray[Int, Nat._9]].size == (4 * 9).toULong)
    assertTrue(tagof[CStruct0].size == 0.toULong)
    assertTrue(tagof[CStruct1[Int]].size == 4.toULong)
    assertTrue(tagof[CStruct2[Byte, Int]].size == 8.toULong)
    assertTrue(tagof[CStruct3[Byte, Byte, Int]].size == 8.toULong)
  }

  @Test def tagSizeShouldBeConsistentWithSizeof(): Unit = {
    assertTrue(tagof[Ptr[_]].size == sizeof[Ptr[_]])
    assertTrue(tagof[Unit].size == sizeof[Unit])
    assertTrue(tagof[Boolean].size == sizeof[Boolean])
    assertTrue(tagof[Char].size == sizeof[Char])
    assertTrue(tagof[Byte].size == sizeof[Byte])
    assertTrue(tagof[UByte].size == sizeof[UByte])
    assertTrue(tagof[Short].size == sizeof[Short])
    assertTrue(tagof[UShort].size == sizeof[UShort])
    assertTrue(tagof[Int].size == sizeof[Int])
    assertTrue(tagof[UInt].size == sizeof[UInt])
    assertTrue(tagof[Long].size == sizeof[Long])
    assertTrue(tagof[ULong].size == sizeof[ULong])
    assertTrue(tagof[Float].size == sizeof[Float])
    assertTrue(tagof[Double].size == sizeof[Double])
    assertTrue(tagof[CArray[Int, Nat._0]].size == sizeof[CArray[Int, Nat._0]])
    assertTrue(tagof[CArray[Int, Nat._3]].size == sizeof[CArray[Int, Nat._3]])
    assertTrue(tagof[CArray[Int, Nat._9]].size == sizeof[CArray[Int, Nat._9]])
    assertTrue(tagof[CStruct0].size == sizeof[CStruct0])
    assertTrue(tagof[CStruct1[Int]].size == sizeof[CStruct1[Int]])
    assertTrue(tagof[CStruct2[Byte, Int]].size == sizeof[CStruct2[Byte, Int]])
    assertTrue(
      tagof[CStruct3[Byte, Byte, Int]].size == sizeof[CStruct3[Byte, Byte, Int]]
    )
    // sizeOf objects calculates their final size based on memory layout
    // tagOf.size always returns sizeOf[Ptr[_]]
    // assertTrue(tagof[Object].size == sizeof[Object])
    // assertTrue(tagof[Array[_]].size == sizeof[Array[_]])
  }

  @Test def tagAlignment(): Unit = {
    assertTrue(tagof[Ptr[_]].alignment == sizeof[Size])
    assertTrue(tagof[Object].alignment == sizeof[Size])
    assertTrue(tagof[Array[_]].alignment == sizeof[Size])
    assertTrue(tagof[Unit].alignment == sizeof[Size])
    assertTrue(tagof[Boolean].alignment == 1.toULong)
    assertTrue(tagof[Char].alignment == 2.toULong)
    assertTrue(tagof[Byte].alignment == 1.toULong)
    assertTrue(tagof[UByte].alignment == 1.toULong)
    assertTrue(tagof[Short].alignment == 2.toULong)
    assertTrue(tagof[UShort].alignment == 2.toULong)
    assertTrue(tagof[Int].alignment == 4.toULong)
    assertTrue(tagof[UInt].alignment == 4.toULong)
    assertTrue(tagof[Long].alignment == sizeof[Size])
    assertTrue(tagof[ULong].alignment == sizeof[Size])
    assertTrue(tagof[Float].alignment == 4.toULong)
    assertTrue(tagof[Double].alignment == sizeof[Size])
    assertTrue(tagof[CArray[Int, Nat._0]].alignment == 4.toULong)
    assertTrue(tagof[CArray[Int, Nat._3]].alignment == 4.toULong)
    assertTrue(tagof[CArray[Int, Nat._9]].alignment == 4.toULong)
    assertTrue(tagof[CStruct0].alignment == 1.toULong)
    assertTrue(tagof[CStruct1[Int]].alignment == 4.toULong)
    assertTrue(tagof[CStruct2[Byte, Int]].alignment == 4.toULong)
    assertTrue(tagof[CStruct3[Byte, Byte, Int]].alignment == 4.toULong)
  }

  @Test def tagAlignmentShouldBeConsistentWithAlignmentof(): Unit = {
    assertTrue(tagof[Ptr[_]].alignment == alignmentof[Ptr[_]])
    assertTrue(tagof[Object].alignment == alignmentof[Object])
    assertTrue(tagof[Array[_]].alignment == alignmentof[Array[_]])
    assertTrue(tagof[Unit].alignment == alignmentof[Unit])
    assertTrue(tagof[Boolean].alignment == alignmentof[Boolean])
    assertTrue(tagof[Char].alignment == alignmentof[Char])
    assertTrue(tagof[Byte].alignment == alignmentof[Byte])
    assertTrue(tagof[UByte].alignment == alignmentof[UByte])
    assertTrue(tagof[Short].alignment == alignmentof[Short])
    assertTrue(tagof[UShort].alignment == alignmentof[UShort])
    assertTrue(tagof[Int].alignment == alignmentof[Int])
    assertTrue(tagof[UInt].alignment == alignmentof[UInt])
    assertTrue(tagof[Long].alignment == alignmentof[Long])
    assertTrue(tagof[ULong].alignment == alignmentof[ULong])
    assertTrue(tagof[Float].alignment == alignmentof[Float])
    assertTrue(tagof[Double].alignment == alignmentof[Double])
    assertTrue(
      tagof[CArray[Int, Nat._0]].alignment == alignmentof[CArray[Int, Nat._0]]
    )
    assertTrue(
      tagof[CArray[Int, Nat._3]].alignment == alignmentof[CArray[Int, Nat._3]]
    )
    assertTrue(
      tagof[CArray[Int, Nat._9]].alignment == alignmentof[CArray[Int, Nat._9]]
    )
    assertTrue(tagof[CStruct0].alignment == alignmentof[CStruct0])
    assertTrue(tagof[CStruct1[Int]].alignment == alignmentof[CStruct1[Int]])
    assertTrue(
      tagof[CStruct2[Byte, Int]].alignment == alignmentof[CStruct2[Byte, Int]]
    )
    assertTrue(
      tagof[CStruct3[Byte, Byte, Int]].alignment == alignmentof[
        CStruct3[Byte, Byte, Int]
      ]
    )
  }

  @Test def tagOffset(): Unit = {
    assertTrue(tagof[CArray[Byte, Nat._0]].offset(0.toUSize) == 0.toULong)
    assertTrue(tagof[CArray[Byte, Nat._0]].offset(1.toUSize) == 1.toULong)
    assertTrue(tagof[CArray[Byte, Nat._0]].offset(42.toUSize) == 42.toULong)
    assertTrue(tagof[CArray[Int, Nat._0]].offset(0.toUSize) == 0.toULong)
    assertTrue(tagof[CArray[Int, Nat._0]].offset(1.toUSize) == 4.toULong)
    assertTrue(
      tagof[CArray[Int, Nat._0]].offset(42.toUSize) == (4 * 42).toULong
    )
    assertTrue(tagof[CStruct1[Int]].offset(0.toUSize) == 0.toULong)
    assertTrue(tagof[CStruct2[Byte, Int]].offset(0.toUSize) == 0.toULong)
    assertTrue(tagof[CStruct2[Byte, Int]].offset(1.toUSize) == 4.toULong)
    assertTrue(tagof[CStruct3[Byte, Byte, Int]].offset(0.toUSize) == 0.toULong)
    assertTrue(tagof[CStruct3[Byte, Byte, Int]].offset(1.toUSize) == 1.toULong)
    assertTrue(tagof[CStruct3[Byte, Byte, Int]].offset(2.toUSize) == 4.toULong)
    assertTrue(
      tagof[CStruct2[Byte, CStruct2[Byte, Int]]].offset(0.toUSize) == 0.toULong
    )
    assertTrue(
      tagof[CStruct2[Byte, CStruct2[Byte, Int]]].offset(1.toUSize) == 4.toULong
    )
  }

  type uint8_t = UByte
  type uint16_t = UShort
  type uint32_t = UInt
  type uint64_t = ULong

  type iovec = CStruct2[
    Ptr[Byte], // iov_base
    CSize // iov_len
  ]

  type socklen_t = CUnsignedInt
  type sa_family_t = CUnsignedShort

  type _14 = Nat.Digit2[Nat._1, Nat._4]

  type sockaddr = CStruct2[
    sa_family_t, // sa_family
    CArray[CChar, _14] // sa_data, size = 14 in OS X and Linux
  ]

  type _15 = Nat.Digit2[Nat._1, Nat._5]

  type sockaddr_storage = CStruct4[
    sa_family_t, // ss_family
    CUnsignedShort, // opaque, __padTo32
    CUnsignedInt, // opaque, __padTo64
    CArray[CUnsignedLongLong, _15] // opaque, align structure to 8 bytes
  ]

  type msghdr = CStruct7[
    Ptr[Byte], // msg_name
    socklen_t, // msg_namelen
    Ptr[iovec], // msg_iov
    CInt, // msg_iovlen
    Ptr[Byte], // msg_control
    socklen_t, // msg_crontrollen
    CInt // msg_flags
  ]
  type cmsghdr = CStruct3[
    socklen_t, // cmsg_len
    CInt, // cmsg_level
    CInt // cmsg_type
  ]
  type linger = CStruct2[
    CInt, // l_onoff
    CInt // l_linger
  ]

  type in_port_t = uint16_t
  type in_addr_t = uint32_t
  type _16 = Nat.Digit2[Nat._1, Nat._6]

  type in_addr = CStruct1[in_addr_t] // s_addr
  type sockaddr_in = CStruct3[
    sa_family_t, // sin_family
    in_port_t, // sin_port
    in_addr // sin_addr
  ]

  type in6_addr = CStruct1[CArray[uint8_t, _16]] // s6_addr
  type sockaddr_in6 = CStruct5[
    in6_addr, // sin6_addr
    sa_family_t, // sin6_family
    in_port_t, // sin6_port
    uint32_t, // sin6_flowinfo
    uint32_t // sin6_scope_id
  ]

  type ipv6_mreq = CStruct2[
    in6_addr, // ipv6mr_multiaddr
    CUnsignedInt // ipv6mr_interface
  ]

  @Test def socketSize(): Unit = {
    assertTrue(tagof[uint8_t].size == sizeof[uint8_t])
    assertTrue(tagof[uint16_t].size == sizeof[uint16_t])
    assertTrue(tagof[uint32_t].size == sizeof[uint32_t])
    assertTrue(tagof[uint64_t].size == sizeof[uint64_t])
    assertTrue(tagof[iovec].size == sizeof[iovec])
    assertTrue(tagof[socklen_t].size == sizeof[socklen_t])
    assertTrue(tagof[sa_family_t].size == sizeof[sa_family_t])
    assertTrue(tagof[sockaddr].size == sizeof[sockaddr])
    assertTrue(tagof[sockaddr_storage].size == sizeof[sockaddr_storage])
    assertTrue(tagof[msghdr].size == sizeof[msghdr])
    assertTrue(tagof[cmsghdr].size == sizeof[cmsghdr])
    assertTrue(tagof[linger].size == sizeof[linger])
    assertTrue(tagof[in_port_t].size == sizeof[in_port_t])
    assertTrue(tagof[in_addr_t].size == sizeof[in_addr_t])
    assertTrue(tagof[in_addr].size == sizeof[in_addr])
    assertTrue(tagof[sockaddr_in].size == sizeof[sockaddr_in])
    assertTrue(tagof[in6_addr].size == sizeof[in6_addr])
    assertTrue(tagof[sockaddr_in6].size == sizeof[sockaddr_in6])
    assertTrue(tagof[ipv6_mreq].size == sizeof[ipv6_mreq])
  }

  @Test def abstractTypeTag(): Unit = {
    // https://github.com/scala-native/scala-native/issues/3196
    val PtrAnyClassTag = Tag.Ptr(Tag.Class(classOf[AnyRef]))
    object abstractTagWrapper {
      type Foo
    }
    assertEquals(PtrAnyClassTag, tagof[Ptr[abstractTagWrapper.Foo]])
    assertEquals(PtrAnyClassTag, tagof[Ptr[_]])
    assertEquals(
      Tag.Ptr(PtrAnyClassTag),
      tagof[Ptr[Ptr[abstractTagWrapper.Foo]]]
    )
  }
}
