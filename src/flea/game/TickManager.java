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

public class TickManager {
    private long m_tickRate;
    private long m_tickNext;
    private long m_tickCount;

    public void setTickRate(long tickRate) {
        m_tickRate = tickRate;
    }

    public void reset() {
        m_tickNext = System.currentTimeMillis() + m_tickNext;
        m_tickCount = 0;
    }

    public long update() {
        long elapsed = System.currentTimeMillis() - m_tickNext;
        if (elapsed >= 0) {
            long tickSkip = 1 + (elapsed / m_tickRate);
            m_tickNext += tickSkip * m_tickRate;
            m_tickCount++;
        }
        return elapsed;
    }

    public long getTickCount() {
        return m_tickCount;
    }
}
