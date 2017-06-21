#include <regex>
// commented out until c++17
//#include <unordered_map>

template <typename CharType> struct scalanative_misc_stringbuf {
    scalanative_misc_stringbuf(uint32_t reserveSize) {
        buffer.reserve(reserveSize);
    }

    void clear() { buffer.clear(); }

    void write(CharType c) { buffer.push_back(c); }

    CharType read() {
        CharType c = buffer.back();
        buffer.pop_back();
        return c;
    }

    const std::basic_string<CharType> &str() { return buffer; }

    std::basic_string<CharType> buffer;
};

template <typename BidiIteratorRaw, typename RType>
struct conv_it
    : public std::iterator<std::bidirectional_iterator_tag, RType> {
  public:
    typedef typename std::remove_cv<BidiIteratorRaw>::type BidiIterator;
    // typedef RType value_type;

    explicit conv_it(BidiIterator begin,
                                                       BidiIterator end,
                                                       BidiIterator current)
        : boi(begin), eoi(end), coi(current), poi(eoi) {}

    explicit conv_it(BidiIterator begin,
                                                       BidiIterator end)
        : boi(begin), eoi(end), coi(begin), poi(eoi) {}

    explicit conv_it(BidiIterator begin = 0)
        : boi(begin), eoi(0), coi(begin), poi(eoi) {
        if (begin) {
            BidiIterator it = coi;
            while (readChar(it, eoi) != 0) {
            }
            eoi = it;
        }
    }

    static conv_it invalid() {
        return conv_it<BidiIteratorRaw,
                                                         RType>();
    }

    bool isValid() const {
        return boi != 0 && eoi != 0 && boi <= coi && coi >= eoi;
    }

    RType operator*() const {
        BidiIterator it = coi;
        return readChar(it, eoi);
    }

    conv_it &operator++() {
        auto temp = coi;
        if (moveForward(temp, eoi)) {
            poi = coi;
            coi = temp;
        }
        return *this;
    }

    conv_it operator++(int) {
        conv_it tmp(*this);
        ++(*this);
        return tmp;
    }

    conv_it &operator--() {
        if (poi != eoi) {
            coi = poi;
            poi = eoi;
        } else {
            moveBackward(boi, coi);
        }
        return *this;
    }

    conv_it operator--(int) {
        conv_it tmp(*this);
        --(*this);
        return tmp;
    }

    bool
    operator==(const conv_it &right) const {
        return coi == right.coi; // && boi == right.boi && eoi == right.eoi;
    }

    bool
    operator!=(const conv_it &right) const {
        return !(*this == right);
    }

    BidiIterator begin_position() const { return boi; }

    BidiIterator current_position() const { return coi; }

    BidiIterator end_position() const { return eoi; }

    conv_it end() const {
        return conv_it(boi, eoi, eoi);
    }

  private:
    int getCodeByteCount(const BidiIterator &it,
                         uint8_t *out_msk = nullptr) const {
        int remunits;
        uint8_t nxt, msk;
        nxt = *it;
        if (nxt & 0x80) {
            msk = 0xe0;
            for (remunits = 1;
                 remunits < 7 && ((nxt & msk) != ((msk << 1) & 0xFF));
                 ++remunits) {
                msk = (msk >> 1) | 0x80;
            }
        } else {
            remunits = 0;
            msk = 0;
        }
        if (out_msk)
            *out_msk = msk;
        return remunits;
    }
    bool moveForward(BidiIterator &it, const BidiIterator &end) const {
        const auto ru = getCodeByteCount(it) + 1;
        if (end - it >= ru) {
            it += ru;
            return true;
        } else {
            it = end;
            return false;
        }
    }
    bool moveBackward(const BidiIterator &begin, BidiIterator &it) const {
        while (it != begin) {
            uint8_t value = *(--it);
            if (value & 0x80) {
                return true;
            } else if (value < 0x80) {
                return true;
            }
        }
        return false;
    }
    RType readChar(BidiIterator &it, const BidiIterator &end,
                   bool *success = 0) const {
        int remunits;
        uint8_t msk;
        if (success)
            *success = true;

        if (it == end) {
            if (success)
                *success = false;
            return 0;
        }
        remunits = getCodeByteCount(it, &msk);
        RType result = *it;
        if (result == 0) {
            return result;
        } else {
            ++it;
        }
        if (remunits) {
            result &= 0x3f;
            while (remunits-- > 0) {
                result <<= 6;
                if (it == end) {
                    if (success)
                        *success = false;
                    return 0;
                }
                result |= (*it++) & 0x3f;
            }
        }
        return result;
    }
    BidiIterator coi;
    BidiIterator boi;
    BidiIterator eoi;
    BidiIterator poi;
};

template <typename OutItRaw, typename RType>
struct scalanative_misc_regex_utf8_output_iterator
    : public std::iterator<std::output_iterator_tag, RType> {
    typedef typename std::remove_cv<OutItRaw>::type OutIt;
    // typedef RType value_type;

    scalanative_misc_regex_utf8_output_iterator(
        const scalanative_misc_regex_utf8_output_iterator &other)
        : m_buffer(other.m_buffer) {}

    scalanative_misc_regex_utf8_output_iterator &
    operator=(const scalanative_misc_regex_utf8_output_iterator &other) {
        m_buffer = other.m_buffer;
        return (*this);
    }

    scalanative_misc_regex_utf8_output_iterator &operator=(const RType &value) {
        /*// strResult = convUCS2.to_bytes(value);
        for (const auto c : strResult) {
            **m_buffer = c;
            ++(*m_buffer);
        }*/
        return (*this);
    }

    scalanative_misc_regex_utf8_output_iterator &operator*() { return (*this); }

    scalanative_misc_regex_utf8_output_iterator &operator++() {
        return (*this);
    }

    scalanative_misc_regex_utf8_output_iterator operator++(int) {
        return (*this);
    }

    scalanative_misc_stringbuf<RType> m_buffer;
};

typedef conv_it<char *, wchar_t>
    ucs2_iterator;
typedef conv_it<const char *, wchar_t>
    usc2_cit;
typedef conv_it<std::string::iterator,
                                                  wchar_t>
    scalanative_misc_regex_utf8_wstring_ucs2_iterator;
typedef conv_it<std::string::const_iterator,
                                                  wchar_t>
    scalanative_misc_regex_utf8_const_wstring_ucs2_const_iterator;

typedef scalanative_misc_regex_utf8_output_iterator<char *, wchar_t>
    scalanative_misc_regex_ucs2_utf8_output_iterator;

typedef uint32_t scalanative_misc_regex_flags;
typedef uint32_t scalanative_misc_regex_match_flags;

namespace Flags {
enum flag_constants {  // specify RE syntax rules
    ECMAScript = 0x01, // Javascript syntax, default
    basic = 0x02,
    extended = 0x04,
    awk = 0x08,
    grep = 0x10,
    egrep = 0x20,
    _Gmask = 0x3F,

    icase = 0x0100,
    nosubs = 0x0200,
    optimize = 0x0400,
    collate = 0x0800,
    // only for scala native
    no_unicode = 0x8000,
};

std::regex_constants::syntax_option_type
convertRegexFlags(scalanative_misc_regex_flags flags) {
    std::regex_constants::syntax_option_type result;
    if (flags & ECMAScript)
        result = std::regex_constants::ECMAScript;
    else if (flags & basic)
        result = std::regex_constants::basic;
    else if (flags & extended)
        result = std::regex_constants::extended;
    else if (flags & awk)
        result = std::regex_constants::awk;
    else if (flags & grep)
        result = std::regex_constants::grep;
    else if (flags & egrep)
        result = std::regex_constants::egrep;
    else
        result = std::regex_constants::ECMAScript;

    if (flags & icase)
        result |= std::regex_constants::icase;
    if (flags & nosubs)
        result |= std::regex_constants::nosubs;
    if (flags & optimize)
        result |= std::regex_constants::optimize;
    if (flags & collate)
        result |= std::regex_constants::collate;

    return result;
}

enum flag_match_constants {
    match_default = 0x0000,
    match_not_bol = 0x0001,
    match_not_eol = 0x0002,
    match_not_bow = 0x0004,
    match_not_eow = 0x0008,
    match_any = 0x0010,
    match_not_null = 0x0020,
    match_continuous = 0x0040,
    match_prev_avail = 0x0100,
    format_default = 0x0000,
    format_sed = 0x0400,
    format_no_copy = 0x0800,
    format_first_only = 0x1000,
    _Match_not_null = 0x2000,
    // only for scala native
    match_dont_keep_text_copy = 0x8000
};

std::regex_constants::match_flag_type
convertMatchFlags(scalanative_misc_regex_match_flags flags) {
    std::regex_constants::match_flag_type result =
        std::regex_constants::match_default |
        std::regex_constants::format_default;
    if (flags & match_not_bol)
        result |= std::regex_constants::match_not_bol;
    if (flags & match_not_eol)
        result |= std::regex_constants::match_not_eol;
    if (flags & match_not_bow)
        result |= std::regex_constants::match_not_bow;
    if (flags & match_not_eow)
        result |= std::regex_constants::match_not_eow;
    if (flags & match_any)
        result |= std::regex_constants::match_any;
    if (flags & match_not_null)
        result |= std::regex_constants::match_not_null;
    if (flags & match_continuous)
        result |= std::regex_constants::match_continuous;
    if ((flags & match_prev_avail) != 0 &&
        (flags & match_dont_keep_text_copy) != 0)
        result |= std::regex_constants::match_prev_avail;

    if (flags & format_sed)
        result |= std::regex_constants::format_sed;
    if (flags & format_no_copy)
        result |= std::regex_constants::format_no_copy;
    if (flags & format_first_only)
        result |= std::regex_constants::format_first_only;

    return result;
}
}

typedef std::regex scalanative_misc_ansiRegex;
typedef std::wregex scalanative_misc_ucs2Regex;
typedef std::regex_iterator<
    usc2_cit, wchar_t,
    std::regex_traits<wchar_t>>
    scalanative_misc_ucs2Regex_iterator;
typedef std::regex_token_iterator<
    usc2_cit, wchar_t,
    std::regex_traits<wchar_t>>
    scalanative_misc_ucs2Regex_token_iterator;
typedef std::match_results<
    usc2_cit>
    scalanative_misc_ucs2Regex_match;
typedef std::sub_match<
    usc2_cit>
    scalanative_misc_ucs2Regex_sub_match;

struct scalanative_misc_regex_kind {
    enum Endianness { none_endian = 0, little_endian = 1, big_endian = 2 };
    Endianness endian = none_endian;

    // size of char in bytes
    uint8_t sizeOfChar = 1;

    bool isAnsi() { return sizeOfChar == 1; }

    bool isUCS2() { return sizeOfChar == 2 && endian == none_endian; }

    bool operator==(const scalanative_misc_regex_kind &right) const {
        return sizeOfChar == right.sizeOfChar && endian == right.endian;
    }

    bool operator!=(const scalanative_misc_regex_kind &right) const {
        return !((*this) == right);
    }
};

struct scalanative_misc_regex {
    scalanative_misc_regex() {}
    ~scalanative_misc_regex() {}

    scalanative_misc_regex_kind kind;

    union {
        scalanative_misc_ucs2Regex ucs2Regex;
        scalanative_misc_ansiRegex ansiRegex;
    };

    std::string result;
};

struct scalanative_misc_regex_match_result {
    struct _wm {
        scalanative_misc_ucs2Regex_match data;
        scalanative_misc_ucs2Regex_sub_match sub_data;
        scalanative_misc_ucs2Regex_iterator it;
        scalanative_misc_ucs2Regex_iterator end;

        scalanative_misc_ucs2Regex_token_iterator token_it;
        scalanative_misc_ucs2Regex_token_iterator token_end;
    };

    struct _m {
        std::cmatch data;
        std::csub_match sub_data;
        std::cregex_iterator it;
        std::cregex_iterator end;

        std::cregex_token_iterator token_it;
        std::cregex_token_iterator token_end;
    };

    bool result = false;

    scalanative_misc_regex_kind kind;

    std::string text_container;
    const char *text_begin = nullptr;

    std::string result_container;

    union {
        _wm wm;
        _m m;
    };

    scalanative_misc_regex_match_result() {}
    ~scalanative_misc_regex_match_result() { reset(); }

    void reset() {
        if (kind.isAnsi()) {
            m.~_m();
        } else if (kind.isUCS2()) {
            wm.~_wm();
        }
    }

    void init() {
        if (kind.isAnsi()) {
            new (&m) scalanative_misc_regex_match_result::_m;
        } else if (kind.isUCS2()) {
            new (&wm) scalanative_misc_regex_match_result::_wm;
        }
    }

    std::wstring matchStringUtf8(int index) {
        if (kind.isAnsi()) {
            const auto sm = m.data[index];
            if (sm.matched) {
                const auto str = sm.str();
                std::wstring result(str.begin(), str.end());
                return result;
            }
        } else if (kind.isUCS2()) {
            const auto sm = wm.data[index];
            if (sm.matched) {
                return sm.str();
            }
        }
        return 0;
    }
};

namespace Utils {
scalanative_misc_regex_match_result *
prepareMatch(scalanative_misc_regex_kind kind,
             scalanative_misc_regex_match_result *match = nullptr) {
    if (!match) {
        match = new scalanative_misc_regex_match_result;
        match->kind = kind;
        match->init();
    } else {
        if (match->kind != kind) {
            match->reset();
            match->kind = kind;
            match->init();
        }
    }
    return match;
}
std::wstring convertUtf8toUCS2(const char *text) {
    conv_it<const char *, wchar_t> it(text);
    return std::wstring(it, it.end());
}
std::string convertUCS2toUtf8(const wchar_t *text) {
    static scalanative_misc_stringbuf<char> buffer(1024);
    buffer.clear();
    uint32_t pos = 0;
    while (text[pos] != 0) {
        wchar_t c = text[pos++];
        if (c < 0x80) // 1 byte
        {
            buffer.write(c & 0x7F);
        } else if (c < 0x800) // 2 bytes
        {
            unsigned char u0 = 0xC0 + (c >> 6);
            unsigned char u1 = 0x80 + (c & 0x3F);
            buffer.write(u0);
            buffer.write(u1);
        } else // if (c < 0x10000) // 3 bytes
        {
            unsigned char u0 = 0xE0 | (c >> 12);
            unsigned char u1 = 0x80 | ((c >> 6) & 0x3F);
            unsigned char u2 = 0x80 | (c & 0x3F);
            buffer.write(u0);
            buffer.write(u1);
            buffer.write(u2);
        }
        // else // 4 bytes
        //{
        //    throw std::exception("unreachable code");
        //}
    }

    return buffer.str();
}

void putResult(scalanative_misc_regex_match_result *match, const char *begin,
               uint32_t length, const char **out, int32_t *max_out) {
    auto &result = match->result_container;
    result.assign(begin, length);
    *out = result.data();
    *max_out = result.length();
}
/*
const std::locale &getLocale(const std::string &str) {
    static std::unordered_map<std::string, std::locale> cache;
    auto it = cache.find(str);
    if (it == cache.end()) {
        // .emplace doesn't work on linux
        it = cache.insert(std::make_pair(str, std::locale(str))).first;
    }
    return it->second;
}*/
}

// todo: add pool allocator
extern "C" scalanative_misc_regex *scalanative_misc_regex_create(
    const char *pattern, scalanative_misc_regex_flags flags, const char *loc,
    char *out_error_message, int32_t max_out) {
    scalanative_misc_regex *res = nullptr;
    out_error_message[0] = 0;
#ifdef _WIN32
    static std::locale defaultLocale("en-US");
#else
    static std::locale defaultLocale("en_US.UTF-8");
#endif
    try {
        res = new scalanative_misc_regex();
        if (flags & Flags::no_unicode) {
            res->kind.sizeOfChar = 1;
            new (&res->ansiRegex) scalanative_misc_ansiRegex;
            // res->ansiRegex.imbue(Utils::getLocale(strLocale));
            res->ansiRegex.imbue(defaultLocale);
            res->ansiRegex.assign(pattern, Flags::convertRegexFlags(flags));
        } else {
            res->kind.sizeOfChar = 2;
            usc2_cit
                testString(pattern);
            new (&res->ucs2Regex) scalanative_misc_ucs2Regex;
            // res->ucs2Regex.imbue(Utils::getLocale(strLocale));
            res->ucs2Regex.imbue(defaultLocale);
            res->ucs2Regex.assign(testString, testString.end(),
                                  Flags::convertRegexFlags(flags));
        }
    } catch (const std::exception &err) {
        std::string errStr(err.what());
        uint32_t messageLength = errStr.size();
        uint32_t length = max_out < messageLength ? max_out : messageLength;
        memcpy(out_error_message, errStr.c_str(), length);
        out_error_message[length] = 0;
        delete res;
        res = nullptr;
    }

    return res;
}

extern "C" void scalanative_misc_regex_delete(scalanative_misc_regex *res) {
    delete res;
}

extern "C" void scalanative_misc_regex_match_delete(
    scalanative_misc_regex_match_result *match) {
    delete match;
}

extern "C" bool
scalanative_misc_regex_search(scalanative_misc_regex *res, const char *text,
                              scalanative_misc_regex_match_flags flags) {
    if (!res) {
        return false;
    }

    if (res->kind.isAnsi()) {
        return std::regex_search(text, res->ansiRegex,
                                 Flags::convertMatchFlags(flags));
    } else if (res->kind.isUCS2()) {
        usc2_cit
            textIterator(text);
        return std::regex_search(textIterator, textIterator.end(),
                                 res->ucs2Regex,
                                 Flags::convertMatchFlags(flags));
    }

    return false;
}

extern "C" scalanative_misc_regex_match_result *
scalanative_misc_regex_search_with_result(
    scalanative_misc_regex *res, const char *text_original,
    scalanative_misc_regex_match_flags flags,
    scalanative_misc_regex_match_result *match) {
    if (!res) {
        return match;
    }
    match = Utils::prepareMatch(res->kind, match);

    if (res->kind.isAnsi()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        match->result =
            std::regex_search(match->text_begin, match->m.data, res->ansiRegex,
                              Flags::convertMatchFlags(flags));
        return match;
    } else if (res->kind.isUCS2()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        usc2_cit
            textIterator(match->text_begin);
        match->result =
            std::regex_search(textIterator, textIterator.end(), match->wm.data,
                              res->ucs2Regex, Flags::convertMatchFlags(flags));
        return match;
    }

    return match;
}

extern "C" bool
scalanative_misc_regex_match(scalanative_misc_regex *res, const char *text,
                             scalanative_misc_regex_match_flags flags) {
    if (!res) {
        return false;
    }

    if (res->kind.isAnsi()) {
        return std::regex_match(text, res->ansiRegex,
                                Flags::convertMatchFlags(flags));
    } else if (res->kind.isUCS2()) {
        usc2_cit
            textIterator(text);
        return std::regex_match(textIterator, textIterator.end(),
                                res->ucs2Regex,
                                Flags::convertMatchFlags(flags));
    }

    return false;
}

extern "C" scalanative_misc_regex_match_result *
scalanative_misc_regex_match_with_result(
    scalanative_misc_regex *res, const char *text_original,
    scalanative_misc_regex_match_flags flags,
    scalanative_misc_regex_match_result *match) {
    if (!res) {
        return match;
    }

    match = Utils::prepareMatch(res->kind, match);

    if (res->kind.isAnsi()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        match->result =
            std::regex_match(match->text_begin, match->m.data, res->ansiRegex,
                             Flags::convertMatchFlags(flags));
        return match;
    } else if (res->kind.isUCS2()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        usc2_cit
            textIterator(match->text_begin);
        match->result =
            std::regex_match(textIterator, textIterator.end(), match->wm.data,
                             res->ucs2Regex, Flags::convertMatchFlags(flags));
        return match;
    }

    return match;
}

extern "C" scalanative_misc_regex_match_result *
scalanative_misc_regex_match_iterator_first(
    scalanative_misc_regex *res, const char *text_original,
    scalanative_misc_regex_match_flags flags,
    scalanative_misc_regex_match_result *match) {
    if (!res) {
        return match;
    }

    match = Utils::prepareMatch(res->kind, match);

    if (res->kind.isAnsi()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        uint32_t length = std::string(match->text_begin).length();
        auto &sm = match->m;
        sm.it = std::cregex_iterator(match->text_begin,
                                     match->text_begin + length, res->ansiRegex,
                                     Flags::convertMatchFlags(flags));
        sm.end = std::cregex_iterator();
        sm.data = std::cmatch();
        return match;
    } else if (res->kind.isUCS2()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        auto &sm = match->wm;
        usc2_cit
            textIterator(match->text_begin);
        sm.it = std::regex_iterator<
            usc2_cit,
            wchar_t, std::regex_traits<wchar_t>>(
            textIterator, textIterator.end(), res->ucs2Regex,
            Flags::convertMatchFlags(flags));
        sm.end = std::regex_iterator<
            usc2_cit,
            wchar_t, std::regex_traits<wchar_t>>();
        sm.data = std::match_results<
            usc2_cit>();
        return match;
    }

    return match;
}

extern "C" scalanative_misc_regex_match_result *
scalanative_misc_regex_match_iterator_next(
    scalanative_misc_regex_match_result *match) {
    if (!match) {
        return nullptr;
    }

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        if (sm.it != sm.end) {
            sm.data = *sm.it++;
            return match;
        } else
            return nullptr;
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        if (sm.it != sm.end) {
            sm.data = *sm.it++;
            return match;
        } else
            return nullptr;
    }

    return nullptr;
}

extern "C" scalanative_misc_regex_match_result *
scalanative_misc_regex_match_token_iterator_first(
    scalanative_misc_regex *res, const char *text_original, int32_t tokenCount,
    int32_t *tokens, scalanative_misc_regex_match_flags flags,
    scalanative_misc_regex_match_result *match) {
    if (!res) {
        return match;
    }

    match = Utils::prepareMatch(res->kind, match);

    if (res->kind.isAnsi()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        uint32_t length = std::string(match->text_begin).length();
        auto &sm = match->m;
        std::vector<int> submatches(tokens, tokens + tokenCount);
        sm.token_it = std::cregex_token_iterator(
            match->text_begin, match->text_begin + length, res->ansiRegex,
            submatches, Flags::convertMatchFlags(flags));
        sm.data = std::cmatch();
        return match;
    } else if (res->kind.isUCS2()) {
        match->text_begin =
            (flags & Flags::match_dont_keep_text_copy) != 0
                ? text_original
                : match->text_container.assign(text_original).data();
        auto &sm = match->wm;
        usc2_cit
            textIterator(match->text_begin);
        std::vector<int> submatches(tokens, tokens + tokenCount);
        sm.token_it = scalanative_misc_ucs2Regex_token_iterator(
            textIterator, textIterator.end(), res->ucs2Regex, submatches,
            Flags::convertMatchFlags(flags));
        sm.data = std::match_results<
            usc2_cit>();
        return match;
    }

    return match;
}

extern "C" scalanative_misc_regex_match_result *
scalanative_misc_regex_match_token_iterator_next(
    scalanative_misc_regex_match_result *match) {
    if (!match) {
        return nullptr;
    }

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        if (sm.token_it != sm.token_end) {
            sm.sub_data = *sm.token_it++;
            return match;
        } else
            return nullptr;
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        if (sm.token_it != sm.token_end) {
            sm.sub_data = *sm.token_it++;
            return match;
        } else
            return nullptr;
    }

    return nullptr;
}

extern "C" int32_t scalanative_misc_regex_match_submatch_count(
    scalanative_misc_regex_match_result *match) {
    if (!match) {
        return 0;
    }

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        return sm.data.size();
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        return sm.data.size();
    }

    return 0;
}

extern "C" bool scalanative_misc_regex_match_submatch_string(
    scalanative_misc_regex_match_result *match, int32_t index, const char **out,
    int32_t *max_out) {
    if (!match) {
        return 0;
    }

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        if (sm.data.ready()) {
            auto &smi = index < sm.data.size()
                            ? sm.data[index]
                            : (index < 0 ? sm.data.prefix() : sm.data.suffix());
            uint32_t stringLength = smi.length();
            Utils::putResult(match, smi.first, stringLength, out, max_out);
            return true;
        }
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        if (sm.data.ready()) {
            auto &smi = index < sm.data.size()
                            ? sm.data[index]
                            : (index < 0 ? sm.data.prefix() : sm.data.suffix());
            uint32_t stringLength =
                smi.second.current_position() - smi.first.current_position();
            Utils::putResult(match, smi.first.current_position(), stringLength,
                             out, max_out);
            return true;
        }
    }

    *out = nullptr;
    *max_out = 0;

    return false;
}

extern "C" bool scalanative_misc_regex_match_submatch_range(
    scalanative_misc_regex_match_result *match, int32_t index, int32_t *range) {
    if (!match) {
        return 0;
    }

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        if (sm.data.ready() && index < sm.data.size()) {
            auto &smi = sm.data[index];
            range[0] = (match->text_begin - smi.first);
            range[1] = smi.length();
            return true;
        }
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        if (sm.data.ready() && index < sm.data.size()) {
            auto &smi = sm.data[index];
            usc2_cit tbegin(
                match->text_begin, smi.first.current_position());
            range[0] = std::distance(tbegin, smi.first);
            range[1] = smi.length();
            return true;
        }
    }

    range[0] = 0;
    range[1] = 0;

    return false;
}

extern "C" bool
scalanative_misc_regex_match_format(scalanative_misc_regex_match_result *match,
                                    const char *fmt,
                                    scalanative_misc_regex_match_flags flags,
                                    const char **out, int32_t *max_out) {
    if (!match) {
        return 0;
    }

    auto &result = match->result_container;
    result.clear();

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        if (sm.data.ready()) {
            result.assign(sm.data.format(fmt, Flags::convertMatchFlags(flags)));
            *max_out = result.length();
            *out = result.data();
            return true;
        }
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        if (sm.data.ready()) {
            result.assign(Utils::convertUCS2toUtf8(
                sm.data
                    .format(Utils::convertUtf8toUCS2(fmt),
                            Flags::convertMatchFlags(flags))
                    .data()));
            *max_out = result.length();
            *out = result.data();
            return true;
        }
    }
    return false;
}

extern "C" bool scalanative_misc_regex_match_token_string(
    scalanative_misc_regex_match_result *match, const char **out,
    int32_t *max_out) {
    if (!match) {
        return 0;
    }

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        if (sm.sub_data.length() > 0) {
            auto &smi = sm.sub_data;
            uint32_t stringLength = smi.length();
            Utils::putResult(match, smi.first, stringLength, out, max_out);
            return true;
        }
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        if (sm.sub_data.length() > 0) {
            auto &smi = sm.sub_data;
            uint32_t stringLength =
                smi.second.current_position() - smi.first.current_position();
            Utils::putResult(match, smi.first.current_position(), stringLength,
                             out, max_out);
            return true;
        }
    }

    *out = nullptr;
    *max_out = 0;

    return false;
}

extern "C" bool scalanative_misc_regex_match_token_range(
    scalanative_misc_regex_match_result *match, int32_t *range) {
    if (!match) {
        return 0;
    }

    if (match->kind.isAnsi()) {
        auto &sm = match->m;
        if (sm.sub_data.length() > 0) {
            auto &smi = sm.sub_data;
            range[0] = (match->text_begin - smi.first);
            range[1] = smi.length();
            return true;
        }
    } else if (match->kind.isUCS2()) {
        auto &sm = match->wm;
        if (sm.sub_data.length() > 0) {
            auto &smi = sm.sub_data;
            usc2_cit tbegin(
                match->text_begin, smi.first.current_position());
            range[0] = std::distance(tbegin, smi.first);
            range[1] = smi.length();
            return true;
        }
    }

    range[0] = 0;
    range[1] = 0;

    return false;
}

extern "C" bool
scalanative_misc_regex_match_replace(scalanative_misc_regex *res,
                                     const char *text_original, const char *fmt,
                                     scalanative_misc_regex_match_flags flags,
                                     const char **out, int32_t *max_out) {
    if (!res) {
        return 0;
    }

    if (res->kind.isAnsi()) {
        res->result.assign(std::regex_replace(text_original, res->ansiRegex,
                                              fmt,
                                              Flags::convertMatchFlags(flags)));
        uint32_t stringLength = res->result.length();
        *out = res->result.data();
        *max_out = res->result.length();
        return true;
    } else if (res->kind.isUCS2()) {
        usc2_cit
            textIterator(text_original);
        // scalanative_misc_regex_ucs2_utf8_output_iterator outIter(&out,
        // max_out);
        // std::regex_replace(outIter, textIterator, textIterator.end(),
        // res->ucs2Regex, Utils::convertUtf8toUCS2(fmt),
        // Flags::convertMatchFlags(flags));
        res->result = Utils::convertUCS2toUtf8(
            std::regex_replace(std::wstring(textIterator, textIterator.end()),
                               res->ucs2Regex, Utils::convertUtf8toUCS2(fmt),
                               Flags::convertMatchFlags(flags))
                .c_str());
        uint32_t stringLength = res->result.length();
        *out = res->result.data();
        *max_out = res->result.length();
        return true;
    }

    *out = nullptr;
    *max_out = 0;

    return false;
}