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
import java.awt.geom.Point2D.Double;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 *
 * @author demory
 */
public class CoordTransformConverter implements CoordConverter {

  private MathTransform mt_;

  public CoordTransformConverter(String sourceCRSCode, String destCRSCode) {
    try {
      CoordinateReferenceSystem sourceCRS = CRS.decode(sourceCRSCode);
      CoordinateReferenceSystem destCRS = CRS.decode(destCRSCode);
      mt_ = CRS.findMathTransform(sourceCRS, destCRS);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public Point2D.Double convert(Double pt) {
    DirectPosition2D srcDP = new DirectPosition2D(pt), destDP = new DirectPosition2D();
    try {
      mt_.transform(srcDP, destDP);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return (Point2D.Double) destDP.toPoint2D();
  }


}
