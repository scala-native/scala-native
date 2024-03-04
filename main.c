#include <stdlib.h>
#include <stdio.h>

extern void* nextTask();
int foo(){
  void* task = nextTask();
  int n = 0;
  while(task != NULL){
    n += 1;
    printf("%d\n", n);
  }
  return n;
}