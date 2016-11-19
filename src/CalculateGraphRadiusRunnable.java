import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CalculateGraphRadiusRunnable implements Runnable {

    private double areaRadius;
    private double obstacleRadius;
    private double deviceRadius;
    private int minVertexCount;
    private int maxVertexCount;
    private int stepVertexCount;
    private int iterCount;
    private File resultsFile;

    public CalculateGraphRadiusRunnable(double areaRadius, double obstacleRadius, double deviceRadius, int minVertexCount, int maxVertexCount, int stepVertexCount, int iterCount, File resultsFile) {
        super();
        this.areaRadius = areaRadius;
        this.obstacleRadius = obstacleRadius;
        this.deviceRadius = deviceRadius;
        this.minVertexCount = minVertexCount;
        this.maxVertexCount = maxVertexCount;
        this.stepVertexCount = stepVertexCount;
        this.iterCount = iterCount;
        this.resultsFile = resultsFile;
    }


    @Override
    public void run() {
        // Generate area and obstacle.
        Shape area = new Ellipse2D.Double(-this.areaRadius, -this.areaRadius, 2 * this.areaRadius, 2 * this.areaRadius);
        Shape obstacle = new Ellipse2D.Double(-this.obstacleRadius, -this.obstacleRadius, 2 * this.obstacleRadius, 2 * this.obstacleRadius);
        // Calculate for different graph size.
        for (int count = this.minVertexCount; count <= this.maxVertexCount; count += this.stepVertexCount) {
            System.out.println("Calculating  (" + areaRadius + " : " + count + "); ");
            double expectedValue = 0;
            // Generate graphs.
            long startTime = System.nanoTime();
            for (int i = 0; i < this.iterCount; i++) {
                Graph<Point2D, DefaultEdge> graph = GraphCommon.generateGraphInArea(area, obstacle, this.deviceRadius, count);
                // Calculate radius for graph.
                double graphRadius = GraphCommon.calculateGraphRadiusFloyd(graph);
                // Calculate expected value
                expectedValue += graphRadius;
            }
            long estTime = System.nanoTime() - startTime;
            System.out.println("Calculated  (" + areaRadius + " : " + count + "); Time: " + (estTime / 1000000) + " ms");
            double result = expectedValue / this.iterCount;
            writeDoubleToFile(count, result);
        }
    }

    private void writeDoubleToFile(int count, double result) {
        // Write results to file.
        try {
            FileWriter writer = new FileWriter(resultsFile, true);
            writer.append(Integer.toString(count));
            writer.append(", ");
            writer.append(String.format("%.5f", result));
            writer.append("\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("Error on file writing.");
            e.printStackTrace();
        }
    }

}