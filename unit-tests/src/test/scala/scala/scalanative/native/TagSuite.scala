package scala.scalanative.native

object TagSuite extends tests.Suite {

  test("tag size") {
    assert(tagof[Ptr[_]].size == 8)
    assert(tagof[Object].size == 8)
    assert(tagof[Array[_]].size == 8)
    assert(tagof[Unit].size == 8)
    assert(tagof[Boolean].size == 1)
    assert(tagof[Char].size == 2)
    assert(tagof[Byte].size == 1)
    assert(tagof[UByte].size == 1)
    assert(tagof[Short].size == 2)
    assert(tagof[UShort].size == 2)
    assert(tagof[Int].size == 4)
    assert(tagof[UInt].size == 4)
    assert(tagof[Long].size == 8)
    assert(tagof[ULong].size == 8)
    assert(tagof[Float].size == 4)
    assert(tagof[Double].size == 8)
    assert(tagof[CArray[Int, Nat._0]].size == 0)
    assert(tagof[CArray[Int, Nat._3]].size == 4 * 3)
    assert(tagof[CArray[Int, Nat._9]].size == 4 * 9)
    assert(tagof[CStruct0].size == 0)
    assert(tagof[CStruct1[Int]].size == 4)
    assert(tagof[CStruct2[Byte, Int]].size == 8)
    assert(tagof[CStruct3[Byte, Byte, Int]].size == 8)
  }

  test("tag size should be consistent with sizeof") {
    assert(tagof[Ptr[_]].size == sizeof[Ptr[_]])
    assert(tagof[Object].size == sizeof[Object])
    assert(tagof[Array[_]].size == sizeof[Array[_]])
    assert(tagof[Unit].size == sizeof[Unit])
    assert(tagof[Boolean].size == sizeof[Boolean])
    assert(tagof[Char].size == sizeof[Char])
    assert(tagof[Byte].size == sizeof[Byte])
    assert(tagof[UByte].size == sizeof[UByte])
    assert(tagof[Short].size == sizeof[Short])
    assert(tagof[UShort].size == sizeof[UShort])
    assert(tagof[Int].size == sizeof[Int])
    assert(tagof[UInt].size == sizeof[UInt])
    assert(tagof[Long].size == sizeof[Long])
    assert(tagof[ULong].size == sizeof[ULong])
    assert(tagof[Float].size == sizeof[Float])
    assert(tagof[Double].size == sizeof[Double])
    assert(tagof[CArray[Int, Nat._0]].size == sizeof[CArray[Int, Nat._0]])
    assert(tagof[CArray[Int, Nat._3]].size == sizeof[CArray[Int, Nat._3]])
    assert(tagof[CArray[Int, Nat._9]].size == sizeof[CArray[Int, Nat._9]])
    assert(tagof[CStruct0].size == sizeof[CStruct0])
    assert(tagof[CStruct1[Int]].size == sizeof[CStruct1[Int]])
    assert(tagof[CStruct2[Byte, Int]].size == sizeof[CStruct2[Byte, Int]])
    assert(
      tagof[CStruct3[Byte, Byte, Int]].size == sizeof[CStruct3[Byte,
                                                               Byte,
                                                               Int]])
  }

  test("tag alignment") {
    assert(tagof[Ptr[_]].alignment == 8)
    assert(tagof[Object].alignment == 8)
    assert(tagof[Array[_]].alignment == 8)
    assert(tagof[Unit].alignment == 8)
    assert(tagof[Boolean].alignment == 1)
    assert(tagof[Char].alignment == 2)
    assert(tagof[Byte].alignment == 1)
    assert(tagof[UByte].alignment == 1)
    assert(tagof[Short].alignment == 2)
    assert(tagof[UShort].alignment == 2)
    assert(tagof[Int].alignment == 4)
    assert(tagof[UInt].alignment == 4)
    assert(tagof[Long].alignment == 8)
    assert(tagof[ULong].alignment == 8)
    assert(tagof[Float].alignment == 4)
    assert(tagof[Double].alignment == 8)
    assert(tagof[CArray[Int, Nat._0]].alignment == 4)
    assert(tagof[CArray[Int, Nat._3]].alignment == 4)
    assert(tagof[CArray[Int, Nat._9]].alignment == 4)
    assert(tagof[CStruct0].alignment == 1)
    assert(tagof[CStruct1[Int]].alignment == 4)
    assert(tagof[CStruct2[Byte, Int]].alignment == 4)
    assert(tagof[CStruct3[Byte, Byte, Int]].alignment == 4)
  }

  test("tag alignment should be consistent with alignmentof") {
    assert(tagof[Ptr[_]].alignment == alignmentof[Ptr[_]])
    assert(tagof[Object].alignment == alignmentof[Object])
    assert(tagof[Array[_]].alignment == alignmentof[Array[_]])
    assert(tagof[Unit].alignment == alignmentof[Unit])
    assert(tagof[Boolean].alignment == alignmentof[Boolean])
    assert(tagof[Char].alignment == alignmentof[Char])
    assert(tagof[Byte].alignment == alignmentof[Byte])
    assert(tagof[UByte].alignment == alignmentof[UByte])
    assert(tagof[Short].alignment == alignmentof[Short])
    assert(tagof[UShort].alignment == alignmentof[UShort])
    assert(tagof[Int].alignment == alignmentof[Int])
    assert(tagof[UInt].alignment == alignmentof[UInt])
    assert(tagof[Long].alignment == alignmentof[Long])
    assert(tagof[ULong].alignment == alignmentof[ULong])
    assert(tagof[Float].alignment == alignmentof[Float])
    assert(tagof[Double].alignment == alignmentof[Double])
    assert(
      tagof[CArray[Int, Nat._0]].alignment == alignmentof[CArray[Int, Nat._0]])
    assert(
      tagof[CArray[Int, Nat._3]].alignment == alignmentof[CArray[Int, Nat._3]])
    assert(
      tagof[CArray[Int, Nat._9]].alignment == alignmentof[CArray[Int, Nat._9]])
    assert(tagof[CStruct0].alignment == alignmentof[CStruct0])
    assert(tagof[CStruct1[Int]].alignment == alignmentof[CStruct1[Int]])
    assert(
      tagof[CStruct2[Byte, Int]].alignment == alignmentof[CStruct2[Byte, Int]])
    assert(
      tagof[CStruct3[Byte, Byte, Int]].alignment == alignmentof[CStruct3[Byte,
                                                                         Byte,
                                                                         Int]])
  }

  test("tag offset") {
    assert(tagof[CArray[Byte, Nat._0]].offset(0) == 0)
    assert(tagof[CArray[Byte, Nat._0]].offset(1) == 1)
    assert(tagof[CArray[Byte, Nat._0]].offset(42) == 42)
    assert(tagof[CArray[Int, Nat._0]].offset(0) == 0)
    assert(tagof[CArray[Int, Nat._0]].offset(1) == 4)
    assert(tagof[CArray[Int, Nat._0]].offset(42) == 4 * 42)
    assert(tagof[CStruct1[Int]].offset(0) == 0)
    assert(tagof[CStruct2[Byte, Int]].offset(0) == 0)
    assert(tagof[CStruct2[Byte, Int]].offset(1) == 4)
    assert(tagof[CStruct3[Byte, Byte, Int]].offset(0) == 0)
    assert(tagof[CStruct3[Byte, Byte, Int]].offset(1) == 1)
    assert(tagof[CStruct3[Byte, Byte, Int]].offset(2) == 4)
    assert(tagof[CStruct2[Byte, CStruct2[Byte, Int]]].offset(0) == 0)
    assert(tagof[CStruct2[Byte, CStruct2[Byte, Int]]].offset(1) == 4)
  }

  type uint8_t  = UByte
  type uint16_t = UShort
  type uint32_t = UInt
  type uint64_t = ULong

  type iovec = CStruct2[Ptr[Byte], // iov_base
                        CSize] // iov_len

  type socklen_t   = CUnsignedInt
  type sa_family_t = CUnsignedShort
  type _14         = Nat.Digit[Nat._1, Nat._4]
  type sockaddr =
    CStruct2[sa_family_t, // sa_family
             CArray[CChar, _14]] // sa_data, size = 14 in OS X and Linux
  type sockaddr_storage = CStruct1[sa_family_t] // ss_family
  type msghdr = CStruct7[Ptr[Byte], // msg_name
                         socklen_t, // msg_namelen
                         Ptr[iovec], // msg_iov
                         CInt, // msg_iovlen
                         Ptr[Byte], // msg_control
                         socklen_t, // msg_crontrollen
                         CInt] // msg_flags
  type cmsghdr = CStruct3[socklen_t, // cmsg_len
                          CInt, // cmsg_level
                          CInt] // cmsg_type
  type linger = CStruct2[CInt, // l_onoff
                         CInt] // l_linger

  type in_port_t = uint16_t
  type in_addr_t = uint32_t
  type _16       = Nat.Digit[Nat._1, Nat._6]

  type in_addr = CStruct1[in_addr_t] // s_addr
  type sockaddr_in = CStruct3[sa_family_t, // sin_family
                              in_port_t, // sin_port
                              in_addr] // sin_addr

  type in6_addr = CStruct1[CArray[uint8_t, _16]] // s6_addr
  type sockaddr_in6 = CStruct5[in6_addr, // sin6_addr
                               sa_family_t, // sin6_family
                               in_port_t, // sin6_port
                               uint32_t, // sin6_flowinfo
                               uint32_t] // sin6_scope_id

  type ipv6_mreq = CStruct2[in6_addr, // ipv6mr_multiaddr
                            CUnsignedInt] // ipv6mr_interface

  test("socket size") {
    assert(tagof[uint8_t].size == sizeof[uint8_t])
    assert(tagof[uint16_t].size == sizeof[uint16_t])
    assert(tagof[uint32_t].size == sizeof[uint32_t])
    assert(tagof[uint64_t].size == sizeof[uint64_t])
    assert(tagof[iovec].size == sizeof[iovec])
    assert(tagof[socklen_t].size == sizeof[socklen_t])
    assert(tagof[sa_family_t].size == sizeof[sa_family_t])
    assert(tagof[sockaddr].size == sizeof[sockaddr])
    assert(tagof[sockaddr_storage].size == sizeof[sockaddr_storage])
    assert(tagof[msghdr].size == sizeof[msghdr])
    assert(tagof[cmsghdr].size == sizeof[cmsghdr])
    assert(tagof[linger].size == sizeof[linger])
    assert(tagof[in_port_t].size == sizeof[in_port_t])
    assert(tagof[in_addr_t].size == sizeof[in_addr_t])
    assert(tagof[in_addr].size == sizeof[in_addr])
    assert(tagof[sockaddr_in].size == sizeof[sockaddr_in])
    assert(tagof[in6_addr].size == sizeof[in6_addr])
    assert(tagof[sockaddr_in6].size == sizeof[sockaddr_in6])
    assert(tagof[ipv6_mreq].size == sizeof[ipv6_mreq])
  }
}
