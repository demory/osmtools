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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Usage: PolyGen path/to/stops_dir output_poly_file
 * @author demory
 */
public class PolyGen {
  public static void main(String[] args) {
    
    CoordConverter cc = new CoordLatLonConverter();
    GTFSStopLoader stopLoader = new GTFSStopLoader();

    GeometryFactory gf = new GeometryFactory();
    Geometry mainPoly = null;

    File stopDir = new File(args[0]);

    for(File file : stopDir.listFiles()) {
      if(file.isDirectory()) continue;
      System.out.println("file="+file.getName());
      Map<String, Point2D> stopMap = stopLoader.buildMap(file, cc);

      int si = 1;
      for(Point2D pt : stopMap.values()) {
        //if(si % 100 == 0) System.out.println("stop "+si+" of "+stopMap.size());
        si++;

        Coordinate coord = new Coordinate(pt.getX(), pt.getY());
        Geometry bufferedPt = gf.createPoint(coord).buffer(0.04);
        if(mainPoly == null) mainPoly = bufferedPt;
        else mainPoly = mainPoly.union(bufferedPt);
      }
    }
    
    System.out.println("gnp="+mainPoly.getNumPoints());
    System.out.println("type="+mainPoly.getGeometryType());


    try {
      FileWriter writer = new FileWriter(args[1]);
      writer.write("pacificnw\n");

      Set<Polygon> polys = new HashSet<Polygon>();

      if(mainPoly.getGeometryType().equals("Polygon")) {
        polys.add((Polygon) mainPoly);
      }
      if(mainPoly.getGeometryType().equals("MultiPolygon")) {
        MultiPolygon mpoly = (MultiPolygon) mainPoly;        
        for(int pi = 0; pi < mpoly.getNumGeometries(); pi++)
          polys.add((Polygon) mpoly.getGeometryN(pi));
      }
      
      int polyId = 1;
      for(Polygon poly : polys) {
        writer.write((polyId++)+"\n");
        for(Coordinate coord : poly.getExteriorRing().getCoordinates()) {
          writer.write("    "+coord.x+"    "+coord.y+"\n");
        }
        writer.write("END\n");

        if(poly.getNumInteriorRing() > 0) {
          for(int iri=0; iri < poly.getNumInteriorRing(); iri++) {
            writer.write("!"+(polyId++)+"\n");
            for(Coordinate coord : poly.getInteriorRingN(iri).getCoordinates()) {
              writer.write("    "+coord.x+"    "+coord.y+"\n");
            }
            writer.write("END\n");
          }
        }
      }
      writer.write("END\n");
      writer.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }
}
