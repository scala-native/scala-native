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

Scala-Native modifications (see cre2.cpp)
*/

#ifndef scalanative_cre2_H
#define scalanative_cre2_H 1

/** --------------------------------------------------------------------
 ** Headers.
 ** ----------------------------------------------------------------- */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef scalanative_cre2_decl
#define scalanative_cre2_decl extern
#endif

/** --------------------------------------------------------------------
 ** Regular expressions configuration options.
 ** ----------------------------------------------------------------- */

typedef void scalanative_cre2_options_t;

typedef enum scalanative_cre2_encoding_t {
    scalanative_cre2_UNKNOWN = 0, /* should never happen */
    scalanative_cre2_UTF8 = 1,
    scalanative_cre2_Latin1 = 2
} scalanative_cre2_encoding_t;

scalanative_cre2_decl scalanative_cre2_options_t *
scalanative_cre2_opt_new(void);
scalanative_cre2_decl void
scalanative_cre2_opt_delete(scalanative_cre2_options_t *opt);

scalanative_cre2_decl void
scalanative_cre2_opt_set_posix_syntax(scalanative_cre2_options_t *opt,
                                      int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_longest_match(scalanative_cre2_options_t *opt,
                                       int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_log_errors(scalanative_cre2_options_t *opt, int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_literal(scalanative_cre2_options_t *opt, int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_never_nl(scalanative_cre2_options_t *opt, int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_dot_nl(scalanative_cre2_options_t *opt, int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_never_capture(scalanative_cre2_options_t *opt,
                                       int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_case_sensitive(scalanative_cre2_options_t *opt,
                                        int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_perl_classes(scalanative_cre2_options_t *opt,
                                      int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_word_boundary(scalanative_cre2_options_t *opt,
                                       int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_one_line(scalanative_cre2_options_t *opt, int flag);
scalanative_cre2_decl void
scalanative_cre2_opt_set_max_mem(scalanative_cre2_options_t *opt, int64_t m);
scalanative_cre2_decl void
scalanative_cre2_opt_set_encoding(scalanative_cre2_options_t *opt,
                                  scalanative_cre2_encoding_t enc);

/** --------------------------------------------------------------------
 ** Precompiled regular expressions.
 ** ----------------------------------------------------------------- */

typedef struct scalanative_cre2_string_t {
    const char *data;
    int length;
} scalanative_cre2_string_t;

typedef void scalanative_cre2_regexp_t;

/* This definition  must be  kept in sync  with the definition  of "enum
   ErrorCode" in the file "re2.h" of the original RE2 distribution. */
typedef enum scalanative_cre2_error_code_t {
    scalanative_cre2_NO_ERROR = 0,
    scalanative_cre2_ERROR_INTERNAL,           /* unexpected error */
                                               /* parse errors */
    scalanative_cre2_ERROR_BAD_ESCAPE,         /* bad escape sequence */
    scalanative_cre2_ERROR_BAD_CHAR_CLASS,     /* bad character class */
    scalanative_cre2_ERROR_BAD_CHAR_RANGE,     /* bad character class range */
    scalanative_cre2_ERROR_MISSING_BRACKET,    /* missing closing ] */
    scalanative_cre2_ERROR_MISSING_PAREN,      /* missing closing ) */
    scalanative_cre2_ERROR_TRAILING_BACKSLASH, /* trailing \ at end of regexp */
    scalanative_cre2_ERROR_REPEAT_ARGUMENT, /* repeat argument missing, e.g. "*"
                                               */
    scalanative_cre2_ERROR_REPEAT_SIZE,     /* bad repetition argument */
    scalanative_cre2_ERROR_REPEAT_OP,       /* bad repetition operator */
    scalanative_cre2_ERROR_BAD_PERL_OP,     /* bad perl operator */
    scalanative_cre2_ERROR_BAD_UTF8,        /* invalid UTF-8 in regexp */
    scalanative_cre2_ERROR_BAD_NAMED_CAPTURE, /* bad named capture group */
    scalanative_cre2_ERROR_PATTERN_TOO_LARGE, /* pattern too large (compile
                                                 failed) */
} scalanative_cre2_error_code_t;

/* construction and destruction */
scalanative_cre2_decl scalanative_cre2_regexp_t *
scalanative_cre2_new(const char *pattern, int pattern_len,
                     const scalanative_cre2_options_t *opt);

scalanative_cre2_decl void
scalanative_cre2_delete(scalanative_cre2_regexp_t *re);

/* regular expression inspection */
scalanative_cre2_decl int
scalanative_cre2_error_code(const scalanative_cre2_regexp_t *re);
scalanative_cre2_decl int
scalanative_cre2_num_capturing_groups(const scalanative_cre2_regexp_t *re);
scalanative_cre2_decl int scalanative_cre2_find_named_capturing_groups(
    const scalanative_cre2_regexp_t *re, const char *name);
scalanative_cre2_decl void
scalanative_cre2_error_arg(const scalanative_cre2_regexp_t *re,
                           scalanative_cre2_string_t *arg);

/* invalidated by further re use */
scalanative_cre2_decl const char *
scalanative_cre2_error_string(const scalanative_cre2_regexp_t *re);

/** --------------------------------------------------------------------
 ** Main matching functions.
 ** ----------------------------------------------------------------- */

typedef enum scalanative_cre2_anchor_t {
    scalanative_cre2_UNANCHORED = 1,
    scalanative_cre2_ANCHOR_START = 2,
    scalanative_cre2_ANCHOR_BOTH = 3
} scalanative_cre2_anchor_t;

typedef struct scalanative_cre2_range_t {
    long start; /* inclusive start index for bytevector */
    long past;  /* exclusive end index for bytevector */
} scalanative_cre2_range_t;

scalanative_cre2_decl int
scalanative_cre2_match(const scalanative_cre2_regexp_t *re, const char *text,
                       int textlen, int startpos, int endpos,
                       scalanative_cre2_anchor_t anchor,
                       scalanative_cre2_string_t *match, int nmatch);

/** --------------------------------------------------------------------
 ** Problematic functions.
 ** ----------------------------------------------------------------- */

/* Match the  text in  the buffer "text_and_target"  against the  rex in
   "pattern" or "rex".  Mutate "text_and_target" so that it references a
   malloc'ed buffer  holding the original  text in which the  first, and
   only  the first,  match is  substituted with  the text  in "rewrite".
   Numeric backslash  sequences (\1 to \9) in  "rewrite" are substituted
   with the  portions of  text matching the  corresponding parenthetical
   subexpressions.

   Return 0 if  no match, 1 if successful match,  -1 if error allocating
   memory. */

scalanative_cre2_decl int
scalanative_cre2_replace_re(scalanative_cre2_regexp_t *rex,
                            scalanative_cre2_string_t *text_and_target,
                            scalanative_cre2_string_t *rewrite);

/* Match the  text in  the buffer "text_and_target"  against the  rex in
   "pattern" or "rex".  Mutate "text_and_target" so that it references a
   malloc'ed  buffer holding  the original  text  in which  the all  the
   matching  substrings  are substituted  with  the  text in  "rewrite".
   Numeric backslash  sequences (\1 to \9) in  "rewrite" are substituted
   with the  portions of  text matching the  corresponding parenthetical
   subexpressions.

   Return 0  if no  match, positive integer  representing the  number of
   substitutions performed  if successful match, -1  if error allocating
   memory. */

scalanative_cre2_decl int
scalanative_cre2_global_replace_re(scalanative_cre2_regexp_t *rex,
                                   scalanative_cre2_string_t *text_and_target,
                                   scalanative_cre2_string_t *rewrite);

/* Allocate a zero-terminated malloc'ed buffer and fill it with the text
   from  "original" having all  the regexp  meta characters  quoted with
   single backslashes.   Return 0 if  successful, return -1 if  an error
   allocating memory occurs.  */
scalanative_cre2_decl int
scalanative_cre2_quote_meta(scalanative_cre2_string_t *quoted,
                            scalanative_cre2_string_t *original);

#ifdef __cplusplus
} // extern "C"
#endif

#endif /* scalanative_cre2_H */