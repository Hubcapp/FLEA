/**
 * FLEA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors: Based on rscminus server by Ornox, <https://github.com/RSCPlus/rscminus>
 *          FLEA was created by Logg aka Hubcapp, <https://github.com/Hubcapp/FLEA>
 *          In preparation for OpenRSC, <https://gitlab.com/open-runescape-classic/FLEA>
 */

package flea.game;

import flea.common.*;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server implements Runnable {
    private static Server m_instance;
    private static Thread m_thread;

    private ServerSocketChannel m_socket;
    private boolean m_running;
    private String m_name;
    private PlayerManager m_playerManager;
    private TickManager m_tickManager;

    // Constants
    public static final int DEFAULT_PORT = 43594;
    public static final int DEFAULT_PLAYER_MAX = 5000;
    public static final int DEFAULT_TICK_RATE = 640;
    public static final String DEFAULT_SERVER_NAME = "RuneScape";

    public Server() {
        m_running = true;
        m_playerManager = new PlayerManager();
        m_tickManager = new TickManager();
    }

    public void run() {
        // Initialize FLEA
        Crypto.init();
        System.out.println("exponent: " + Crypto.getPublicExponent());
        System.out.println("modulus: " + Crypto.getPublicModulus());

        // Initialize server
        m_name = DEFAULT_SERVER_NAME;
        m_playerManager.init(DEFAULT_PLAYER_MAX);
        m_tickManager.setTickRate(DEFAULT_TICK_RATE);

        try {
            m_socket = ServerSocketChannel.open();
            m_socket.bind(new InetSocketAddress(DEFAULT_PORT));
            m_socket.configureBlocking(false);
        } catch (Exception e) {
            m_running = false;
            e.printStackTrace();
        }
        System.out.println("Server started");

        m_tickManager.reset();
        while (m_running) {
            try {
                SocketChannel socket = m_socket.accept();
                if (socket != null) {
                    socket.configureBlocking(false);
                    m_playerManager.addQueuedPlayer(socket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            long elapsed = m_tickManager.update();
            if (elapsed >= 0) {
                // Handle players logging in
                m_playerManager.processLoginQueue();

                // Process players
                m_playerManager.processIncomingPackets();
                m_playerManager.process();

                // Process these last
                m_playerManager.processDisconnect();
                m_playerManager.processOutgoingPackets();

            }
            Sleep.sleep(1);
        }

        try {
            m_socket.close();
        } catch (Exception e) {
        }


        System.out.println("Server exited successfully");
    }

    public String getName() {
        return m_name;
    }

    public PlayerManager getPlayerManager() {
        return m_playerManager;
    }

    public static Server getInstance() {
        return m_instance;
    }

    public static void main(String args[]) {
        m_instance = new Server();
        m_thread = new Thread(m_instance);
        m_thread.start();
    }

    public void shutdownGracefully() {
        m_running = false;
    }
}
