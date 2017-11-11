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
        
        TrainCreator tc = new TrainCreator("tc.json");
  //      CatDogDetector detector = new CatDogDetector();
   //     detector.readOneByOne(Paths.get("train_cat/neg"));
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
