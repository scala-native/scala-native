namespace nrt {
    class Exception {
        public:
            void* object;
    };
}

int f() {
    nrt::Exception* exc = new nrt::Exception;
    exc->object = 0;
    throw exc;
}
int g() {
    try {
        f();
        return 1;
    } catch (nrt::Exception& e) {
        return 2;
    }
    return 4;
}
int main(int argc, const char* argv[]) {
    g();
    return 0;
}
