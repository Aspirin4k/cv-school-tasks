/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.catdogdetector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

/**
 *
 * @author Andew
 */
public class CatDogDetector {
    
    private List<MatOfFloat> negative;
    private HOGDescriptor hog;
    
    /**
     * Инициализация переменных
     */
    public CatDogDetector() {
        this.negative = new ArrayList<>();
        this.hog = new HOGDescriptor(
                new Size(128,128),    // Считаем одну гистограму для всей картинки
                new Size(32,32),      // Размер блока
                new Size(16,16),      // Смещение, на которое может сдвигаться блок
                new Size(16,16),      // Размер ячейки
                9,
                1,
                -1,
                0,
                0.2,
                true,
                64,
                true
        );
    }
    
    /**
     * Загружает изображения из папки по одному
     * @param path путь до папки
     * @throws java.io.IOException
     */
    public void readOneByOne(Path path) throws IOException {
        // Каждый файл в директории
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile).forEach((file) -> {
                // Сверяем расширение перед загрузкой
                String filename = file.toString();
                String extention = filename.substring(filename.lastIndexOf(".") + 1);
                if (extention.equalsIgnoreCase("jpg") || extention.equalsIgnoreCase("png") ||
                        extention.equalsIgnoreCase("bmp"))
                {
                    try {
                        Mat mat;
                        mat = Imgcodecs.imread(filename);
                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
                        MatOfFloat descriptors = new MatOfFloat();
                        this.hog.compute(mat, descriptors);
                        this.negative.add(descriptors);
                        mat.release();
                    } catch (Exception ex) {
                        // Есть битые изображения
                        System.out.println(String.format("Ошибка %s в файле %s", ex.toString(), filename));
                    }
                }
            });
        }
    }
}
