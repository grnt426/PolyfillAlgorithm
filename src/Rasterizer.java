//
//  Rasterizer.java
//  
//
//  Created by Joe Geigel on 1/21/10.
//  Implemented by Grant Kurtz
//

import java.util.ArrayList;
import java.util.Collections;

/**
 * This is a class that performs rasterization algorithms
 */

public class Rasterizer{

	private static final int EDGE_Y = 0;
	private static final int EDGE_X = 1;
	private static final int EDGE_DX = 2;
	private static final int EDGE_DY = 3;
	private static final int EDGE_C = 4;

	/**
	 * number of scanlines
	 */
	int n_scanlines;

	/**
	 * All the vertices currently under consideration.
	 */
	private ArrayList<Integer[]> aet = new ArrayList<Integer[]>();

	/**
	 * All vertices that are composed of the polygon being drawn.
	 */
	private ArrayList<ArrayList<Integer[]>> get =
			new ArrayList<ArrayList<Integer[]>>();

	/**
	 * Constructor
	 *
	 * @param n - number of scanlines
	 */
	Rasterizer(int n){
		n_scanlines = n;
	}

	/**
	 * Draw a filled polygon in the simpleCanvas C.
	 * <p/>
	 * The polygon has n distinct vertices. The coordinates of the vertices making
	 * up the polygon are stored in the x and y arrays.  The ith vertex will have
	 * coordinate  (x[i], y[i])
	 * <p/>
	 * You are to add the implementation here using only calls to C.setPixel()
	 */
	public void drawPolygon(int n, int x[], int y[], simpleCanvas C){
		buildGlobalEdgeTable(n, x, y);

		for(int line = 0; line < n_scanlines; line++){

			// move all edges that have vertices starting at this height (line)
			aet.addAll(get.get(line));
			sortActiveEdgeTable();

			// remove lines whose upper y bound starts at this line
			for(int i = 0; i < aet.size(); i++){
				// Remove edges that will be obsolete in the next iteration
				if(aet.get(i)[EDGE_Y] == line){
					aet.set(i, null);
					continue;
				}
			}

			// clean up all the nulls we left behind
			while(aet.contains(null)){
				aet.remove(null);
			}

			// fill this scan line
			for(int edge = 0; edge < aet.size(); edge++){

				if(edge != aet.size() - 1){
					for(int x_c = aet.get(edge)[EDGE_X];
						x_c < aet.get(edge + 1)[EDGE_X]; x_c++){
						C.setPixel(x_c, line);
					}
				}

				// update the edge to adjust the x
				aet.get(edge)[EDGE_C] += aet.get(edge)[EDGE_DX];
				if(aet.get(edge)[EDGE_DY] != 0){
					aet.get(edge)[EDGE_X] += (aet.get(edge)[EDGE_C]
											  / aet.get(edge)[EDGE_DY]);

					// modulo doesn't treat negatives well
					if(aet.get(edge)[EDGE_C] < 0){
						aet.get(edge)[EDGE_C] = (Math.abs(aet.get(edge)[EDGE_C])
												 % Math.abs(aet.get(edge)[EDGE_DY]))
												* -1;
					}
					else{
						aet.get(edge)[EDGE_C] = Math.abs(aet.get(edge)[EDGE_C])
												% Math.abs(aet.get(edge)[EDGE_DY]);
					}
				}
			}
		}

		get.clear();
		aet.clear();
	}

	private void buildGlobalEdgeTable(int n, int[] x, int[] y){

		// initialize each internal array first
		for(int line = 0; line < n_scanlines; line++){
			get.add(new ArrayList<Integer[]>());
		}

		for(int vertex = 0; vertex < n; vertex++){
			Integer[] vertex_data = new Integer[5];
			int next_vertex = (vertex + 1) % n;

			// We need the max of y of both vertices, but the x of the
			// min y vertex
			if(y[vertex] >= y[next_vertex]){
				vertex_data[EDGE_Y] = y[vertex];
				vertex_data[EDGE_X] = x[next_vertex];
			}
			else{
				vertex_data[EDGE_Y] = y[next_vertex];
				vertex_data[EDGE_X] = x[vertex];
			}

			// Store the slope of the edge
			vertex_data[EDGE_DX] = x[vertex] - x[next_vertex];
			vertex_data[EDGE_DY] = y[vertex] - y[next_vertex];
			vertex_data[EDGE_C] = 0;

			// we need to add this edge to the table based on the lowest
			// vertex
			get.get(Math.min(y[vertex], y[next_vertex])).add(vertex_data);
		}
	}

	private void sortActiveEdgeTable(){
		// Just do a basic bubble-sort, nothing fancy needed
		for(int i = 0; i < aet.size(); i++){
			for(int k = 1; k < (aet.size() - i); k++){

				// we need to sort on x, which is the second value
				if(aet.get(k - 1)[1]
				   > aet.get(k)[1]){
					Collections.swap(aet, k - 1, k);
				}
			}
		}
	}

}
