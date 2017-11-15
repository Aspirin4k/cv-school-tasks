/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Дополнительные функции, не обязательно связанные с компьютерным зрением
 * @author aspid
 */
public class ExtraFunctions {
    /**
     * Функция возвращает медиану для входного массива
     * @param array массив значений
     * @return медиана
     * @throws java.lang.Exception функция пока что работает только с массивами нечетной длины
     */
    public static double getMedian(double[] array) throws Exception {
        
        if (array.length % 2 == 0) 
            throw new Exception("Функционал для получения медианы из массива четной длины не реализован");
        
        // По факту мы можем найти не обязательно медиану. k - номер порядковой
        // статистики, поэтому можно таким же образом найти любое по величине значение
        int k = array.length / 2;
        int left = 0;
        int right = array.length - 1;
        double buffer;
        for (;;)
        {
            if (right <= left + 1)
            {
                if ((right == left + 1) && (array[right] < array[left]))
                    swap(array, left, right);
                return array[k];
            }

            int mid = (left + right) / 2;
            buffer = array[mid];
            array[mid] = array[left+1];
            array[left+1] = buffer;
            if (array[left] > array[right])
                swap(array, left, right);
            if (array[left+1] > array[right])
                swap(array, left+1, right);
            if (array[left] > array[left+1])
                swap(array, left, left+1);

            // реализация функции разбиния из "быстрой сортировки"
            int i = left + 1;
            int j = right;
            double cur = array[left + 1];
            for (;;)
            {
                while(array[++i] < cur);
                while(array[--j] > cur);
                if (i>j)
                    break;
                swap(array, i, j);
            }

            array[left+1] = array[j];
            array[j] = cur;

            if (j >= k)
                right = j -1;
            if (j <= k)
                left = i;
        }
    }
    
    /**
     * Меняет местами 2 значения в массиве
     * @param array изначальный (он же и результирующий) массив
     * @param i индекс 1
     * @param j индекс 2
     */
    private static void swap(double[] array, int i, int j) {
        double buffer = array[i];
        array[i] = array[j];
        array[j] = buffer;
    }
    
    /**
     * Конвертирует 4 байта из массива по данному смещению в целочисленное 4-х байтовое
     * @param bytes массив байт
     * @param offset смещение
     * @return целочисленное
     */
    public static int toInt(byte[] bytes, int offset) {
        int ret = 0;
        for (int i=0; i<4 && i+offset<bytes.length; i++) {
          ret <<= 8;
          ret |= (int)bytes[i+offset] & 0xFF;
        }
        return ret;
    }
    
    /**
     * Нормализует изображение (Вычитание среднего и деление на отклонение)
     * @param image исходное изображение
     */
    public static void normalizeImage(Mat image) {
        MatOfDouble meanArr = new MatOfDouble();
        MatOfDouble stdArr = new MatOfDouble();
        Core.meanStdDev(image, meanArr, stdArr);
        // Оно считает среднее и отклонение для каждого канала
        // однако, сейчас используется только для черно-белого (1 канал)
        // позже надо бы исправить под многоканальные изображения
        double mean = meanArr.toArray()[0];
        double std = stdArr.toArray()[0];
        Double min = Double.NaN;
        double[][] matrix = new double[image.rows()][];
        for (int i=0; i < image.rows(); i++) {
            matrix[i] = new double[image.cols()];
            for (int j=0; j < image.cols(); j++) {
                matrix[i][j] = (image.get(i, j)[0] - mean) / std;
                if ((matrix[i][j] < min) || Double.isNaN(min)) min = matrix[i][j];
            }
        }
        
        for (int i=0; i< image.rows(); i++) {
            for (int j=0; j< image.cols(); j++) {
                image.put(i, j, matrix[i][j] + Math.abs(min));
            }
        }
        
        Imgcodecs.imwrite("test.jpg", image);
    }
}
