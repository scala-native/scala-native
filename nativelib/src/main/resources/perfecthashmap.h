typedef struct PerfectHashMap {
	int size;
	int* salts;
	int* keys;
	void** values;

} PerfectHashMap;