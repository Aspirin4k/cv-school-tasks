/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.traincreator;

import cv.school.tasks.VideoLoader;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Path;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author Andew
 */
public class TrainCreator {
    
    private TrainCreatorForm form;
    private VideoCapture cap;
    private int frameNum;
    
    /**
     * Инициализирует форму
     * @param path
     * @throws java.lang.Exception
     */
    public void start(Path path) throws Exception {

        this.cap = VideoLoader.loadFromFile(path);
        if (this.cap == null) throw new Exception("Файл не найден");
        
        this.form = new TrainCreatorForm();
        this.form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.frameNum = 0;
        this.nextFrame();
        
        this.form.setVisible(true);
    }
    
    /**
     * Рисует на форме следующий кадр
     */
    private void nextFrame() {
        Mat frame = new Mat();
        if (this.cap.read(frame)) {
            ImageIcon img =  new ImageIcon(this.convertMat2BI(frame));
            this.form.getImgLabel().setIcon(img);
        }
    }
    
    /**
     * Конвертирует из Mat в BufferedImage
     * @param src
     * @return 
     */
    private BufferedImage convertMat2BI(Mat src) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = src.channels() * src.cols() * src.rows();
        byte[] b = new byte[bufferSize];
        // Получаем все пиксели
        src.get(0, 0, b);
        BufferedImage bi = new BufferedImage(src.cols(), src.rows(), type);
        final byte[] targetPixels = ((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return bi;
    }
}
