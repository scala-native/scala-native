cmake_minimum_required(VERSION 3.6)
project(immix)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES
        datastructures/Bitmap.c
        datastructures/Bitmap.h
        datastructures/BlocList.c
        datastructures/BlocList.h
        datastructures/Stack.c
        datastructures/Stack.h
        headers/BlockHeader.h
        headers/LineHeader.h
        headers/ObjectHeader.h
        utils/MathUtils.h
        Allocator.c
        Allocator.h
        Block.c
        Block.h
        Constants.h
        GCTypes.h
        Heap.c
        Heap.h
        ImmixGC.c
        LargeAllocator.c
        LargeAllocator.h
        Line.c
        Line.h
        Log.h
        Marker.c
        Marker.h
        Object.c
        Object.h stats/AllocatorStats.c stats/AllocatorStats.h State.c State.h)

add_executable(immix ${SOURCE_FILES})