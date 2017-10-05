/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import cv.school.tasks.rawclassificator.RawClassificator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        // Проверка на количество аргументов командной строки
        if (args.length != 1) {
            System.out.println("Использование: run <target>");
            System.out.println("<target> - папка с изображениями");
            return;
        }
        
        String data_dir = args[0];
        
        Path path = Paths.get(data_dir);
        if (Files.notExists(path)) {
            System.out.println("Целевая директория не найдена");
            return;
        }
        
        // Дебажная информация о подкаталогах в целевой директории
        if (DEBUG) {
            System.out.println("Целевая директория найдена и содержат подкаталоги:");
            for (String dir : DATA_SETS) {
                if (Files.exists(path.resolve(dir))) {
                    System.out.println("Каталог содержит набор " + dir);
                }
            }
        }
        
        ArrayList<Mat> data_set;
        Path subpath;
        Path pospath;
        Path negpath;
        // Для каталогов train, test и validation внутри целевой папки
        for (String dir : DATA_SETS) {
            subpath = path.resolve(dir);
            if (Files.exists(subpath)) {
                // К-ты для точности
                int tp = 0; // true positive
                int tn = 0; // true negative
                int fp = 0; // false positive
                int fn = 0; // false negative
                int n = 0;  // общее количество образцов
                
                System.out.println("Результаты для " + subpath.toString());
                
                // Загружаются изображения с огнем
                pospath = subpath.resolve("pos");
                if (Files.exists(pospath)) {
                    // Загружаются в память изображения
                    data_set = ImgLoader.loadData(pospath);
                    // Каждое классифицируется
                    for (Mat img : data_set) {
                        int classificated = RawClassificator.classify(img);
                        // Дополнительно для вычисления точности определяются к-ты
                        if (classificated == 1) {
                            tp++;
                        } else {
                            fn++;
                        }
                        n++;
                    }
                } else {
                    if (DEBUG) {
                        System.out.println("Ожидал увидеть pos в " + subpath.toString() + ", но его нет");
                    }
                }
                
                // Загружаются изображения без огня
                negpath = subpath.resolve("neg");
                if (Files.exists(negpath)) {
                    // Загружаются в память изображения
                    data_set = ImgLoader.loadData(negpath);
                    // Каждое классифицируется
                    for (Mat img : data_set) {
                        int classificated = RawClassificator.classify(img);
                        
                        // Дополнительно для вычисления точности определяются к-ты
                        if (classificated == 0) {
                            tn++;
                        } else {
                            fp++;
                        }
                        n++;
                    }
                } else {
                    if (DEBUG) {
                        System.out.println("Ожидал увидеть neg в " + subpath.toString() + ", но его нет");
                    }
                }
                
                System.out.println("Точность=" + ((n>0) ? (double)(tp + tn) / n :  0));
            }
        }
    }
    
}
