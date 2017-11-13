/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;
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
                        extention.equalsIgnoreCase("bmp") || extention.equalsIgnoreCase("pgm"))
                {
                    resultList.put(dirPath.toString() + filename,Imgcodecs.imread(child.getPath()));
                }
                
                if (child.isDirectory()) {
                    resultList.putAll(ImgLoader.loadData(child.toPath()));
                }
            }
        }
        
        return resultList;
    }
    
     /**
     * Загружает изображения из папки по одному и выполняет указанное действие
     * @param operation действие, которое будет выполнено для изображения
     * @param path путь до папки
     * @throws java.io.IOException
     */
    public static void readOneByOne(IImgLoadingOp operation, Path path) throws IOException {
        // Каждый файл в директории
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile).forEach((file) -> {
                // Сверяем расширение перед загрузкой
                String filename = file.toString();
                String extention = filename.substring(filename.lastIndexOf(".") + 1);
                if (extention.equalsIgnoreCase("jpg") || extention.equalsIgnoreCase("png") ||
                        extention.equalsIgnoreCase("bmp") || extention.equalsIgnoreCase("pgm"))
                {
                    try {
                        Mat mat;
                        mat = Imgcodecs.imread(filename);
                        operation.execute(mat);
                    } catch (Exception ex) {
                        // Есть битые изображения
                        System.out.println(String.format("Ошибка %s в файле %s", ex.toString(), filename));
                    }
                }
            });
        }
    }
}
