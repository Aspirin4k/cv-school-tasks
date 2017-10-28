/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.digmodel;

import cv.school.tasks.ExtraFunctions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author aspid
 */
public class DigModel {
    
    private List<Byte> labels;
    private List<Mat> images; 
    
    /**
     * Метод извлекает изображения или лейблы из файла
     * @param path путь к файлу
     * @throws java.io.IOException 
     */
    public void parseFiles(Path path) throws Exception {
        byte[] data = Files.readAllBytes(path);
        
        int offset = 0;
        int magic = ExtraFunctions.toInt(data, 0);
        offset += 4;
        switch (magic) {
            // Сигнатура для лейблов
            case 2049: {
                this.parseLabels(data, offset);
                break;
            }
            // Сигнатура для изображений
            case 2051: {
                this.parseImages(data, offset);
                break;
            }
            default: {
                throw new Exception("Неадекватная сигнатура файла");
            }
        }
    }
    
    /**
     * Парсит лейблы
     * @param data 
     */
    private int parseLabels(byte[] data, int offset) throws Exception {
        int count = ExtraFunctions.toInt(data, offset);
        offset += 4;
        
        if (count < 0) throw new Exception("Отрицательное количество лейблов");
        
        this.labels = new ArrayList<>();
        for (int i=0; i< count; i++)
            labels.add(data[offset + i]);
        
        offset += count;
        return offset;
    }
    
    /**
     * Парсит изображения в локальный список Mat
     * @param data массив байт
     * @param offset смещение
     * @return смещение после парсинга
     * @throws Exception 
     */
    private int parseImages(byte[] data, int offset) throws Exception {
        int count = ExtraFunctions.toInt(data, offset);
        offset +=4;
        
        if (count < 0) throw new Exception("Отрицательное количество изображений");
        
        int height = ExtraFunctions.toInt(data, offset);
        offset += 4;
        int width = ExtraFunctions.toInt(data, offset);
        offset += 4;
        int pixelsCount = height * width;
        this.images = new ArrayList<>();
        for (int i=0; i < count; i++) {
            double[] pixels = new double[pixelsCount];
            for (int j=0; j< pixelsCount; j++) {
                pixels[j] = (data[j+offset] + 256) % 256;
            }
            offset += pixelsCount;
            Mat mat = new Mat(height, width, CvType.CV_8UC1);
            mat.put(0, 0, pixels);
            this.images.add(mat);
        }
        
        return offset;
    }
    
    /**
     * Записать изображения на диск
     * @param path путь до папки, в которую будут сохранены изображения
     * @throws java.lang.Exception
     */
    public void writeToDisk(Path path) throws Exception {
        if (!path.toFile().exists()) Files.createDirectories(path);
        
        if (!path.toFile().isDirectory()) throw new Exception("Ожидалась директория");

        if (this.images.size() != this.labels.size()) throw new Exception("Листы разной длины");
        
        for (int i=0; i< this.images.size(); i++)
        {
            Imgcodecs.imwrite(String.format("%s/%d_%d.jpg", path.toAbsolutePath(), i, this.labels.get(i)), this.images.get(i));
        }
    }
}
