/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author aspid
 */
public class ImgLoader {
    /**
     * Функция загружает в оперативную память все изображения из каталога
     * @param dirPath путь до директории, содержащей изображения
     * @return коллекция объектов загруженных изображений
     */
    public static HashMap<String, Mat> loadData(Path dirPath) {
        HashMap<String, Mat> resultList = new HashMap<>();
        
        File dir = dirPath.toFile();
        // Получаем список файлов внутри каталога
        File[] directoryContent = dir.listFiles();
        if (directoryContent != null) {
            for (File child : directoryContent) {
                // Сверяем расширение перед загрузкой
                String filename = child.getName();
                String extention = filename.substring(filename.lastIndexOf(".") + 1);
                if (extention.equalsIgnoreCase("jpg") || extention.equalsIgnoreCase("png") ||
                        extention.equalsIgnoreCase("bmp"))
                {
                    resultList.put(filename,Imgcodecs.imread(child.getPath()));
                }
            }
        }
        
        return resultList;
    }
}
