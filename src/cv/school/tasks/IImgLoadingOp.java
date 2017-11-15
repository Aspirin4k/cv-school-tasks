/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks;

import org.opencv.core.Mat;

/**
 * Позволяет исполнить операцию для каждой картинки
 * @author roma2_000
 */
public interface IImgLoadingOp {
    public void execute(Mat image);
}
