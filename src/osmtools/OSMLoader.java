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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.jgrapht.graph.Pseudograph;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author demory
 */
public class OSMLoader {

  private Map<Long, Point2D.Double> nodePts_;
  private Set<OSMWay> ways_;
  private Map<Long, Integer> networkNodeIDs_;
  private Set<Long> vertexIDs_;
  private Set<NetworkEdge> edges_;

  private Pseudograph<Point2D, NetworkEdge> graph_;

  public OSMLoader() {

    nodePts_ = new HashMap<Long, Point2D.Double>();
    ways_ = new HashSet<OSMWay>();

    networkNodeIDs_ = new HashMap<Long, Integer>();

    vertexIDs_ = new HashSet<Long>();
    edges_ = new HashSet<NetworkEdge>();
  }

  public StreetGraph buildGraph(String filename, CoordConverter cc) {

    File file = new File(filename);
    if(!file.exists()) {
      System.out.println("could not load file: "+filename);
      System.exit(0);
    }

    showMem();
    readNodes(file, cc);
    showMem();
    readWays(file);
    showMem();
    splitWays();
    showMem();
    //writeEdgeShapefile();
    constructGraph();
    showMem();

    return new StreetGraph(graph_);
  }

  public void showMem() {
    long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    System.out.println("mem usage: "+usedMem);
  }
  public int countNodes(File file) {
    int c =0;
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(fileInputStream);

      while (xmlStreamReader.hasNext()) {
        int eventCode = xmlStreamReader.next();
        switch (eventCode) {
          case 1 :
            if(xmlStreamReader.getLocalName().equals("node")) {
              c++;
              //if(c % 10000 == 0) System.out.println("nodes="+c);
            }
        }
      }
      xmlStreamReader.close();

    } catch(Exception e) {
      e.printStackTrace();
    }
    return c;
  }

  public void readNodes(File file,  CoordConverter cc) {
    System.out.println("reading nodes..");
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(fileInputStream);

      while (xmlStreamReader.hasNext()) {
        int eventCode = xmlStreamReader.next();
        switch (eventCode) {
          case XMLStreamReader.START_ELEMENT:
            if(xmlStreamReader.getLocalName().equals("node")) {
              long id = new Long(xmlStreamReader.getAttributeValue("", "id"));
              double lat = new Double(xmlStreamReader.getAttributeValue("", "lat"));
              double lon = new Double(xmlStreamReader.getAttributeValue("", "lon"));
              nodePts_.put(id, cc.convert(new Point2D.Double(lat, lon)));
            }
        }
      }
      xmlStreamReader.close();

      System.out.println("read "+nodePts_.size()+" nodes");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void readWays(File file) {
    System.out.println("reading ways..");
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(fileInputStream);

      boolean inWayElement = false, isRelevantWay = false;
      List<Long> nodeIDs = null;
      String wayType = null, wayName = null;
      while (xmlStreamReader.hasNext()) {
        int eventCode = xmlStreamReader.next();
        switch (eventCode) {
          case XMLStreamReader.START_ELEMENT:
            if(xmlStreamReader.getLocalName().equals("way")) {
              inWayElement = true;
              isRelevantWay = false;
              nodeIDs = new LinkedList<Long>();
            }
            else if(xmlStreamReader.getLocalName().equals("nd") && inWayElement == true) {
              long id = new Long(xmlStreamReader.getAttributeValue("", "ref"));
              nodeIDs.add(id);
            }
            else if(xmlStreamReader.getLocalName().equals("tag") && inWayElement == true) {
              String k = xmlStreamReader.getAttributeValue("", "k");
              String v = xmlStreamReader.getAttributeValue("", "v");
              if(k.equals("highway")) {
                isRelevantWay = true;
                wayType = v;
              }
              if(k.equals("name")) {
                wayName = v;
              }
            }
            break;
          case XMLStreamReader.END_ELEMENT:
            if(xmlStreamReader.getLocalName().equals("way")) {
              inWayElement = false;
              if(isRelevantWay) {
                for(long id : nodeIDs) {
                  if(networkNodeIDs_.containsKey(id))
                    networkNodeIDs_.put(id, networkNodeIDs_.get(id)+1);
                  else
                    networkNodeIDs_.put(id, 1);
                }

                ways_.add(new OSMWay(nodeIDs, wayName, wayType));
              }
            }
            break;
        }
      }

      xmlStreamReader.close();

      System.out.println("read "+ways_.size()+" ways");
      System.out.println("visited "+networkNodeIDs_.size() + " nodes");

      for(Map.Entry<Long, Integer> e : networkNodeIDs_.entrySet())
        if(e.getValue() > 1) vertexIDs_.add(e.getKey()); //vertexCount++;

      System.out.println("graph vertex count = "+vertexIDs_.size());


    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void splitWays() {
    System.out.println("splitting ways..");
    for(OSMWay way : ways_) {
      int i = 0;
      long start = 0;
      List<Long> shapeNodes = new LinkedList<Long>();
      for(long nodeID : way.getNodeIDs()) {
        if(i==0) { // first node
          if(!vertexIDs_.contains(nodeID)) vertexIDs_.add(nodeID);
          start = nodeID;
        }
        else if(i==way.getNodeIDs().size()-1) { // last node
          if(!vertexIDs_.contains(nodeID)) vertexIDs_.add(nodeID);
          edges_.add(new NetworkEdge(start, nodeID, nodeIDsToPoint2Ds(shapeNodes), way));
        }
        else if(vertexIDs_.contains(nodeID)) {
          edges_.add(new NetworkEdge(start, nodeID, nodeIDsToPoint2Ds(shapeNodes), way));
          start = nodeID;
          shapeNodes = new LinkedList<Long>();
        }
        else {
          shapeNodes.add(nodeID);
        }
        i++;
      }
    }
    System.out.println("created "+edges_.size()+" split edges");
  }

  private List<Point2D> nodeIDsToPoint2Ds(List<Long> ids) {
    List<Point2D> pts = new LinkedList<Point2D>();
    for(long id : ids) {
      Point2D pt = nodePts_.get(id);
      if(pt != null) pts.add(pt);
    }
    return pts;
  }

  public void writeEdgeShapefile() {
    try {

      // CREATE FILE

      String name = "osm_edges";
      File file = new File("/home/demory/snapper/edges.shp");
      if(!file.exists()) file.createNewFile();

      // create the datastore
      ShapefileDataStore outStore = new ShapefileDataStore(file.toURI().toURL());

      // Tell this shapefile what type of data it will store
      SimpleFeatureType featureType = DataUtilities.createType(name, "geom:MultiLineString,id:Integer,from:Integer,to:Integer,type:String,name:String");
      outStore.createSchema(featureType);

      // initialize the CRS
      /*String crsCode = rm_.getEngine().getDataPkg().getProperty("crsCode");
      CoordinateReferenceSystem crs = CRS.decode(crsCode);
      //System.out.println("crs: "+crs.getName());
      outStore.forceSchemaCRS(crs);*/

      FeatureWriter outFeatureWriter = outStore.getFeatureWriter(outStore.getTypeNames()[0], Transaction.AUTO_COMMIT);

      // POPULATE THE DATA


      GeometryFactory gf = new GeometryFactory();
      //int totalItems = rm_.getEngine().getDataPkg().getBaseNet().linkCount(), numWritten = 0, pctWritten = 0, oldPctWritten;
      for(NetworkEdge edge : edges_) {

        Point2D startPt = nodePts_.get(edge.getStartID());
        Point2D endPt = nodePts_.get(edge.getEndID());
        if(startPt == null) {
          System.out.println("bad start node id = "+edge.getStartID());
          continue;
        }
        if(endPt == null) {
          System.out.println("bad end node id = "+edge.getStartID());
          continue;
        }
        Coordinate start = new Coordinate(startPt.getX(), startPt.getY());
        Coordinate end = new Coordinate(endPt.getX(), endPt.getY());

        //List<OSMNode> shpNodes = new LinkedList<Point2D.Double>();//edge.getShapeNodes();


        Coordinate[] coords = new Coordinate[2 + edge.getShapeNodes().size()];// shpNodes.size()];

        coords[0] = start;
        int spi = 1;
        for(Point2D pt : edge.getShapeNodes()) {
          if(pt == null) {
            coords = null;
            break;
          }
          coords[spi] = new Coordinate(pt.getX(), pt.getY());
          spi++;
        }
        if(coords == null) continue;
        coords[1 + edge.getShapeNodes().size()] = end;

        LineString line = gf.createLineString(coords);
        LineString[] lines = new LineString[1];
        lines[0] = line;

        MultiLineString multi = gf.createMultiLineString(lines);

        SimpleFeature feature = (SimpleFeature) outFeatureWriter.next();
        feature.setDefaultGeometry(multi);
        feature.setAttribute("id", 0);
        feature.setAttribute("from", edge.getStartID());
        feature.setAttribute("to", edge.getEndID());
        feature.setAttribute("type", edge.getType());
        feature.setAttribute("name", edge.getName());

        outFeatureWriter.write();

      }

      outFeatureWriter.close();
      System.out.println("Shapefile written");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void constructGraph() {
    System.out.println("constructing graph..");
    graph_ = new Pseudograph<Point2D, NetworkEdge>(NetworkEdge.class);

    for(Long id : vertexIDs_) {
      Point2D pt = nodePts_.get(id);
      if(pt == null) {
        System.out.println("bad node id = "+id);
        continue;
      }
      graph_.addVertex(pt);
    }

    for(NetworkEdge edge : edges_) {

      Point2D startPt = nodePts_.get(edge.getStartID());
      Point2D endPt = nodePts_.get(edge.getEndID());
      if(startPt == null) {
        System.out.println("bad start node id = "+edge.getStartID());
        continue;
      }
      if(endPt == null) {
        System.out.println("bad end node id = "+edge.getStartID());
        continue;
      }

      graph_.addEdge(startPt, endPt, edge);
    }

    System.out.println("graph constructed");

  }

}
