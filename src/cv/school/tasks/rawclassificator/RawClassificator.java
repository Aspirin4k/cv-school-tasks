/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.rawclassificator;

import org.opencv.core.Mat;

/**
 *
 * @author aspid
 */
public class RawClassificator {
    // Чувствительность
    private static final double SENS = 0.5;
    
    public static int classify(Mat img) {
        int nRows = img.rows();
        int nCols = img.cols();
        
        double avgR = 0;
        double avgG = 0;
        double avgB = 0;
        for (int i=0; i<nRows; i++)
        {
            for (int j=0; j<nCols; j++)
            {
                // Ожидается, что пиксель в RGB
                double[] pixel = img.get(i, j);
                avgR += (pixel[0] / 32 / 32);
                avgG += (pixel[1] / 32 / 32);
                avgB += (pixel[2] / 32 / 32);
            }
        }
        
        System.out.println(String.format("AVERAGE: %.2f %.2f %.2f",avgR, avgG, avgB));
        return 1;
    }
}
