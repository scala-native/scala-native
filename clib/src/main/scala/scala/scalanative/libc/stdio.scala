package scala.scalanative
package libc

import scalanative.unsafe._
import stddef.size_t

@extern object stdio extends stdio

@extern private[scalanative] trait stdio {

  // File access

  /** Opens a file indicated by filename and returns a file stream associated
   *  with that file. mode is used to determine the file access mode.
   *
   *  @return
   *    If successful, returns a pointer to the object that controls the opened
   *    file stream, with both eof and error bits cleared. The stream is fully
   *    buffered unless filename refers to an interactive device.
   */
  def fopen(filename: CString, mode: CString): Ptr[FILE] = extern

  /** Closes the given file stream. Any unwritten buffered data are flushed to
   *  the OS. Any unread buffered data are discarded.
   *
   *  Whether or not the operation succeeds, the stream is no longer associated
   *  with a file, and the buffer allocated by setbuf or setvbuf, if any, is
   *  also disassociated and deallocated if automatic allocation was used.
   *
   *  The behavior is undefined if the value of the pointer stream is used after
   *  fclose returns.
   *
   *  @param stream
   *    the file stream to close
   *  @return
   *    0 on success, EOF otherwise
   */
  @blocking def fclose(stream: Ptr[FILE]): CInt = extern

  /** For output streams (and for update streams on which the last operation was
   *  output), writes any unwritten data from the stream's buffer to the
   *  associated output device.
   *
   *  For input streams (and for update streams on which the last operation was
   *  input), the behavior is undefined.
   *
   *  If stream is a null pointer, all open output streams are flushed,
   *  including the ones manipulated within library packages or otherwise not
   *  directly accessible to the program.
   *
   *  @param stream
   *    the file stream to write out
   *  @return
   *    0 on success. Otherwise EOF is returned and the error indicator of the
   *    file stream is set.
   */
  def fflush(stream: Ptr[FILE]): CInt = extern

  /** Sets the internal buffer to use for stream operations. It should be at
   *  least BUFSIZ characters long.
   *
   *  @param stream
   *    the file stream to set the buffer to
   *  @param buffer
   *    pointer to a buffer for the stream to use. If a null pointer is
   *    supplied, the buffering is turned off
   */
  def setbuf(stream: Ptr[FILE], buffer: Ptr[CChar]): Unit = extern

  /** Changes the buffering mode of the given file stream stream as indicated by
   *  the argument mode. In addition,
   *
   *  @param stream
   *    the file stream to set the buffer to
   *
   *  @param buffer
   *    pointer to a buffer for the stream to use or null pointer to change size
   *    and mode only
   *  @param mode
   *    buffering mode to use. It can be one of the following values:
   *
   *  | mode   | buffer         |
   *  |:-------|:---------------|
   *  | _IOFBF | full buffering |
   *  | _IOLBF | line buffering |
   *  | _IONBF | no buffering   |
   *  @return
   *    ​0​ on success or nonzero on failure.
   */
  def setvbuf(
      stream: Ptr[FILE],
      buffer: Ptr[CChar],
      mode: CInt,
      size: CSize
  ): CInt =
    extern

  /** If mode > 0, attempts to make stream wide-oriented. If mode < 0, attempts
   *  to make stream byte-oriented. If mode==0, only queries the current
   *  orientation of the stream.
   *
   *  If the orientation of the stream has already been decided (by executing
   *  output or by an earlier call to fwide), this function does nothing.
   *
   *  @param stream
   *    pointer to the C I/O stream to modify or query
   *  @param mode
   *    integer value greater than zero to set the stream wide, less than zero
   *    to set the stream narrow, or zero to query only
   *
   *  @return
   *    An integer greater than zero if the stream is wide-oriented after this
   *    call, less than zero if the stream is byte-oriented after this call, and
   *    zero if the stream has no orientation.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fwide]]
   */
  def fwide(stream: Ptr[FILE], mode: CInt): CInt = extern

  // Direct input/output

  /** Reads up to count objects into the array buffer from the given input
   *  stream stream as if by calling fgetc size times for each object, and
   *  storing the results, in the order obtained, into the successive positions
   *  of buffer, which is reinterpreted as an array of unsigned char. The file
   *  position indicator for the stream is advanced by the number of characters
   *  read.
   *
   *  If an error occurs, the resulting value of the file position indicator for
   *  the stream is indeterminate. If a partial element is read, its value is
   *  indeterminate.
   *
   *  @param buffer
   *    pointer to the array where the read objects are stored
   *  @param size
   *    size of each object in bytes
   *  @param count
   *    the number of the objects to be read
   *  @param stream
   *    the stream to read
   *
   *  @return
   *    Number of objects read successfully, which may be less than count if an
   *    error or end-of-file condition occurs.
   *
   *  If size or count is zero, fread returns zero and performs no other action.
   *
   *  fread does not distinguish between end-of-file and error, and callers must
   *  use feof and ferror to determine which occurred.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fread]]
   */
  @blocking def fread(
      buffer: Ptr[Byte],
      size: CSize,
      count: CSize,
      stream: Ptr[FILE]
  ): CSize = extern

  /** Writes count of objects from the given array buffer to the output stream
   *  stream. The objects are written as if by reinterpreting each object as an
   *  array of unsigned char and calling fputc size times for each object to
   *  write those unsigned chars into stream, in order. The file position
   *  indicator for the stream is advanced by the number of characters written.
   *
   *  If an error occurs, the resulting value of the file position indicator for
   *  the stream is indeterminate.
   *
   *  @param buffer
   *    pointer to the first object in the array to be written
   *
   *  @param size
   *    size of each object
   *
   *  @param count
   *    the number of the objects to be written
   *
   *  @param stream
   *    pointer to the output stream
   *
   *  @return
   *    The number of objects written successfully, which may be less than count
   *    if an error occurs.If size or count is zero, fwrite returns zero and
   *    performs no other action.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fwrite]]
   */

  @blocking def fwrite(
      buffer: Ptr[Byte],
      size: CSize,
      count: CSize,
      stream: Ptr[FILE]
  ): CSize = extern

  // Unformatted input/output
  /** Reads the next character from the given input stream.
   *  @param stream
   *    stream to read the character from
   *
   *  @return
   *    Return value On success, returns the obtained character as an unsigned
   *    char converted to an int. On failure, returns EOF. If the failure has
   *    been caused by end-of-file condition, additionally sets the eof
   *    indicator (see feof()) on stream. If the failure has been caused by some
   *    other error, sets the error indicator (see ferror()) on stream.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fgetc]]
   */
  @blocking def fgetc(stream: Ptr[FILE]): CInt = extern

  /** Same as fgetc, except that if getc is implemented as a macro, it may
   *  evaluate stream more than once, so the corresponding argument should never
   *  be an expression with side effects.
   *  @param stream
   *    stream to read the character from
   *
   *  @return
   *    Return value On success, returns the obtained character as an unsigned
   *    char converted to an int. On failure, returns EOF. If the failure has
   *    been caused by end-of-file condition, additionally sets the eof
   *    indicator (see feof()) on stream. If the failure has been caused by some
   *    other error, sets the error indicator (see ferror()) on stream.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fgetc]]
   */
  @blocking def getc(stream: Ptr[FILE]): CInt = extern

  /** Reads at most count - 1 characters from the given file stream and stores
   *  them in the character array pointed to by str. Parsing stops if a newline
   *  character is found, in which case str will contain that newline character,
   *  or if end-of-file occurs. If bytes are read and no errors occur, writes a
   *  null character at the position immediately after the last character
   *  written to str.
   *
   *  @param str
   *    pointer to an element of a char array
   *  @param count
   *    maximum number of characters to write (typically the length of str)
   *  @param stream
   *    file stream to read the data from
   *
   *  @return
   *    str on success, null pointer on failure. If the end-of-file condition is
   *    encountered, sets the eof indicator on stream (see feof()). This is only
   *    a failure if it causes no bytes to be read, in which case a null pointer
   *    is returned and the contents of the array pointed to by str are not
   *    altered (i.e. the first byte is not overwritten with a null character).
   *    If the failure has been caused by some other error, sets the error
   *    indicator (see ferror()) on stream. The contents of the array pointed to
   *    by str are indeterminate (it may not even be null-terminated).
   */
  @blocking def fgets(str: CString, count: CInt, stream: Ptr[FILE]): CString =
    extern

  /** Writes a character ch to the given output stream stream. putc() may be
   *  implemented as a macro and evaluate stream more than once, so the
   *  corresponding argument should never be an expression with side effects.
   *
   *  Internally, the character is converted to unsigned char just before being
   *  written.
   *
   *  @param ch
   *    character to be written
   *  @param stream
   *    output stream
   *  @return
   *    On success, returns the written character. On failure, returns EOF and
   *    sets the error indicator (see ferror()) on stream.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fputc]]
   */
  @blocking def fputc(ch: CInt, stream: Ptr[FILE]): CInt = extern

  /** Writes a character ch to the given output stream stream. putc() may be
   *  implemented as a macro and evaluate stream more than once, so the
   *  corresponding argument should never be an expression with side effects.
   *
   *  Internally, the character is converted to unsigned char just before being
   *  written.
   *
   *  @param ch
   *    character to be written
   *  @param stream
   *    output stream
   *  @return
   *    On success, returns the written character. On failure, returns EOF and
   *    sets the error indicator (see ferror()) on stream.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fputc]]
   */
  @blocking def putc(ch: CInt, stream: Ptr[FILE]): CInt = extern

  /** Writes every character from the null-terminated string str to the output
   *  stream stream, as if by repeatedly executing fputc.
   *
   *  The terminating null character from str is not written.
   *  @param str
   *    null-terminated character string to be written
   *  @param stream
   *    output stream
   *  @return
   *    non-negative value on success. Otherwise, returns EOF and sets the error
   *    indicator (see ferror()) on stream.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fputs]]
   */
  @blocking def fputs(str: CString, stream: Ptr[FILE]): CInt = extern

  /** Reads the next character from stdin.
   *
   *  @return
   *    The obtained character on success or EOF on failure. If the failure has
   *    been caused by end-of-file condition, additionally sets the eof
   *    indicator (see feof()) on stdin. If the failure has been caused by some
   *    other error, sets the error indicator (see ferror()) on stdin.
   */
  @blocking def getchar(): CInt = extern

  /** Reads stdin into given character string until a newline character is found
   *  or end-of-file occurs.
   *  @param str
   *    character string to be written
   *
   *  @return
   *    str on success, a null pointer on failure. If the failure has been
   *    caused by end of file condition, additionally sets the eof indicator
   *    (see std::feof()) on stdin. If the failure has been caused by some other
   *    error, sets the error indicator (see std::ferror()) on stdin.
   *  @see
   *    [[https://en.cppreference.com/w/cpp/io/c/gets]]
   */
  @blocking def gets(str: CString): CString = extern

  /** Writes a character ch to stdout. Internally, the character is converted to
   *  unsigned char just before being written.
   *
   *  @param ch
   *    character to be written
   *
   *  @return
   *    the written character on success. Otherwise return EOF and sets the
   *    error indicator on stdout.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/putchar]]
   *  @see
   *    [[https://en.cppreference.com/w/c/io/ferror]] for error indicators.
   */
  @blocking def putchar(ch: CInt): CInt = extern

  /** Writes every character from the null-terminated string str and one
   *  additional newline character '\n' to the output stream stdout, as if by
   *  repeatedly executing fputc.
   *
   *  The terminating null character from str is __NOT__ written.
   *
   *  @param str
   *    character string to be written
   *
   *  @return
   *    a non-negative value on success. Otherwise, return EOF and sets the
   *    error indicator on stream.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/puts]]
   *  @see
   *    [[https://en.cppreference.com/w/c/io/ferror]] for error indicators.
   */
  @blocking def puts(str: CString): CInt = extern

  /** If ch does not equal EOF, pushes the character ch (reinterpreted as
   *  unsigned char) into the input buffer associated with the stream stream in
   *  such a manner that subsequent read operation from stream will retrieve
   *  that character. The external device associated with the stream is not
   *  modified.
   *
   *  Stream repositioning operations fseek, fsetpos, and rewind discard the
   *  effects of ungetc.
   *
   *  If ungetc is called more than once without an intervening read or
   *  repositioning, it may fail (in other words, a pushback buffer of size 1 is
   *  guaranteed, but any larger buffer is implementation-defined). If multiple
   *  successful ungetc were performed, read operations retrieve the pushed-back
   *  characters in reverse order of ungetc.
   *
   *  If ch equals EOF, the operation fails and the stream is not affected.
   *
   *  A successful call to ungetc clears the end of file status flag feof.
   *
   *  A successful call to ungetc on a binary stream decrements the stream
   *  position indicator by one (the behavior is indeterminate if the stream
   *  position indicator was zero).
   *
   *  A successful call to ungetc on a text stream modifies the stream position
   *  indicator in unspecified manner but guarantees that after all pushed-back
   *  characters are retrieved with a read operation, the stream position
   *  indicator is equal to its value before ungetc.
   *
   *  @param ch
   *    character to be pushed into the input stream buffer
   *  @param stream
   *    file stream to put the character back to
   *
   *  @return
   *    ch on success. Otherwise returns EOF and the given stream remains
   *    unchanged.
   */
  @blocking def ungetc(ch: CInt, stream: Ptr[FILE]): CInt = extern

  // Formatted input/output

  /** Reads data from stdin and stores them according to the parameter format
   *  into the locations pointed by the additional arguments.
   *  @param format
   *    C string that contains a sequence of characters that control how
   *    characters extracted from the stream are treated
   *
   *  @param vargs
   *    Depending on the format string, the function may expect a sequence of
   *    additional arguments, each containing a pointer to allocated storage
   *    where the interpretation of the extracted characters is stored with the
   *    appropriate type. There should be at least as many of these arguments as
   *    the number of values stored by the format specifiers. Additional
   *    arguments are ignored by the function.
   *  @return
   *    the number of items of the argument listsuccessfully filled on success.
   *    If a reading error happens or the end-of-file is reached while reading,
   *    the proper indicator is set (feof or ferror). And, if either happens
   *    before any data could be successfully read, EOF is returned.
   */
  @blocking def scanf(format: CString, vargs: Any*): CInt = extern

  /** Reads data from the stream and stores them according to the parameter
   *  format into the locations pointed by the additional arguments.
   *  @param stream
   *    Pointer to a FILE object that identifies the input stream to read data
   *    from.
   *  @param format
   *    C string that contains a sequence of characters that control how
   *    characters extracted from the stream are treated
   *
   *  @param vargs
   *    Depending on the format string, the function may expect a sequence of
   *    additional arguments, each containing a pointer to allocated storage
   *    where the interpretation of the extracted characters is stored with the
   *    appropriate type. There should be at least as many of these arguments as
   *    the number of values stored by the format specifiers. Additional
   *    arguments are ignored by the function.
   *  @return
   *    the number of items of the argument listsuccessfully filled on success.
   *    If a reading error happens or the end-of-file is reached while reading,
   *    the proper indicator is set (feof or ferror). And, if either happens
   *    before any data could be successfully read, EOF is returned.
   */
  @blocking def fscanf(stream: Ptr[FILE], format: CString, vargs: Any*): CInt =
    extern

  /** Reads data from s and stores them according to parameter format into the
   *  locations given by the additional arguments, as if scanf was used, but
   *  reading from s instead of the standard input
   *  @param s
   *    C string that the function processes as its source to retrieve the data.
   *  @param format
   *    C string that contains a sequence of characters that control how
   *    characters extracted from the stream are treated
   *
   *  @param vargs
   *    Depending on the format string, the function may expect a sequence of
   *    additional arguments, each containing a pointer to allocated storage
   *    where the interpretation of the extracted characters is stored with the
   *    appropriate type. There should be at least as many of these arguments as
   *    the number of values stored by the format specifiers. Additional
   *    arguments are ignored by the function.
   *  @return
   *    the number of items of the argument listsuccessfully filled on success.
   *    If a reading error happens or the end-of-file is reached while reading,
   *    the proper indicator is set (feof or ferror). And, if either happens
   *    before any data could be successfully read, EOF is returned.
   */
  @blocking def sscanf(s: CString, format: CString, vargs: Any*): CInt = extern

  /** Read formatted data into variable argument list Reads data from the
   *  standard input (stdin) and stores them according to parameter format into
   *  the locations pointed by the elements in the variable argument list
   *  identified by arg.
   *  @param format
   *    C string that contains a format string that follows the same
   *    specifications as format in scanf (see scanf for details).
   *
   *  @param valist
   *    A value identifying a variable arguments list initialized with va_start.
   *    va_list is a special type defined in <cstdarg>.
   *  @return
   *    the number of items of the argument listsuccessfully filled on success.
   *    If a reading error happens or the end-of-file is reached while reading,
   *    the proper indicator is set (feof or ferror). And, if either happens
   *    before any data could be successfully read, EOF is returned.
   */
  @blocking def vscanf(format: CString, valist: CVarArgList): CInt = extern

  /** Read formatted data from stream into variable argument list Reads data
   *  from the stream and stores them according to parameter format into the
   *  locations pointed by the elements in the variable argument list identified
   *  by arg.
   *  @param stream
   *    Pointer to a FILE object that identifies an input stream.
   *  @param format
   *    C string that contains a format string that follows the same
   *    specifications as format in scanf (see scanf for details).
   *  @param valist
   *    A value identifying a variable arguments list initialized with va_start.
   *    va_list is a special type defined in <cstdarg>.
   *  @return
   *    the number of items of the argument listsuccessfully filled on success.
   *    If a reading error happens or the end-of-file is reached while reading,
   *    the proper indicator is set (feof or ferror). And, if either happens
   *    before any data could be successfully read, EOF is returned.
   *
   *  @see
   *    [[https://www.cplusplus.com/reference/cstdio/vfscanf/]]
   */
  @blocking def vfscanf(
      stream: Ptr[FILE],
      format: CString,
      valist: CVarArgList
  ): CInt =
    extern

  /** Reads the data from stdin
   *
   *  @param buffer
   *    pointer to a null-terminated character string to read from
   *  @param format
   *    pointer to a null-terminated character string specifying how to read the
   *    input
   *  @param vlist
   *    variable argument list containing the receiving arguments.
   *
   *  @return
   *    Number of receiving arguments successfully assigned, or EOF if read
   *    failure occurs before the first receiving argument was assigned
   */
  @blocking def vsscanf(
      buffer: CString,
      format: CString,
      valist: CVarArgList
  ): CInt =
    extern

  /** Writes the results to stdout.
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/printf]]
   */
  @blocking def printf(format: CString, vargs: Any*): CInt = extern

  /** Writes the results to selected stream.
   *  @param stream
   *    output file stream to write to
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fprintf]]
   */
  @blocking def fprintf(stream: Ptr[FILE], format: CString, vargs: Any*): CInt =
    extern

  /** Writes the results to a character string buffer.
   *  @param buffer
   *    pointer to a character string to write to
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/sprintf]]
   */
  def sprintf(
      buffer: Ptr[CChar],
      format: CString,
      vargs: Any*
  ): CInt = extern

  /** Writes the results to a character string buffer. At most bufsz - 1
   *  characters are written. The resulting character string will be terminated
   *  with a null character, unless bufsz is zero. If bufsz is zero, nothing is
   *  written and buffer may be a null pointer, however the return value (number
   *  of bytes that would be written not including the null terminator) is still
   *  calculated and returned.
   *  @param buffer
   *    pointer to a character string to write to
   *  @param busz
   *    number of character to write
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/snprintf]]
   */
  def snprintf(
      buffer: Ptr[CChar],
      bufsz: size_t,
      format: CString,
      vargs: Any*
  ): CInt = extern

  /** Writes the results to stdout.
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/printf_s]]
   */
  @blocking def printf_s(format: CString, vargs: Any*): CInt = extern

  /** Writes the results to selected stream.
   *  @param stream
   *    output file stream to write to
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/fprintf_s]]
   */
  @blocking def fprintf_s(
      stream: Ptr[FILE],
      format: CString,
      vargs: Any*
  ): CInt = extern

  /** Writes the results to a character string buffer.
   *  @param buffer
   *    pointer to a character string to write to
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/sprintf_s]]
   */
  def sprintf_s(
      buffer: Ptr[CChar],
      format: CString,
      vargs: Any*
  ): CInt = extern

  /** Writes the results to a character string buffer. At most bufsz - 1
   *  characters are written. The resulting character string will be terminated
   *  with a null character, unless bufsz is zero. If bufsz is zero, nothing is
   *  written and buffer may be a null pointer, however the return value (number
   *  of bytes that would be written not including the null terminator) is still
   *  calculated and returned.
   *  @param buffer
   *    pointer to a character string to write to
   *  @param busz
   *    number of character to write
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vargs
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/snprintf_s]]
   */
  def snprintf_s(
      buffer: Ptr[CChar],
      bufsz: size_t,
      format: CString,
      vargs: Any*
  ): CInt = extern

  /** Writes the results to stdout.
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param valist
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/vfprintf]]
   */
  @blocking def vprintf(format: CString, valist: CVarArgList): CInt = extern

  /** Writes the results to a file stream stream.
   *  @param stream
   *    output file stream to write to
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vlist
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/vfprintf]]
   */
  @blocking def vfprintf(
      stream: Ptr[FILE],
      format: CString,
      valist: CVarArgList
  ): CInt =
    extern

  /** Writes the results to a character string buffer.
   *  @param buffer
   *    pointer to a character string to write to
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vlist
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/vfprintf]]
   */
  @blocking def vsprintf(
      buffer: CString,
      format: CString,
      valist: CVarArgList
  ): CInt =
    extern

  /** The number of characters written if successful or negative value if an
   *  error occurred. If the resulting string gets truncated due to buf_size
   *  limit, function returns the total number of characters (not including the
   *  terminating null-byte) which would have been written, if the limit was not
   *  imposed. *
   *  @param buffer
   *    pointer to a character string to write to
   *  @param bufsz
   *    up to bufsz - 1 characters may be written, plus the null terminator
   *
   *  @param format
   *    pointer to a null-terminated character string specifying how to
   *    interpret the data
   *  @param vlist
   *    variable argument list containing the data to print.
   *
   *  @return
   *    The number of characters written if successful or negative value if an
   *    error occurred.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/vfprintf]]
   */
  @blocking def vsnprintf(
      buffer: CString,
      bufsz: CInt,
      format: CString,
      valist: CVarArgList
  ): CInt = extern

  // File positioning

  /** Returns the current value of the position indicator of the stream.
   *
   *  @param stream
   *    Pointer to a FILE object that identifies the stream.
   *
   *  @return
   *    the current value of the position indicator is returned on success. -1L
   *    on Failure and errno is set to system-specific positive value.
   */
  def ftell(stream: Ptr[FILE]): CLong = extern

  /** Retrieves the current position in the stream.
   *  @param stream
   *    Pointer to a FILE object that identifies the stream.
   *
   *  @return
   *    0 on success. non-zero value on failure and errno is set.
   *  @see
   *    [[https://www.cplusplus.com/reference/cstdio/fgetpos/]]
   */
  def fgetpos(stream: Ptr[FILE], pos: Ptr[fpos_t]): CInt = extern

  /** Sets the position indicator associated with the stream to a new position.
   *  @param stream
   *    Pointer to a FILE object that identifies the stream.
   *  @param offset
   *    \- Binary files: Number of bytes to offset from origin. \- Text files:
   *    Either zero, or a value returned by ftell.
   *  @param origin
   *    Position used as reference for the offset. It is specified by one of the
   *    following constants defined in <cstdio> exclusively to be used as
   *    arguments for this function:
   *    | Constant | Reference position                   |
   *    |:---------|:-------------------------------------|
   *    | SEEK_SET | Beginning of file                    |
   *    | SEEK_CUR | Current position of the file pointer |
   *    | SEEK_END | End of file(1)                       |
   *
   *  (1). Library implementations are allowed to not meaningfully support
   *  SEEK_END (therefore, code using it has no real standard portability).
   *  @return
   *    0 on success, non-zero value on failure.
   *  @see
   *    [[https://www.cplusplus.com/reference/cstdio/fseek/]]
   */
  def fseek(stream: Ptr[FILE], offset: CLong, origin: CInt): CInt = extern

  /** Restores the current position in the stream to pos.
   *
   *  @param stream
   *    Pointer to a FILE object that identifies the stream.
   *  @param position
   *    Pointer to a fpos_t object containing a position previously obtained
   *    with fgetpos.
   *  @return
   *    0 on success. Non-zero value on failure.
   *  @see
   *    [[https://www.cplusplus.com/reference/cstdio/fsetpos/]]
   */
  def fsetpos(stream: Ptr[FILE], pos: Ptr[fpos_t]): CInt = extern

  /** Sets the position indicator associated with stream to the beginning of the
   *  file.
   *
   *  @param stream
   *    pointer to a FILE object that identifies the stream.
   *
   *  @see
   *    [[https://www.cplusplus.com/reference/cstdio/rewind/]]
   */
  def rewind(stream: Ptr[FILE]): Unit = extern

  // Error handling

  /** Resets the error flags and the EOF indicator for the given file stream.
   *
   *  @param stream
   *    the file to reset the error flags for
   *  @see
   *    [[https://en.cppreference.com/w/c/io/clearerr]]
   */
  def clearerr(stream: Ptr[FILE]): Unit = extern

  /** Checks if the end of the given file stream has been reached.
   *
   *  @param stream
   *    the file stream to check
   *  @return
   *    nonzero value if the end of the stream has been reached, otherwise ​0​
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/feof]]
   */
  def feof(stream: Ptr[FILE]): CInt = extern

  /** Checks the given stream for errors.
   *
   *  @param stream
   *    the file stream to check
   *
   *  @return
   *    Nonzero value if the file stream has errors occurred, ​0​ otherwise
   */
  def ferror(stream: Ptr[FILE]): CInt = extern

  /** Prints a textual description of the error code currently stored in the
   *  system variable errno to stderr.
   *  @param str
   *    pointer to a null-terminated string with explanatory message.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/perror]]
   */
  def perror(str: CString): Unit = extern

  // Operations on files

  /** Deletes the file identified by character string pointed to by fname.
   *
   *  @param fname
   *    pointer to a null-terminated string containing the path identifying the
   *    file to delete
   *  @return
   *    ​0​ upon success or non-zero value on error.
   *  @see
   *    [[https://en.cppreference.com/w/c/io/remove]]
   */
  def remove(fname: CString): CInt = extern

  /** Changes the filename of a file. The file is identified by character string
   *  pointed to by old_filename. The new filename is identified by character
   *  string pointed to by new_filename.
   *
   *  If new_filename exists, the behavior is implementation-defined.
   *
   *  @param old_filename
   *    pointer to a null-terminated string containing the path identifying the
   *    file to rename
   *  @param new_filename
   *    pointer to a null-terminated string containing the new path of the file
   *  @return
   *    ​0​ upon success or non-zero value on error.
   *
   *  @see
   *    [[https://en.cppreference.com/w/c/io/rename]]
   */
  def rename(oldFileName: CString, newFileName: CString): CInt = extern

  /** Creates and opens a temporary file with a unique auto-generated filename.
   *  @return
   *    The associated file stream or a null pointer if an error has occurred
   *  @see
   *    [[https://en.cppreference.com/w/cpp/io/c/tmpfile]]
   */
  def tmpfile(): Ptr[FILE] = extern

  /** Creates a unique filename that does not name a currently existing file,
   *  and stores it in the character string pointed to by filename. The function
   *  is capable of generating up to TMP_MAX of unique filenames, but some or
   *  all of them may already be in use, and thus not suitable return values.
   *
   *  @param filename
   *    pointer to the character array capable of holding at least L_tmpnam
   *    bytes, to be used as a result buffer. If a null pointer is passed, a
   *    pointer to an internal static buffer is returned.
   *  @return
   *    filename if filename was not a null pointer. Otherwise a pointer to an
   *    internal static buffer is returned. If no suitable filename can be
   *    generated, a null pointer is returned.
   *
   *  @see
   *    [[https://en.cppreference.com/w/cpp/io/c/tmpnam]]
   */
  def tmpnam(fileName: CString): CString = extern

  // Types

  type FILE = CStruct0
  type fpos_t = CStruct0

  // Macros

  @name("scalanative_stdin")
  def stdin: Ptr[FILE] = extern
  @name("scalanative_stdout")
  def stdout: Ptr[FILE] = extern
  @name("scalanative_stderr")
  def stderr: Ptr[FILE] = extern
  @name("scalanative_eof")
  def EOF: CInt = extern
  @name("scalanative_fopen_max")
  def FOPEN_MAX: CUnsignedInt = extern
  @name("scalanative_filename_max")
  def FILENAME_MAX: CUnsignedInt = extern
  @name("scalanative_bufsiz")
  def BUFSIZ: CUnsignedInt = extern
  @name("scalanative_iofbf")
  def _IOFBF: CInt = extern
  @name("scalanative_iolbf")
  def _IOLBF: CInt = extern
  @name("scalanative_ionbf")
  def _IONBF: CInt = extern
  @name("scalanative_seek_set")
  def SEEK_SET: CInt = extern
  @name("scalanative_seek_cur")
  def SEEK_CUR: CInt = extern
  @name("scalanative_seek_end")
  def SEEK_END: CInt = extern
  @name("scalanative_tmp_max")
  def TMP_MAX: CUnsignedInt = extern
  @name("scalanative_l_tmpnam")
  def L_tmpnam: CUnsignedInt = extern
}
