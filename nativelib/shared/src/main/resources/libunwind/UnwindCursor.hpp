//===------------------------- UnwindCursor.hpp ---------------------------===//
//
//                     The LLVM Compiler Infrastructure
//
// This file is dual licensed under the MIT and the University of Illinois Open
// Source Licenses. See LICENSE.TXT for details.
//
//
// C++ interface to lower levels of libunwind
//===----------------------------------------------------------------------===//

#ifndef __UNWINDCURSOR_HPP__
#define __UNWINDCURSOR_HPP__

#include <algorithm>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <unwind.h>

#ifdef __APPLE__
  #include <mach-o/dyld.h>
#endif

#include "config.h"

#include "AddressSpace.hpp"
#include "CompactUnwinder.hpp"
#include "config.h"
#include "DwarfInstructions.hpp"
#include "EHHeaderParser.hpp"
#include "libunwind.h"
#include "Registers.hpp"
#include "RWMutex.hpp"
#include "Unwind-EHABI.h"

namespace libunwind {

#if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
/// Cache of recently found FDEs.
template <typename A>
class _LIBUNWIND_HIDDEN DwarfFDECache {
  typedef typename A::pint_t pint_t;
public:
  static pint_t findFDE(pint_t mh, pint_t pc);
  static void add(pint_t mh, pint_t ip_start, pint_t ip_end, pint_t fde);
  static void removeAllIn(pint_t mh);
  static void iterateCacheEntries(void (*func)(unw_word_t ip_start,
                                               unw_word_t ip_end,
                                               unw_word_t fde, unw_word_t mh));

private:

  struct entry {
    pint_t mh;
    pint_t ip_start;
    pint_t ip_end;
    pint_t fde;
  };

  // These fields are all static to avoid needing an initializer.
  // There is only one instance of this class per process.
  static RWMutex _lock;
#ifdef __APPLE__
  static void dyldUnloadHook(const struct mach_header *mh, intptr_t slide);
  static bool _registeredForDyldUnloads;
#endif
  // Can't use std::vector<> here because this code is below libc++.
  static entry *_buffer;
  static entry *_bufferUsed;
  static entry *_bufferEnd;
  static entry _initialBuffer[64];
};

template <typename A>
typename DwarfFDECache<A>::entry *
DwarfFDECache<A>::_buffer = _initialBuffer;

template <typename A>
typename DwarfFDECache<A>::entry *
DwarfFDECache<A>::_bufferUsed = _initialBuffer;

template <typename A>
typename DwarfFDECache<A>::entry *
DwarfFDECache<A>::_bufferEnd = &_initialBuffer[64];

template <typename A>
typename DwarfFDECache<A>::entry DwarfFDECache<A>::_initialBuffer[64];

template <typename A>
RWMutex DwarfFDECache<A>::_lock;

#ifdef __APPLE__
template <typename A>
bool DwarfFDECache<A>::_registeredForDyldUnloads = false;
#endif

template <typename A>
typename A::pint_t DwarfFDECache<A>::findFDE(pint_t mh, pint_t pc) {
  pint_t result = 0;
  _LIBUNWIND_LOG_IF_FALSE(_lock.lock_shared());
  for (entry *p = _buffer; p < _bufferUsed; ++p) {
    if ((mh == p->mh) || (mh == 0)) {
      if ((p->ip_start <= pc) && (pc < p->ip_end)) {
        result = p->fde;
        break;
      }
    }
  }
  _LIBUNWIND_LOG_IF_FALSE(_lock.unlock_shared());
  return result;
}

template <typename A>
void DwarfFDECache<A>::add(pint_t mh, pint_t ip_start, pint_t ip_end,
                           pint_t fde) {
#if !defined(_LIBUNWIND_NO_HEAP)
  _LIBUNWIND_LOG_IF_FALSE(_lock.lock());
  if (_bufferUsed >= _bufferEnd) {
    size_t oldSize = (size_t)(_bufferEnd - _buffer);
    size_t newSize = oldSize * 4;
    // Can't use operator new (we are below it).
    entry *newBuffer = (entry *)malloc(newSize * sizeof(entry));
    memcpy(newBuffer, _buffer, oldSize * sizeof(entry));
    if (_buffer != _initialBuffer)
      free(_buffer);
    _buffer = newBuffer;
    _bufferUsed = &newBuffer[oldSize];
    _bufferEnd = &newBuffer[newSize];
  }
  _bufferUsed->mh = mh;
  _bufferUsed->ip_start = ip_start;
  _bufferUsed->ip_end = ip_end;
  _bufferUsed->fde = fde;
  ++_bufferUsed;
#ifdef __APPLE__
  if (!_registeredForDyldUnloads) {
    _dyld_register_func_for_remove_image(&dyldUnloadHook);
    _registeredForDyldUnloads = true;
  }
#endif
  _LIBUNWIND_LOG_IF_FALSE(_lock.unlock());
#endif
}

template <typename A>
void DwarfFDECache<A>::removeAllIn(pint_t mh) {
  _LIBUNWIND_LOG_IF_FALSE(_lock.lock());
  entry *d = _buffer;
  for (const entry *s = _buffer; s < _bufferUsed; ++s) {
    if (s->mh != mh) {
      if (d != s)
        *d = *s;
      ++d;
    }
  }
  _bufferUsed = d;
  _LIBUNWIND_LOG_IF_FALSE(_lock.unlock());
}

#ifdef __APPLE__
template <typename A>
void DwarfFDECache<A>::dyldUnloadHook(const struct mach_header *mh, intptr_t ) {
  removeAllIn((pint_t) mh);
}
#endif

template <typename A>
void DwarfFDECache<A>::iterateCacheEntries(void (*func)(
    unw_word_t ip_start, unw_word_t ip_end, unw_word_t fde, unw_word_t mh)) {
  _LIBUNWIND_LOG_IF_FALSE(_lock.lock());
  for (entry *p = _buffer; p < _bufferUsed; ++p) {
    (*func)(p->ip_start, p->ip_end, p->fde, p->mh);
  }
  _LIBUNWIND_LOG_IF_FALSE(_lock.unlock());
}
#endif // defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)


#define arrayoffsetof(type, index, field) ((size_t)(&((type *)0)[index].field))

#if defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)
template <typename A> class UnwindSectionHeader {
public:
  UnwindSectionHeader(A &addressSpace, typename A::pint_t addr)
      : _addressSpace(addressSpace), _addr(addr) {}

  uint32_t version() const {
    return _addressSpace.get32(_addr +
                               offsetof(unwind_info_section_header, version));
  }
  uint32_t commonEncodingsArraySectionOffset() const {
    return _addressSpace.get32(_addr +
                               offsetof(unwind_info_section_header,
                                        commonEncodingsArraySectionOffset));
  }
  uint32_t commonEncodingsArrayCount() const {
    return _addressSpace.get32(_addr + offsetof(unwind_info_section_header,
                                                commonEncodingsArrayCount));
  }
  uint32_t personalityArraySectionOffset() const {
    return _addressSpace.get32(_addr + offsetof(unwind_info_section_header,
                                                personalityArraySectionOffset));
  }
  uint32_t personalityArrayCount() const {
    return _addressSpace.get32(
        _addr + offsetof(unwind_info_section_header, personalityArrayCount));
  }
  uint32_t indexSectionOffset() const {
    return _addressSpace.get32(
        _addr + offsetof(unwind_info_section_header, indexSectionOffset));
  }
  uint32_t indexCount() const {
    return _addressSpace.get32(
        _addr + offsetof(unwind_info_section_header, indexCount));
  }

private:
  A                     &_addressSpace;
  typename A::pint_t     _addr;
};

template <typename A> class UnwindSectionIndexArray {
public:
  UnwindSectionIndexArray(A &addressSpace, typename A::pint_t addr)
      : _addressSpace(addressSpace), _addr(addr) {}

  uint32_t functionOffset(uint32_t index) const {
    return _addressSpace.get32(
        _addr + arrayoffsetof(unwind_info_section_header_index_entry, index,
                              functionOffset));
  }
  uint32_t secondLevelPagesSectionOffset(uint32_t index) const {
    return _addressSpace.get32(
        _addr + arrayoffsetof(unwind_info_section_header_index_entry, index,
                              secondLevelPagesSectionOffset));
  }
  uint32_t lsdaIndexArraySectionOffset(uint32_t index) const {
    return _addressSpace.get32(
        _addr + arrayoffsetof(unwind_info_section_header_index_entry, index,
                              lsdaIndexArraySectionOffset));
  }

private:
  A                   &_addressSpace;
  typename A::pint_t   _addr;
};

template <typename A> class UnwindSectionRegularPageHeader {
public:
  UnwindSectionRegularPageHeader(A &addressSpace, typename A::pint_t addr)
      : _addressSpace(addressSpace), _addr(addr) {}

  uint32_t kind() const {
    return _addressSpace.get32(
        _addr + offsetof(unwind_info_regular_second_level_page_header, kind));
  }
  uint16_t entryPageOffset() const {
    return _addressSpace.get16(
        _addr + offsetof(unwind_info_regular_second_level_page_header,
                         entryPageOffset));
  }
  uint16_t entryCount() const {
    return _addressSpace.get16(
        _addr +
        offsetof(unwind_info_regular_second_level_page_header, entryCount));
  }

private:
  A &_addressSpace;
  typename A::pint_t _addr;
};

template <typename A> class UnwindSectionRegularArray {
public:
  UnwindSectionRegularArray(A &addressSpace, typename A::pint_t addr)
      : _addressSpace(addressSpace), _addr(addr) {}

  uint32_t functionOffset(uint32_t index) const {
    return _addressSpace.get32(
        _addr + arrayoffsetof(unwind_info_regular_second_level_entry, index,
                              functionOffset));
  }
  uint32_t encoding(uint32_t index) const {
    return _addressSpace.get32(
        _addr +
        arrayoffsetof(unwind_info_regular_second_level_entry, index, encoding));
  }

private:
  A &_addressSpace;
  typename A::pint_t _addr;
};

template <typename A> class UnwindSectionCompressedPageHeader {
public:
  UnwindSectionCompressedPageHeader(A &addressSpace, typename A::pint_t addr)
      : _addressSpace(addressSpace), _addr(addr) {}

  uint32_t kind() const {
    return _addressSpace.get32(
        _addr +
        offsetof(unwind_info_compressed_second_level_page_header, kind));
  }
  uint16_t entryPageOffset() const {
    return _addressSpace.get16(
        _addr + offsetof(unwind_info_compressed_second_level_page_header,
                         entryPageOffset));
  }
  uint16_t entryCount() const {
    return _addressSpace.get16(
        _addr +
        offsetof(unwind_info_compressed_second_level_page_header, entryCount));
  }
  uint16_t encodingsPageOffset() const {
    return _addressSpace.get16(
        _addr + offsetof(unwind_info_compressed_second_level_page_header,
                         encodingsPageOffset));
  }
  uint16_t encodingsCount() const {
    return _addressSpace.get16(
        _addr + offsetof(unwind_info_compressed_second_level_page_header,
                         encodingsCount));
  }

private:
  A &_addressSpace;
  typename A::pint_t _addr;
};

template <typename A> class UnwindSectionCompressedArray {
public:
  UnwindSectionCompressedArray(A &addressSpace, typename A::pint_t addr)
      : _addressSpace(addressSpace), _addr(addr) {}

  uint32_t functionOffset(uint32_t index) const {
    return UNWIND_INFO_COMPRESSED_ENTRY_FUNC_OFFSET(
        _addressSpace.get32(_addr + index * sizeof(uint32_t)));
  }
  uint16_t encodingIndex(uint32_t index) const {
    return UNWIND_INFO_COMPRESSED_ENTRY_ENCODING_INDEX(
        _addressSpace.get32(_addr + index * sizeof(uint32_t)));
  }

private:
  A &_addressSpace;
  typename A::pint_t _addr;
};

template <typename A> class UnwindSectionLsdaArray {
public:
  UnwindSectionLsdaArray(A &addressSpace, typename A::pint_t addr)
      : _addressSpace(addressSpace), _addr(addr) {}

  uint32_t functionOffset(uint32_t index) const {
    return _addressSpace.get32(
        _addr + arrayoffsetof(unwind_info_section_header_lsda_index_entry,
                              index, functionOffset));
  }
  uint32_t lsdaOffset(uint32_t index) const {
    return _addressSpace.get32(
        _addr + arrayoffsetof(unwind_info_section_header_lsda_index_entry,
                              index, lsdaOffset));
  }

private:
  A                   &_addressSpace;
  typename A::pint_t   _addr;
};
#endif // defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)

class _LIBUNWIND_HIDDEN AbstractUnwindCursor {
public:
  // NOTE: provide a class specific placement deallocation function (S5.3.4 p20)
  // This avoids an unnecessary dependency to libc++abi.
  void operator delete(void *, size_t) {}

  virtual ~AbstractUnwindCursor() {}
  virtual bool validReg(int) { _LIBUNWIND_ABORT("validReg not implemented"); }
  virtual unw_word_t getReg(int) { _LIBUNWIND_ABORT("getReg not implemented"); }
  virtual void setReg(int, unw_word_t) {
    _LIBUNWIND_ABORT("setReg not implemented");
  }
  virtual bool validFloatReg(int) {
    _LIBUNWIND_ABORT("validFloatReg not implemented");
  }
  virtual unw_fpreg_t getFloatReg(int) {
    _LIBUNWIND_ABORT("getFloatReg not implemented");
  }
  virtual void setFloatReg(int, unw_fpreg_t) {
    _LIBUNWIND_ABORT("setFloatReg not implemented");
  }
  virtual int step() { _LIBUNWIND_ABORT("step not implemented"); }
  virtual void getInfo(unw_proc_info_t *) {
    _LIBUNWIND_ABORT("getInfo not implemented");
  }
  virtual void jumpto() { _LIBUNWIND_ABORT("jumpto not implemented"); }
  virtual bool isSignalFrame() {
    _LIBUNWIND_ABORT("isSignalFrame not implemented");
  }
  virtual bool getFunctionName(char *, size_t, unw_word_t *) {
    _LIBUNWIND_ABORT("getFunctionName not implemented");
  }
  virtual void setInfoBasedOnIPRegister(bool = false) {
    _LIBUNWIND_ABORT("setInfoBasedOnIPRegister not implemented");
  }
  virtual const char *getRegisterName(int) {
    _LIBUNWIND_ABORT("getRegisterName not implemented");
  }
#ifdef __arm__
  virtual void saveVFPAsX() { _LIBUNWIND_ABORT("saveVFPAsX not implemented"); }
#endif
};

/// UnwindCursor contains all state (including all register values) during
/// an unwind.  This is normally stack allocated inside a unw_cursor_t.
template <typename A, typename R>
class UnwindCursor : public AbstractUnwindCursor{
  typedef typename A::pint_t pint_t;
public:
                      UnwindCursor(unw_context_t *context, A &as);
                      UnwindCursor(A &as, void *threadArg);
  virtual             ~UnwindCursor() {}
  virtual bool        validReg(int);
  virtual unw_word_t  getReg(int);
  virtual void        setReg(int, unw_word_t);
  virtual bool        validFloatReg(int);
  virtual unw_fpreg_t getFloatReg(int);
  virtual void        setFloatReg(int, unw_fpreg_t);
  virtual int         step();
  virtual void        getInfo(unw_proc_info_t *);
  virtual void        jumpto();
  virtual bool        isSignalFrame();
  virtual bool        getFunctionName(char *buf, size_t len, unw_word_t *off);
  virtual void        setInfoBasedOnIPRegister(bool isReturnAddress = false);
  virtual const char *getRegisterName(int num);
#ifdef __arm__
  virtual void        saveVFPAsX();
#endif

private:

#if defined(_LIBUNWIND_ARM_EHABI)
  bool getInfoFromEHABISection(pint_t pc, const UnwindInfoSections &sects);

  int stepWithEHABI() {
    size_t len = 0;
    size_t off = 0;
    // FIXME: Calling decode_eht_entry() here is violating the libunwind
    // abstraction layer.
    const uint32_t *ehtp =
        decode_eht_entry(reinterpret_cast<const uint32_t *>(_info.unwind_info),
                         &off, &len);
    if (_Unwind_VRS_Interpret((_Unwind_Context *)this, ehtp, off, len) !=
            _URC_CONTINUE_UNWIND)
      return UNW_STEP_END;
    return UNW_STEP_SUCCESS;
  }
#endif

#if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
  bool getInfoFromDwarfSection(pint_t pc, const UnwindInfoSections &sects,
                                            uint32_t fdeSectionOffsetHint=0);
  int stepWithDwarfFDE() {
    return DwarfInstructions<A, R>::stepWithDwarf(_addressSpace,
                                              (pint_t)this->getReg(UNW_REG_IP),
                                              (pint_t)_info.unwind_info,
                                              _registers);
  }
#endif

#if defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)
  bool getInfoFromCompactEncodingSection(pint_t pc,
                                            const UnwindInfoSections &sects);
  int stepWithCompactEncoding() {
  #if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
    if ( compactSaysUseDwarf() )
      return stepWithDwarfFDE();
  #endif
    R dummy;
    return stepWithCompactEncoding(dummy);
  }

#if defined(_LIBUNWIND_TARGET_X86_64)
  int stepWithCompactEncoding(Registers_x86_64 &) {
    return CompactUnwinder_x86_64<A>::stepWithCompactEncoding(
        _info.format, _info.start_ip, _addressSpace, _registers);
  }
#endif

#if defined(_LIBUNWIND_TARGET_I386)
  int stepWithCompactEncoding(Registers_x86 &) {
    return CompactUnwinder_x86<A>::stepWithCompactEncoding(
        _info.format, (uint32_t)_info.start_ip, _addressSpace, _registers);
  }
#endif

#if defined(_LIBUNWIND_TARGET_PPC)
  int stepWithCompactEncoding(Registers_ppc &) {
    return UNW_EINVAL;
  }
#endif

#if defined(_LIBUNWIND_TARGET_PPC64)
  int stepWithCompactEncoding(Registers_ppc64 &) {
    return UNW_EINVAL;
  }
#endif


#if defined(_LIBUNWIND_TARGET_AARCH64)
  int stepWithCompactEncoding(Registers_arm64 &) {
    return CompactUnwinder_arm64<A>::stepWithCompactEncoding(
        _info.format, _info.start_ip, _addressSpace, _registers);
  }
#endif

#if defined(_LIBUNWIND_TARGET_MIPS_O32)
  int stepWithCompactEncoding(Registers_mips_o32 &) {
    return UNW_EINVAL;
  }
#endif

#if defined(_LIBUNWIND_TARGET_MIPS_NEWABI)
  int stepWithCompactEncoding(Registers_mips_newabi &) {
    return UNW_EINVAL;
  }
#endif

  bool compactSaysUseDwarf(uint32_t *offset=NULL) const {
    R dummy;
    return compactSaysUseDwarf(dummy, offset);
  }

#if defined(_LIBUNWIND_TARGET_X86_64)
  bool compactSaysUseDwarf(Registers_x86_64 &, uint32_t *offset) const {
    if ((_info.format & UNWIND_X86_64_MODE_MASK) == UNWIND_X86_64_MODE_DWARF) {
      if (offset)
        *offset = (_info.format & UNWIND_X86_64_DWARF_SECTION_OFFSET);
      return true;
    }
    return false;
  }
#endif

#if defined(_LIBUNWIND_TARGET_I386)
  bool compactSaysUseDwarf(Registers_x86 &, uint32_t *offset) const {
    if ((_info.format & UNWIND_X86_MODE_MASK) == UNWIND_X86_MODE_DWARF) {
      if (offset)
        *offset = (_info.format & UNWIND_X86_DWARF_SECTION_OFFSET);
      return true;
    }
    return false;
  }
#endif

#if defined(_LIBUNWIND_TARGET_PPC)
  bool compactSaysUseDwarf(Registers_ppc &, uint32_t *) const {
    return true;
  }
#endif

#if defined(_LIBUNWIND_TARGET_PPC64)
  bool compactSaysUseDwarf(Registers_ppc64 &, uint32_t *) const {
    return true;
  }
#endif

#if defined(_LIBUNWIND_TARGET_AARCH64)
  bool compactSaysUseDwarf(Registers_arm64 &, uint32_t *offset) const {
    if ((_info.format & UNWIND_ARM64_MODE_MASK) == UNWIND_ARM64_MODE_DWARF) {
      if (offset)
        *offset = (_info.format & UNWIND_ARM64_DWARF_SECTION_OFFSET);
      return true;
    }
    return false;
  }
#endif

#if defined(_LIBUNWIND_TARGET_MIPS_O32)
  bool compactSaysUseDwarf(Registers_mips_o32 &, uint32_t *) const {
    return true;
  }
#endif

#if defined(_LIBUNWIND_TARGET_MIPS_NEWABI)
  bool compactSaysUseDwarf(Registers_mips_newabi &, uint32_t *) const {
    return true;
  }
#endif
#endif // defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)

#if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
  compact_unwind_encoding_t dwarfEncoding() const {
    R dummy;
    return dwarfEncoding(dummy);
  }

#if defined(_LIBUNWIND_TARGET_X86_64)
  compact_unwind_encoding_t dwarfEncoding(Registers_x86_64 &) const {
    return UNWIND_X86_64_MODE_DWARF;
  }
#endif

#if defined(_LIBUNWIND_TARGET_I386)
  compact_unwind_encoding_t dwarfEncoding(Registers_x86 &) const {
    return UNWIND_X86_MODE_DWARF;
  }
#endif

#if defined(_LIBUNWIND_TARGET_PPC)
  compact_unwind_encoding_t dwarfEncoding(Registers_ppc &) const {
    return 0;
  }
#endif

#if defined(_LIBUNWIND_TARGET_PPC64)
  compact_unwind_encoding_t dwarfEncoding(Registers_ppc64 &) const {
    return 0;
  }
#endif

#if defined(_LIBUNWIND_TARGET_AARCH64)
  compact_unwind_encoding_t dwarfEncoding(Registers_arm64 &) const {
    return UNWIND_ARM64_MODE_DWARF;
  }
#endif

#if defined(_LIBUNWIND_TARGET_ARM)
  compact_unwind_encoding_t dwarfEncoding(Registers_arm &) const {
    return 0;
  }
#endif

#if defined (_LIBUNWIND_TARGET_OR1K)
  compact_unwind_encoding_t dwarfEncoding(Registers_or1k &) const {
    return 0;
  }
#endif

#if defined (_LIBUNWIND_TARGET_MIPS_O32)
  compact_unwind_encoding_t dwarfEncoding(Registers_mips_o32 &) const {
    return 0;
  }
#endif

#if defined (_LIBUNWIND_TARGET_MIPS_NEWABI)
  compact_unwind_encoding_t dwarfEncoding(Registers_mips_newabi &) const {
    return 0;
  }
#endif
#endif // defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)


  A               &_addressSpace;
  R                _registers;
  unw_proc_info_t  _info;
  bool             _unwindInfoMissing;
  bool             _isSignalFrame;
};


template <typename A, typename R>
UnwindCursor<A, R>::UnwindCursor(unw_context_t *context, A &as)
    : _addressSpace(as), _registers(context), _unwindInfoMissing(false),
      _isSignalFrame(false) {
  static_assert((check_fit<UnwindCursor<A, R>, unw_cursor_t>::does_fit),
                "UnwindCursor<> does not fit in unw_cursor_t");
  memset(&_info, 0, sizeof(_info));
}

template <typename A, typename R>
UnwindCursor<A, R>::UnwindCursor(A &as, void *)
    : _addressSpace(as), _unwindInfoMissing(false), _isSignalFrame(false) {
  memset(&_info, 0, sizeof(_info));
  // FIXME
  // fill in _registers from thread arg
}


template <typename A, typename R>
bool UnwindCursor<A, R>::validReg(int regNum) {
  return _registers.validRegister(regNum);
}

template <typename A, typename R>
unw_word_t UnwindCursor<A, R>::getReg(int regNum) {
  return _registers.getRegister(regNum);
}

template <typename A, typename R>
void UnwindCursor<A, R>::setReg(int regNum, unw_word_t value) {
  _registers.setRegister(regNum, (typename A::pint_t)value);
}

template <typename A, typename R>
bool UnwindCursor<A, R>::validFloatReg(int regNum) {
  return _registers.validFloatRegister(regNum);
}

template <typename A, typename R>
unw_fpreg_t UnwindCursor<A, R>::getFloatReg(int regNum) {
  return _registers.getFloatRegister(regNum);
}

template <typename A, typename R>
void UnwindCursor<A, R>::setFloatReg(int regNum, unw_fpreg_t value) {
  _registers.setFloatRegister(regNum, value);
}

template <typename A, typename R> void UnwindCursor<A, R>::jumpto() {
  _registers.jumpto();
}

#ifdef __arm__
template <typename A, typename R> void UnwindCursor<A, R>::saveVFPAsX() {
  _registers.saveVFPAsX();
}
#endif

template <typename A, typename R>
const char *UnwindCursor<A, R>::getRegisterName(int regNum) {
  return _registers.getRegisterName(regNum);
}

template <typename A, typename R> bool UnwindCursor<A, R>::isSignalFrame() {
  return _isSignalFrame;
}

#if defined(_LIBUNWIND_ARM_EHABI)
struct EHABIIndexEntry {
  uint32_t functionOffset;
  uint32_t data;
};

template<typename A>
struct EHABISectionIterator {
  typedef EHABISectionIterator _Self;

  typedef std::random_access_iterator_tag iterator_category;
  typedef typename A::pint_t value_type;
  typedef typename A::pint_t* pointer;
  typedef typename A::pint_t& reference;
  typedef size_t size_type;
  typedef size_t difference_type;

  static _Self begin(A& addressSpace, const UnwindInfoSections& sects) {
    return _Self(addressSpace, sects, 0);
  }
  static _Self end(A& addressSpace, const UnwindInfoSections& sects) {
    return _Self(addressSpace, sects,
                 sects.arm_section_length / sizeof(EHABIIndexEntry));
  }

  EHABISectionIterator(A& addressSpace, const UnwindInfoSections& sects, size_t i)
      : _i(i), _addressSpace(&addressSpace), _sects(&sects) {}

  _Self& operator++() { ++_i; return *this; }
  _Self& operator+=(size_t a) { _i += a; return *this; }
  _Self& operator--() { assert(_i > 0); --_i; return *this; }
  _Self& operator-=(size_t a) { assert(_i >= a); _i -= a; return *this; }

  _Self operator+(size_t a) { _Self out = *this; out._i += a; return out; }
  _Self operator-(size_t a) { assert(_i >= a); _Self out = *this; out._i -= a; return out; }

  size_t operator-(const _Self& other) { return _i - other._i; }

  bool operator==(const _Self& other) const {
    assert(_addressSpace == other._addressSpace);
    assert(_sects == other._sects);
    return _i == other._i;
  }

  typename A::pint_t operator*() const { return functionAddress(); }

  typename A::pint_t functionAddress() const {
    typename A::pint_t indexAddr = _sects->arm_section + arrayoffsetof(
        EHABIIndexEntry, _i, functionOffset);
    return indexAddr + signExtendPrel31(_addressSpace->get32(indexAddr));
  }

  typename A::pint_t dataAddress() {
    typename A::pint_t indexAddr = _sects->arm_section + arrayoffsetof(
        EHABIIndexEntry, _i, data);
    return indexAddr;
  }

 private:
  size_t _i;
  A* _addressSpace;
  const UnwindInfoSections* _sects;
};

template <typename A, typename R>
bool UnwindCursor<A, R>::getInfoFromEHABISection(
    pint_t pc,
    const UnwindInfoSections &sects) {
  EHABISectionIterator<A> begin =
      EHABISectionIterator<A>::begin(_addressSpace, sects);
  EHABISectionIterator<A> end =
      EHABISectionIterator<A>::end(_addressSpace, sects);
  if (begin == end)
    return false;

  EHABISectionIterator<A> itNextPC = std::upper_bound(begin, end, pc);
  if (itNextPC == begin)
    return false;
  EHABISectionIterator<A> itThisPC = itNextPC - 1;

  pint_t thisPC = itThisPC.functionAddress();
  // If an exception is thrown from a function, corresponding to the last entry
  // in the table, we don't really know the function extent and have to choose a
  // value for nextPC. Choosing max() will allow the range check during trace to
  // succeed.
  pint_t nextPC = (itNextPC == end) ? std::numeric_limits<pint_t>::max()
                                    : itNextPC.functionAddress();
  pint_t indexDataAddr = itThisPC.dataAddress();

  if (indexDataAddr == 0)
    return false;

  uint32_t indexData = _addressSpace.get32(indexDataAddr);
  if (indexData == UNW_EXIDX_CANTUNWIND)
    return false;

  // If the high bit is set, the exception handling table entry is inline inside
  // the index table entry on the second word (aka |indexDataAddr|). Otherwise,
  // the table points at an offset in the exception handling table (section 5 EHABI).
  pint_t exceptionTableAddr;
  uint32_t exceptionTableData;
  bool isSingleWordEHT;
  if (indexData & 0x80000000) {
    exceptionTableAddr = indexDataAddr;
    // TODO(ajwong): Should this data be 0?
    exceptionTableData = indexData;
    isSingleWordEHT = true;
  } else {
    exceptionTableAddr = indexDataAddr + signExtendPrel31(indexData);
    exceptionTableData = _addressSpace.get32(exceptionTableAddr);
    isSingleWordEHT = false;
  }

  // Now we know the 3 things:
  //   exceptionTableAddr -- exception handler table entry.
  //   exceptionTableData -- the data inside the first word of the eht entry.
  //   isSingleWordEHT -- whether the entry is in the index.
  unw_word_t personalityRoutine = 0xbadf00d;
  bool scope32 = false;
  uintptr_t lsda;

  // If the high bit in the exception handling table entry is set, the entry is
  // in compact form (section 6.3 EHABI).
  if (exceptionTableData & 0x80000000) {
    // Grab the index of the personality routine from the compact form.
    uint32_t choice = (exceptionTableData & 0x0f000000) >> 24;
    uint32_t extraWords = 0;
    switch (choice) {
      case 0:
        personalityRoutine = (unw_word_t) &__aeabi_unwind_cpp_pr0;
        extraWords = 0;
        scope32 = false;
        lsda = isSingleWordEHT ? 0 : (exceptionTableAddr + 4);
        break;
      case 1:
        personalityRoutine = (unw_word_t) &__aeabi_unwind_cpp_pr1;
        extraWords = (exceptionTableData & 0x00ff0000) >> 16;
        scope32 = false;
        lsda = exceptionTableAddr + (extraWords + 1) * 4;
        break;
      case 2:
        personalityRoutine = (unw_word_t) &__aeabi_unwind_cpp_pr2;
        extraWords = (exceptionTableData & 0x00ff0000) >> 16;
        scope32 = true;
        lsda = exceptionTableAddr + (extraWords + 1) * 4;
        break;
      default:
        _LIBUNWIND_ABORT("unknown personality routine");
        return false;
    }

    if (isSingleWordEHT) {
      if (extraWords != 0) {
        _LIBUNWIND_ABORT("index inlined table detected but pr function "
                         "requires extra words");
        return false;
      }
    }
  } else {
    pint_t personalityAddr =
        exceptionTableAddr + signExtendPrel31(exceptionTableData);
    personalityRoutine = personalityAddr;

    // ARM EHABI # 6.2, # 9.2
    //
    //  +---- ehtp
    //  v
    // +--------------------------------------+
    // | +--------+--------+--------+-------+ |
    // | |0| prel31 to personalityRoutine   | |
    // | +--------+--------+--------+-------+ |
    // | |      N |      unwind opcodes     | |  <-- UnwindData
    // | +--------+--------+--------+-------+ |
    // | | Word 2        unwind opcodes     | |
    // | +--------+--------+--------+-------+ |
    // | ...                                  |
    // | +--------+--------+--------+-------+ |
    // | | Word N        unwind opcodes     | |
    // | +--------+--------+--------+-------+ |
    // | | LSDA                             | |  <-- lsda
    // | | ...                              | |
    // | +--------+--------+--------+-------+ |
    // +--------------------------------------+

    uint32_t *UnwindData = reinterpret_cast<uint32_t*>(exceptionTableAddr) + 1;
    uint32_t FirstDataWord = *UnwindData;
    size_t N = ((FirstDataWord >> 24) & 0xff);
    size_t NDataWords = N + 1;
    lsda = reinterpret_cast<uintptr_t>(UnwindData + NDataWords);
  }

  _info.start_ip = thisPC;
  _info.end_ip = nextPC;
  _info.handler = personalityRoutine;
  _info.unwind_info = exceptionTableAddr;
  _info.lsda = lsda;
  // flags is pr_cache.additional. See EHABI #7.2 for definition of bit 0.
  _info.flags = isSingleWordEHT ? 1 : 0 | scope32 ? 0x2 : 0;  // Use enum?

  return true;
}
#endif

#if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
template <typename A, typename R>
bool UnwindCursor<A, R>::getInfoFromDwarfSection(pint_t pc,
                                                const UnwindInfoSections &sects,
                                                uint32_t fdeSectionOffsetHint) {
  typename CFI_Parser<A>::FDE_Info fdeInfo;
  typename CFI_Parser<A>::CIE_Info cieInfo;
  bool foundFDE = false;
  bool foundInCache = false;
  // If compact encoding table gave offset into dwarf section, go directly there
  if (fdeSectionOffsetHint != 0) {
    foundFDE = CFI_Parser<A>::findFDE(_addressSpace, pc, sects.dwarf_section,
                                    (uint32_t)sects.dwarf_section_length,
                                    sects.dwarf_section + fdeSectionOffsetHint,
                                    &fdeInfo, &cieInfo);
  }
#if defined(_LIBUNWIND_SUPPORT_DWARF_INDEX)
  if (!foundFDE && (sects.dwarf_index_section != 0)) {
    foundFDE = EHHeaderParser<A>::findFDE(
        _addressSpace, pc, sects.dwarf_index_section,
        (uint32_t)sects.dwarf_index_section_length, &fdeInfo, &cieInfo);
  }
#endif
  if (!foundFDE) {
    // otherwise, search cache of previously found FDEs.
    pint_t cachedFDE = DwarfFDECache<A>::findFDE(sects.dso_base, pc);
    if (cachedFDE != 0) {
      foundFDE =
          CFI_Parser<A>::findFDE(_addressSpace, pc, sects.dwarf_section,
                                 (uint32_t)sects.dwarf_section_length,
                                 cachedFDE, &fdeInfo, &cieInfo);
      foundInCache = foundFDE;
    }
  }
  if (!foundFDE) {
    // Still not found, do full scan of __eh_frame section.
    foundFDE = CFI_Parser<A>::findFDE(_addressSpace, pc, sects.dwarf_section,
                                      (uint32_t)sects.dwarf_section_length, 0,
                                      &fdeInfo, &cieInfo);
  }
  if (foundFDE) {
    typename CFI_Parser<A>::PrologInfo prolog;
    if (CFI_Parser<A>::parseFDEInstructions(_addressSpace, fdeInfo, cieInfo, pc,
                                            &prolog)) {
      // Save off parsed FDE info
      _info.start_ip          = fdeInfo.pcStart;
      _info.end_ip            = fdeInfo.pcEnd;
      _info.lsda              = fdeInfo.lsda;
      _info.handler           = cieInfo.personality;
      _info.gp                = prolog.spExtraArgSize;
      _info.flags             = 0;
      _info.format            = dwarfEncoding();
      _info.unwind_info       = fdeInfo.fdeStart;
      _info.unwind_info_size  = (uint32_t)fdeInfo.fdeLength;
      _info.extra             = (unw_word_t) sects.dso_base;

      // Add to cache (to make next lookup faster) if we had no hint
      // and there was no index.
      if (!foundInCache && (fdeSectionOffsetHint == 0)) {
  #if defined(_LIBUNWIND_SUPPORT_DWARF_INDEX)
        if (sects.dwarf_index_section == 0)
  #endif
        DwarfFDECache<A>::add(sects.dso_base, fdeInfo.pcStart, fdeInfo.pcEnd,
                              fdeInfo.fdeStart);
      }
      return true;
    }
  }
  //_LIBUNWIND_DEBUG_LOG("can't find/use FDE for pc=0x%llX", (uint64_t)pc);
  return false;
}
#endif // defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)


#if defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)
template <typename A, typename R>
bool UnwindCursor<A, R>::getInfoFromCompactEncodingSection(pint_t pc,
                                              const UnwindInfoSections &sects) {
  const bool log = false;
  if (log)
    fprintf(stderr, "getInfoFromCompactEncodingSection(pc=0x%llX, mh=0x%llX)\n",
            (uint64_t)pc, (uint64_t)sects.dso_base);

  const UnwindSectionHeader<A> sectionHeader(_addressSpace,
                                                sects.compact_unwind_section);
  if (sectionHeader.version() != UNWIND_SECTION_VERSION)
    return false;

  // do a binary search of top level index to find page with unwind info
  pint_t targetFunctionOffset = pc - sects.dso_base;
  const UnwindSectionIndexArray<A> topIndex(_addressSpace,
                                           sects.compact_unwind_section
                                         + sectionHeader.indexSectionOffset());
  uint32_t low = 0;
  uint32_t high = sectionHeader.indexCount();
  uint32_t last = high - 1;
  while (low < high) {
    uint32_t mid = (low + high) / 2;
    //if ( log ) fprintf(stderr, "\tmid=%d, low=%d, high=%d, *mid=0x%08X\n",
    //mid, low, high, topIndex.functionOffset(mid));
    if (topIndex.functionOffset(mid) <= targetFunctionOffset) {
      if ((mid == last) ||
          (topIndex.functionOffset(mid + 1) > targetFunctionOffset)) {
        low = mid;
        break;
      } else {
        low = mid + 1;
      }
    } else {
      high = mid;
    }
  }
  const uint32_t firstLevelFunctionOffset = topIndex.functionOffset(low);
  const uint32_t firstLevelNextPageFunctionOffset =
      topIndex.functionOffset(low + 1);
  const pint_t secondLevelAddr =
      sects.compact_unwind_section + topIndex.secondLevelPagesSectionOffset(low);
  const pint_t lsdaArrayStartAddr =
      sects.compact_unwind_section + topIndex.lsdaIndexArraySectionOffset(low);
  const pint_t lsdaArrayEndAddr =
      sects.compact_unwind_section + topIndex.lsdaIndexArraySectionOffset(low+1);
  if (log)
    fprintf(stderr, "\tfirst level search for result index=%d "
                    "to secondLevelAddr=0x%llX\n",
                    low, (uint64_t) secondLevelAddr);
  // do a binary search of second level page index
  uint32_t encoding = 0;
  pint_t funcStart = 0;
  pint_t funcEnd = 0;
  pint_t lsda = 0;
  pint_t personality = 0;
  uint32_t pageKind = _addressSpace.get32(secondLevelAddr);
  if (pageKind == UNWIND_SECOND_LEVEL_REGULAR) {
    // regular page
    UnwindSectionRegularPageHeader<A> pageHeader(_addressSpace,
                                                 secondLevelAddr);
    UnwindSectionRegularArray<A> pageIndex(
        _addressSpace, secondLevelAddr + pageHeader.entryPageOffset());
    // binary search looks for entry with e where index[e].offset <= pc <
    // index[e+1].offset
    if (log)
      fprintf(stderr, "\tbinary search for targetFunctionOffset=0x%08llX in "
                      "regular page starting at secondLevelAddr=0x%llX\n",
              (uint64_t) targetFunctionOffset, (uint64_t) secondLevelAddr);
    low = 0;
    high = pageHeader.entryCount();
    while (low < high) {
      uint32_t mid = (low + high) / 2;
      if (pageIndex.functionOffset(mid) <= targetFunctionOffset) {
        if (mid == (uint32_t)(pageHeader.entryCount() - 1)) {
          // at end of table
          low = mid;
          funcEnd = firstLevelNextPageFunctionOffset + sects.dso_base;
          break;
        } else if (pageIndex.functionOffset(mid + 1) > targetFunctionOffset) {
          // next is too big, so we found it
          low = mid;
          funcEnd = pageIndex.functionOffset(low + 1) + sects.dso_base;
          break;
        } else {
          low = mid + 1;
        }
      } else {
        high = mid;
      }
    }
    encoding = pageIndex.encoding(low);
    funcStart = pageIndex.functionOffset(low) + sects.dso_base;
    if (pc < funcStart) {
      if (log)
        fprintf(
            stderr,
            "\tpc not in table, pc=0x%llX, funcStart=0x%llX, funcEnd=0x%llX\n",
            (uint64_t) pc, (uint64_t) funcStart, (uint64_t) funcEnd);
      return false;
    }
    if (pc > funcEnd) {
      if (log)
        fprintf(
            stderr,
            "\tpc not in table, pc=0x%llX, funcStart=0x%llX, funcEnd=0x%llX\n",
            (uint64_t) pc, (uint64_t) funcStart, (uint64_t) funcEnd);
      return false;
    }
  } else if (pageKind == UNWIND_SECOND_LEVEL_COMPRESSED) {
    // compressed page
    UnwindSectionCompressedPageHeader<A> pageHeader(_addressSpace,
                                                    secondLevelAddr);
    UnwindSectionCompressedArray<A> pageIndex(
        _addressSpace, secondLevelAddr + pageHeader.entryPageOffset());
    const uint32_t targetFunctionPageOffset =
        (uint32_t)(targetFunctionOffset - firstLevelFunctionOffset);
    // binary search looks for entry with e where index[e].offset <= pc <
    // index[e+1].offset
    if (log)
      fprintf(stderr, "\tbinary search of compressed page starting at "
                      "secondLevelAddr=0x%llX\n",
              (uint64_t) secondLevelAddr);
    low = 0;
    last = pageHeader.entryCount() - 1;
    high = pageHeader.entryCount();
    while (low < high) {
      uint32_t mid = (low + high) / 2;
      if (pageIndex.functionOffset(mid) <= targetFunctionPageOffset) {
        if ((mid == last) ||
            (pageIndex.functionOffset(mid + 1) > targetFunctionPageOffset)) {
          low = mid;
          break;
        } else {
          low = mid + 1;
        }
      } else {
        high = mid;
      }
    }
    funcStart = pageIndex.functionOffset(low) + firstLevelFunctionOffset
                                                              + sects.dso_base;
    if (low < last)
      funcEnd =
          pageIndex.functionOffset(low + 1) + firstLevelFunctionOffset
                                                              + sects.dso_base;
    else
      funcEnd = firstLevelNextPageFunctionOffset + sects.dso_base;
    if (pc < funcStart) {
      _LIBUNWIND_DEBUG_LOG("malformed __unwind_info, pc=0x%llX not in second  "
                           "level compressed unwind table. funcStart=0x%llX",
                            (uint64_t) pc, (uint64_t) funcStart);
      return false;
    }
    if (pc > funcEnd) {
      _LIBUNWIND_DEBUG_LOG("malformed __unwind_info, pc=0x%llX not in second  "
                          "level compressed unwind table. funcEnd=0x%llX",
                           (uint64_t) pc, (uint64_t) funcEnd);
      return false;
    }
    uint16_t encodingIndex = pageIndex.encodingIndex(low);
    if (encodingIndex < sectionHeader.commonEncodingsArrayCount()) {
      // encoding is in common table in section header
      encoding = _addressSpace.get32(
          sects.compact_unwind_section +
          sectionHeader.commonEncodingsArraySectionOffset() +
          encodingIndex * sizeof(uint32_t));
    } else {
      // encoding is in page specific table
      uint16_t pageEncodingIndex =
          encodingIndex - (uint16_t)sectionHeader.commonEncodingsArrayCount();
      encoding = _addressSpace.get32(secondLevelAddr +
                                     pageHeader.encodingsPageOffset() +
                                     pageEncodingIndex * sizeof(uint32_t));
    }
  } else {
    _LIBUNWIND_DEBUG_LOG("malformed __unwind_info at 0x%0llX bad second "
                         "level page",
                          (uint64_t) sects.compact_unwind_section);
    return false;
  }

  // look up LSDA, if encoding says function has one
  if (encoding & UNWIND_HAS_LSDA) {
    UnwindSectionLsdaArray<A> lsdaIndex(_addressSpace, lsdaArrayStartAddr);
    uint32_t funcStartOffset = (uint32_t)(funcStart - sects.dso_base);
    low = 0;
    high = (uint32_t)(lsdaArrayEndAddr - lsdaArrayStartAddr) /
                    sizeof(unwind_info_section_header_lsda_index_entry);
    // binary search looks for entry with exact match for functionOffset
    if (log)
      fprintf(stderr,
              "\tbinary search of lsda table for targetFunctionOffset=0x%08X\n",
              funcStartOffset);
    while (low < high) {
      uint32_t mid = (low + high) / 2;
      if (lsdaIndex.functionOffset(mid) == funcStartOffset) {
        lsda = lsdaIndex.lsdaOffset(mid) + sects.dso_base;
        break;
      } else if (lsdaIndex.functionOffset(mid) < funcStartOffset) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }
    if (lsda == 0) {
      _LIBUNWIND_DEBUG_LOG("found encoding 0x%08X with HAS_LSDA bit set for "
                    "pc=0x%0llX, but lsda table has no entry",
                    encoding, (uint64_t) pc);
      return false;
    }
  }

  // extact personality routine, if encoding says function has one
  uint32_t personalityIndex = (encoding & UNWIND_PERSONALITY_MASK) >>
                              (__builtin_ctz(UNWIND_PERSONALITY_MASK));
  if (personalityIndex != 0) {
    --personalityIndex; // change 1-based to zero-based index
    if (personalityIndex > sectionHeader.personalityArrayCount()) {
      _LIBUNWIND_DEBUG_LOG("found encoding 0x%08X with personality index %d,  "
                            "but personality table has only %d entires",
                            encoding, personalityIndex,
                            sectionHeader.personalityArrayCount());
      return false;
    }
    int32_t personalityDelta = (int32_t)_addressSpace.get32(
        sects.compact_unwind_section +
        sectionHeader.personalityArraySectionOffset() +
        personalityIndex * sizeof(uint32_t));
    pint_t personalityPointer = sects.dso_base + (pint_t)personalityDelta;
    personality = _addressSpace.getP(personalityPointer);
    if (log)
      fprintf(stderr, "getInfoFromCompactEncodingSection(pc=0x%llX), "
                      "personalityDelta=0x%08X, personality=0x%08llX\n",
              (uint64_t) pc, personalityDelta, (uint64_t) personality);
  }

  if (log)
    fprintf(stderr, "getInfoFromCompactEncodingSection(pc=0x%llX), "
                    "encoding=0x%08X, lsda=0x%08llX for funcStart=0x%llX\n",
            (uint64_t) pc, encoding, (uint64_t) lsda, (uint64_t) funcStart);
  _info.start_ip = funcStart;
  _info.end_ip = funcEnd;
  _info.lsda = lsda;
  _info.handler = personality;
  _info.gp = 0;
  _info.flags = 0;
  _info.format = encoding;
  _info.unwind_info = 0;
  _info.unwind_info_size = 0;
  _info.extra = sects.dso_base;
  return true;
}
#endif // defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)


template <typename A, typename R>
void UnwindCursor<A, R>::setInfoBasedOnIPRegister(bool isReturnAddress) {
  pint_t pc = (pint_t)this->getReg(UNW_REG_IP);
#if defined(_LIBUNWIND_ARM_EHABI)
  // Remove the thumb bit so the IP represents the actual instruction address.
  // This matches the behaviour of _Unwind_GetIP on arm.
  pc &= (pint_t)~0x1;
#endif

  // If the last line of a function is a "throw" the compiler sometimes
  // emits no instructions after the call to __cxa_throw.  This means
  // the return address is actually the start of the next function.
  // To disambiguate this, back up the pc when we know it is a return
  // address.
  if (isReturnAddress)
    --pc;

  // Ask address space object to find unwind sections for this pc.
  UnwindInfoSections sects;
  if (_addressSpace.findUnwindSections(pc, sects)) {
#if defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)
    // If there is a compact unwind encoding table, look there first.
    if (sects.compact_unwind_section != 0) {
      if (this->getInfoFromCompactEncodingSection(pc, sects)) {
  #if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
        // Found info in table, done unless encoding says to use dwarf.
        uint32_t dwarfOffset;
        if ((sects.dwarf_section != 0) && compactSaysUseDwarf(&dwarfOffset)) {
          if (this->getInfoFromDwarfSection(pc, sects, dwarfOffset)) {
            // found info in dwarf, done
            return;
          }
        }
  #endif
        // If unwind table has entry, but entry says there is no unwind info,
        // record that we have no unwind info.
        if (_info.format == 0)
          _unwindInfoMissing = true;
        return;
      }
    }
#endif // defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)

#if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
    // If there is dwarf unwind info, look there next.
    if (sects.dwarf_section != 0) {
      if (this->getInfoFromDwarfSection(pc, sects)) {
        // found info in dwarf, done
        return;
      }
    }
#endif

#if defined(_LIBUNWIND_ARM_EHABI)
    // If there is ARM EHABI unwind info, look there next.
    if (sects.arm_section != 0 && this->getInfoFromEHABISection(pc, sects))
      return;
#endif
  }

#if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
  // There is no static unwind info for this pc. Look to see if an FDE was
  // dynamically registered for it.
  pint_t cachedFDE = DwarfFDECache<A>::findFDE(0, pc);
  if (cachedFDE != 0) {
    CFI_Parser<LocalAddressSpace>::FDE_Info fdeInfo;
    CFI_Parser<LocalAddressSpace>::CIE_Info cieInfo;
    const char *msg = CFI_Parser<A>::decodeFDE(_addressSpace,
                                                cachedFDE, &fdeInfo, &cieInfo);
    if (msg == NULL) {
      typename CFI_Parser<A>::PrologInfo prolog;
      if (CFI_Parser<A>::parseFDEInstructions(_addressSpace, fdeInfo, cieInfo,
                                                                pc, &prolog)) {
        // save off parsed FDE info
        _info.start_ip         = fdeInfo.pcStart;
        _info.end_ip           = fdeInfo.pcEnd;
        _info.lsda             = fdeInfo.lsda;
        _info.handler          = cieInfo.personality;
        _info.gp               = prolog.spExtraArgSize;
                                  // Some frameless functions need SP
                                  // altered when resuming in function.
        _info.flags            = 0;
        _info.format           = dwarfEncoding();
        _info.unwind_info      = fdeInfo.fdeStart;
        _info.unwind_info_size = (uint32_t)fdeInfo.fdeLength;
        _info.extra            = 0;
        return;
      }
    }
  }

  // Lastly, ask AddressSpace object about platform specific ways to locate
  // other FDEs.
  pint_t fde;
  if (_addressSpace.findOtherFDE(pc, fde)) {
    CFI_Parser<LocalAddressSpace>::FDE_Info fdeInfo;
    CFI_Parser<LocalAddressSpace>::CIE_Info cieInfo;
    if (!CFI_Parser<A>::decodeFDE(_addressSpace, fde, &fdeInfo, &cieInfo)) {
      // Double check this FDE is for a function that includes the pc.
      if ((fdeInfo.pcStart <= pc) && (pc < fdeInfo.pcEnd)) {
        typename CFI_Parser<A>::PrologInfo prolog;
        if (CFI_Parser<A>::parseFDEInstructions(_addressSpace, fdeInfo,
                                                cieInfo, pc, &prolog)) {
          // save off parsed FDE info
          _info.start_ip         = fdeInfo.pcStart;
          _info.end_ip           = fdeInfo.pcEnd;
          _info.lsda             = fdeInfo.lsda;
          _info.handler          = cieInfo.personality;
          _info.gp               = prolog.spExtraArgSize;
          _info.flags            = 0;
          _info.format           = dwarfEncoding();
          _info.unwind_info      = fdeInfo.fdeStart;
          _info.unwind_info_size = (uint32_t)fdeInfo.fdeLength;
          _info.extra            = 0;
          return;
        }
      }
    }
  }
#endif // #if defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)

  // no unwind info, flag that we can't reliably unwind
  _unwindInfoMissing = true;
}

template <typename A, typename R>
int UnwindCursor<A, R>::step() {
  // Bottom of stack is defined is when unwind info cannot be found.
  if (_unwindInfoMissing)
    return UNW_STEP_END;

  // Use unwinding info to modify register set as if function returned.
  int result;
#if defined(_LIBUNWIND_SUPPORT_COMPACT_UNWIND)
  result = this->stepWithCompactEncoding();
#elif defined(_LIBUNWIND_SUPPORT_DWARF_UNWIND)
  result = this->stepWithDwarfFDE();
#elif defined(_LIBUNWIND_ARM_EHABI)
  result = this->stepWithEHABI();
#else
  #error Need _LIBUNWIND_SUPPORT_COMPACT_UNWIND or \
              _LIBUNWIND_SUPPORT_DWARF_UNWIND or \
              _LIBUNWIND_ARM_EHABI
#endif

  // update info based on new PC
  if (result == UNW_STEP_SUCCESS) {
    this->setInfoBasedOnIPRegister(true);
    if (_unwindInfoMissing)
      return UNW_STEP_END;
    if (_info.gp)
      setReg(UNW_REG_SP, getReg(UNW_REG_SP) + _info.gp);
  }

  return result;
}

template <typename A, typename R>
void UnwindCursor<A, R>::getInfo(unw_proc_info_t *info) {
  *info = _info;
}

template <typename A, typename R>
bool UnwindCursor<A, R>::getFunctionName(char *buf, size_t bufLen,
                                                           unw_word_t *offset) {
  return _addressSpace.findFunctionName((pint_t)this->getReg(UNW_REG_IP),
                                         buf, bufLen, offset);
}

} // namespace libunwind

#endif // __UNWINDCURSOR_HPP__
