/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.digmodel;

import cv.school.tasks.ExtraFunctions;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

/**
 *
 * @author aspid
 */
public class DigModel {
    
    public enum Set {
        TRAIN,
        TEST
    }
    
    private List<Byte> labelsTrain;
    private List<Mat> imagesTrain; 
    private List<Byte> labelsTest;
    private List<Mat> imagesTest; 
    private Model model;
    
    public DigModel() {
        this.labelsTrain = null;
        this.imagesTrain = null;
        this.labelsTest = null;
        this.imagesTest = null;
        this.model = null;
    }
    
    /**
     * Функция строит модель используя для этого входные файлы как тренировочный набор
     * @param pathLabel
     * @param pathImage
     * @throws Exception 
     */
    public void buildModel(Path pathLabel, Path pathImage) throws Exception {
        // Подгрузим файлы
        this.parseFiles(pathLabel, pathImage, Set.TRAIN);
        
        // Параметры модели
        double C = 1.0;
        double eps = 0.01;
        Parameter param = new Parameter(SolverType.L2R_LR, C, eps); // LR - logistic regression
        Problem problem = new Problem();
        
        // Количество тренировочных записей
        int NUM_OF_TS_EXAMPLES = this.imagesTrain.size();
        problem.l = NUM_OF_TS_EXAMPLES;
        
        // Количество фич
        // TODO: АДЕКВАТНО ВЫЧИСЛЯТЬ НАДО, А НЕ ХАРДКОДИТЬ В НАЧАЛЕ!
        problem.n = 324;
        
        // Результаты трейн сета. Костылим, т.к тип примитивный
        // TODO: ЛИБА ОКОНЧАТЕЛЬНО ВЫБРАНА, ЗНАЧИТ ФОРМАТ ВХОДНЫХ ДАННЫХ УТВЕРЖДЕН - ПЕРЕПИСАТЬ ПОД НЕГО!
        double[] GROUPS_ARRAY = new double[this.labelsTrain.size()];
        for (int i=0; i< this.labelsTrain.size(); i++)
            GROUPS_ARRAY[i] = this.labelsTrain.get(i);
        problem.y = GROUPS_ARRAY;

        // Настройки дескриптора HOG
        HOGDescriptor hog = new HOGDescriptor(
                new Size(28,28),    // Считаем одну гистограму для всей картинки
                new Size(14,14),    // Размер блока
                new Size(7,7),      // Смещение, на которое может сдвигаться блок
                new Size(7,7),      // Размер ячейки
                9,
                1,
                -1,
                0,
                0.2,
                true,
                64,
                true
        );
        
        // Заполняем тренировочный набор
        List<FeatureNode[]> trainSet = new ArrayList<>();
        for (int i=0; i<this.labelsTrain.size(); i++)
        {
            List<FeatureNode> instance = new ArrayList<>();
            Mat mat = this.imagesTrain.get(i);
            MatOfFloat descriptors = new MatOfFloat();
            hog.compute(mat, descriptors);
            
            float[] buffer = descriptors.toArray();
            for (int y=0; y<buffer.length; y++)
                if (buffer[y] != 0)
                    instance.add(new FeatureNode(y+1,buffer[y]));
            FeatureNode[] fn = new FeatureNode[instance.size()];
            instance.toArray(fn);
            trainSet.add(fn);
            if ((i!=0) && (i % 1000 == 0)) System.out.println(String.format("Создано %d тренировочных записей...",i));
        }
        
        // Криво косо парсим и используем как X
        FeatureNode[][] trainArray = new FeatureNode[trainSet.size()][];
        trainSet.toArray(trainArray);
        problem.x = trainArray;
        
        System.out.println("Строим модель...");
        Linear.disableDebugOutput();
        this.model = Linear.train(problem, param);
        System.out.println("Модель построена!");
    }
   
    /**
     * Тестирование модели
     * @param pathLabel
     * @param pathImage
     * @throws Exception 
     */
    public void testModel(Path pathLabel, Path pathImage) throws Exception {
         // Подгрузим файлы
        this.parseFiles(pathLabel, pathImage, Set.TEST);
        
        // Настройки дескриптора HOG
        HOGDescriptor hog = new HOGDescriptor(
                new Size(28,28),    // Считаем одну гистограму для всей картинки
                new Size(14,14),    // Размер блока
                new Size(7,7),      // Смещение, на которое может сдвигаться блок
                new Size(7,7),      // Размер ячейки
                9,
                1,
                -1,
                0,
                0.2,
                true,
                64,
                true
        );
        
        double k=0;
        for (int i=0; i< this.imagesTest.size(); i++) {
            List<FeatureNode> instance = new ArrayList<>();
            
            Mat mat = this.imagesTest.get(i);
            MatOfFloat descriptors = new MatOfFloat();
            hog.compute(mat, descriptors);
            
            float[] buffer = descriptors.toArray();
            for (int y=0; y<buffer.length; y++)
                if (buffer[y] != 0)
                    instance.add(new FeatureNode(y+1,buffer[y]));
            
            FeatureNode[] fn = new FeatureNode[instance.size()];
            instance.toArray(fn);
            
            if (Linear.predict(model, fn) == this.labelsTest.get(i))
                k++;
        }
        System.out.println(String.format("На выборке %s имеем точность %f", pathImage.toString(), k / this.imagesTest.size()));
    }
    
    /**
     * Проверить запись
     * @param map
     */
    public void testModel(HashMap<String, Mat> map) {
        // Настройки дескриптора HOG
        HOGDescriptor hog = new HOGDescriptor(
                new Size(28,28),    // Считаем одну гистограму для всей картинки
                new Size(14,14),    // Размер блока
                new Size(7,7),      // Смещение, на которое может сдвигаться блок
                new Size(7,7),      // Размер ячейки
                9,
                1,
                -1,
                0,
                0.2,
                true,
                64,
                true
        );
        
        double k=0;
        int i=1;
        for (Map.Entry<String, Mat> entry : map.entrySet()) {
            Mat mat = entry.getValue();
            double num = Double.parseDouble(entry.getKey().substring(0, entry.getKey().indexOf(".")).split("_")[1]);
            
            List<FeatureNode> instance = new ArrayList<>();
            
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
            MatOfFloat descriptors = new MatOfFloat();
            hog.compute(mat, descriptors);
            
            float[] buffer = descriptors.toArray();
            for (int y=0; y<buffer.length; y++)
                if (buffer[y] != 0)
                    instance.add(new FeatureNode(y+1,buffer[y]));
            
            FeatureNode[] fn = new FeatureNode[instance.size()];
            instance.toArray(fn);
            
            double predicted = Linear.predict(model, fn);
            System.out.println(String.format("Распознали %f как %f", num, predicted));
            if (predicted == num)
            {
                k++;
            }
            i++;
        }
        System.out.println(String.format("Имеем точность %f", k / i));
    }
    
    /**
     * Метод извлекает изображения или лейблы из файла
     * @param pathLabel
     * @param pathImage
     * @param set
     * @throws java.io.IOException 
     */
    private void parseFiles(Path pathLabel, Path pathImage, Set set) throws Exception {
        byte[] dataLabel = Files.readAllBytes(pathLabel);
        int offsetLabel = 0;
        int magicLabel = ExtraFunctions.toInt(dataLabel, 0);
        offsetLabel += 4;
        
        if (magicLabel != 2049) throw new Exception("Файл с лейблами имеет неверную сигнатуру");
        
        byte[] dataImage = Files.readAllBytes(pathImage);
        int offsetImage = 0;
        int magicImage = ExtraFunctions.toInt(dataImage, 0);
        offsetImage += 4;
        
        if (magicImage != 2051) throw new Exception("Файл с изображениями имеет неверную сигнатуру");
        
        switch (set) {
            case TRAIN: {
                    this.labelsTrain = this.parseLabels(dataLabel, offsetLabel);
                    this.imagesTrain = this.parseImages(dataImage, offsetImage);
                break;
            }
            case TEST: {
                    this.labelsTest = this.parseLabels(dataLabel, offsetLabel);
                    this.imagesTest = this.parseImages(dataImage, offsetImage);
                break;
            }
        }
    }
    
    /**
     * Парсит лейблы
     * @param data 
     */
    private ArrayList<Byte> parseLabels(byte[] data, int offset) throws Exception {
        int count = ExtraFunctions.toInt(data, offset);
        offset += 4;
        
        if (count < 0) throw new Exception("Отрицательное количество лейблов");
        
        ArrayList<Byte> result = new ArrayList<>();
        for (int i=0; i< count; i++)
            result.add(data[offset + i]);
        
        offset += count;
        return result;
    }
    
    /**
     * Парсит изображения в локальный список Mat
     * @param data массив байт
     * @param offset смещение
     * @return смещение после парсинга
     * @throws Exception 
     */
    private ArrayList<Mat> parseImages(byte[] data, int offset) throws Exception {
        int count = ExtraFunctions.toInt(data, offset);
        offset +=4;
        
        if (count < 0) throw new Exception("Отрицательное количество изображений");
        
        int height = ExtraFunctions.toInt(data, offset);
        offset += 4;
        int width = ExtraFunctions.toInt(data, offset);
        offset += 4;
        int pixelsCount = height * width;
        ArrayList<Mat> result = new ArrayList<>();
        for (int i=0; i < count; i++) {
            double[] pixels = new double[pixelsCount];
            for (int j=0; j< pixelsCount; j++) {
                pixels[j] = (data[j+offset] + 256) % 256;
            }
            offset += pixelsCount;
            Mat mat = new Mat(height, width, CvType.CV_8UC1);
            mat.put(0, 0, pixels);
            // TODO: АХТУНГ! КОСТЫЛИЩЕ! ИСПРАВИТЬ ПОСЛЕ ТОГО КАК РАЗБЕРЕШЬСЯ, ПОЧЕМУ НЕ РАБОТАЕТ С 24х24!
        //    Imgproc.resize(mat, mat, new Size(24,24));
            result.add(mat);
        }
        
        return result;
    }
    
    /**
     * Записать изображения на диск
     * @param path путь до папки, в которую будут сохранены изображения
     * @throws java.lang.Exception
     */
    public void writeToDisk(Path path) throws Exception {
        if (!path.toFile().exists()) Files.createDirectories(path);
        
        if (!path.toFile().isDirectory()) throw new Exception("Ожидалась директория");

        if (this.imagesTrain.size() != this.labelsTrain.size()) throw new Exception("Листы разной длины");
        
        for (int i=0; i< this.imagesTrain.size(); i++)
        {
            Imgcodecs.imwrite(String.format("%s/%d_%d.jpg", path.toAbsolutePath(), i, this.labelsTrain.get(i)), this.imagesTrain.get(i));
        }
    }
}
