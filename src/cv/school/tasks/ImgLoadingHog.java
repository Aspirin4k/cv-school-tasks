/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

/**
 * Вычисляет hog от изображения и сохраняет данные
 * @author roma2_000
 */
public class ImgLoadingHog implements IImgLoadingOp {

    private final List<MatOfFloat> data;
    private final HOGDescriptor hog;
    
    public ImgLoadingHog(HOGDescriptor hog) {
        this.hog = hog;
        this.data = new ArrayList<>();
    }
    
    public List<MatOfFloat> getData() { return this.data; }
    
    @Override
    /**
     * Вычисляет HOG для изображения
     */
    public void execute(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        MatOfFloat descriptors = new MatOfFloat();
        this.hog.compute(image, descriptors);
        this.data.add(descriptors);
        image.release();
    }
}
