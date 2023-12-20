import lombok.Data;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.util.*;

@Data
public class Gelder {
    private Mat image;
    private List<Integer> radiuses;
    private Double minGelder;
    private Double maxGelder;

    private Integer getIntance(double[] rgb){
        return new Long(Math.round(rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.114)).intValue();
    }
    public List<Integer> measure(Integer x, Integer y, Integer eps){
        List<Integer> measures = new ArrayList<>();
        //Integer n = 1;
        //Integer radius = 2 * n + 1;
        double[] rgb = image.get(x,y);
        Integer centerValue = getIntance(rgb);
        //measures.add(1);
        for (Integer radius: radiuses){
            int measure = 0;
            int n = (radius - 1) / 2;
            if (radius != 1){
                for (int i = x - n; i <= x + n; i++){
                    for (int j = y - n; j <= y + n; j++){
                        Integer pixelIntance = 255;
                        if (i >= 0 && i < image.rows() && j >= 0 && j< image.cols()){
                            pixelIntance = getIntance(image.get(i,j));
                        }
                        if (Math.abs(centerValue - pixelIntance) <= eps){
                            measure++;
                        }
                    }
                }
                measures.add(measure);
            } else  measures.add(1);

        }
        return measures;
    }

    public Integer getNumberOfGelderClass(Double gelderValue, Integer steps){
        Double l = maxGelder - minGelder;
        Double step = l / steps;
        Integer result = 0;
        for (int i = 0; i< steps; i++){
            if (gelderValue >= step * i && gelderValue < step * (i + 1)){
                result = i +1;
                break;
            }
            if (i == steps - 1){
                if (gelderValue >= step * i && gelderValue <= step * (i + 1)){
                    result = i + 1;
                    break;
                }
            }
        }
        return result;
    }

    public List<Double>[][] getMultifractalMatrix(Integer r, int[][] classesMatrix, Integer steps, List<Integer> radiuses){
        List<Double>[][] multifractalSpectrs = new List[image.rows()][image.cols()];
        Integer n = (r - 1) / 2;
        List<Integer>[][] coverage = new List[r][r];

        //multifractalSpectrsNumbers[i][j] = new HashMap<>();
        for (int k = 0; k < coverage.length; k++){
            for (int z = 0; z < coverage[k].length; z++){
                coverage[k][z] = new ArrayList<>();
            }
        }
        for (int co = 0; co < radiuses.size(); co++){
            Integer coverageNumber = 0;
            for (int k = 0; k <= 2 * n; k += radiuses.get(co)){
                for (int z = 0; z <= 2 * n; z += radiuses.get(co)){
                    for (int q = 0; q < radiuses.get(co); q++){
                        for (int w = 0; w < radiuses.get(co); w++)
                            if (k +q < r && z + w < r){
                                coverage[k + q][z + w].add(coverageNumber);
                            }
                    }
                    coverageNumber++;
                }
            }
        }
        for (int co = 0; co < radiuses.size(); co++){
            for (int k = 0; k <= 2 * n; k++){
                for (int z = 0; z <= 2 * n; z++){
                    //System.out.print(coverage[k][z].get(co) + " ");
                }
               // System.out.println();
            }
            System.out.println();
            System.out.println();
        }
        for (int i = 0; i < image.rows(); i++){
            for (int j = 0; j < image.cols(); j++){
                multifractalSpectrs[i][j] = new ArrayList<>();
                for (int s = 1; s <= steps; s++){
                    List<Set<Integer>> level = new ArrayList<>();
                    for (int co = 0; co < radiuses.size(); co++){
                        level.add(new HashSet<>());
                    }
                    for (int k = i - n; k <= i + n; k++){
                        for (int z = j - n; z <= j + n; z++){

                            //Выделяем множесьво точек каждого уровня
                            //Считаем число покрытий для каждого радиуса каждого уровня
                            //Аппроксимируем значение отношений логарифма количества к логарифму радиуса и записываем

                            Integer classNumber = steps;
                            if (k >= 0 && k < image.rows() && z >= 0 && z < image.cols()){
                                classNumber = classesMatrix[k][z];
                            } else {
                                classNumber = 10;
                            }
                            if (classNumber == s){
                                for (int co = 0; co < radiuses.size(); co++){
                                    level.get(co).add(coverage[k - (i - n)][z - (j - n)].get(co));
                                }
                            }
                           // Integer count = multifractalSpectrsNumbers[i][j].getOrDefault(classNumber, 0);
                           // multifractalSpectrsNumbers[i][j].put(classNumber, count + 1);
                        }
                    }
                    List<Double> lnNumbers = new ArrayList<>();
                    List<Integer> numbers = new ArrayList<>();
                    List<Double> lnRadiuses = new ArrayList<>();
                    boolean flag = true;
                    for (int co = 0; co < radiuses.size(); co++){
                        lnNumbers.add(Math.log(level.get(co).size()));
                        numbers.add(level.get(co).size());
                        if (numbers.get(co) == 0) flag = false;
                        lnRadiuses.add(Math.log(radiuses.get(co)));
                    }
                    Double result = 0.0;
                    if (flag){
                        result = LinearRegression.count(lnRadiuses, lnNumbers);
                    }
                    multifractalSpectrs[i][j].add(result);
                }
            }
        }
        return multifractalSpectrs;
    }
}
