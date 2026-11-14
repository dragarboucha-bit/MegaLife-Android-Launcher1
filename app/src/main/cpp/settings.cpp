#include "settings.h"
#include <cstring>
#include <cstdio>

CSettings::CSettings(const char* szStoragePath, const char* szOverrideNick) {
    std::memset(&m_Settings, 0, sizeof(stSettings));

    if (szOverrideNick && std::strlen(szOverrideNick) > 0) {
        std::strncpy(m_Settings.szNickName, szOverrideNick, sizeof(m_Settings.szNickName) - 1);
    } else {
        std::strncpy(m_Settings.szNickName, "MegaLife_Player", sizeof(m_Settings.szNickName) - 1);
    }

    // Hardcode font and performance defaults
    std::strncpy(m_Settings.szFont, "Arial.ttf", sizeof(m_Settings.szFont) - 1);
    m_Settings.fFontSize = 30.0f;
    m_Settings.iFontOutline = 2;
    m_Settings.iChatMaxLines = 12;
    m_Settings.iFPS = 60;

    // Permanently lock Host & Port to Mega Life Roleplay regardless of external INI edits
    EnforceMegaLifeLock();
}

void CSettings::EnforceMegaLifeLock() {
    std::strncpy(m_Settings.szHost, MEGALIFE_SERVER_HOST, sizeof(m_Settings.szHost) - 1);
    m_Settings.iPort = MEGALIFE_SERVER_PORT;
    m_Settings.szPassword[0] = '\0';
    LOGI("CSettings: Locked server target -> %s:%d (Player: %s)",
         m_Settings.szHost, m_Settings.iPort, m_Settings.szNickName);
}
