/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package osmtools;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author demory
 */
public class GTFSStopLoader {

  public Map<String, Point2D> buildMap(String filename, CoordConverter cc) {
    File file = new File(filename);
    if(!file.exists()) {
      System.out.println("could not read "+filename);
      return null;
    }
    return buildMap(file, cc);
  }
  
  public Map<String, Point2D> buildMap(File file, CoordConverter cc) {

    String line = null;

    Map<String, Point2D> stopMap = new HashMap<String, Point2D>();

    try {
      System.out.println("Reading GTFS file: "+file.getName());
      BufferedReader reader = new BufferedReader(new FileReader(file));

      line = reader.readLine(); // read the header line
      String[] splitHeader = line.split(",");

      int idIndex=0, latIndex=0, lonIndex=0;
      for(int i=0; i<splitHeader.length; i++) {
        if(splitHeader[i].contains("stop_id")) idIndex = i;
        if(splitHeader[i].contains("stop_lat")) latIndex = i;
        if(splitHeader[i].contains("stop_lon")) lonIndex = i;
      }

      Map<Integer, Map<Integer,Point2D>> workingMap = new HashMap<Integer, Map<Integer, Point2D>>();
      while((line = reader.readLine()) != null) {
        try {
          String[] split = stripQuotedCommas(line).split(",");
          String stopID = split[idIndex];
          Point2D pt = cc.convert(new Point2D.Double(Double.parseDouble(split[latIndex]), Double.parseDouble(split[lonIndex])));
          stopMap.put(stopID, pt);
        } catch(Exception ex) {
          System.out.println("error reading line: "+line);  
          System.out.println("  "+ex.toString());
        }
      }

      return stopMap;

    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  private String stripQuotedCommas(String str) {
    String r = "";
    boolean inQuotes = false;
    for(int i=0; i<str.length(); i++) {
      if(str.charAt(i) == '\"') inQuotes = !inQuotes;
      else {
        if(str.charAt(i) == ',' && inQuotes) continue;
        r += str.charAt(i);
      }
    }
    return r;
  }
}
