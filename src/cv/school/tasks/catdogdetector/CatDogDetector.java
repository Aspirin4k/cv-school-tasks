/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.catdogdetector;

import cv.school.tasks.ImgLoader;
import cv.school.tasks.ImgLoadingHog;
import cv.school.tasks.ImgLoadingPredict;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

/**
 *
 * @author Andew
 */
public class CatDogDetector {
    
    private final List<MatOfFloat> negative;
    private final List<MatOfFloat> positive;
    private final HOGDescriptor hog;
    private svm_model model;
    
    /**
     * Инициализация переменных
     */
    public CatDogDetector() {
        this.negative = new ArrayList<>();
        this.positive = new ArrayList<>();
        this.hog = new HOGDescriptor(
                new Size(64,64),    // Считаем одну гистограму для всей картинки
                new Size(32,32),      // Размер блока
                new Size(16,16),      // Смещение, на которое может сдвигаться блок
                new Size(16,16),      // Размер ячейки
                9,
                1,
                -1,
                0,
                0.2,
                true,
                64,
                true
        );
    }
    
    public int getPositiveCount() { return this.positive.size(); }
    public int getNegativeCount() { return this.negative.size(); }
    
    /**
     * Пытается предсказать класс изображения
     * @param mat изображение 128х128
     * @return 
     */
    public boolean predict(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(mat, mat, new Size(3,3), 0);
        MatOfFloat descriptors = new MatOfFloat();
        this.hog.compute(mat, descriptors);
        mat.release();
        
        float[] features = descriptors.toArray();
        svm_node[] nodes = new svm_node[features.length];
        int index = 0;
        for (float v: features) {
            svm_node node = new svm_node();
            node.index = index;
            node.value = v;
            nodes[index++] = node;
        }
        
        return svm.svm_predict(model, nodes) == 1;
    }
    
    /**
     * Пытается предсказать класс всех изображений из папки
     * @param path путь до директории
     * @param positive известный класс изображений
     * @throws java.io.IOException
     */
    public void preidct(Path path, boolean positive) throws IOException {
        ImgLoadingPredict operation = new ImgLoadingPredict(this);
        ImgLoader.readOneByOne(operation, path);
        System.out.println(
                String.format("Предсказано правильно %d из %d. Точность: %f", 
                        positive ? operation.getPos() : operation.getNeg(), 
                        operation.getPos() + operation.getNeg(),
                        positive ? 
                                (double)operation.getPos() / (operation.getPos() + operation.getNeg()) : 
                                (double)operation.getNeg() / (operation.getPos() + operation.getNeg()))
        );
    }
    
    /**
     * Сохраняет текущую модель в файл
     * @throws IOException 
     */
    public void saveDetector() throws IOException {
        svm.svm_save_model("cat_model.mdl", this.model);
    }
    
    /**
     * Пытается загрузить модель из файла и возвращает истину при успехе
     * @param path путь до модели
     * @return 
     */
    public boolean loadDetector(Path path) {
        if (!Files.exists(path))
            return false;
        
        try {
            this.model = svm.svm_load_model(path.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }
    
    /**
     * Обучает классификатор, используя данные, загруженные ранее
     */
    public void trainDetector() {
        if (this.negative.isEmpty() && this.positive.isEmpty())
            return;
        // Количество атрибутов для каждого входного примера
        int featureCount = this.negative.get(0).toArray().length;
        
        svm_problem problem = new svm_problem();

        // Количество тренировочных записей
        problem.l = this.getNegativeCount() + this.getPositiveCount();

        // Результаты для каждой тренировочной записи
        double[] classes = new double[this.getNegativeCount() + this.getPositiveCount()];
        for (int i=0; i<this.getNegativeCount(); i++)
            classes[i] = 0;
        for (int i=this.getNegativeCount(); i<this.getNegativeCount()+this.getPositiveCount(); i++)
            classes[i] = 1;
        problem.y = classes;
       
        // Входные примеры в виде массива атрибутов
        svm_node[][] trainSet = new svm_node[this.getNegativeCount() + this.getPositiveCount()][featureCount];
        for (int i=0; i<this.getNegativeCount(); i++) {
            float[] features = this.negative.get(i).toArray();
            trainSet[i] = new svm_node[featureCount];
            int index = 0;
            for (float v: features) {
                svm_node node = new svm_node();
                node.index = index;
                node.value = v;
                trainSet[i][index++] = node;
            }
        }
        // TODO: Дублирование кода - некрасиво
        for (int i=this.getNegativeCount(); i<this.getNegativeCount() + this.getPositiveCount(); i++) {
            float[] features = this.positive.get(i-this.getNegativeCount()).toArray();
            trainSet[i] = new svm_node[featureCount];
            int index = 0;
            for (float v: features) {
                svm_node node = new svm_node();
                node.index = index;
                node.value = v;
                trainSet[i][index++] = node;
            }
        }
        problem.x = trainSet;
        
        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 100;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.cache_size = 20000;
        param.eps = 0.001;
        
        this.model = svm.svm_train(problem, param);
    }
    
    /**
     * Загружает в память выборку и вычисляет HOG
     * @param path путь до директории
     * @param positive присутствует ли объект на изображении
     * @throws java.io.IOException
     */
    public void loadSet(Path path, boolean positive) throws IOException {
        ImgLoadingHog operation = new ImgLoadingHog(this.hog);
        ImgLoader.readOneByOne(operation, path);
        if (positive) {
            this.positive.addAll(operation.getData());
        } else {
            this.negative.addAll(operation.getData());
        }
    }
}
