/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector.haar;

import cv.school.tasks.facedetector.IntegralImage;

/**
 *
 * @author roma2_000
 */
public interface IHaarFeature {
    public Double computeFeature(IntegralImage image, int x, int y, int w, int h);
}
