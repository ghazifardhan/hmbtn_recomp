#!/usr/bin/env bash
export PSX_FPS_TELEMETRY=1
export PSX_RENDER_THREAD=1
export PSX_PRESENT_THREAD=1
export PSX_VSYNC=0
export SDL_VIDEO_MINIMIZE_ON_FOCUS_LOSS=0
export __GL_SYNC_TO_VBLANK=0
exec "$(dirname "$0")/build/Harvest_Moon___Back_to_Nature_Recompiled" "$@"
