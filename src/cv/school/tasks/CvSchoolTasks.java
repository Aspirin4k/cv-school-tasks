/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import cv.school.tasks.movdetector.MovDetector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.opencv.core.Core;

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
        if (args.length != 1) {
            System.out.println("Использование: run <target>");
            System.out.println("<target> - видео");
            return;
        }
        
        String data_dir = args[0];
        
        Path path = Paths.get(data_dir);
        if (Files.notExists(path)) {
            System.out.println("Видео не найдено");
            return;
        }
        
        // Подгружаем видео из файла и обрабатываем его
        MovDetector.Detect(VideoLoader.loadFromFile(path));
   //     MovDetector.Detect(VideoLoader.loadFromCam());
    }
}
