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
import java.util.List;
import java.util.Map;

/**
 * Usage: GraphSplitter path/to/file.osm destCRScode
 * @author demory
 */
public class GraphSplitter {
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    
    OSMLoader loader = new OSMLoader();
    System.out.println(args[1]);
    CoordConverter cc = new CoordTransformConverter("EPSG:4326", args[1]);

    StreetGraph graph = loader.buildGraph(args[0], cc);
  }
}
