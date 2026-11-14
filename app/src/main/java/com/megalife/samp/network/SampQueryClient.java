package com.megalife.samp.network;

import com.megalife.samp.config.ServerConfig;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Implements the official SA-MP / open.mp UDP Query Protocol ('i' and 'p' opcodes)
 * locked exclusively to mega-life.top:7777.
 */
public class SampQueryClient {

    public static class ServerStatus {
        public final boolean online;
        public final int players;
        public final int maxPlayers;
        public final int pingMs;
        public final String hostname;
        public final String gamemode;
        public final String language;

        public ServerStatus(boolean online, int players, int maxPlayers, int pingMs,
                            String hostname, String gamemode, String language) {
            this.online = online;
            this.players = players;
            this.maxPlayers = maxPlayers;
            this.pingMs = pingMs;
            this.hostname = hostname;
            this.gamemode = gamemode;
            this.language = language;
        }

        public static ServerStatus offline() {
            return new ServerStatus(
                    false,
                    0,
                    0,
                    -1,
                    ServerConfig.SERVER_NAME,
                    "Server unreachable or under maintenance",
                    "-"
            );
        }
    }

    public static ServerStatus queryMegaLifeServer(int timeoutMs) {
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getByName(ServerConfig.SERVER_HOST);
            byte[] ipBytes = address.getAddress();
            int port = ServerConfig.SERVER_PORT;

            socket = new DatagramSocket();
            socket.setSoTimeout(timeoutMs);

            // Build SA-MP 'i' (Information) query packet:
            // "SAMP" (4 bytes) + IP (4 bytes) + Port (2 bytes LE) + 'i' (1 byte) = 11 bytes
            byte[] packetData = buildQueryPacket(ipBytes, port, (byte) 'i');
            DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, address, port);

            long startTime = System.currentTimeMillis();
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            int pingMs = (int) Math.max(1L, System.currentTimeMillis() - startTime);

            int length = receivePacket.getLength();
            if (length < 11) {
                return ServerStatus.offline();
            }

            ByteBuffer buffer = ByteBuffer.wrap(receiveBuffer, 0, length).order(ByteOrder.LITTLE_ENDIAN);
            // Skip 11-byte header ("SAMP" + 4-byte IP + 2-byte Port + 'i')
            buffer.position(11);

            byte passworded = buffer.get();
            int players = buffer.getShort() & 0xFFFF;
            int maxPlayers = buffer.getShort() & 0xFFFF;

            String hostname = readLengthPrefixedString(buffer);
            String gamemode = readLengthPrefixedString(buffer);
            String language = readLengthPrefixedString(buffer);

            if (hostname == null || hostname.trim().isEmpty()) {
                hostname = ServerConfig.SERVER_NAME;
            }
            if (gamemode == null || gamemode.trim().isEmpty()) {
                gamemode = "Mega Life Roleplay Official";
            }

            return new ServerStatus(true, players, maxPlayers, pingMs, hostname, gamemode, language);

        } catch (Exception e) {
            return ServerStatus.offline();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private static byte[] buildQueryPacket(byte[] ipBytes, int port, byte opcode) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(11);
        out.write(new byte[]{'S', 'A', 'M', 'P'});
        out.write(ipBytes, 0, 4);
        out.write((byte) (port & 0xFF));
        out.write((byte) ((port >> 8) & 0xFF));
        out.write(opcode);
        return out.toByteArray();
    }

    private static String readLengthPrefixedString(ByteBuffer buffer) {
        if (buffer.remaining() < 4) return "";
        int strLen = buffer.getInt();
        if (strLen <= 0 || strLen > buffer.remaining()) return "";
        byte[] strBytes = new byte[strLen];
        buffer.get(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }
}
