#pragma once

#include "main.h"

class CNetGame {
public:
    CNetGame(const char* szHostOrIp, int iPort, const char* szPlayerName, const char* szPass);
    ~CNetGame();

    void Process();
    const char* GetLockedHost() const { return m_szHost; }
    int GetLockedPort() const { return m_iPort; }

private:
    char m_szHost[64];
    char m_szPlayerName[32];
    int m_iPort;
    bool m_bConnected;
};
