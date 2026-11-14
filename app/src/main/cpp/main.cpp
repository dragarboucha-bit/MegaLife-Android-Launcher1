#include "main.h"
#include "settings.h"
#include "netgame.h"
#include <memory>

const char* g_pszStorage = nullptr;
static std::unique_ptr<CSettings> g_pSettings;
static std::unique_ptr<CNetGame> g_pNetGame;

extern "C" {

JNIEXPORT void JNICALL
Java_com_megalife_samp_game_GTASA_nativeInitSampEngine(
        JNIEnv* env,
        jobject /*thiz*/,
        jstring gameRootDir,
        jstring playerNickname,
        jstring /*lockedHost*/,
        jint /*lockedPort*/) {

    const char* szRoot = env->GetStringUTFChars(gameRootDir, nullptr);
    const char* szNick = env->GetStringUTFChars(playerNickname, nullptr);

    LOGI("Initializing Mega Life Roleplay Native SA-MP Client...");
    LOGI("Game Directory: %s", szRoot);

    g_pSettings = std::make_unique<CSettings>(szRoot, szNick);
    g_pNetGame = std::make_unique<CNetGame>(
            MEGALIFE_SERVER_HOST,
            MEGALIFE_SERVER_PORT,
            g_pSettings->Get().szNickName,
            g_pSettings->Get().szPassword
    );

    env->ReleaseStringUTFChars(gameRootDir, szRoot);
    env->ReleaseStringUTFChars(playerNickname, szNick);
}

JNIEXPORT jstring JNICALL
Java_com_megalife_samp_game_GTASA_nativeGetLockedEndpoint(JNIEnv* env, jobject /*thiz*/) {
    char buf[128];
    std::snprintf(buf, sizeof(buf), "%s:%d", MEGALIFE_SERVER_HOST, MEGALIFE_SERVER_PORT);
    return env->NewStringUTF(buf);
}

JNIEXPORT void JNICALL
Java_com_megalife_samp_game_GTASA_nativeOnSurfaceCreated(JNIEnv* /*env*/, jobject /*thiz*/) {
    LOGI("Surface created: Mega Life Roleplay OpenGL ES 2.0 context ready.");
    if (g_pNetGame) {
        g_pNetGame->Process();
    }
}

JNIEXPORT void JNICALL
Java_com_megalife_samp_game_GTASA_nativeOnSurfaceDestroyed(JNIEnv* /*env*/, jobject /*thiz*/) {
    LOGI("Surface destroyed: cleaning up active render state.");
}

} // extern "C"
