/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cv.school.tasks.facedetector;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author roma2_000
 */
public class ArrayIndexComparator implements Comparator<Integer> {

    private final ArrayList<Double> array;
    
    public ArrayIndexComparator(ArrayList<Double> array) {
        this.array = array;
    }
    
    public ArrayList<Integer> createIndexArray() {
        ArrayList<Integer> indexes = new ArrayList<>(this.array.size());
        for (int i=0; i< this.array.size(); i++) {
            indexes.add(i,i);
        }
        return indexes;
    }
    
    @Override
    public int compare(Integer o1, Integer o2) {
        return this.array.get(o1).compareTo(this.array.get(o2));
    }
    
}
