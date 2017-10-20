/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.imgaligner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author aspid
 */
public class ImgAligner {
    // Отступ от верхнего края в пикселях
    private static final int TOP_INDENT = 30;
    // К-т дистанции, по которому идет отбор (т.н. лучшие совпадения)
    private static final double TRESSHOLD_DISTANCE = 0.9;
    
    /**
     * Функция возвращает цветное изображение
     * @param image исходное изображение, содержащее каналы
     * @return цветное изображение
     */
    public static Mat getAlignedImage(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        Mat[] channels = getSplittedImage(image);
        
        return mergeImages(channels);
    }
    
    /**
     * Функция сливает изображения в единое
     * @param images массив изображений
     * @return совмещенное изображение
     */
    public static Mat mergeImages(Mat[] images) {
        Mat result = new Mat();
        
        Mat bufferG = alignImages(images[0], images[1]);
        images[1].release();
        images[1] = bufferG;
        
        Mat bufferR = alignImages(images[0], images[2]);
        images[2].release();
        images[2] = bufferR;
        
        Core.merge(Arrays.asList(images), result);
        return result;
    }
    
    /**
     * Возвращает 3 изображения, являющиеся результатом разделения
     * исходного изображения на 3 равные по вертикали части
     * @param image исходное изображение
     * @return массив из 3-х изображений
     */
    public static Mat[] getSplittedImage(Mat image) {
        int channelsCount = 3;
        Mat[] result = new Mat[channelsCount];
        int height = (image.height() - TOP_INDENT) / 3;
        int width = image.width();
        for (int i=0; i < channelsCount; i++)
            result[i] = image.submat(TOP_INDENT + height * i, TOP_INDENT + height * (i+1), 0, width);
        
        return result;
    }

    /**
     * Функция пытается найти ключевые точки и сместить одно изображение так, чтобы эти точки совпадали на обоих изображениях.
     * Ожидается, что изображения будут одинаковых размеров
     * @param src исходное изображение, относительно которого будет происзведено смещение
     * @param target целевое изображение, которое будет смещено по x и y. Пустота заполнена 0
     * @return новая матрица изображения target, ключевые точки в которой совпадают с такими же в src
     */
    private static Mat alignImages(Mat src, Mat target)
    {
        // Дескриптор и фичедетектор для нахождения и описания точек
        FeatureDetector orbF = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor orbD = DescriptorExtractor.create(DescriptorExtractor.ORB);
        // Сопостоавитель дескрипторов для нахождения соответсвтия
        DescriptorMatcher bfD = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        
        // Матрица ключевых точек, а также матрица дескрипторов для обоих изображений
        MatOfKeyPoint keypointsSrc = new MatOfKeyPoint();
        Mat descriptorsSrc = new Mat();
        MatOfKeyPoint keypointsTarget = new MatOfKeyPoint();
        Mat descriptorsTarget = new Mat();
        
        // Находим точки, а также вычисляем их дескрипторы для обоих изображений
        orbF.detect(src, keypointsSrc);
        orbD.compute(src, keypointsSrc, descriptorsSrc);
        orbF.detect(target, keypointsTarget);
        orbD.compute(target, keypointsTarget, descriptorsTarget);
        
        // Устанавливаем соответствия между точками
        MatOfDMatch matches = new MatOfDMatch();
        bfD.match(descriptorsSrc, descriptorsTarget, matches);
        
         // Сортируем совмещенные точки по дальности. Таким образом получим в начале лучшие совпадения
        DMatch[] sortedMatchesG = matches.toArray();
        Arrays.sort(sortedMatchesG, new DistanceComparator());
        
         // Вычисляем необходиме смещение по x и y для изображнеия. Положительыное значение - необходимо сместить изображение влево/вверх
        double dx = - keypointsSrc.toArray()[sortedMatchesG[0].queryIdx].pt.x + keypointsTarget.toArray()[sortedMatchesG[0].trainIdx].pt.x;
        double dy = - keypointsSrc.toArray()[sortedMatchesG[0].queryIdx].pt.y + keypointsTarget.toArray()[sortedMatchesG[0].trainIdx].pt.y;
        // Вычисляем ширину и высоту области, которую сможем сохранить
        double width = target.cols() - Math.abs(dx);
        double height = target.rows() - Math.abs(dy);
        
        // Создаем новую пустую матрицу с размерами изначального изображения
        Mat buffer = new Mat(target.rows(), target.cols(), target.type(), new Scalar(0));
        // Вырезаем кусок из изначального изображения, который хотим сместить
        Mat submat = target.submat(
                dy < 0 ? 0 : (int)Math.floor(dy), 
                (int)Math.floor(height), 
                dx < 0 ? 0 : (int)Math.floor(dx),
                (int)Math.floor(width)
            );
        // Вставляем в соответствующую позицию в пустом изображении
        submat.copyTo(buffer
                        .rowRange(
                                dy > 0 ? 0 : buffer.rows() - submat.rows(), 
                                dy > 0 ? submat.rows() : buffer.rows())
                        .colRange(
                                dx > 0 ? 0 : buffer.cols()- submat.cols(),
                                dx > 0 ? submat.cols(): buffer.cols()));
        
        return buffer;
    }
    
}

