package first;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.jgrapht.*;
import org.jgrapht.graph.*;

public class First
{
    public static void main(String[] args) throws IOException
    {
        int i,n,m;
        int T;
        Graph<String, DefaultEdge> stringGraph = createStringGraph();
        ArrayList<ArrayList<String>> N = new ArrayList<ArrayList<String>>();
        ArrayList<String> inner = new ArrayList<String>();   
        
        //n is the number of vertices
        n = (stringGraph.vertexSet()).size();
        
        //number of processors
        //int threadCount = Runtime.getRuntime().availableProcessors();
        //System.out.println("Number of processors "+threadCount);
        
        //undirected edges are printed as: {<v1>,<v2>}
        //System.out.println("NODES WITH PAIRS");
        //System.out.println(stringGraph.toString());
        
        ArrayList d = new ArrayList<>();
        d = findDegree(stringGraph);
        //System.out.println("DEGREE OF EACH NODE");
        //System.out.println(d);
        
        //number of edges
        Set edges = new HashSet();
        edges = stringGraph.edgeSet();
        m = edges.size();
        //System.out.println("EDGES");
        //System.out.println(edges);
        
        //creation of array of each node's neighbors
        for (Object edge : edges) {  //for each edge
            String e = edge.toString();
            String[] parts = e.split(":"); //split in :
            String u = parts[0].substring(1); //first node of edge (we cut the character ( from the start and ' ' from the end) 
            u = u.substring(0, u.length() - 1);
            String v = parts[1].substring(1); //second node of edge (we cut the character ' ' from the start and ) from the end)
            v = v.substring(0, v.length() - 1);
            
            //degree of each node
            int du = (int) d.get(Integer.parseInt(u));
            int dv = (int) d.get(Integer.parseInt(v));
            
            //if node u has smaller degree than node v
            if (du < dv) {
                //add node v in the neighbors of u                
                N = addToInnerArrayList(N,u,v);
            } 
            //else if node u has bigger or equal degree with node v
            else {
                //add node u in the neighbors of v                
                N = addToInnerArrayList(N,v,u);
            }   
        }

        //sorting inner lists
        for (ArrayList in : N)
            Collections.sort(in);
 
        /*for (i=0; i<N.size(); i++) {
            inner = N.get(i);
            System.out.println(inner);         
        }*/
        
        T = countTriangles(N);
        System.out.println("TOTAL TRIANGLES: "+T);
    }

    /**
     * Create a graph based on an input file.
     *
     * @return a graph based on an input file.
     */
    private static Graph<String, DefaultEdge> createStringGraph() throws FileNotFoundException, IOException
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);        
        String line = null;
        
        BufferedReader br = new BufferedReader(new FileReader("facebook_combined.txt"));        
        while ((line = br.readLine()) != null) {
            
            //take each line and get the two nodes in two seperate variables
            String[] parts = line.split("\\s+");
            String v1 = parts[0];
            String v2 = parts[1];
            
            //check if the edge (v1,v2) already exists in graph g
            //and if not, then add it to graph g
            if (!(g.containsEdge(v1, v2) || g.containsEdge(v2, v1))) {
                g.addVertex(v1);
                g.addVertex(v2);
                g.addEdge(v1, v2);
            }
            
        } 
        
        return g;
    }
    
    private static ArrayList findDegree(Graph<String, DefaultEdge> g) {
        
        ArrayList d = new ArrayList<>();
        int i, n, degree;
        
        //Set of graph's vertices
        Set vertexHashSet = new HashSet();
        vertexHashSet = g.vertexSet();
        
        //we transfer the hashset to an arraylist to get it sorted        
        List sortedList = new ArrayList(vertexHashSet);
        Collections.sort(sortedList, new Comparator<String>() {
            //we use this function to have the numbers sorted correctly (not with an order like 0,1,11,12,...)
            public int compare(String o1, String o2) {
                Integer i1 = Integer.parseInt(o1);
                Integer i2 = Integer.parseInt(o2);
                return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
            }
        });

        //n is the number of vertices
        n = sortedList.size();
        //for each node, find and store the degree in an arraylist
        for (Object s : sortedList) {
            degree = (g.edgesOf(s.toString()).size());
            d.add(degree);
        }        
        return d;
    }
    
    //for each new index creates a new inner list (so we never get IndexOutOfBounds exception
    //then adds the element in the right position
    private static ArrayList<ArrayList<String>> addToInnerArrayList (ArrayList<ArrayList<String>> N, String index, String element) {
        int i = Integer.parseInt(index);
        while (i >= N.size())
            N.add(new ArrayList<String>());
        
        N.get(i).add(element);
        return N;
    }
    
    private static int countTriangles (ArrayList<ArrayList<String>> N) {
        int T=0;
        
        for (int u=0; u<N.size(); u++) {
            for(ArrayList inner : N) {
                if (inner.contains(Integer.toString(u))) {
                    List<String> S = new ArrayList<String>(inner);
                    S.retainAll(N.get(u));
                    T = T + S.size();
                }
            }
        }
        return T;
    }
}
