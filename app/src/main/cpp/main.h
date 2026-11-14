#pragma once

#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "MEGALIFE_SAMP"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Immutable Mega Life Roleplay Server Constants (Enforced at C++ Binary Level)
#define MEGALIFE_SERVER_HOST "mega-life.top"
#define MEGALIFE_SERVER_PORT 7777
#define MEGALIFE_DISCORD_URL "https://discord.gg/nrfgsHCbZ"

extern const char* g_pszStorage;
