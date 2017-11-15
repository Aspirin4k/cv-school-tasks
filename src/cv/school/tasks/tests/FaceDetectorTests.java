/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.tests;

import cv.school.tasks.facedetector.DecisionStump;
import cv.school.tasks.facedetector.IntegralImage;
import cv.school.tasks.facedetector.haar.HaarCross4S;
import cv.school.tasks.facedetector.haar.HaarHorizontal2S;
import cv.school.tasks.facedetector.haar.HaarHorizontal3S;
import cv.school.tasks.facedetector.haar.HaarVertical2S;
import cv.school.tasks.facedetector.haar.HaarVertical3S;
import cv.school.tasks.facedetector.haar.IHaarFeature;
import java.util.Arrays;

/**
 *
 * @author roma2_000
 */
public class FaceDetectorTests {
    /**
     * Тестирование корректности генерации интегрального изображения
     */
    public static void testIntegral() {
        System.out.println("Наничаем тестирование корректности генерации интегрального изображения:");
        double[][] data = {
            {1,2,3},
            {4,5,6},
            {7,8,9}
        };
        for (double[] row: data) {
            for (double element: row)
                System.out.print(Double.toString(element) + " ");
            System.out.println();
        }
        IntegralImage integral = new IntegralImage(data);
        System.out.println(String.format("Sum((%d,%d),(%d,%d))=%f", 0,0,2,2, integral.getSum(0, 2, 0, 2)));
        System.out.println(String.format("Sum((%d,%d),(%d,%d))=%f", 0,0,0,0, integral.getSum(0, 0, 0, 0)));
        System.out.println(String.format("Sum((%d,%d),(%d,%d))=%f", 0,0,2,0, integral.getSum(0, 2, 0, 0)));
        System.out.println(String.format("Sum((%d,%d),(%d,%d))=%f", 0,1,1,2, integral.getSum(0, 1, 1, 2)));
    }
    
     /**
     * Проверка корректности вычисления признаков Хаара
     */
    public static void testHaar() {
        System.out.println("Тестирование признаков Хаара..");
        System.out.println("Vertical 2S");
        IHaarFeature haar1 = new HaarVertical2S();
        
        double[][] data1 = {
            { 1, 1, 2, 2 },
            { 1, 1, 2, 2 },
            { 1, 1, 2, 2 }
        };
        for (double[] row: data1) {
            for (double element: row)
                System.out.print(Double.toString(element) + " ");
            System.out.println();
        }
        System.out.println(String.format("Feautre=%f", haar1.computeFeature(new IntegralImage(data1), 0, 0, 4, 3)));
        
        System.out.println("Vertical 3S");
        IHaarFeature haar2 = new HaarVertical3S();
        
        double[][] data2 = {
            { 1, 1, 3, 3, 1, 1 },
            { 1, 1, 3, 3, 1, 1 },
            { 1, 1, 3, 3, 1, 1 }
        };
        for (double[] row: data2) {
            for (double element: row)
                System.out.print(Double.toString(element) + " ");
            System.out.println();
        }
        System.out.println(String.format("Feautre=%f", haar2.computeFeature(new IntegralImage(data2), 0, 0, 6, 3)));
        
        System.out.println("Horizontal 2S");
        IHaarFeature haar3 = new HaarHorizontal2S();
        
        double[][] data3 = {
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 },
            { 2, 2, 2, 2 },
            { 2, 2, 2, 2 }
        };
        for (double[] row: data3) {
            for (double element: row)
                System.out.print(Double.toString(element) + " ");
            System.out.println();
        }
        System.out.println(String.format("Feautre=%f", haar3.computeFeature(new IntegralImage(data3), 0, 0, 4, 4)));
        
        System.out.println("Horizontal 3S");
        IHaarFeature haar4 = new HaarHorizontal3S();
        
        double[][] data4 = {
            { 1, 1, 1 },
            { 1, 1, 1 },
            { 3, 3, 3 },
            { 3, 3, 3 },
            { 1, 1, 1 },
            { 1, 1, 1 },
        };
        for (double[] row: data4) {
            for (double element: row)
                System.out.print(Double.toString(element) + " ");
            System.out.println();
        }
        System.out.println(String.format("Feautre=%f", haar4.computeFeature(new IntegralImage(data4), 0, 0, 3, 6)));
        
        System.out.println("Cross 4S");
        IHaarFeature haar5 = new HaarCross4S();
        
        double[][] data5 = {
            { 1, 1, 0, 0 },
            { 1, 1, 1, 1 },
            { 0, 0, 1, 1 },
            { 0, 0, 1, 1 },
        };
        for (double[] row: data5) {
            for (double element: row)
                System.out.print(Double.toString(element) + " ");
            System.out.println();
        }
        System.out.println(String.format("Feautre=%f", haar5.computeFeature(new IntegralImage(data5), 0, 0, 4, 4)));
    }
    
    /**
     * Проверка корректности тренировки пенька
     */
    public static void testStump() {
        System.out.println("Тестирование пеньков..");
        DecisionStump classifier1 = new DecisionStump(0,1);
        Double[] X1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        Integer[] Y1 = {0, 0, 1, 1, 1};
        Double[] W1 = {1.0, 1.0, 1.0, 1.0, 1.0};
        Integer[] indices1 = {0, 1, 2, 3, 4};
        double error1 = classifier1.train(Arrays.asList(X1), Arrays.asList(Y1),Arrays.asList(W1), Arrays.asList(indices1));
        System.out.println(String.format("Минимальная ошибка %f. Threshold: %f. Polarity: %f", error1, classifier1.getTrheshold(), classifier1.getPolarity()));
        
        DecisionStump classifier2 = new DecisionStump(0,-1);
        Double[] X2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        Integer[] Y2 = {1, 1, 0, 0, 0};
        Double[] W2 = {1.0, 1.0, 1.0, 1.0, 1.0};
        Integer[] indices2 = {0, 1, 2, 3, 4};
        double error2 = classifier2.train(Arrays.asList(X2), Arrays.asList(Y2),Arrays.asList(W2), Arrays.asList(indices2));
        System.out.println(String.format("Минимальная ошибка %f. Threshold: %f. Polarity: %f", error2, classifier2.getTrheshold(), classifier2.getPolarity()));
        
        DecisionStump classifier3 = new DecisionStump(0,1);
        Double[] X3 = {0.0, 1.0, 2.0, 3.0, 4.0};
        Integer[] Y3 = {0, 1, 0, 1, 1};
        Double[] W3 = {1.0, 1.0, 10.0, 1.0, 1.0};
        Integer[] indices3 = {0, 1, 2, 3, 4};
        double error3 = classifier3.train(Arrays.asList(X3), Arrays.asList(Y3),Arrays.asList(W3), Arrays.asList(indices3));
        System.out.println(String.format("Минимальная ошибка %f. Threshold: %f. Polarity: %f", error3, classifier3.getTrheshold(), classifier3.getPolarity()));
    }
}
