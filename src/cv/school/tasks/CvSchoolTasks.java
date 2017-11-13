/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import cv.school.tasks.catdogdetector.CatDogDetector;
import cv.school.tasks.imgaligner.ImgAligner;
import cv.school.tasks.traincreator.TrainCreator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author aspid
 */
public class CvSchoolTasks {
    
    private static final boolean DEBUG = true;
    private static final String[] DATA_SETS = {
        "train",
        "validation",
        "test"
    };

    /**
     * @param args аргументы командной строки. Ожидается 1 аргумент - папка с изображениями
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
   //     TrainCreator tc = new TrainCreator("tc.json");
        CatDogDetector catDetector = new CatDogDetector();
        // Пытаемся загрузить модель, если уже тренировали
        if (!catDetector.loadDetector(Paths.get("cat_model.mdl"))) {
            catDetector.loadSet(Paths.get("train_cat/neg"), false);
            catDetector.loadSet(Paths.get("train_cat/dog"), false);
            catDetector.loadSet(Paths.get("train_cat/cat"), true);
            System.out.println(
                    String.format("Обучающая выборка содержит %d изображений с котами и %d без", 
                            catDetector.getPositiveCount(), catDetector.getNegativeCount())
            );
            System.out.println("Обучаем котэ-детектор...");
            catDetector.trainDetector();
            catDetector.saveDetector();
        }
        
//        catDetector.preidct(Paths.get("train_cat/neg"), false);
//        catDetector.preidct(Paths.get("train_cat/dog"), false);
//        catDetector.preidct(Paths.get("train_cat/cat"), true);
        
        System.out.println("Ищем котов...");
        ImgLoadingPredictWithWindow operation = new ImgLoadingPredictWithWindow(catDetector, "output/cat");
        ImgLoader.readOneByOne(operation, Paths.get("task5/cat"));
        ImgLoader.readOneByOne(operation, Paths.get("task5/dog"));
        
        CatDogDetector dogDetector = new CatDogDetector();
        // Пытаемся загрузить модель, если уже тренировали
        if (!dogDetector.loadDetector(Paths.get("dog_model.mdl"))) {
            dogDetector.loadSet(Paths.get("train_cat/neg"), false);
            dogDetector.loadSet(Paths.get("train_cat/dog"), true);
            dogDetector.loadSet(Paths.get("train_cat/cat"), false);
            System.out.println(
                    String.format("Обучающая выборка содержит %d изображений с псами и %d без", 
                            dogDetector.getPositiveCount(), dogDetector.getNegativeCount())
            );
            System.out.println("Обучаем песько-детектор...");
            dogDetector.trainDetector();
            dogDetector.saveDetector();
        }
        
        System.out.println("Ищем песьков...");
        ImgLoadingPredictWithWindow operationDog = new ImgLoadingPredictWithWindow(dogDetector, "output/dog");
        ImgLoader.readOneByOne(operation, Paths.get("task5/cat"));
        ImgLoader.readOneByOne(operation, Paths.get("task5/dog"));
    }
    
    /**
     * Метод вычисляет для каждого изображения из исходной папки совмещенное изображение
     * @param src директория с исходными изображениями
     * @param dst дриектория, куда будут помещены результаты
     * @throws IOException 
     */
    public static void alignImages(Path src, Path dst) throws IOException
    {
        HashMap<String, Mat> images = ImgLoader.loadData(src);
        
        // Создаем папки, если их нет
        if (Files.notExists(dst)) Files.createDirectories(dst);
        
        for (Entry<String, Mat> entry : images.entrySet())
        {
            System.out.println(String.format("Обрабатываем %s ...",entry.getKey()));      
            // Получаем изображение
            Mat alignedImg = ImgAligner.getAlignedImage(entry.getValue());
            
            Imgcodecs.imwrite(String.format("%s/aligned_%s", dst.toAbsolutePath(), entry.getKey()), alignedImg);
        }
    }
}
