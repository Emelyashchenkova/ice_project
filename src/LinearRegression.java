import java.util.ArrayList;
import java.util.List;

// Расчет мультифрактального спектра

public class LinearRegression {
    public static Double count(List<Double> x1, List<Double> y1){
        double[] x = new double[x1.size()];
        double[] y = new double[y1.size()];

        double sumx = 0.0;
        double sumy = 0.0;
        double sumx2 = 0.0;

        for (int i = 0; i < x1.size(); i++){
            x[i] = x1.get(i);
            y[i] = y1.get(i);
            sumx += x[i];
            sumx2 += x[i] * x[i];
            sumy += y[i];
        }
        double xbar = sumx / x1.size();
        double ybar = sumy / y1.size();

        double xxbar = 0.0;
        double yybar = 0.0;
        double xybar = 0.0;

        for (int i =0; i < x1.size(); i++){
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }

        double beta1 = xybar / xxbar;

        return beta1;

    }
}
