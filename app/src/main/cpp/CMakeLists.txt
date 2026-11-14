cmake_minimum_required(VERSION 3.22.1)
project("samp")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    samp
    SHARED
    main.cpp
    settings.cpp
    netgame.cpp
)

find_library(log-lib log)
find_library(android-lib android)
find_library(glesv2-lib GLESv2)

target_link_libraries(
    samp
    ${log-lib}
    ${android-lib}
    ${glesv2-lib}
)
