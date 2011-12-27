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

import com.vividsolutions.jts.geom.Geometry;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.graph.Pseudograph;

/**
 *
 * @author demory
 */
public class StreetGraph {

  private Pseudograph<Point2D, NetworkEdge> graph_;

  public StreetGraph(Pseudograph<Point2D, NetworkEdge> graph) {
    graph_ = graph;
  }

  public Pseudograph<Point2D, NetworkEdge> getGraph() {
    return graph_;
  }

  public Set<NetworkEdge> getEdges(Rectangle2D rect) {
    Set<NetworkEdge> edges = new HashSet<NetworkEdge>();
    for(Point2D pt : graph_.vertexSet()) {
      if(rect.contains(pt)) {
        edges.addAll(graph_.edgesOf(pt));
      }
    }
    System.out.println("edges found: "+edges.size());
    return edges;
  }


}
