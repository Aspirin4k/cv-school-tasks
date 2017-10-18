/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import cv.school.tasks.imgaligner.ImgAligner;
import cv.school.tasks.movdetector.MovDetector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import org.opencv.core.Core;
import org.opencv.core.Mat;

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
     */
    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        // Проверка на количество аргументов командной строки
        if (args.length != 2) {
            System.out.println("Использование: run <source> <target>");
            System.out.println("<source> - папка с необработанными изображениями");
            System.out.println("<source> - папка с обработанными изображениями");
            return;
        }
        
        String dataDir = args[0];    
        Path dataPath = Paths.get(dataDir);
        if (Files.notExists(dataPath)) {
            System.out.println("Папка с рисунками не найдена");
            return;
        }
        
        String resDir = args[1];
        Path resPath = Paths.get(resDir);
        
        // Вычисляем совмещенные изображения
        alignImages(dataPath, resPath);
    }
    
    public static void alignImages(Path src, Path dst)
    {
        HashMap<String, Mat> images = ImgLoader.loadData(src);
        for (Entry<String, Mat> entry : images.entrySet())
        {
            System.out.println(String.format("Обрабатываем %s ...",entry.getKey()));      
            // Получаем изображение
            ImgAligner.getAlignedImage(entry.getValue());
        }
    }
}
