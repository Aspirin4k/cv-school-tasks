/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.movdetector;

import cv.school.tasks.ExtraFunctions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

/**
 *
 * @author aspid
 */
public class MovDetector {
    // Размер ядра Гауссса
    private static final Size GAUSSIAN_KERNEL_SIZE =  new Size(3,3);
    // Размер буфера кадров
    private static final int QUEUE_SIZE = 7;
    // Длительность записи видео с вебки в секундах
    private static final int RECORD_LENGTH = 15;
    // Количество кадров, которые будут пропускаться между фактически
    // обрабатываемыми кадрами (при высоком фпс)
    private static final int SKIP_FRAME = 1;
    // Коэффициент для уменьшения кадра
    private static final double RESIZE_KT = 0.3;
    // Порог вычитания
    private static final double THRESHOLD = 35;
    
    /**
     * Обнаруживает движение на видео
     * @param cap поток видео данных
     * @throws java.lang.Exception
     */
    public static void Detect(VideoCapture cap) throws Exception {
        // Объект содержит каждый отдельный кадр
        Mat frame = new Mat();
        Random rand = new Random();
        
        if (!cap.read(frame))
            return;
        
        VideoWriter writer = 
                new VideoWriter(
                "output.avi", 
                VideoWriter.fourcc('D', 'I', 'V', 'X'),
                cap.get(Videoio.CAP_PROP_FPS),
                new Size(cap.get(Videoio.CV_CAP_PROP_FRAME_WIDTH) * RESIZE_KT, cap.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT) * RESIZE_KT),
                true);
        
        VideoWriter writerBoundries = 
                new VideoWriter(
                "output_bound.avi", 
                VideoWriter.fourcc('D', 'I', 'V', 'X'),
                cap.get(Videoio.CAP_PROP_FPS),
                new Size(cap.get(Videoio.CV_CAP_PROP_FRAME_WIDTH), cap.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT)),
                true);
        
        // Блокирующая потокобезопасная очередь.
        // TODO: Прикрутить многопоточность
        LinkedBlockingQueue<Mat> matsPool = new LinkedBlockingQueue<>();
        
        // Двунаправленный список для однопоточной реализации
        LinkedList<Mat> matsList = new LinkedList<>();
        
        int framesCount = 0;
        int frameNum = SKIP_FRAME;
        Mat procFrame;
        do {
            // Пропускаем несколько кадров для оптимизации (таким образом
            // обрабатывается каждый SKIP_FRAME-овый кадр
            if (frameNum == SKIP_FRAME)
            {
                frameNum = 0;
                procFrame = frame.clone();
                // Уменьшаем изображение, чтобы уменьшить нагрузку
                Imgproc.resize(frame, procFrame, new Size(), RESIZE_KT, RESIZE_KT, Imgproc.INTER_LINEAR);
                
                // Убираем гауссовкий шум с кадра                     
 //               Imgproc.GaussianBlur(procFrame, procFrame, GAUSSIAN_KERNEL_SIZE, 0);     
//                Imgproc.Laplacian(frame, frame, frame.depth());
                // Используем такой велосипедный метод вместо функции буфера .size(),
                // т.к. size() очень, ОЧЕНЬ низкопроизводительна
                if (framesCount < QUEUE_SIZE) {
                    // В начало добавляем новый кадр
                    matsList.addFirst(procFrame.clone());
                    framesCount++;
                } else {
                    // TODO: Вычислить медианное изображение
                    Mat median = getMedianImg(matsList.toArray(new Mat[QUEUE_SIZE-1]));
                  
                    // TODO: Вычесть кадры
                    Core.absdiff(median, procFrame, median);
                    Imgproc.threshold(median, median, THRESHOLD, 255, Imgproc.THRESH_BINARY);

                    // TODO: Убрать шум
                    Mat kernel = Mat.ones(3,3, CvType.CV_8UC1);
                    Imgproc.morphologyEx(median, median, Imgproc.MORPH_ERODE, kernel);
                    kernel = Mat.ones(15,15, CvType.CV_8UC1);
                    Imgproc.morphologyEx(median, median, Imgproc.MORPH_DILATE, kernel);
                    
                    // TODO: 8-связное ядро              
                    List<MatOfPoint> contours = new ArrayList<>();
                    Mat image8uc = new Mat();
                    Imgproc.cvtColor(median, image8uc, Imgproc.COLOR_RGB2GRAY);
                    Imgproc.findContours(image8uc, contours, 
                            new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                    
                    for (int i=0; i<contours.size(); i++)
                    {
                        Rect rect = Imgproc.boundingRect(contours.get(i));
                        double xl = rect.x / RESIZE_KT;
                        if (xl < 0) xl = 0;
                        if (xl >= frame.cols()) xl = frame.cols() - 1;
                        double yt = rect.y / RESIZE_KT;
                        if (yt < 0) yt = 0;
                        if (yt >= frame.rows()) yt = frame.rows() - 1;
                        double xr = xl + rect.width / RESIZE_KT;
                        if (xr < 0) xr = 0;
                        if (xr >= frame.cols()) xr = frame.cols() - 1;
                        double yb = yt + rect.height / RESIZE_KT;
                        if (yb < 0) yb = 0;
                        if (yb >= frame.rows()) yb = frame.rows() - 1;
                        Imgproc.rectangle(frame, new Point(xl,yt), new Point(xr,yb), new Scalar(0,0,255));
                        
                        for (Point point: contours.get(i).toList())
                        {
                            double[] buffer = { 0, 0, 255 };
                            median.put((int)Math.round(point.y), 
                                    (int)Math.round(point.x), buffer);
                        }
                    }
                    
                    // Запись кадра в файл
                    writer.write(median);
                    writerBoundries.write(frame);
                    median.release();
                    
                    // В начало добавляем новый кадр
                    matsList.addFirst(procFrame.clone());
                    // Из конца удаляем кадр
                    matsList.getLast().release();
                    matsList.removeLast();
                }
            }
            else
            {
                frameNum++;
            }
            
        } while(cap.read(frame));
        
        writer.release();
        cap.release();
    }
    
    private static Mat getMedianImg(Mat[] mArr) throws Exception {
        double[] buffer = new double[mArr.length];
        Mat result = new Mat(mArr[0].rows(), mArr[0].cols(), mArr[0].type());
        for (int y=0; y< mArr[0].rows(); y++)
        {
            for (int x=0; x< mArr[0].cols(); x++) {
                // Вероятно, этот сегмент кода нужно оптимизировать
                // Предполагаем, что кадры в RGB
                double[] rgb_buffer = new double[3];
                for (int c = 0; c < 3; c++)
                {
                    for (int el=0; el< mArr.length; el++) {
                        buffer[el] = mArr[el].get(y, x)[c];
                    }
                    rgb_buffer[c] = ExtraFunctions.getMedian(buffer);
                }
                result.put(y, x, rgb_buffer);
            }
        }
        
        return result;
    }
}
