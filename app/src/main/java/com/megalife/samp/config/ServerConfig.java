package com.megalife.samp.config;

/**
 * Hardcoded, immutable Server Configuration for Mega Life Roleplay.
 * All other servers and server lists are permanently disabled.
 */
public final class ServerConfig {
    public static final String SERVER_NAME = "Mega Life Roleplay";
    public static final String SERVER_HOST = "mega-life.top";
    public static final int SERVER_PORT = 7777;
    public static final String DISCORD_INVITE_URL = "https://discord.gg/nrfgsHCbZ";

    // Default CDN URLs for GTA SA Lite Game Cache & Custom TXD/DFF Mod Pack
    // Can also be updated dynamically in the launcher CDN Settings dialog.
    public static final String DEFAULT_GAME_CACHE_ZIP_URL = "https://mega-life.top/cdn/gtasa_megalife_cache.zip";
    public static final String DEFAULT_CUSTOM_MODS_ZIP_URL = "https://mega-life.top/cdn/megalife_mods_txd_dff.zip";

    private ServerConfig() {
        // Prevent instantiation
    }
}
