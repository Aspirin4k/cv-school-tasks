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
    private double polarity;
    
    public DecisionStump(double t, double p) {
        this.trheshold = t;
        this.polarity = p;
    }
    
    public DecisionStump() {}
    
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
        ArrayList<Double> Ms1 = new ArrayList<>(X.size());
        ArrayList<Double> Ms2 = new ArrayList<>(X.size());
        // X - не отсортирован!
        for (int i=0; i< X.size(); i++) {
            
            double m1 = 0;
            double m2 = 0;
            // Все что слева от порога, классифицированное как 1 для polarity 1 и 0 для polarity -1
            for (int j=0; j< i; j++)
                if (Y.get(indices.get(j)) == 1) {
                    m1 += W.get(indices.get(j));
                } else {
                    m2 += W.get(indices.get(j));
                }
            // Все что справа от порога (включая порог)
            for (int j=i; j< X.size(); j++)
                if (Y.get(indices.get(j)) == 0) {
                    m1 += W.get(indices.get(j));
                } else {
                    m2 += W.get(indices.get(j));
                }
            
            Ms1.add(m1);
            Ms2.add(m2);
        }
        int M1min = Ms1.indexOf(Collections.min(Ms1));
        int M2min = Ms2.indexOf(Collections.min(Ms2));
        int Mmin;
        if (Ms1.get(M1min) < Ms2.get(M2min)) {
            this.polarity = 1;
            Mmin = M1min;
        } else {
            this.polarity = -1;
            Mmin = M2min;
        }
        this.trheshold = X.get(indices.get(Mmin));
        return this.polarity == 1 ? Ms1.get(M1min) : Ms2.get(M2min);
    }
    
    public Integer classify(double x) {
        return x * this.polarity >= this.trheshold * this.polarity ? 1 : 0;
    }
    
    public double getTrheshold() { return this.trheshold; }
    public double getPolarity()  { return this.polarity;  }
}
