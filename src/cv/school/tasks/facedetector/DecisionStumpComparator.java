/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector;

import cv.school.tasks.facedetector.FaceDetector.ClassifierStructure;
import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author roma2_000
 */
public class DecisionStumpComparator implements Comparator<ClassifierStructure> {

    @Override
    public int compare(ClassifierStructure o1, ClassifierStructure o2) {
        if (Objects.equals(o1.getError(), o2.getError())) {
            return 0;
        } else if (o1.getError() < o2.getError()) {
            return -1;
        } else {
            return 1;
        }
    }
    
}
