# 配置CMake
# https://developer.android.com/studio/projects/configure-cmake?hl=zh-cn#add-ndk-api
project("app")
cmake_minimum_required(VERSION 3.22.1)

add_library(
        native-lib
        SHARED
        src/main/cpp/native-lib.cpp
        src/main/cpp/include/my_gl_renderer.cpp
        src/main/cpp/include/gl_bitmap.cpp
        src/main/cpp/include/matrix.cpp
        src/main/cpp/include/log_util.cpp
)

# Specifies a path to native header files.
include_directories(src/main/cpp/include/)

#find_library(
#        log-lib
#        log
#)

# 原生API https://developer.android.com/ndk/guides/stable_apis?hl=zh-cn
target_link_libraries(
        native-lib
        log
        GLESv2
        EGL
        jnigraphics
)