/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector.haar;

import cv.school.tasks.facedetector.IntegralImage;

/**
 * Три горизонтальные полосы. Белая-черная-белая
 * @author roma2_000
 */
public class HaarHorizontal3S implements IHaarFeature {

    @Override
    public Double computeFeature(IntegralImage image, int x, int y, int w, int h) {
         if ((x >= 0) && (y >= 0) && (x + w <= image.getWidth()) && (y + h) <= image.getHeight() && (h % 3 == 0) && (h>=3) && (w>=2)) {
            double s1 = image.getSum(x, x + w - 1, y, y + h / 3 - 1) + image.getSum(x, x + w - 1, y + 2 * h / 3, y + h - 1);
            double s2 = image.getSum(x, x + w - 1, y + h / 3, y + 2 * h / 3 - 1);
            return s1 - s2;
        }
        return Double.NaN;
    }
    
}
