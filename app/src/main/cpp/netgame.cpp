#include "netgame.h"
#include <cstring>

CNetGame::CNetGame(const char* /*szHostOrIp*/, int /*iPort*/, const char* szPlayerName, const char* /*szPass*/) {
    // Strictly ignore any third-party IP/Port arguments and lock to mega-life.top:7777
    std::strncpy(m_szHost, MEGALIFE_SERVER_HOST, sizeof(m_szHost) - 1);
    m_iPort = MEGALIFE_SERVER_PORT;
    std::strncpy(m_szPlayerName, szPlayerName ? szPlayerName : "MegaLife_Player", sizeof(m_szPlayerName) - 1);
    m_bConnected = true;

    LOGI("CNetGame initialized exclusively for Mega Life Roleplay (%s:%d) with nickname %s",
         m_szHost, m_iPort, m_szPlayerName);
}

CNetGame::~CNetGame() {
    m_bConnected = false;
}

void CNetGame::Process() {
    // Core RakNet / SA-MP packet processing loop hook
}
