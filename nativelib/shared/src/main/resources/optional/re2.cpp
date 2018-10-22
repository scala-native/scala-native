/*
  https://github.com/marcomaggi/cre2 (0.3.0)

  Source  file  for  cre2, a  C   language  wrapper  for RE2:  a   regular
  expressions library by Google.

  Copyright (c) 2012 Marco Maggi <marco.maggi-ipsu@poste.it>
  Copyright (c) 2011 Keegan McAllister
  All rights reserved.

  Redistribution and  use in source and binary  forms, with or
  without  modification,  are   permitted  provided  that  the
  following conditions are met:

  1.  Redistributions of  source  code must  retain the  above
     copyright  notice,  this   list  of  conditions  and  the
     following disclaimer.

  2. Redistributions  in binary form must  reproduce the above
     copyright  notice,  this   list  of  conditions  and  the
     following  disclaimer in  the documentation  and/or other
     materials provided with the distribution.

  3.  Neither the  name of  the author  nor the  names  of his
     contributors may  be used to endorse  or promote products
     derived from this software without specific prior written
     permission.

  THIS  SOFTWARE  IS PROVIDED  BY  THE  COPYRIGHT HOLDERS  AND
  CONTRIBUTORS   ``AS  IS''   AND  ANY   EXPRESS   OR  IMPLIED
  WARRANTIES,  INCLUDING,  BUT  NOT  LIMITED TO,  THE  IMPLIED
  WARRANTIES OF  MERCHANTABILITY AND FITNESS  FOR A PARTICULAR
  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL THE  AUTHORS OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)
  HOWEVER CAUSED  AND ON ANY  THEORY OF LIABILITY,  WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  OTHERWISE)  ARISING  IN ANY  WAY  OUT  OF  THE USE  OF  THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


Scala-Native modifications

we add the scalanative_ prefix to avoid potential linking with the original cre2
library (if someone creates another wrapper for example)

 + cre2_find_named_capturing_groups (https://github.com/marcomaggi/cre2/pull/14)

 - cre2_opt_* (options getter)
 - cre2_easy_match
 - cre2_match_*
 - cre2_strings_to_ranges
 - cre2_replace & cre2_global_replace (we use functions accepting regex)
 - cre2_possible_match_range
 - cre2_check_rewrite_string
 - cre2_extract & cre2_extract_re
*/

#include <re2/re2.h>
#include <re2/set.h>
#include "re2.h"

#include <cstdlib>
#include <cstdio>
#include <vector>
#include <map>

/** --------------------------------------------------------------------
 ** Options objects.
 ** ----------------------------------------------------------------- */

/* Cast    the  pointer    argument   "opt"   to   a   pointer   of   type
   "RE2::Options*". */
#define TO_OPT(opt) (reinterpret_cast<RE2::Options *>(opt))

/* Allocate and return a new options object. */
scalanative_cre2_options_t *scalanative_cre2_opt_new(void) {
    // FIXME: is  this use of  "nothrow" good to avoid  raising exceptions
    // when memory allocation fails and to return NULL instead?
    return reinterpret_cast<void *>(new (std::nothrow) RE2::Options());
}

/* Finalise an options object. */
void scalanative_cre2_opt_delete(scalanative_cre2_options_t *opt) {
    delete TO_OPT(opt);
}

/* Set or unset option flags in an options object. */
#define OPT_BOOL(name)                                                         \
    void scalanative_cre2_opt_set_##name(scalanative_cre2_options_t *opt,      \
                                         int flag) {                           \
        TO_OPT(opt)->set_##name(bool(flag));                                   \
    }                                                                          \
    int scalanative_cre2_opt_##name(scalanative_cre2_options_t *opt) {         \
        return TO_OPT(opt)->name();                                            \
    }
OPT_BOOL(posix_syntax)
OPT_BOOL(longest_match)
OPT_BOOL(log_errors)
OPT_BOOL(literal)
OPT_BOOL(never_nl)
OPT_BOOL(dot_nl)
OPT_BOOL(never_capture)
OPT_BOOL(case_sensitive)
OPT_BOOL(perl_classes)
OPT_BOOL(word_boundary)
OPT_BOOL(one_line)
#undef OPT_BOOL

/* Select the encoding in an options object. */
void scalanative_cre2_opt_set_encoding(scalanative_cre2_options_t *opt,
                                       scalanative_cre2_encoding_t enc) {
    switch (enc) {
    case scalanative_cre2_UTF8:
        TO_OPT(opt)->set_encoding(RE2::Options::EncodingUTF8);
        break;
    case scalanative_cre2_Latin1:
        TO_OPT(opt)->set_encoding(RE2::Options::EncodingLatin1);
        break;
    default:
        fprintf(stderr,
                "scalanative_cre2: internal error: unknown encoding %d\n", enc);
        exit(EXIT_FAILURE);
    }
}

scalanative_cre2_encoding_t
scalanative_cre2_opt_encoding(scalanative_cre2_options_t *opt) {
    RE2::Options::Encoding E = TO_OPT(opt)->encoding();
    switch (E) {
    case RE2::Options::EncodingUTF8:
        return scalanative_cre2_UTF8;
    case RE2::Options::EncodingLatin1:
        return scalanative_cre2_Latin1;
    default:
        return scalanative_cre2_UNKNOWN;
    }
}

/* Configure the maximum amount of memory in an options object. */
void scalanative_cre2_opt_set_max_mem(scalanative_cre2_options_t *opt,
                                      int64_t m) {
    TO_OPT(opt)->set_max_mem(m);
}

int64_t scalanative_cre2_opt_max_mem(scalanative_cre2_options_t *opt) {
    return TO_OPT(opt)->max_mem();
}

/** --------------------------------------------------------------------
 ** Precompiled regular expressions objects.
 ** ----------------------------------------------------------------- */

#define TO_RE2(re) (reinterpret_cast<RE2 *>(re))
#define TO_CONST_RE2(re) (reinterpret_cast<const RE2 *>(re))

scalanative_cre2_regexp_t *
scalanative_cre2_new(const char *pattern, int pattern_len,
                     const scalanative_cre2_options_t *opt) {
    re2::StringPiece pattern_re2(pattern, pattern_len);
    if (opt) {
        // FIXME:  is  this   use   of  "nothrow"  enough  to  avoid   raising
        // exceptions  when   memory   allocation  fails and  to  return  NULL
        // instead?
        return reinterpret_cast<void *>(new (std::nothrow) RE2(
            pattern_re2, *reinterpret_cast<const RE2::Options *>(opt)));
    } else {
        return reinterpret_cast<void *>(new (std::nothrow) RE2(pattern_re2));
    }
}

void scalanative_cre2_delete(scalanative_cre2_regexp_t *re) {
    delete TO_RE2(re);
}

int scalanative_cre2_error_code(const scalanative_cre2_regexp_t *re) {
    return int(TO_CONST_RE2(re)->error_code());
}

const char *scalanative_cre2_error_string(const scalanative_cre2_regexp_t *re) {
    return TO_CONST_RE2(re)->error().c_str();
}

void scalanative_cre2_error_arg(const scalanative_cre2_regexp_t *re,
                                scalanative_cre2_string_t *arg) {
    const std::string &argstr = TO_CONST_RE2(re)->error_arg();
    arg->data = argstr.data();
    arg->length = argstr.length();
}

int scalanative_cre2_num_capturing_groups(const scalanative_cre2_regexp_t *re) {
    return TO_CONST_RE2(re)->NumberOfCapturingGroups();
}

int scalanative_cre2_find_named_capturing_groups(
    const scalanative_cre2_regexp_t *re, const char *group_name) {
    const std::map<std::string, int> &m =
        TO_CONST_RE2(re)->NamedCapturingGroups();
    std::map<std::string, int>::const_iterator it = m.find(group_name);

    if (it != m.end()) {
        return it->second;
    } else {
        return -1;
    }
}

/** --------------------------------------------------------------------
 ** Matching with precompiled regular expressions objects.
 ** ----------------------------------------------------------------- */

static RE2::Anchor
to_scalanative_cre2_anchor(scalanative_cre2_anchor_t anchor) {
    RE2::Anchor anchor_re2 = RE2::UNANCHORED;
    switch (anchor) {
    case scalanative_cre2_ANCHOR_START:
        anchor_re2 = RE2::ANCHOR_START;
        break;
    case scalanative_cre2_ANCHOR_BOTH:
        anchor_re2 = RE2::ANCHOR_BOTH;
        break;
    case scalanative_cre2_UNANCHORED:
        break;
    }
    return anchor_re2;
}

int scalanative_cre2_match(const scalanative_cre2_regexp_t *re,
                           const char *text, int textlen, int startpos,
                           int endpos, scalanative_cre2_anchor_t anchor,
                           scalanative_cre2_string_t *match, int nmatch) {

    re2::StringPiece text_re2(text, textlen);
    std::vector<re2::StringPiece> match_re2(nmatch);
    RE2::Anchor anchor_re2 = to_scalanative_cre2_anchor(anchor);
    bool retval; // 0 for no match
                 // 1 for successful matching
    retval = TO_CONST_RE2(re)->Match(text_re2, startpos, endpos, anchor_re2,
                                     match_re2.data(), nmatch);
    if (retval) {
        for (int i = 0; i < nmatch; i++) {
            match[i].data = match_re2[i].data();
            match[i].length = match_re2[i].length();
        }
    }
    return (retval) ? 1 : 0;
}

/** --------------------------------------------------------------------
 ** Problematic functions.
 ** ----------------------------------------------------------------- */

/* The following  functions rely  on C++ memory   allocation.  It  is not
   clear how they can be written to allow a correct API towards C.  */

int scalanative_cre2_replace_re(scalanative_cre2_regexp_t *rex,
                                scalanative_cre2_string_t *text_and_target,
                                scalanative_cre2_string_t *rewrite) {
    std::string S(text_and_target->data, text_and_target->length);
    re2::StringPiece R(rewrite->data, rewrite->length);
    char *buffer; /* this exists to make GCC shut up about const */
    bool retval;
    retval = RE2::Replace(&S, *TO_RE2(rex), R);
    text_and_target->length = S.length();
    buffer = (char *)malloc(1 + text_and_target->length);
    if (buffer) {
        S.copy(buffer, text_and_target->length);
        buffer[text_and_target->length] = '\0';
        text_and_target->data = buffer;
    } else
        return -1;
    return int(retval);
}

int scalanative_cre2_global_replace_re(
    scalanative_cre2_regexp_t *rex, scalanative_cre2_string_t *text_and_target,
    scalanative_cre2_string_t *rewrite) {
    std::string S(text_and_target->data, text_and_target->length);
    re2::StringPiece R(rewrite->data, rewrite->length);
    char *buffer; /* this exists to make GCC shut up about const */
    int retval;
    retval = RE2::GlobalReplace(&S, *TO_RE2(rex), R);
    text_and_target->length = S.length();
    buffer = (char *)malloc(1 + text_and_target->length);
    if (buffer) {
        S.copy(buffer, text_and_target->length);
        buffer[text_and_target->length] = '\0';
        text_and_target->data = buffer;
    } else
        return -1;
    return retval;
}

/* ------------------------------------------------------------------ */

int scalanative_cre2_quote_meta(scalanative_cre2_string_t *quoted,
                                scalanative_cre2_string_t *original) {
    re2::StringPiece O(original->data, original->length);
    std::string Q;
    char *buffer; /* this exists to make GCC shut up about const */
    Q = RE2::QuoteMeta(O);
    quoted->length = Q.length();
    buffer = (char *)malloc(1 + quoted->length);
    if (buffer) {
        Q.copy(buffer, quoted->length);
        buffer[quoted->length] = '\0';
        quoted->data = buffer;
        return 0;
    } else
        return -1;
}
