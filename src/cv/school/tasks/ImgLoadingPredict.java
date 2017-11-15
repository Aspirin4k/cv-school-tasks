/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import cv.school.tasks.catdogdetector.CatDogDetector;
import org.opencv.core.Mat;

/**
 * Предсказывает класс для каждого изображения
 * @author roma2_000
 */
public class ImgLoadingPredict implements IImgLoadingOp {

    private final CatDogDetector detector;
    private int pos;
    private int neg;
    
    public ImgLoadingPredict(CatDogDetector detector) {
        this.detector = detector;
        this.pos = 0;
        this.neg = 0;
    }
    
    public int getPos() { return this.pos; }
    public int getNeg() { return this.neg; }

    @Override
    public void execute(Mat image) {
        if (this.detector.predict(image))
            pos++;
        else
            neg++;
    }
    
}
