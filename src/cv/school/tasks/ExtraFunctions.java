/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

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
}
