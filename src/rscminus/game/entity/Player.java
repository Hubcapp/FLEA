/**
 * rscminus
 *
 * This file is part of rscminus.
 *
 * rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * Authors: see <https://github.com/OrN/rscminus>
 */

package rscminus.game.entity;

import rscminus.common.*;
import rscminus.game.*;
import rscminus.game.data.LoginInfo;

import java.nio.channels.SocketChannel;

public class Player extends Entity {
    private NetworkStream m_incomingStream;
    private byte[] m_incomingStream_buffer_copy;
    private NetworkStream m_outgoingStream;
    private byte[] m_outgoingStream_buffer_copy;
    private NetworkStream m_packetStream;
    private LoginInfo m_loginInfo;
    private ISAACCipher m_isaacIncoming;
    private ISAACCipher m_isaacOutgoing;
    private PlayerManager m_playerManager;
    private SocketChannel m_socket;


    // Tick update variables
    private boolean m_tickRequestLogout;

    // Packet opcodes
    public static final int OPCODE_DISCONNECT = 31;
    public static final int OPCODE_KEEPALIVE = 67;
    public static final int OPCODE_LOGOUT = 102;

    // Game state
    private int m_index;
    private boolean m_loggedIn;
    private boolean m_loggedOut;

    public Player(int index, PlayerManager playerManager) {
        m_index = index;
        m_playerManager = playerManager;
        m_incomingStream = new NetworkStream();
        m_outgoingStream = new NetworkStream();
        m_packetStream = new NetworkStream();
        m_isaacIncoming = new ISAACCipher();
        m_isaacOutgoing = new ISAACCipher();
    }

    public void reset() {
        m_incomingStream.flip();
        m_outgoingStream.flip();
        m_packetStream.flip();
        m_loggedIn = false;
        m_loggedOut = false;
        m_isaacOutgoing.reset();
        m_isaacIncoming.reset();
        setActive(false);
        setSocket(null);

        // Game state
        m_tickRequestLogout = false;
    }

    public void setLoginInfo(LoginInfo loginInfo) {
        m_loginInfo = loginInfo;
        m_isaacIncoming.setKeys(m_loginInfo.keys);
        m_isaacOutgoing.setKeys(m_loginInfo.keys);
        m_loggedIn = false;
    }


    public SocketChannel getSocket() {
        return m_socket;
    }

    public String getUsername() {
        return m_loginInfo.username;
    }

    public LoginInfo getLoginInfo() {
        return m_loginInfo;
    }

    public int getIndex() {
        return m_index;
    }

    public ISAACCipher getISAACCipher() {
        return m_isaacOutgoing;
    }

    public NetworkStream getNetworkStream() {
        return m_outgoingStream;
    }

    public void closeSocket() {
        if (m_socket != null) {
            SocketUtil.close(m_socket);
            m_socket = null;
        }
    }

    public void setSocket(SocketChannel socket) {
        if (m_socket != null)
            SocketUtil.close(m_socket);
        m_socket = socket;
    }

    public boolean canLogout() {
        return true;
    }

    public void process() {
        if (!m_loggedIn) {
            m_loggedIn = true;
        }
    }

    public void processDisconnect() {
        if (!canLogout()) {
            if (m_socket != null && m_tickRequestLogout) {
                PacketBuilder.denyLogout(m_outgoingStream, m_isaacOutgoing);
                m_tickRequestLogout = false;
            }
            return;
        }

        // User is disconnected
        if (m_socket == null) {
            m_loggedOut = true;
            return;
        }

        if (m_tickRequestLogout) {
            PacketBuilder.logout(m_outgoingStream, m_isaacOutgoing);
            m_loggedOut = true;
        }
    }

    public void processIncomingPackets() {
        m_incomingStream.fill(m_socket);

        // TODO: probably don't have to copy entire buffer
        m_incomingStream_buffer_copy = new byte[5000];
        System.arraycopy(m_incomingStream.getByteArray(), 0, m_incomingStream_buffer_copy, 0, m_incomingStream.getByteArray().length);

        // TODO: perhaps handle all ISAAC in FLEA

        int length;
        while ((length = m_incomingStream.readPacket(m_packetStream)) > 0) {
            int opcode = m_packetStream.readOpcode(m_isaacIncoming);

            // Handle incoming packets
            switch (opcode) {
            case OPCODE_DISCONNECT:
                break;
            case OPCODE_KEEPALIVE:
                break;
            case OPCODE_LOGOUT:
                m_tickRequestLogout = true;
                break;
            default:
                System.out.println("undefined opcode: " + opcode + ", length: " + length);
                while (length-- > 0) {
                    System.out.print(m_packetStream.readByte() + " ");
                }
                System.out.println();
                break;
            }
        }
    }

    public void processOutgoingPackets() {
        m_outgoingStream.flush(m_socket);

        // Player is logged out, remove them from the player list
        if (m_loggedOut)
            m_playerManager.removePlayer(m_index);
    }
}
