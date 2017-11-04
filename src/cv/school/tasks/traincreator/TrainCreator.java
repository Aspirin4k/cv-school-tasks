/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.traincreator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import cv.school.tasks.VideoLoader;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 *
 * @author Andew
 */
public class TrainCreator {
    private final double SPEED_CAP = 128;
    
    // Содержит настройки приложения
    private final TrainCreatorConfiguration config;
    // Содержит подгруженное видео
    private final ArrayList<Mat> frames;
    // Номер кадра видео
    private int frameNumTotal;
    // Форма
    private final TrainCreatorForm form;
    
    private VideoCapture cap;
    
    private Mat currentFrame;
    private double frameWidth;
    private double frameHeight;
    private double scaleKt;
    
    private boolean pause;
    private int rectX;
    private int rectY;
    private double speed;
    
    /**
     * Создает объект извлекателя тренировочных изображений
     * @param path путь до конфига
     * @throws java.io.IOException
     */
    public TrainCreator(String path) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser jp = factory.createParser(new File(path));
        this.config = TrainCreatorConfiguration.readJSON(jp);
        this.frames = new ArrayList<>();
        this.loadVideo(Paths.get(config.getInitialVideo()));
        
        this.form = new TrainCreatorForm(this);
        this.form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.frameNumTotal = 0;
        this.currentFrame = null;
        this.pause = false;
        this.speed = 1;
        this.rectX = 0;
        this.rectY = 0;
        this.scaleKt = this.form.getImgLabel().getWidth() / this.frameWidth; 
        
        this.form.setVisible(true);
        
        RenderThread render = new RenderThread();
        render.run();
    }

    /**
     * Отрисовывает кадр с текущим индекcом
     */
    private void renderCurrentFrame() {
        JLabel img = this.form.getImgLabel();
        
        this.currentFrame = this.frames.get(0).clone();
        double actualWidth = this.scaleKt * this.frameWidth;
        double actualHeight = this.scaleKt * this.frameHeight;
        Imgproc.resize(this.currentFrame, this.currentFrame, new Size(actualWidth, actualHeight));
        
        double actualRectWidth = this.scaleKt * this.config.getRectWidth();
        double x = this.rectX - actualRectWidth / 2;
        if (x < 0) x = 0;
        if (x + actualRectWidth > actualWidth) x = actualWidth - actualRectWidth - 1;
        
        double y = this.rectY - actualRectWidth;
        if (y < 0) y = 0;
        if (y + actualRectWidth > actualHeight) y = actualHeight - actualRectWidth - 1;
        
        Imgproc.rectangle(currentFrame, new Point(x,y), new Point(x + actualRectWidth, y + actualRectWidth), new Scalar(0,255,0, 255));
    }
    
    /**
     * Выводит текущий кадр на форму
     */
    private void displayCurrentFrame() {
        if (this.currentFrame != null) {
            ImageIcon icon = new ImageIcon(this.convertMat2BI(this.currentFrame));
            this.form.getImgLabel().setIcon(icon);
        }
    }
    
    /**
     * Получает следующий кадр видео
     */
    private void step() {
        if (this.cap.isOpened()) {
            this.frameNumTotal++;
            this.loadBuffer();
            this.renderCurrentFrame();
            this.form.setFrameNum(this.frameNumTotal);
        }
    }
    
    /** 
     * Подрузить кадры из файла в буфер
     */
    private void loadBuffer() {
        // Количество кадров, которое мы скипаем прежде чем записать
        double fps = cap.get(Videoio.CAP_PROP_FPS);
        long skipRate = (long)Math.ceil(fps / config.getToFps());
        long j = 0;
        if (this.frames.size() > 0)
        {
            this.frames.get(0).release();
            this.frames.remove(0);
        }
        
        Mat frame = new Mat();
        while (cap.read(frame) && (this.frames.size() < this.config.getBufferLength())) {
            if (j == 0)
                this.frames.add(frame.clone());
            j = (j + 1) % skipRate;
        }
    }
    
    /**
     * Подгружает в память видео из файла
     * @param path путь до видео
     * @throws IOException 
     */
    public final void loadVideo(Path path) throws IOException {
        this.cap = VideoLoader.loadFromFile(path);
        if (this.cap == null) throw new IOException("Файл не найден");
        
        this.frameWidth = cap.get(Videoio.CV_CAP_PROP_FRAME_WIDTH);
        this.frameHeight = cap.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT);
        
        this.loadBuffer();
    }
    
    /**
     * Обрабатывает событие взаимодействия с формой
     */
    public void notifyPause() {
        this.pause = !this.pause;
    }
    
    /**
     * Обрабатывает перемещение мыши по кадру
     * @param x
     * @param y 
     */
    public void notifyMouseOverFrame(int x, int y) {
        this.rectX = x;
        this.rectY = y;
    }
    
    /**
     * Обрабатывает клик по кадру
     * @param button индекс нажатой клавиши
     * @throws java.io.IOException
     */
    public void notifyMouseClickedFrame(int button) throws IOException {
        this.saveRect(button);
    }
    
    /**
     * Обрабатывается изменение скорости воспроизведения
     * @param up 
     */
    public void notifySpeedChange(boolean up) {
        if (up) {
            this.speed = this.speed / 2;
            if (this.speed < 1 / this.SPEED_CAP) this.speed = 1 /this.SPEED_CAP;
        }
        else {
            this.speed = this.speed * 2;
            if (this.speed > this.SPEED_CAP) this.speed = this.SPEED_CAP;
        }
    }
    
    /**
     * Сохраняет выделенный фрагмент
     * @param button индекс нажатой клавиши
     * @throws java.io.IOException
     */
    public void saveRect(int button) throws IOException {
        Path output = Paths.get(this.config.getTrainDir());
        if (button == 1)
            output = output.resolve("pos");
        else
            output = output.resolve("neg");
        
        if (!Files.exists(output) || !Files.isDirectory(output)) Files.createDirectories(output);
        
        long count = Files.list(output).count();
        output = output.resolve(Long.toString(count));
        if (!Files.exists(output) || !Files.isDirectory(output)) Files.createDirectories(output);
        
        double x = this.rectX / this.scaleKt - this.config.getRectWidth() / 2;
        double y = this.rectY / this.scaleKt - this.config.getRectWidth();
        if (x<0) x = 0;
        if (x + this.config.getRectWidth() > this.frameWidth) x = this.frameWidth - this.config.getRectWidth() - 1;
        if (y<0) y = 0;
        if (y + this.config.getRectWidth() > this.frameHeight) y = this.frameHeight - this.config.getRectWidth() - 1;
        int xint = (int)Math.floor(x);
        int yint = (int)Math.floor(y);
        
        for (int i=0; i< this.frames.size(); i++)
        {
            Mat submat = this.frames.get(i).submat(yint, yint+ this.config.getRectWidth(), xint,  xint+ this.config.getRectWidth());
            Imgcodecs.imwrite(output.toString() + "/" + Integer.toString(i) + ".png", submat);
            submat.release();
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
    
    /**
     * Рендерит кадры для формы
     */
    private class RenderThread implements Runnable {
        @Override
        public void run() {
            long tickTimeOld = System.currentTimeMillis();
            long tickTimeNew;
            long framerateTimeOld = System.currentTimeMillis();
            long framerateTimeNew;
            while (true) {
                tickTimeNew = System.currentTimeMillis();
                framerateTimeNew = tickTimeNew;
                if (tickTimeNew - tickTimeOld > 25)  {
                    if (framerateTimeNew - framerateTimeOld > (1000 / config.getToFps()) * speed)
                    {
                        if (!pause) {
                            step();
                        }
                        framerateTimeOld = System.currentTimeMillis();
                    }
                    displayCurrentFrame();
                    renderCurrentFrame();
                    tickTimeOld = System.currentTimeMillis();
                }
            }
        }
    }
}
