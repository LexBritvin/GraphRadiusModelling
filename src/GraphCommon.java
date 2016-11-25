import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class GraphCommon {

    public static Set<Point2D> getPointsInCircle(Shape area, Shape obstacle, int vCount) {
        Random rand = new Random(System.currentTimeMillis());
        Set<Point2D> points = new HashSet<>();
        Rectangle2D areaBounds = area.getBounds2D();
        Rectangle2D obstacleBounds = obstacle.getBounds2D();
        // Generate vertices.
        while (points.size() != vCount) {
            // Calculate available distance from center.
            double theta = 2 * Math.PI * rand.nextDouble();
            double u = rand.nextDouble() + rand.nextDouble();
            double r = u > 1 ? 2 - u : u;
            double rx = r * Math.cos(theta);
            double ry = r * Math.sin(theta);

            // Generate point.
            double x = rx * areaBounds.getWidth() / 2;
            double y = ry * areaBounds.getHeight() / 2;
            Point2D point = new Point2D.Double(x, y);
            if (obstacle.contains(point)) {
                point.setLocation(x + Math.signum(x) * obstacleBounds.getWidth() / 2, y + Math.signum(y) * obstacleBounds.getHeight() / 2);
            }
            if (area.contains(point) && !obstacle.contains(point)) {
                points.add(point);
            }
        }

        return points;
    }

    public static Set<Point2D> getPointsInRectangle(Shape area, Shape obstacle, int vCount) {
        Random rand = new Random(System.currentTimeMillis());
        Set<Point2D> points = new HashSet<>();
        Rectangle2D areaBounds = area.getBounds2D();
        Rectangle2D obstacleBounds = obstacle.getBounds2D();
        // Generate vertices.
        while (points.size() != vCount) {
            // Calculate available distance from center.
            double x = (rand.nextDouble() * 2 - 1) * areaBounds.getWidth() / 2;
            double y = (rand.nextDouble() * 2 - 1) * areaBounds.getHeight() / 2;

            // Generate point.
            Point2D point = new Point2D.Double(x, y);
            if (obstacle.contains(point)) {
                point.setLocation(x + Math.signum(x) * obstacleBounds.getWidth() / 2, y + Math.signum(y) * obstacleBounds.getHeight() / 2);
            }
            if (area.contains(point) && !obstacle.contains(point)) {
                points.add(point);
            }
        }

        return points;
    }

    public static Graph<Point2D, DefaultEdge> generateGraphInArea(Set<Point2D> points, Shape obstacle, double deviceRadius) {
        // Generate graph vertices.
        Graph<Point2D, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        points.forEach(g::addVertex);
        // Add edges to graph.
        for (Point2D point1 : points) {
            for (Point2D point2 : points) {
                if (point1 != point2 && point1.distance(point2) <= deviceRadius) {
                    // Check if edge doesn't intersect obstacle.
                    Line2D line = new Line2D.Double(point1, point2);
                    if (!intersectsShape(line, obstacle)) {
                        g.addEdge(line.getP1(), line.getP2());
                    }
                }
            }
        }

        return g;
    }

    public static double calculateGraphRadiusDijkstra(Graph<Point2D, DefaultEdge> graph) {
        Set<Point2D> vertices = graph.vertexSet();
        Set<Pair<Point2D>> calculatedPaths = new HashSet<>();
        double graphRadius = Integer.MAX_VALUE;
        for (Point2D startVertex : vertices) {
            double vertexEccentricity = 0;
            for (Point2D endVertex : vertices) {
                if (startVertex != endVertex) {
                    Pair<Point2D> pathPair = new Pair<>(startVertex, endVertex);
                    if (!calculatedPaths.contains(pathPair)) {
                        List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, startVertex, endVertex);
                        if (path != null) {
                            vertexEccentricity = path.size() > vertexEccentricity ? path.size() : vertexEccentricity;
                        }
                        calculatedPaths.add(pathPair);
                    }
                }
            }
            // Update graph radius if eccentricity was calculated.
            graphRadius = vertexEccentricity > 0.0 && vertexEccentricity < graphRadius ? vertexEccentricity : graphRadius;
        }
        // Graph radius is infinite when the graph is disconnected.
        return graphRadius;
    }

    public static double calculateGraphRadiusFloyd(Graph<Point2D, DefaultEdge> graph) {
        MyPinkFloyd<Point2D, DefaultEdge> floyd = new MyPinkFloyd<>(graph);
        // Graph radius is infinite when the graph is disconnected.
        // True to skip infinite values and use only finite values for radius.
        return floyd.getRadius(true);
    }

    public static double calculateGraphConnectvityComponents(UndirectedGraph<Point2D, DefaultEdge> graph) {
        ConnectivityInspector<Point2D, DefaultEdge> conn = new ConnectivityInspector<>(graph);
        // Calculate connectivity components count.
        return conn.connectedSets().size();
    }

    private static boolean intersectsShape(Line2D line, Shape shape) {
        final double flatness = 0.1;
        PathIterator pi = shape.getPathIterator(null, flatness);
        double coords[] = new double[6];
        pi.currentSegment(coords);
        Point2D point1 = new Point((int) coords[0], (int) coords[1]);
        pi.next();
        while (!pi.isDone()) {
            pi.currentSegment(coords);
            Point2D point2 = new Point((int) coords[0], (int) coords[1]);
            Line2D temp_line = new Line2D.Double(point1, point2);
            if (line.intersectsLine(temp_line)) {
                return true;
            }
            point1 = point2;
            pi.next();
        }

        return false;
    }

}
