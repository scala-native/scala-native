int scalanative_compare_and_swap_int(int* ptr, int oldValue, int newValue) {
  return __sync_bool_compare_and_swap(ptr, oldValue, newValue);
}