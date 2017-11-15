/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author roma2_000
 */
public class DecisionStump {
    private double trheshold;
    private final double polarity;
    
    public DecisionStump(double t, double p) {
        this.trheshold = t;
        this.polarity = p;
    }
    
    /**
     * Обучение слабого классификатора
     * @param X вектор признаков. Вектор не отсортирован
     * @param Y вектор классов. Классы в порядке вектора признаков
     * @param W вектор весов. Классы в порядке вектора признаков
     * @param indices вектор индексов перестановки для отсортированного массива
     * @return 
     */
    public double train(List<Double> X, List<Integer> Y, List<Double> W, List<Integer> indices) {
        // Сюда будут помещены ошибки
        ArrayList<Double> Ms = new ArrayList<>(X.size());
        // X - не отсортирован!
        for (int i=0; i< X.size(); i++) {
            
            double m = 0;
            // Все что слева от порога, классифицированное как 1
            for (int j=0; j< i; j++)
                if (Y.get(indices.get(j)) == (this.polarity == 1 ? 1 : 0)) m += W.get(indices.get(j));
            // Все что справа от порога (включая порог), классифицированное как 0
            for (int j=i; j< X.size(); j++)
                if (Y.get(indices.get(j)) == (this.polarity == 1 ? 0 : 1)) m += W.get(indices.get(j));
            
            Ms.add(m);
        }
        int Mmin = Ms.indexOf(Collections.min(Ms));
        this.trheshold = X.get(indices.get(Mmin));
        return Ms.get(Mmin);
    }
    
    public boolean classify(double x) {
        return x * this.polarity >= this.trheshold * this.polarity;
    }
    
    public double getTrheshold() { return this.trheshold; }
    public double getPolarity()  { return this.polarity;  }
}
