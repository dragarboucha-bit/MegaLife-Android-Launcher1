#pragma once

#include "main.h"

struct stSettings {
    char szNickName[32];
    char szHost[64];
    int iPort;
    char szPassword[64];
    char szFont[32];
    float fFontSize;
    int iFontOutline;
    int iChatMaxLines;
    int iFPS;
};

class CSettings {
public:
    CSettings(const char* szStoragePath, const char* szOverrideNick = nullptr);
    ~CSettings() = default;

    const stSettings& Get() const { return m_Settings; }
    void EnforceMegaLifeLock();

private:
    stSettings m_Settings;
};
