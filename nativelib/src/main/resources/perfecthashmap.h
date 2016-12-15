typedef struct PerfectHashMap {
	int size;
	int* keys;
	void** values;

} PerfectHashMap;