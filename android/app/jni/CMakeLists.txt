cmake_minimum_required(VERSION 3.20)

project(HarvestMoonBTN LANGUAGES C CXX)
set(CMAKE_BUILD_TYPE Release CACHE STRING "Force Release" FORCE)
add_compile_options(-O3 -DNDEBUG -fomit-frame-pointer)

# Build SDL3 as shared library for Android SDLActivity
set(BUILD_SHARED_LIBS ON CACHE BOOL "" FORCE)
set(SDL_SHARED ON CACHE BOOL "" FORCE)
set(SDL_STATIC OFF CACHE BOOL "" FORCE)
add_subdirectory(SDL)

# Root project paths
set(GAME_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../..")
set(PSXRECOMP_ROOT "${GAME_ROOT}/psxrecomp" CACHE PATH "Path to psxrecomp")

set(PSX_RECOMP_UI OFF)
set(PSX_NETPLAY OFF)
set(PSX_SETUP_WIZARD OFF)
set(PSX_REWIND OFF)
set(PSX_DEBUG_TOOLS OFF)
set(BUILD_TESTING OFF)

include("${PSXRECOMP_ROOT}/runtime/runtime.cmake")
set_source_files_properties("${PSXRECOMP_ROOT}/runtime/src/mod_immediate.c" PROPERTIES COMPILE_OPTIONS "-O1")


set(PSX_PRELOADED_MODS "${GAME_ROOT}/mods/preloaded")
if(NOT IS_DIRECTORY "${PSX_PRELOADED_MODS}")
    set(PSX_PRELOADED_MODS "NONE")
endif()

psxrecomp_add_game_runtime(main
    PRELOADED_MODS_DIR "${PSX_PRELOADED_MODS}"
    WINDOW_TITLE "Harvest Moon - Back to Nature Recompiled"
    DEFAULT_GAME_CONFIG_PATH "game.toml"
    MAX_PLAYERS 2
    GEN_MARKER "${GAME_ROOT}/generated/SLUS_011.15_dispatch.c"
    GEN_FULL_GLOB "${GAME_ROOT}/generated/SLUS_011.15_full_*.c"
)
