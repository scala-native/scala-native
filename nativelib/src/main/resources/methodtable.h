typedef struct Method {
	char* signature;
	void* funcPtr;
} Method;


typedef struct MethodTable {
	int nbMethods;
	Method* methods;
} MethodTable;