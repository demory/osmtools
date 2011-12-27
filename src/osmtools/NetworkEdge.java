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
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 *
 * @author demory
 */
public class NetworkEdge {

  private long startID_, endID_;
  private List<Point2D> shapeNodes_;
  private OSMWay way_;
  //private String name_, type_;

  public NetworkEdge(long startID, long endID, List<Point2D> shapeNodes, OSMWay way) { //String name, String type) {
    startID_ = startID;
    endID_ = endID;
    shapeNodes_ = shapeNodes;
    way_ = way;
    //name_ = name;
    //type_ = type;
  }

  public long getStartID() {
    return startID_;
  }

  public long getEndID() {
    return endID_;
  }

  public List<Point2D> getShapeNodes() {
    return shapeNodes_;
  }

  public String getName() {
    return way_.getName();
  }

  public String getType() {
    return way_.getType();
  }

  public Rectangle2D getBoundingBox() {
    return null;
  }

}
