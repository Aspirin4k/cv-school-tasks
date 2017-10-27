/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import cv.school.tasks.imgaligner.ImgAligner;
import cv.school.tasks.movdetector.MovDetector;
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
        
        // Проверка на количество аргументов командной строки
//        if (args.length != 2) {
//            System.out.println("Использование: cv-school-tasks <source> <target>");
//            System.out.println("<source> - папка с необработанными изображениями");
//            System.out.println("<target> - папка с обработанными изображениями");
//            return;
//        }
//        
//        String dataDir = args[0];    
//        Path dataPath = Paths.get(dataDir);
//        if (Files.notExists(dataPath)) {
//            System.out.println(String.format("Папка %s с рисунками не найдена", dataDir));
//            return;
//        }
//        
//        String resDir = args[1];
//        Path resPath = Paths.get(resDir);
        
        // Вычисляем совмещенные изображения
        // alignImages(dataPath, resPath);
    
        // Создание трейн сета
        Path path = Paths.get(args[0]).toAbsolutePath();
        TrainCreator creator = new TrainCreator();
        creator.start(path);
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
