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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author demory
 */
public class GTFSShapeLoader {


  public Map<Integer, List<Point2D>> buildMap(String filename, CoordConverter cc) {

    File file = new File(filename);
    if(!file.exists()) {
      System.out.println("could not read "+filename);
      return null;
    }
    String line = null;

    try {
      System.out.println("Reading GTFS file: "+file.getName());
      BufferedReader reader = new BufferedReader(new FileReader(file));
      line = reader.readLine(); // read the header line

      Map<Integer, Map<Integer,Point2D>> workingMap = new HashMap<Integer, Map<Integer, Point2D>>();
      while((line = reader.readLine()) != null) {
        String[] split = line.split(",");
        int shapeID = new Integer(split[0]);
        Point2D pt = cc.convert(new Point2D.Double(Double.parseDouble(split[1]), Double.parseDouble(split[2])));
        int seq = new Integer(split[3]);
        if(workingMap.containsKey(shapeID)) {
          workingMap.get(shapeID).put(seq, pt);
        }
        else {
          Map<Integer, Point2D> newPtMap = new HashMap<Integer, Point2D>();
          newPtMap.put(seq, pt);
          workingMap.put(shapeID, newPtMap);
        }
      }

      Map<Integer, List<Point2D>> finalMap = new HashMap<Integer, List<Point2D>>();
      for(Map.Entry<Integer, Map<Integer, Point2D>> entry : workingMap.entrySet()) {
        finalMap.put(entry.getKey(), new ArrayList<Point2D>(entry.getValue().values()));
      }

      return finalMap;

    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}
