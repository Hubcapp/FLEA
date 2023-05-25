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

package rscminus.game;

import rscminus.common.ISAACCipher;

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
