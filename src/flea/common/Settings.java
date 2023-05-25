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

package flea.common;

import java.net.URL;

public class Settings {
  // rscminus version number
  public static String versionNumber = "0.3.0a";

  // Logger Settings
  public static int LOG_VERBOSITY = 3;
  public static boolean COLORIZE_CONSOLE_TEXT = true;
  public static boolean LOG_FORCE_LEVEL = true;
  public static boolean LOG_SHOW_LEVEL = true;
  public static boolean LOG_FORCE_TIMESTAMPS = true;
  public static boolean LOG_SHOW_TIMESTAMPS = true;


  // Utils that probably don't belong in settings, but are in Settings.java in RSC+
  public static class Dir {

    public static String JAR;
  }

  public static void initDir() {
    Dir.JAR = ".";
    try {
      Dir.JAR =
              Settings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      int indexFileSep1 = Dir.JAR.lastIndexOf('/');
      int indexFileSep2 = Dir.JAR.lastIndexOf('\\');
      int index = (indexFileSep1 > indexFileSep2) ? indexFileSep1 : indexFileSep2;
      if (index != -1) Dir.JAR = Dir.JAR.substring(0, index);
    } catch (Exception e) {
    }
  }

  public static URL getResource(String fileName) { // TODO: Consider moving to a more relevant place
    URL url = null;
    try {
      url = Settings.class.getResource(fileName);
      return url;
    } catch (Exception e) {
      Logger.Debug("Couldn't load resource from jar " + fileName);
      e.printStackTrace();
    }

    // Try finding assets
    try {
      url = new URL("file://" + FileUtil.findDirectoryReverse("/assets") + fileName);
    } catch (Exception e) {
    }
    Logger.Info("Loading resource: " + fileName);

    return url;
  }
}
