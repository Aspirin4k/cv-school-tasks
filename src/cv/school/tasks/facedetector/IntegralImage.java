/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector;

import org.opencv.core.Mat;

/**
 *
 * @author roma2_000
 */
public class IntegralImage {
    private final double[][] data;
        
    public double[][] getImage() { return this.data; }

    public IntegralImage(Mat image) {
        this.data = new double[image.rows()][];
        for (int i=0; i<image.rows(); i++) {
            this.data[i] = new double[image.cols()];
            for (int j=0; j<image.cols(); j++) {
                double intensity = image.get(i, j)[0];
                // Если это не крайний левый верхний пиксель
                if ((i>0) || (j>0)) {
                    // Сверху есть пиксели
                    if (i>0) intensity += this.data[i-1][j];
                    // Слева есть пиксели
                    if (j>0) intensity += this.data[i][j-1];
                    // Сверху слева есть пиксели
                    if ((i>0) && (j>0)) intensity -= this.data[i-1][j-1];
                }

                this.data[i][j] = intensity;
            }
        }
    }

    public IntegralImage(double[][] image) {
        this.data = new double[image.length][];
        for (int i=0; i<image.length; i++) {
            this.data[i] = new double[image[i].length];
            for (int j=0; j<image[i].length; j++) {
                double intensity = image[i][j];
                // Если это не крайний левый верхний пиксель
                if ((i>0) || (j>0)) {
                    // Сверху есть пиксели
                    if (i>0) intensity += this.data[i-1][j];
                    // Слева есть пиксели
                    if (j>0) intensity += this.data[i][j-1];
                    // Сверху слева есть пиксели
                    if ((i>0) && (j>0)) intensity -= this.data[i-1][j-1];
                }

                this.data[i][j] = intensity;
            }
        }
    }

    /**
     * Возвращает сумму заданного участка
     * @param x1 левый верхний угол X
     * @param x2 правый нижний угол X
     * @param y1 левый верхний угол Y
     * @param y2 правый нижний угол Y
     * @return сумма
     */
    public double getSum(int x1, int x2, int y1, int y2) {
        return this.data[y2][x2] 
                - ((y1 > 0) ? this.data[y1-1][x2] : 0) 
                - ((x1 > 0) ? this.data[y2][x1-1] : 0)
                + (((x1 > 0) && (y1 > 0)) ? this.data[y1-1][x1-1] : 0);
    }
    
    public int getWidth()   { return (this.data.length > 0) ? this.data[0].length : 0; }
    public int getHeight()  { return this.data.length; }
}
