/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector.haar;

import cv.school.tasks.facedetector.IntegralImage;

/**
 * Две горизонтальные полосы. Сверху черная, снизу белая
 * @author roma2_000
 */
public class HaarHorizontal2S implements IHaarFeature {

    @Override
    public Double computeFeature(IntegralImage image, int x, int y, int w, int h) {
        if ((x >= 0) && (y >= 0) && (x + w <= image.getWidth()) && (y + h) <= image.getHeight() && (h % 2 == 0) && (h>=2) && (w>=2)) {
            double s2 = image.getSum(x, x + w - 1, y, y + h / 2 - 1);
            double s1 = image.getSum(x, x + w - 1, y + h / 2, y + h - 1);
            return s1 - s2;
        }
        return Double.NaN;
    }
    
}
