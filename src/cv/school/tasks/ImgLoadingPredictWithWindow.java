/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import cv.school.tasks.catdogdetector.CatDogDetector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author roma2_000
 */
public class ImgLoadingPredictWithWindow implements IImgLoadingOp {

    private final CatDogDetector detector;
    private final int[] windowSizes;
    private int count;
    private final String output;
    
    public ImgLoadingPredictWithWindow(CatDogDetector detector, String out) throws IOException {
        this.windowSizes = new int[]{256, 172, 128, 96, 64, 48, 32};
        this.detector = detector;
        this.count = 0;
        this.output = out;
        Files.createDirectories(Paths.get(this.output));
    }
    
    public int getCount() { return this.count; }
    
    @Override
    public void execute(Mat image) {
        for (int windowSize : this.windowSizes) {
            for (int i=0; i<image.cols() - windowSize; i += 8)
            {
                for (int j=0; j<image.rows() - windowSize; j += 8)
                {
                    Mat mat = image.submat(j, j+ windowSize, i, i+windowSize);
                    Imgproc.resize(mat, mat, new Size(64,64));
                    if (this.detector.predict(mat)) {
                        Imgproc.rectangle(image, new Point(i,j), new Point(i+windowSize, j+windowSize), new Scalar(255,0,0));
                        System.out.println("Нашли!");
                    }
                }
            }
        }
        Imgcodecs.imwrite(String.format("%s/%d.jpg", this.output, this.count), image);
        this.count++;
        image.release();
    }
    
}
