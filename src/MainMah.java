import java.io.File;
import java.io.IOException;

public class MainMah {
    private static int iterCount = 1000;
//    private static int iterCount = 1;
    private static int minVertexCount = 50;
    private static int maxVertexCount = 1000;
    private static int stepVertexCount = 50;

    private static double[] areaRadius = new double[]{5, 10, 20};
    private static int[][] threadTasks = {
            {0, minVertexCount, maxVertexCount},
            {1, minVertexCount, maxVertexCount},
            {2, minVertexCount, maxVertexCount},
    };
    private static double obstacleRadius = 3;
    private static double deviceRadius = 1;

    private static String pathMask = "tests" + File.separator + "expected-connectivity-count-%radius-%n.txt";

    public static void main(String[] args) {
        Thread threads[] = new Thread[MainMah.threadTasks.length];
        File[] files = new File[MainMah.areaRadius.length];
        for (int i = 0; i < MainMah.areaRadius.length; i++) {
            double areaRadius = MainMah.areaRadius[i];
            // Prepare file for results.
            String base_path = MainMah.pathMask.replace("%radius", String.format("%02d", (int) areaRadius));
            int n = 1;
            File resultsFile = new File(base_path.replace("%n", String.format("%03d", n)));

            // Try to create file.
            try {
                boolean canWrite;
                while (resultsFile.exists()) {
                    resultsFile = new File(base_path.replace("%n", String.format("%03d", ++n)));
                }
                resultsFile.getParentFile().mkdirs();
                canWrite = resultsFile.createNewFile() && resultsFile.setWritable(true);
                files[i] = resultsFile;
                if (!canWrite) {
                    System.out.println("Failed on creating file.");
                }
            } catch (IOException e) {
                // Do nothing.
                System.out.println("Error on creating file.");
            }
        }

        // Calculate for different area size.
        for (int i = 0; i < MainMah.threadTasks.length; i++) {
            int area_i = MainMah.threadTasks[i][0];
            double areaRadius = MainMah.areaRadius[area_i];
            File resultsFile = files[area_i];
            int minVertexCount = threadTasks[i][1];
            int maxVertexCount = threadTasks[i][2];

            // Set threads for calculations.
            if (resultsFile != null && resultsFile.canWrite()) {
                CalculateGraphConnectivityRunnable r = new CalculateGraphConnectivityRunnable(
                        areaRadius,
                        MainMah.obstacleRadius,
                        MainMah.deviceRadius,
                        minVertexCount,
                        maxVertexCount,
                        MainMah.stepVertexCount,
                        MainMah.iterCount,
                        resultsFile
                );
                threads[i] = new Thread(r);
            }
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
