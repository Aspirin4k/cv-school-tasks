/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import java.nio.file.Files;
import java.nio.file.Path;
import org.opencv.videoio.VideoCapture;

/**
 * Класс отвечает за загрузку видео файлов
 * @author aspid
 */
public class VideoLoader {
    
//    public static BufferedImage Mat2BufferedImage(Mat m){
//    //source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
//    //Fastest code
//    //The output can be assigned either to a BufferedImage or to an Image
//
//     int type = BufferedImage.TYPE_BYTE_GRAY;
//     if ( m.channels() > 1 ) {
//         type = BufferedImage.TYPE_3BYTE_BGR;
//     }
//     int bufferSize = m.channels()*m.cols()*m.rows();
//     byte [] b = new byte[bufferSize];
//     m.get(0,0,b); // get all the pixels
//     BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
//     final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//     System.arraycopy(b, 0, targetPixels, 0, b.length);  
//     return image;
//    }
    
    /**
     * Получает поток кадров камеры
     * @return 
     */
    public static VideoCapture loadFromCam() {
        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened())
            return null;
        return cap;
    }
    
    /**
     * Получает видео из файла
     * @param path путь до файла
     * @return 
     */
    public static VideoCapture loadFromFile(Path path) {
        if (Files.notExists(path))
            return null;
        VideoCapture cap = new VideoCapture(path.toString());
        if (!cap.isOpened())
            return null;
        return cap;
    }
}
