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

import flea.common.ISAACCipher;

public class PacketBuilder {
    public static final int OPCODE_LOGOUT = 165;
    public static final int OPCODE_DENY_LOGOUT = 183;

    public static void logout(NetworkStream stream, ISAACCipher isaacCipher) {
        stream.startPacket();
        stream.writeOpcode(OPCODE_LOGOUT, isaacCipher);
        stream.endPacket();
    }

    public static void denyLogout(NetworkStream stream, ISAACCipher isaacCipher) {
        stream.startPacket();
        stream.writeOpcode(OPCODE_DENY_LOGOUT, isaacCipher);
        stream.endPacket();
    }

}
