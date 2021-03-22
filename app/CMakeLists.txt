
cmake_minimum_required(VERSION 3.10.2)


project("opengl")

include_directories(src/main/cpp/include)

aux_source_directory(src/main/cpp source)

add_library(
        native-lib
        SHARED
        ${source})



set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/src/main/jniLibs/${CMAKE_ANDROID_ARCH_ABI}")


target_link_libraries(
        native-lib
        opencv_java4
        android
        SeetaFaceLandmarker
        SeetaNet
        log)