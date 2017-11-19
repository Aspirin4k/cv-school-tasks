/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector;

import cv.school.tasks.facedetector.FaceDetector.ClassifierStructure;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author roma2_000
 */
public class Boosting {
    private ArrayList<ArrayList<Double>> X;
    private ArrayList<Integer> Y;
    private ArrayList<Double> W;
    
    private ArrayList<ClassifierStructure> ANS;
    private ArrayList<Double> ALPHAS;
    
    // Количество раундов, по которым будет происходить обучение
    private final int ROUNDS = 100;
    // Требуемая точность
    private final double EPS = 0.00005;
    
    public Boosting(ArrayList<ArrayList<Double>> x, ArrayList<Integer> y, ArrayList<Double> w) {
        this.X = x;
        this.Y = y;
        this.W = w;
        this.ANS = new ArrayList<>(this.ROUNDS);
        this.ALPHAS = new ArrayList<>(this.ROUNDS);
    }
    
    /**
     * Сохраняет обученный классификатор
     * @param path директория для сохранения
     * @throws java.io.IOException
     */
    public void save(Path path) throws IOException {
        if (!Files.exists(path)) Files.createDirectories(path);
        for (int i=0; i < this.ANS.size(); i++) {
            try (PrintWriter out = new PrintWriter (path.resolve(Integer.toString(i)).toString()))  {
                out.print(this.ANS.get(i).getIndex() + " ");
                out.print(this.ANS.get(i).getModel().getPolarity() + " ");
                out.print(this.ANS.get(i).getModel().getTrheshold() + " ");
                out.print(this.ALPHAS.get(i));
            }
        }
    }
    
    /**
     * Обучает ансамбль
     */
    public void learnBoosting() {       
        for (int i=0; i< this.ROUNDS; i++) {
            System.out.println(String.format("Раунд %d!", i));
            
            // Нормируем веса
            double sum = this.W.stream().mapToDouble(Double::doubleValue).sum();
            for(int j=0; j<this.W.size(); j++)
                this.W.set(j, this.W.get(j) / sum);
            // Найдем лучший классификатор
            ClassifierStructure bestClassifier = this.learnBestStump();
            System.out.println("Текущий лучший классификатор: ");
            System.out.println("Index: " + bestClassifier.getIndex());
            
            System.out.println(String.format("Взвешенная ошибка текущего слабого классификатора: %f", bestClassifier.getError()));
            // Если достигли искомой точности, то можно ливать
            if (bestClassifier.getError() < this.EPS) break;
            // Перерасчет весов
            double alpha = 0.5 * Math.log((1 - bestClassifier.getError()) / bestClassifier.getError());
            double beta = bestClassifier.getError() / (1 - bestClassifier.getError());
            System.out.println("Перерасчет весов!");
            for (int j=0; j< this.W.size(); j++) {
             //   System.out.print(String.format("[%d] %f ->", j, this.W.get(j)));
                this.W.set(j, 
                        this.W.get(j) * Math.pow(
                                beta, 
                                this.Y.get(j) == 
                                        bestClassifier.getModel().classify(this.X.get(bestClassifier.getIndex()).get(j)) ?
                                        1 : 0));
             //   System.out.println(this.W.get(j));
            }
            
            this.ANS.add(bestClassifier);
            this.ALPHAS.add(alpha);
            
            // Посчитаем промежуточную точность
            int correct = 0;
            for (int j=0; j< this.X.size(); j++) {
                if (this.Y.get(j) == this.classify(this.X.get(j))) correct++;
            }
            System.out.println(String.format("Промежуточная точность: %f", (double)correct/this.X.size()));
        }
    }
    
    /**
     * Определение класса
     * @param X вектор признаков Хаара
     * @return 
     */
    public double classify(ArrayList<Double> X) {
        double sum = 0;
        for (int i=0; i< this.ANS.size(); i++) {
            sum += this.ALPHAS.get(i) * this.ANS.get(i).getModel().classify(X.get(this.ANS.get(i).getIndex()));
        }
        double sumBeta = 0.5 * this.ALPHAS.stream().mapToDouble(Double::doubleValue).sum();
        return sum >= sumBeta ? 1 : 0;
    }
    
    /**
     * Находит лучший пенек
     * @return 
     */
    private FaceDetector.ClassifierStructure learnBestStump() { 
        ArrayList<FaceDetector.ClassifierStructure> classifiers = new ArrayList<>(this.X.size());
        // Натренириуем каждый классификатор по каждому признаку
        for (int i=0; i<this.X.size(); i++) {
            ArrayList<Double> Xi = new ArrayList<>(this.X.size());
            for(ArrayList<Double> x : this.X) {
                Xi.add(x.get(i));
            }
            // Получаем массив индексов дял перестановки X в отсортированный массив
            ArrayIndexComparator comparator = new ArrayIndexComparator(Xi);
            ArrayList<Integer> indexes = comparator.createIndexArray();
            indexes.sort(comparator);
            
            // Создаем и обучаем пенек
            DecisionStump stump = new DecisionStump();
            classifiers.add(new ClassifierStructure(
                    i,
                    stump.train(Xi, this.Y, this.W, indexes),
                    stump
            ));
        }
        // Определяем лучший
        DecisionStumpComparator comparatorStump = new DecisionStumpComparator();
        return Collections.min(classifiers, comparatorStump);
    }
}
