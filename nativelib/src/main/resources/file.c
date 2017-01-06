#include <stdio.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h> // for Flags O_* and fsync 
#include <locale.h>
#include <langinfo.h>
#include <limits.h>
#include <fcntl.h> // for open
#include <stdint.h> // for int32_t
#include <utime.h>
#include <time.h> // for tzset
#include <dirent.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/ioctl.h> //for ioctl

#define HyOpenRead    1       /* Values for HyFileOpen */
#define HyOpenWrite   2
#define HyOpenCreate  4
#define HyOpenTruncate  8
#define HyOpenAppend  16
#define HyOpenText    32
#define HyOpenCreateNew 64
#define HyOpenSync    128

#define HySeekSet 0
#define HySeekCur 1
#define HySeekEnd 2

//values chosen accordingly to the corresponding "Hy" macros in Apache Harmony
#define isDir 0 
#define isFile 1


int scalanative_tty_available ()
{
  int rc;
  off_t curr, end;

  int avail = 0;

  curr = lseek (STDIN_FILENO, 0L, SEEK_CUR);    /* don't use tell(), it doesn't exist on all platforms, i.e. linux */
  if (curr != -1)
    {
      end = lseek (STDIN_FILENO, 0L, SEEK_END);
      lseek (STDIN_FILENO, curr, SEEK_SET);
      if (end >= curr)
        {
          return end - curr;
        }
    }

  /* ioctl doesn't work for files on all platforms (i.e. SOLARIS) */

  rc = ioctl (STDIN_FILENO, FIONREAD, &avail);

  if (rc != -1)
    {
      return *(int32_t *) & avail;
    }
  return 0;

}

long scalanative_file_seek (int inFD, long offset, int whence)
{
  off_t localOffset = (off_t) offset;

  if ((whence < HySeekSet) || (whence > HySeekEnd))
    {
      return -1;
    }

  /* If file offsets are 32 bit, truncate the seek to that range */
  if (sizeof (off_t) < sizeof (int64_t))
    {
      if (offset > 0x7FFFFFFF)
        {
          localOffset = 0x7FFFFFFF;
        }
      else if (offset < -0x7FFFFFFF)
        {
          localOffset = -0x7FFFFFFF;
        }
    }

  return (long) lseek ((int) inFD, localOffset, whence);
}


int scalanative_file_sync(long inFD){
  long fd = inFD;
  return fsync((int) (fd));

}

typedef void * UDATA;

UDATA scalanative_file_findfirst(char * path, char* resultbuf){

#if defined(_AIX)
  DIR64 *dirp = NULL;
  struct dirent64 *entry;
#else
  DIR *dirp = NULL;
  struct dirent *entry;
#endif

#if defined(_AIX)
  dirp = opendir64 (path);
#else
  dirp = opendir (path);
#endif


  if (dirp == NULL)
    {
      return (UDATA) - 1;
    }
#if defined(_AIX)
  entry = readdir64 (dirp);
#else
  entry = readdir (dirp);
#endif


  if (entry == NULL)
    {
#if defined(_AIX)
      closedir64 (dirp);
#else
      closedir (dirp);
#endif
      return (UDATA) - 1;
    }
  strcpy (resultbuf, entry->d_name);
  return dirp;
}


int32_t scalanative_file_findnext (UDATA findhandle, char *resultbuf)
{
#if defined(_AIX)
  struct dirent64 *entry;
#else
  struct dirent *entry;
#endif


#if defined(_AIX)
  entry = readdir64 ((DIR64 *) findhandle);
#else
  entry = readdir ((DIR *) findhandle);
#endif


  if (entry == NULL)
    {
      return -1;
    }
  strcpy (resultbuf, entry->d_name);
  return 0;
}


void file_findclose (UDATA findhandle)
{
#if defined(_AIX)
  closedir64 ((DIR64 *) findhandle);
#else
  closedir ((DIR *) findhandle);
#endif
}

int scalanative_set_last_mod(char *path, int64_t time)
{
  struct stat statbuf;
  struct utimbuf timebuf;
  if (stat(path, &statbuf)){
      return 0;  
  }
  timebuf.actime = statbuf.st_atime;
  timebuf.modtime = (time_t) (time / 1000);
  return utime (path, &timebuf) == 0;

}

int scalanative_set_read_only_native(char * path)
{
  struct stat buffer;
  mode_t mode;
  if (stat (path, &buffer))
    {
      return 0;
    }
  mode = buffer.st_mode;
  mode = mode & 07555;
  return chmod (path, mode) == 0;
}

int scalanative_file_mkdir(const char * path){
  if (-1 == mkdir (path, S_IRWXU | S_IRWXG | S_IRWXO))
    {
      return -1;
    }
    return 0;
}

int64_t scalanative_file_length(const char * path){
  struct stat st;

  if(stat(path, &st)){
    return -1;
  }
  return (int64_t) st.st_size;
}

uint64_t scalanative_last_mod(const char * path){
  struct stat st;
  tzset();


  if (stat (path, &st))
    {
      return -1;
    }
  return (uint64_t)st.st_mtime * 1000;
}

int32_t translate_open_flags(int32_t flags)
{
  int32_t realFlags = 0;

  if (flags & HyOpenAppend)
    {
      realFlags |= O_APPEND;
    }
  if (flags & HyOpenTruncate)
    {
      realFlags |= O_TRUNC;
    }
  if (flags & HyOpenCreate)
    {
      realFlags |= O_CREAT;
    }
  if (flags & HyOpenCreateNew)
    {
      realFlags |= O_EXCL | O_CREAT;
    }
#ifdef O_SYNC
  if (flags & HyOpenSync) {
    realFlags |= O_SYNC;
  }
#endif    
  if (flags & HyOpenRead)
    {
      if (flags & HyOpenWrite)
        {
          return (O_RDWR | realFlags);
        }
      return (O_RDONLY | realFlags);
    }
  if (flags & HyOpenWrite)
    {
      return (O_WRONLY | realFlags);
    }
  return -1;
}

int scalanative_file_open(const char * path, int flags, int mode){
  struct stat buffer;
  int32_t realFlags = translate_open_flags((int32_t)flags);
  int32_t fd;
  int32_t fdflags;

  if(realFlags == -1){
    errno = EINVAL;
    return -1;
  }

  if((flags&HyOpenRead && !(flags&HyOpenWrite)) && !stat(path, &buffer)){
    if(S_ISDIR(buffer.st_mode)){

      //Has to interpret this error as "File is a directory"
      errno = EISDIR;
      return -1;
    }
  }
  fd = open(path, realFlags, mode);
  if(-1 == fd){
    return -1;
  }
  fdflags = fcntl(fd, F_GETFD, 0);
  fcntl(fd, F_SETFD, fdflags | FD_CLOEXEC);
  return (int) fd;
}

int scalanative_file_descriptor_close(int fd){
    return close ((int) (fd));
}

char scalanative_separator_char(){
#ifdef _WIN32
  return '\\';
#else
  return '/';
#endif
}

char scalanative_path_separator_char(){
#ifdef _WIN32
  return ';';
#else
  return ':';
#endif
}

int scalanative_is_case_sensitive(){
#ifdef _WIN32
  return 0;
#else
  return 1;
#endif
}

//ported from hyfile_attr function in classlib/modules/portlib/src/main/native/port/unix/hyfile.c
int scalanative_file_attr(const char *path)
{
  struct stat buffer;

  if (stat(path, &buffer))
    {
      return -1;
    }
  if (S_ISDIR (buffer.st_mode))
    {
      return isDir;
    }
  return isFile;
}

const char * scalanative_get_os_encoding(){
  setlocale(LC_ALL, "");
  return nl_langinfo(CODESET);
}

const char * scalanative_get_temp_dir(){
  char const * folder = getenv("TMPDIR");
  if (folder == 0) folder = "/tmp";
  return folder;
}