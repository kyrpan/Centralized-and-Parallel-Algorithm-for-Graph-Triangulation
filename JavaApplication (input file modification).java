/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication16;

import com.sun.corba.se.impl.orbutil.graph.Graph;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 *
 * @author Kikh
 */
public class JavaApplication16 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String line = null;
        HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();      

        BufferedReader br = new BufferedReader(new FileReader("CA-AstroPh.txt"));        
        while ((line = br.readLine()) != null) {
            
            //take each line and get the two nodes in two seperate variables
            String[] parts = line.split("\\s+");
            int v1 = Integer.parseInt(parts[0]);
            int v2 = Integer.parseInt(parts[1]);
            
            if (v1 < v2) {
                map = addToList(v1, v2, map);
            }

        }
        System.out.println(map);
         Map<Integer, ArrayList<Integer>> tmap = new TreeMap<Integer, ArrayList<Integer>>(
                new Comparator<Integer>() {

                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1.compareTo(o2);
                    }

                });
         
        tmap.putAll(map);
        System.out.println(tmap);
        
        for (Map.Entry<Integer, ArrayList<Integer>> entry : tmap.entrySet()) {
            int key = entry.getKey();
            ArrayList<Integer> value = entry.getValue();
            for(Integer a : value){
                try(FileWriter fw = new FileWriter("myfile.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
                {
                    out.println(Integer.toString(key)+' '+Integer.toString(a));       
                } catch (IOException e) {}
            }
        }
        
    }
    
    public static HashMap<Integer, ArrayList<Integer>> addToList(int mapKey, int myItem, HashMap<Integer, ArrayList<Integer>> items) {
        ArrayList<Integer> itemsList = items.get(mapKey);

        // if list does not exist create it
        if(itemsList == null) {
             itemsList = new ArrayList<Integer>();
             itemsList.add(myItem);
             items.put(mapKey, itemsList);
        } else {
            // add if item is not already in list
            if(!itemsList.contains(myItem)) itemsList.add(myItem);
        }
        return items;
    }
}