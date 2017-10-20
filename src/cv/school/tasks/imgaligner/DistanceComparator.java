/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.imgaligner;

import java.util.Comparator;
import org.opencv.core.DMatch;

/**
* Класс реализует интерфейс для сравнения объектов DMatch (Сортировка по дистанции)
*/
public class DistanceComparator implements Comparator<DMatch> {
    @Override
    public int compare(DMatch o1, DMatch o2) {
        return o1.distance < o2.distance ? -1 : o1.distance == o2.distance ? 0 : 1;
    }  
}