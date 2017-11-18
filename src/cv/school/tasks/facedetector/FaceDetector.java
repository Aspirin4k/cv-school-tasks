/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector;

import cv.school.tasks.ExtraFunctions;
import cv.school.tasks.ImgLoader;
import cv.school.tasks.facedetector.haar.HaarCross4S;
import cv.school.tasks.facedetector.haar.HaarHorizontal2S;
import cv.school.tasks.facedetector.haar.HaarHorizontal3S;
import cv.school.tasks.facedetector.haar.HaarVertical2S;
import cv.school.tasks.facedetector.haar.HaarVertical3S;
import cv.school.tasks.facedetector.haar.IHaarFeature;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author roma2_000
 */
public class FaceDetector {
    private final int WINDOW_SIZE = 24;
    private static final boolean DEBUG = true;
    
    private final HashMap<String, Mat> positives;
    private final HashMap<String, Mat> negatives;
    private final IntegralImage[] positivesIntegral;
    private final IntegralImage[] negativesIntegral;
    private final ArrayList<ArrayList<Double>> positivesHaar;
    private final ArrayList<ArrayList<Double>> negativesHaar;
    
    private final IHaarFeature[] haar = {
        new HaarCross4S(),
        new HaarHorizontal2S(),
        new HaarHorizontal3S(),
        new HaarVertical2S(),
        new HaarVertical3S()
    };
    
    /**
     * Позволяет загрузить признаки Хаара из директории
     * @param haars
     * @throws Exception 
     */
    public FaceDetector(Path haars) throws Exception {
        this.positives = null;
        this.negatives = null;
        this.positivesIntegral = null;
        this.negativesIntegral = null;
        this.positivesHaar = new ArrayList<>();
        this.negativesHaar = new ArrayList<>();
        this.loadHaarFeatures(haars);
    }
    
    /**
     * Конкструктор, который при инициализации вычисляет признаки Хаара
     * @param positive папка с изображениями с лицами
     * @param negative папка с изображениями без лиц, из которых будет сгенерирован
     * отрицательный датасет
     * @throws Exception 
     */
    public FaceDetector(Path positive, Path negative) throws Exception {
        if (DEBUG) System.out.println("Начинаем загружать положительный датасет..");
        // Нормализуем положительные изображения
        this.positives = ImgLoader.loadData(positive);
        this.positives.forEach((k,v) -> {
            Imgproc.cvtColor(v, v, Imgproc.COLOR_BGR2GRAY);
            ExtraFunctions.normalizeImage(v);
            Imgproc.resize(v, v, new Size(WINDOW_SIZE, WINDOW_SIZE));
        });
        if (DEBUG) System.out.println(String.format("Загрузили %d изображений", this.positives.size()));
        if (DEBUG) System.out.println("Начинаем загружать отрицательный датасет..");
        // Нормализуем негативные изображнеия
        List<Mat> negativesRaw = new ArrayList<>(ImgLoader.loadData(negative).values());
        negativesRaw.forEach((v) -> {
            Imgproc.cvtColor(v, v, Imgproc.COLOR_BGR2GRAY);
            ExtraFunctions.normalizeImage(v);
        });
        if (DEBUG) System.out.println("Начинаем генерацию случайных изображений..");
        // Генерация набора отрицательных по количеству положительных
        this.negatives = this.generateSet(negativesRaw, this.positives.size());
        if (DEBUG) System.out.println(String.format("Сгенерировали %d изображений", this.negatives.size()));
        // Освобождаем память
        negativesRaw.forEach((v) -> { v.release(); });
        
        // Вычисление интегральных изображений
        this.positivesIntegral = new IntegralImage[this.positives.size()];
        this.negativesIntegral = new IntegralImage[this.negatives.size()];
        this.computeIntegralImages(positivesIntegral, new ArrayList<>(this.positives.values()));
        this.computeIntegralImages(negativesIntegral, new ArrayList<>(this.negatives.values()));
        this.positives.entrySet().forEach((entry) -> {
            entry.getValue().release();
        });
        this.negatives.entrySet().forEach((entry) -> {
            entry.getValue().release();
        });
        this.positivesHaar = new ArrayList<>();
        this.negativesHaar = new ArrayList<>();
        this.computeHaarFeatures(this.positivesHaar, this.positivesIntegral);
        this.computeHaarFeatures(this.negativesHaar, this.negativesIntegral);
    }
    
    /**
     * Сохраняет вычисленные признаки Хаара
     * @param path путь до директории сохранения
     * @throws java.io.IOException
     */
    public void saveHaarFeatures(Path path) throws IOException {
        if (DEBUG) System.out.println("Начинаем сохранять фичи..");
        if (Files.exists(path)) Files.createDirectories(path);
        if (DEBUG) System.out.println("Положительные..");
        this.saveHaarFeature(this.positivesHaar, path.resolve("pos"));
        if (DEBUG) System.out.println("Отрицательные..");
        this.saveHaarFeature(this.negativesHaar, path.resolve("neg"));
    }
    
    /**
     * Сохраняет признаки из заданного списка в заданную папку
     * @param haar список списков признаков
     * @param path путь до директории сохранения
     */
    private void saveHaarFeature(ArrayList<ArrayList<Double>> haar, Path path) throws IOException {
        if (!Files.exists(path)) Files.createDirectories(path);
        for (int i=0; i< haar.size(); i++)
        {
            try (PrintWriter out = new PrintWriter (path.resolve(Integer.toString(i)).toString()))  {
                out.println(haar.get(i).size());
                haar.get(i).forEach((feature) -> {
                    out.print(feature + " ");
                });
            }
        }
    }
    
    /**
     * Загружает признаки хаара из директории
     * @param path путь до директории загрузки
     * @throws Exception 
     */
    public final void loadHaarFeatures(Path path) throws Exception {
        if (DEBUG) System.out.println("Начинаем загружать фичи..");
        if (Files.notExists(path)) throw new Exception("Директория " + path.toString() + " отсутствует");
        if (DEBUG) System.out.println("Положительные..");
        this.loadHaarFeature(this.positivesHaar, path.resolve("pos"));
        if (DEBUG) System.out.println(String.format("Считали %d фич", this.positivesHaar.size()));
        if (DEBUG) System.out.println("Отрицательные..");
        this.loadHaarFeature(this.negativesHaar, path.resolve("neg"));
        if (DEBUG) System.out.println(String.format("Считали %d фич", this.negativesHaar.size()));
    }
    
    /**
     * Загружает признаки Хаара из файлов
     * @param haar список списков, в который будут загружены признаки
     * @param path путь до директории загрузки
     * @throws Exception 
     */
    private void loadHaarFeature(ArrayList<ArrayList<Double>> haar, Path path) throws Exception {
        if (Files.notExists(path)) throw new Exception("Директория " + path.toString() + " отсутствует");
        
        int filesCount = path.toFile().listFiles().length;
        // Предполагаем, что файлы названы 0..n
        for (int i=0; i< filesCount; i++) {
            try (BufferedReader stream = new BufferedReader(new FileReader(path.resolve(Integer.toString(i)).toFile()))) {
                int featuresCount = Integer.parseInt(stream.readLine());
                ArrayList<Double> features = new ArrayList<>();
                String[] featuresStrings = stream.readLine().split(" ");
                // я знаю, что featureCount не нужен, но мне лень переписывать
                for (int j=0; j<featuresCount; j++) {
                    features.add(Double.parseDouble(featuresStrings[j]));
                }
                haar.add(features);
            }
        }
    }
    
    /**
     * Генерирует случайные изображения из заданного набора путем вырезания квадратов
     * @param set множество входных изображений
     * @param count количество изображений для генерации
     * @return 
     */
    private HashMap<String, Mat> generateSet(List<Mat> set, int count) {
        Random rnd = new Random();
        HashMap<String, Mat> result = new HashMap<>();
        for (int i=0; i < count; i++) {
            int picNum = rnd.nextInt(set.size());
            Mat image = set.get(picNum);
            int size = rnd.nextInt(Math.min(image.cols(), image.rows()) - WINDOW_SIZE) + WINDOW_SIZE;
            int x = rnd.nextInt(image.cols() - size);
            int y = rnd.nextInt(image.rows() - size);
            Mat subimage = image.submat(y, y+size, x, x+size);
            Imgproc.resize(subimage, subimage, new Size(WINDOW_SIZE, WINDOW_SIZE));
            result.put(Integer.toString(i), subimage);
          }
        return result;
    }
    
    /**
     * Вычислить интегральные изображения
     * @param ints массив, в который будут записаны изображения
     * @param imgs исходные изображения
     * @throws java.lang.Exception
     */
    private void computeIntegralImages(IntegralImage[] ints, List<Mat> imgs) throws Exception {
        if (DEBUG) System.out.println("Начинаем вычисление интегральных изображений..");
        if (ints.length != imgs.size()) throw new Exception("Размеры коллекций не совпадают");
        
        for (int i=0; i< ints.length; i++)
            ints[i] = new IntegralImage(imgs.get(i));
    }
    
    /**
     * Вычисляет признаки Хаара для заданного массива интегральных изображений
     * @param features лист, куда будут записаны листы признаков
     * @param imgs исходные изображения
     * @throws java.io.IOException
     */
    public final void computeHaarFeatures(ArrayList<ArrayList<Double>> features, IntegralImage[] imgs) throws IOException {
        if (DEBUG) System.out.println("Начинаем вычисление признаков Хаара..");
        for (int i=0; i < imgs.length; i++)
        {
            ArrayList<Double> imgFeatures = new ArrayList<>();
            this.computeHaarFeature(imgFeatures, imgs[i]);
            features.add(imgFeatures);
        }
    }
    
    /**
     * Вычисляет признак Хаара для данного интегрального изображения
     * @param fet признак Хаара
     * @param features результаты вычисления
     * @param img изображение
     */
    private void computeHaarFeature(ArrayList<Double> features, IntegralImage img) {
        final int X_STEP = 3;
        final int Y_STEP = 3;
        final int W_STEP = 3;
        final int H_STEP = 3;
        
        for (IHaarFeature fet : this.haar) {
            for (int x=0; x < WINDOW_SIZE; x += X_STEP) {
                for (int y=0; y < WINDOW_SIZE; y += Y_STEP) {
                    for (int w=3; w < WINDOW_SIZE - x; w += W_STEP) {
                        for (int h=3; h < WINDOW_SIZE - y; h += H_STEP) {
                            Double f = fet.computeFeature(img, x, y, w, h);
                            if (!Double.isNaN(f)) features.add(f);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Обучает детектор
     * @throws java.io.IOException
     */
    public void learnDetector() throws IOException {
        ArrayList<ArrayList<Double>> X = new ArrayList<>(2000);
        ArrayList<Integer> Y = new ArrayList<>(2000);
        ArrayList<Double> W = new ArrayList<>(2000);
        this.negativesHaar.subList(0, 1000).forEach((h) -> {
            X.add(h);
            Y.add(0);
            W.add(1.0);
        });
        this.positivesHaar.subList(0, 1000).forEach((h) -> { 
            X.add(h);
            Y.add(1);
            W.add(1.0);
        });
        
        Boosting boost = new Boosting(X, Y, W);
        boost.learnBoosting();
        boost.save(Paths.get("forest"));
    }
    
    /**
     * Вспомогательный класс для хранения информации о классификаторе
     */
    public static class ClassifierStructure {
        private final int index;
        private final Double error;
        private final DecisionStump model;
        
        public ClassifierStructure(int index, Double error, DecisionStump model) {
            this.index = index;
            this.error = error;
            this.model = model;
        }
        
        public int getIndex()           { return this.index; }
        public Double getError()        { return this.error; }
        public DecisionStump getModel() { return this.model; }
    }
}
