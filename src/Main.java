import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.highgui.*;
import org.opencv.highgui.ImageWindow;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
//import org.*;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
//import static com.sun.tools.doclint.Entity.nu;


public class Main {
   static {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
      // System.load("C:\\opencv\\build\\java\\x64\\opencv_java455.dll");
      // System.load("C:\\opencv\\build\\x64\\vc14\\bin\\opencv_videoio_ffmpeg455_64.dll");
        nu.pattern.OpenCV.loadLibrary();
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
       // nu.pattern.OpenCV.loadShared();

      // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

       //OpenCV.loadShared();
        //OpenCV.loadLocally();
    }

    public static Image toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1){
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0,0,b); //get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(),type);
        final byte[] targetPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b,0,targetPixels,0,b.length);
        return image;
    }

    public static double getDistance(List<Double> x, List<Double> y){
        double distance = 0.0;
        for (int i = 0; i < x.size(); i++){
            distance += Math.pow((x.get(i) - y.get(i)), 2);
        }
        distance = Math.sqrt(distance);
        return distance;
    }

    public static List<Double> getNewCenter(List<List<Double>> points){
        List<Double> averageVector = new ArrayList<>();
        List<List<Double>> vector = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            vector.add(new ArrayList<>());
        }
        points.stream().forEach(point -> {
            for (int i =0; i < 10; i++){
                vector.get(i).add(point.get(i));
            }
        });
        averageVector = vector.stream().map(oneVector -> oneVector.stream().mapToDouble(Double::doubleValue).average().getAsDouble()).collect(Collectors.toList());
        double minDistance = 10000000.0;
        List<Double> newCenter = new ArrayList<>();
        for (List<Double> point : points){
            Double distance = getDistance(point,averageVector);
            if (distance < minDistance){
                newCenter = point;
                minDistance = distance;
            }
        }
        return newCenter;
    }

    public static Integer getClusterNumber(List<List<Double>> centers, List<Double> point){
        Integer minDistanceCluster = 1000000;
        Double minDistance = 1000000.0;
        for (List<Double> center : centers){
            Double distance = getDistance(point, center);
            if (distance < minDistance){
                minDistanceCluster = center.indexOf(center) + 1;
                minDistance = distance;
            }
        }
        return minDistanceCluster;
    }

    public static boolean cldc(List<List<Double>> clusterPointEarly, List<List<Double>> clusterPointNow){
        boolean result = true;
        for (int i = 0; i < clusterPointEarly.size(); i++){
            if (!clusterPointEarly.contains(clusterPointNow.get(i)) || !clusterPointNow.contains(clusterPointEarly.get(i))){
                result = false;
            }
        }
        return result;
    }

    public static boolean clusterDoNotChange(List<List<Double>> clusterPointEarly, List<List<Double>> clusterPointNow){
        if (clusterPointEarly.size() != clusterPointNow.size()){
            return false;
        }
        else {
            return true;
        }
      //  if (clusterPointEarly.containsAll(clusterPointNow) && clusterPointNow.containsAll(clusterPointEarly)){
      //      return true;
      //  } else {
        //   return false;
       // }
    }

    public static void main(String[] args){

        //много изображений
        //imread()
       // Mat image = HighGui.imshow("путь до изображения");
        Mat image = Imgcodecs.imread("C:/1.png");

        int variant = 2;
        Gelder gelder = new Gelder();
        gelder.setImage(image);
        //List<List<Double>> allLnMeasure = new ArrayList<>();
        List<Integer> radiuses = new ArrayList<>();
        List<Double> lnRadiuses = new ArrayList<>();
        if (variant == 3){
            radiuses.addAll(Arrays.asList(1,3,5,7,9,15,20));
        }
        if (variant == 1){
            radiuses.addAll(Arrays.asList(1,3,5,7,9));
        }
        if (variant == 2){
            radiuses.addAll(Arrays.asList(1,2,3,4,5));
        }
        if (variant == 4){
            radiuses.addAll(Arrays.asList(1,3,5));
        }

        gelder.setRadiuses(radiuses);
        if (variant == 3){
            lnRadiuses.addAll(Arrays.asList(Math.log(1),Math.log(3),Math.log(5),Math.log(7),Math.log(9),Math.log(15),Math.log(20)));
        }
        if (variant == 2){
            lnRadiuses.addAll(Arrays.asList(Math.log(1),Math.log(2),Math.log(3),Math.log(4),Math.log(5)));
        }
        if (variant == 1){
            lnRadiuses.addAll(Arrays.asList(Math.log(1),Math.log(3),Math.log(5),Math.log(7),Math.log(9)));
        }
        if (variant == 4){
            lnRadiuses.addAll(Arrays.asList(Math.log(1),Math.log(3),Math.log(5)));
        }

        Double maxLnMeasure = 0.0;
        double[][] gelderMatrix = new double[image.rows()][image.cols()];
        int[][] classesMatrix = new int[image.rows()][image.cols()];
        List<Double> gelderValues = new ArrayList<>();
        for (int i = 0; i < image.rows(); i++){
            for (int j =0; j < image.cols(); j++){
                List<Integer> measures = gelder.measure(i,j,5);
                List<Double> lnMeasures = measures.stream().map(a -> a == 0 ? 0 : Math.log(a)).collect(Collectors.toList());
                //allLnMeasure.add(lnMeasures);

                gelderMatrix[i][j] = LinearRegression.count(lnRadiuses, lnMeasures);
                gelderValues.add(LinearRegression.count(lnRadiuses, lnMeasures));
            }
        }

        //List<Double> gelderValues = allLnMeasure.stream().map(lnMeasure -> LinearRegression.count(lnRadiuses, lnMeasure)).collect(Collectors.toList());
        //System.out.println(gelderValues);
        //Double minGelder = Collections.min(gelderValues);
        //Double maxGelder = Collections.max(gelderValues);

        Double minGelder = 1000.0;
        Double maxGelder = -1000.0;

        for (int i =0; i < gelderValues.size(); i++){
            if (gelderValues.get(i) < minGelder){
                minGelder = gelderValues.get(i);
            }
            if (gelderValues.get(i) > maxGelder){
                maxGelder = gelderValues.get(i);
            }
        }

       // Double maxGelderWithount2 = Collections.max(gelderValues.stream().filter(a -> a != 2.0).collect(Collectors.toList()));

        gelder.setMinGelder(minGelder);
        gelder.setMaxGelder(maxGelder);

        System.out.println("min =" + minGelder + ",max =" + maxGelder);

        for (int i = 0; i < image.rows(); i++){
            for (int j = 0; j < image.cols(); j++){
                classesMatrix[i][j] = gelder.getNumberOfGelderClass(gelderMatrix[i][j], 10);
               // System.out.print(classesMatrix[i][j] + " ");
            }
           // System.out.println();
        }
        List<Double>[][] spectrsMatrix;
        System.out.println();
        if (variant == 3){
            List<Integer> radiusesCoverage = Arrays.asList(1,2,3);
            spectrsMatrix = gelder.getMultifractalMatrix(3, classesMatrix,10,radiusesCoverage);
        } else {
            List<Integer> radiusesCoverage = Arrays.asList(1,2,3);
            spectrsMatrix = gelder.getMultifractalMatrix(5, classesMatrix,10,radiusesCoverage);
        }
        //
       // for (int i = 0; i < image.rows(); i++){
        //    for (int j = 0; j < image.cols(); j++){
            //    System.out.print("[");
             //   for (int k = 0; k < spectrsMatrix[i][j].size(); k++){
               //
              //      System.out.printf("%6.3f", spectrsMatrix[i][j].get(k));
                //
               //     System.out.print(" ");
             //   }
          //      System.out.print("]");
             //
           // }
         //   System.out.println();
       // }
       int k = 2;

        List<List<Double>> centers  = new ArrayList<>();
        List<List<List<Double>>> pointsForCenters = new ArrayList<>();
        List<List<List<Double>>> pointsForCentersOld = new ArrayList<>();
        for (int i = 0; i < k; i++){
            pointsForCentersOld.add(new ArrayList<>());
        }
        List<Double> randomPoint = Arrays.asList(0.0,1.0,100.0);
        pointsForCentersOld.get(0).add(randomPoint);
        Integer maxIterations = 100;
        Integer[][] clustersMatrix = new Integer[spectrsMatrix.length][spectrsMatrix[0].length];

        if (variant != 3){
            centers.add(spectrsMatrix[528][76]);
            centers.add(spectrsMatrix[425][294]);
            pointsForCenters.add(new ArrayList<>());
            pointsForCenters.add(new ArrayList<>());
        }
        else {
            for (int i = 0; i < k; i++){
                for(;;){
                    Integer x = new Random().nextInt(spectrsMatrix.length);
                    Integer y = new Random().nextInt(spectrsMatrix[0].length);
                    if (! centers.contains(spectrsMatrix[x][y])){
                        centers.add(spectrsMatrix[x][y]);
                        break;
                    }

                }
                pointsForCenters.add(new ArrayList<>());
            }
        }

        for (int q = 0; q < maxIterations; q++){

            System.out.println("Iteration: " + (q + 1));

            for (int i = 0; i < spectrsMatrix.length; i++){
                for (int j = 0; j < spectrsMatrix[0].length; j++){
                    classesMatrix[i][j] = getClusterNumber(centers, spectrsMatrix[i][j]);
                    pointsForCenters.get(classesMatrix[i][j] - 1).add(spectrsMatrix[i][j]);
                }
            }

            boolean flag = true;

            for (int i = 0; i < pointsForCenters.size(); i++){
                if (! clusterDoNotChange(pointsForCentersOld.get(i), pointsForCenters.get(i))){
                    flag = false;
                }
            }

            if (flag){
                break;
            }

            for (int i = 0; i < k; i++){
                centers.set(i,getNewCenter(pointsForCenters.get(i)));
            }

            pointsForCentersOld = new ArrayList<>();
            for (int i = 0; i < k; i++){
                pointsForCentersOld.add(new ArrayList<>());
            }
            Collections.copy(pointsForCentersOld, pointsForCenters);

            pointsForCenters = new ArrayList<>();

            for (int i = 0; i < k; i++){
                pointsForCenters.add(new ArrayList<>());
            }
        }

        if (variant != 3){
            long forest = 0;
            long other = 0;

            for (int i = 0; i < spectrsMatrix.length; i++){
                for (int j = 0; j < spectrsMatrix[0].length; j++){
                    //System.out.print(classesMatrix[i][j] + " ");
                    if (classesMatrix[i][j] == 1){
                        forest++;
                    } else {
                        other++;
                    }
                }
               // System.out.println();
            }

            System.out.println();
            System.out.println("Процент соотношения льда ко всей территории: " + (double) forest / (double) (spectrsMatrix.length * spectrsMatrix[0].length) * 100 + "%");
        }

        List<List<Double>> colors = new ArrayList<>();
        if (variant == 3){
            List<Double> centri = Arrays.asList(0.0,69.0,255.0);
            List<Double> otherC = Arrays.asList(152.0,251.0,152.0);
            colors.add(otherC);
            colors.add(centri);
        } else{
            List<Double> otherColor = Arrays.asList(181.0,228.0,255.0);
            List<Double> forestColor = Arrays.asList(0.0,128.0,0.0);
            colors.add(forestColor);
            colors.add(otherColor);
        }
        for (int i = 0; i < spectrsMatrix.length; i++){
            for (int j = 0; j < spectrsMatrix[0].length; j++){
                image.put(i,j,colors.get(clustersMatrix[i][j] - 1).stream().mapToDouble(Double::doubleValue).toArray());
            }
        }

        JPanel panel = new JPanel();

        Image bufferedImage = toBufferedImage(image);
        JLabel label = new JLabel(new ImageIcon(bufferedImage));
        panel.add(label);

        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Clustering result");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(panel);

        frame.pack();

        frame.setVisible(true);

    }

}
