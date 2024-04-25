import java.util.ArrayList;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.Math;
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter; 
import Jama.*;

class RayTracer {
	
	RayTracer(){
		
	}
	
    public static void createImage(String[] args) {
        ArrayList<String> inputs = getInput(args[0]);
		
		ArrayList<Sphere> Spheres = new ArrayList<Sphere>();
		
		ArrayList<Light> Lights = new ArrayList<Light>();
		
		float near = locateField("NEAR", inputs);
		float left = locateField("LEFT", inputs);
		float right = locateField("RIGHT", inputs);
		float bottom = locateField("BOTTOM", inputs);
		float top = locateField("TOP", inputs);
		
		int resIndex = 0;
		
		for(int i = 0;i < inputs.size();i++){
			if(inputs.get(i).contains("RES")){
				resIndex = i;
				break;
			}
		}
		
		int [] res = getFieldInt(inputs.get(resIndex));
		
		float [][][] pixels = new float[res[0]][res[1]][3];
		
		for(int i = 0;i < inputs.size();i++){
			
			if((inputs.get(i)).contains("SPHERE")){
				String sphereRemoval = inputs.get(i).substring(7);
				
                String [] temp = sphereRemoval.split("\\s+", 0);
				float [] l = getField(sphereRemoval);
				float [] pos = {l[0], l[1], l[2]};
				float [] scale = {l[3], l[4], l[5]};
				
				sphereRemoval = sphereRemoval.substring(0, 2);
				
				
				Sphere s = new Sphere(temp[0], pos, scale, l[6], l[7], l[8], l[9], l[10], l[11], l[12], l[13]);
				
				Spheres.add(s);
				
			}
		}
		
		
		for(int i = 0;i < inputs.size();i++){
			
			if((inputs.get(i)).contains("LIGHT")){
				String lightRemoval = inputs.get(i).substring(6);
				
				String [] temp = lightRemoval.split("\\s+", 0);
				float [] l = getField(lightRemoval);
				float [] pos = {l[0], l[1], l[2]};
				float [] color = {l[3], l[4], l[5]};
				
				lightRemoval = lightRemoval.substring(0, 2);
				
				
				Light s = new Light(temp[0], pos, color);
				
				Lights.add(s);
				
			}
		}
		
		float [] back = locateFields("BACK", inputs);
		float [] ambient = locateFields("AMBIENT", inputs);
		
		for(int i = 0;i < inputs.size();i++){
			if(inputs.get(i).contains("OUTPUT")){
				resIndex = i;
				break;
			}
		}
		
		
		String outName = inputs.get(resIndex);
		
		String [] outArr = outName.split("\\s+");
		
		outName = outArr[1];
		
		String pixStr = "";
		
		pixStr += "P3\n";
		pixStr += "# " + outName + "\n";
		pixStr += pixels.length + " " + pixels[0].length + "\n";
		pixStr += 255 + "\n";
		
		pixStr = getShapes(pixels, Spheres, Lights, near, left, right, top, bottom, back, ambient, pixStr);
		
		writePpm(pixels, outName, pixStr);
	}
	
	// reads in input file
	public static ArrayList<String> getInput(String input){		
		ArrayList<String> inputs = new ArrayList<String>();
		
		try {
			File in = new File(input);
			Scanner reader = new Scanner(in);
			
			while(reader.hasNextLine()){
				String data = reader.nextLine();
				inputs.add(data);
			}
		}catch(Exception e){
			System.exit(0);
		}
		
		return inputs;
		
	}
	
	// processes single float
	public static float getSingleField(String inputs){
		String [] temp = inputs.split("\\s+", 0);
		
		return Float.parseFloat(temp[1]);
	
	}
	
	// processes list of floats
	public static float[] getField(String inputs){
		String [] temp = inputs.split("\\s+", 0);
		
		for(int i = 0;temp[0] == "\\s" || temp[0] == "";i++){
			for(int j = 0;j < temp.length - 1;j++){
				temp[j] = temp[j+1];
			}
		}
		
		float [] r = new float [temp.length-1];
		
		for(int i = 0;i < temp.length-1;i++){
			r[i] = Float.parseFloat(temp[i+1]);
		}
		
		return r;
	
	}
	
	// gets single integer field
	public static int[] getFieldInt(String inputs){
		String [] temp = inputs.split("\\s+", 0);
		
		int [] r = new int [temp.length-1];
		
		for(int i = 0;i < temp.length-1;i++){
			r[i] = Integer.parseInt(temp[i+1]);
		}
		
		return r;
	
	}
	
	// gets the initial starting vectors for each pixel and calls bounces
	// saves output to be written to ppm
	public static String getShapes(float[][][] pixels, ArrayList<Sphere> Spheres, ArrayList<Light> Lights, float near, float left, float right, float top, float bottom, float [] back, float [] ambient, String pixStr){
		float H = (top - bottom) / 2;
		float W = (right - left) / 2;

		int check = 0;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(pixStr);
		
		// i = row
		for(int i = 0;i < pixels.length;i++){
		
			
			// j = column
			for(int j = 0;j < pixels[i].length;j++){
			
				float [] r = new float [3];
			
				float u = 2*W*j / pixels.length - W;
			
				float v = -2*H*i / pixels[i].length + H;
				
				
				// since eye at (0, 0, 0) subtracting and/or adding eye is same (0, 0, 0) + t(P(r,x) - (0, 0, 0)) = tP(r,c)
				
				float [] c = {u, v, -near, 0};
				normalize(c);
				
				double[] c2 = {(double)c[0], (double)c[1], (double)c[2], 0};

				boolean hasHit = false;
				
				double [] Si = {0, 0, 0, 1};
				
				float [] colors = bounces(pixels, Spheres, Lights, c2, Si, back, near, hasHit, r, 0, ambient, -5);
				
				pixels[i][j][0] = colors[0];
				pixels[i][j][1] = colors[1];
				pixels[i][j][2] = colors[2];
				
				sb.append(pixels[i][j][0]*255 + " " + pixels[i][j][1]*255 + " " + pixels[i][j][2]*255 + " "); 
				
			}
		}
		
		return sb.toString();
		

	}
	
	// writes string to ppm file with output name
	public static void writePpm(float [][][] pixels, String outName, String pixStr){

		try {
			File imageC = new File("./" + outName);
			FileWriter writer = new FileWriter("./" + outName);
			writer.write(pixStr);
		} catch(IOException e) {

		}
		
	}
	
	public static float min(float[] list){
		float min = (float)list[0];
		
		for(int i = 1;i < list.length;i++){
			if(list[i] < min){
				min = (float)list[i];
			}
		}
		
		if(list[0] % min != 0 || list[1] % min != 0 || list[2] % min != 0){
			min = 1;
		}
		
		return min;
	}
	
	// determines collision from ray and origin then sets colors based on that data
	// calls itself recusively to add reflections
	public static float [] bounces(float [][][] pixels, ArrayList<Sphere> Spheres, ArrayList<Light> Lights, double [] c2, double [] Si, float[] back, float near, boolean hasHit, float [] r, int counter, float [] ambient, int omit){
		if(counter >= 3 || (counter > 0 && Spheres.get(omit).kr == 0)){
			return r;
		}
		
		int inside = 1; // used to flip normal if on inside of sphere
		
		float zBuffer = -100;// initialize of zBuffer
		
		float tf = 0; // scalar of collision location
		
		int intersectOb = 0; // object index of intersection
		
		Matrix modelIn = Matrix.identity(4, 4); 

		Matrix model = Matrix.identity(4, 4);
		
		float [] N = {0, 0, 0};
		
		for(int k = 0;k < Spheres.size();k++){
			
			// 
			if(k == omit){
				continue;
			}
			
			//translation and scale matrix arrays
			double [] [] T_data = {{1, 0, 0, Spheres.get(k).pos[0]}, {0, 1, 0, Spheres.get(k).pos[1]}, {0, 0, 1, Spheres.get(k).pos[2]}, {0, 0, 0, 1}};
			double [] [] s_data = {{Spheres.get(k).scale[0], 0, 0, 0}, {0, Spheres.get(k).scale[1], 0, 0}, {0, 0, Spheres.get(k).scale[2], 0}, {0, 0, 0, 1}};
			
           //set double of origin
			double [] S = {Si[0], Si[1], Si[2], Si[3]};
			
			// get model matrix
			Matrix translation = new Matrix(T_data, 4, 4);
			
			Matrix scale = new Matrix(s_data, 4, 4);
			
			Matrix scaleIn = scale.inverse();
			
			Matrix translationIn = translation.inverse();
			
			modelIn = scaleIn.times(translationIn);
			
			model = translation.times(scale);
			
			Matrix c_matrix = new Matrix(c2, 4);
			c_matrix = modelIn.times(c_matrix);
			
			Matrix s_matrix = new Matrix(S, 4);
			s_matrix = modelIn.times(s_matrix);
			
			
			double [] [] c3 = c_matrix.getArray();
			
			double [] [] s2 = s_matrix.getArray();
			
			
			//get ABC for quadratic
			float A = (float)(c3[0][0]*c3[0][0] + c3[1][0]*c3[1][0] + c3[2][0]*c3[2][0]);

			float B = (float)((s_matrix.transpose().times(c_matrix).get(0, 0)));
			
			float C = (float)(s2[0][0] * s2[0][0] + s2[1][0]*s2[1][0] + s2[2][0]*s2[2][0]) - 1;
			
			float numSol = B*B - A*C;
			
			// verifies collision is in appropriate spot + sets ambient/background color
			if(numSol >= 0){

				float t = - B / A - (float)Math.sqrt(numSol) / (A);
				float t2 = - B / A + (float)Math.sqrt(numSol) / ( A);
				float tf2 = 0;
				
				//gets smallest positive
				if(t < t2 && t > 0){
					tf = t;
					tf2 = t2;
				}else if(t2 < t && t2 > 0){
					tf = t2;
					tf2 = t;
				}
				
				// depth saves
				float z = (float)c2[2] * tf;
				float z2 = (float)c2[2] * t2;		
				
				// checks if min positive t value isn't behind object or cut off by nearplane
				if(z < -near && z > zBuffer && tf > 0 || (counter > 0 && tf > 0)){
					zBuffer = z;
					intersectOb = k;
					
					
					
					if(counter == 0){
						r[0] = Spheres.get(k).ka * ambient[0] * Spheres.get(k).r;
						r[1] = Spheres.get(k).ka * ambient[1] * Spheres.get(k).g;
						r[2] = Spheres.get(k).ka * ambient[2] * Spheres.get(k).b;
						
					}
				
				// secondary check for alternative intersection for near plane cut off 
				}else if((float)c2[2] * tf2 < -near && (float)c2[2] * tf2 > zBuffer && tf2 > 0 || (counter > 0 && tf2 > 0)){
					zBuffer = (float)c2[2] * tf2;
					intersectOb = k;
					tf = tf2;
					inside = -1;
					
					if(counter == 0){
						r[0] = Spheres.get(k).ka * ambient[0] * Spheres.get(k).r;
						r[1] = Spheres.get(k).ka * ambient[1] * Spheres.get(k).g;
						r[2] = Spheres.get(k).ka * ambient[2] * Spheres.get(k).b;
						
					}
					
				// meant to continue loop or return if both t values are invalid
				}else if (!hasHit){
					
					if(counter == 0){
						r[0] = back[0];
						r[1] = back[1];
						r[2] = back[2];
					}
					
					if(k == Spheres.size() - 1){
						return r;
					}
					
					continue;
				}
				
				
				hasHit = true;

				
			// return background if no intersections or returns original color if is a reflected ray
			}else if(k == Spheres.size() - 1 && !hasHit){
				if(counter == 0){
					r[0] = back[0];
					r[1] = back[1];
					r[2] = back[2];
				}
				
				return r;
			}
			
		}
	
	    //set normal vector
		double [] N_doub = {(float)c2[0] * tf - Spheres.get(intersectOb).pos[0], (float)c2[1] * tf  - Spheres.get(intersectOb).pos[1], (float)c2[2] * tf  - Spheres.get(intersectOb).pos[2], 0};
		
		Matrix N_matrix = new Matrix(N_doub, 4);
		
		N_matrix = model.inverse().transpose().times(N_matrix);
		
		double [] [] n_temp = N_matrix.getArray();
		
		N[0] = (float)n_temp[0][0];
		N[1] = (float)n_temp[1][0];
		N[2] = (float)n_temp[2][0];
		
		normalize(N);
		
		N[0] = inside*N[0];
		N[1] = inside*N[1];
		N[2] = inside*N[2];
		
		
		
		// call ads for each light
		for(int k = 0;k < Lights.size();k++){
			float [] temp = {0, 0, 0};
			
			temp = ads(Spheres, Lights, intersectOb, N, k, ambient, modelIn);
			
			// adds ads lighting if isn't reflected ray and not in shadow
			if(counter == 0){
				
				//get shadow points and direction
				double [] shadowOrigin = {c2[0]*tf, c2[1]*tf, c2[2]*tf, 1};
				
				float [] lightDir = {Lights.get(k).pos[0] - (float)shadowOrigin[0], Lights.get(k).pos[1] - (float)shadowOrigin[1], Lights.get(k).pos[2] - (float)shadowOrigin[2], 0};
				normalize(lightDir);
				
				double [] lightDirDoub = {lightDir[0], lightDir[1], lightDir[2], 0};
				
				
				//determine if in shadow
				boolean isShadowed = collision(Spheres, lightDirDoub, shadowOrigin, back, near, Spheres.get(intersectOb));
				
				
				if(!isShadowed){
					r[0] += temp[0]; 
					r[1] += temp[1]; 
					r[2] += temp[2];
				}
				
			}else {
				r[0] += Spheres.get(omit).kr * temp[0]; 
				r[1] += Spheres.get(omit).kr * temp[1]; 
				r[2] += Spheres.get(omit).kr * temp[2];
			}
			
			// cap color values
			if(r[0] > 1){
				r[0] = 1;
			}
			if(r[1] > 1){
				r[1] = 1;
			}
			if(r[2] > 1){
				r[2] = 1;
			}
			
		}
		
		// stops reflecting if hits something
		if(hasHit && counter > 0){
			return r;
		}
		
		
		// set up start point and direction for reflected ray
		double [] P = {c2[0] * tf, c2[1] * tf, c2[2] * tf, 1};
		
		float temp = -2 * (N[0]*(float)c2[0] + N[1]*(float)c2[1] + N[2]*(float)c2[2]);
		
		float [] v = {(temp * N[0] + (float)c2[0]), temp * N[1] + (float)c2[1], temp * N[2] + (float)c2[2], 0};
		
		normalize(v);
		
		double [] vd = {v[0], v[1], v[2], 0};
		
        // repeat for reflection
		return bounces(pixels, Spheres, Lights, vd, P, back, near, false, r, ++counter, ambient, intersectOb);
	}
	
	// determines collision from start point and direction for use of determining if in shadow
	public static boolean collision(ArrayList<Sphere> Spheres, double [] c2, double [] Si, float[] back, float near, Sphere current){
		
		Matrix modelIn = Matrix.identity(4, 4);
		
		for(int k = 0;k < Spheres.size();k++){
			if(Spheres.get(k).name == current.name){
				continue;
			}
			
			double [] [] T_data = {{1, 0, 0, Spheres.get(k).pos[0]}, {0, 1, 0, Spheres.get(k).pos[1]}, {0, 0, 1, Spheres.get(k).pos[2]}, {0, 0, 0, 1}};
			double [] [] s_data = {{Spheres.get(k).scale[0], 0, 0, 0}, {0, Spheres.get(k).scale[1], 0, 0}, {0, 0, Spheres.get(k).scale[2], 0}, {0, 0, 0, 1}};

			double [] S = {Si[0], Si[1], Si[2], Si[3]};
			
			
			// model matrix calc
			Matrix translation = new Matrix(T_data, 4, 4);
			
			Matrix scale = new Matrix(s_data, 4, 4);

			Matrix scaleIn = scale.inverse();
			
			Matrix translationIn = translation.inverse();
			
			modelIn = scaleIn.times(translationIn);
			
			Matrix c_matrix = new Matrix(c2, 4);
			c_matrix = modelIn.times(c_matrix);
			
			Matrix s_matrix = new Matrix(S, 4);
			s_matrix = modelIn.times(s_matrix);
			
			
			double [] [] c3 = c_matrix.getArray();
			
			double [] [] s2 = s_matrix.getArray();
			
			
			//intersection check
			float A = (float)(c3[0][0]*c3[0][0] + c3[1][0]*c3[1][0] + c3[2][0]*c3[2][0]);

			float B = (float)((s_matrix.transpose().times(c_matrix).get(0, 0)));
			
			float C = (float)(s2[0][0] * s2[0][0] + s2[1][0]*s2[1][0] + s2[2][0]*s2[2][0]) - 1;
			
			float numSol = B*B - A*C;
				
			
			if(numSol >= 0){
				
				float t = - B / A - (float)Math.sqrt(numSol) / (A);
				float t2 = - B / A + (float)Math.sqrt(numSol) / (A);		
				
				float z = (float)Si[2] + (float)c2[2] * t;
				float z2 = (float)Si[2] + (float) c2[2] * t2;
				
				// if isn't cut off and intesection is between light and point of ray
				if((t > 0 && z < -near) || (t2 > 0 && z2 < -near)){				
					return true;
				}	
			
			}
		}
		
		return false;
	}
	
	
	public static void normalize(float [] vector){
		float sum = 0;
		
		for(int i = 0;i < vector.length;i++){
			sum += vector[i] * vector[i];
		}
		
		float x = (float)Math.sqrt(sum);
		
		for(int i = 0;i < vector.length;i++){
			vector[i] = vector[i] / x;
		}
	}
	
	
	// locates index desired field is and obtains single field
	// precondition: only works if there is only one field to search for
	public static float locateField(String field, ArrayList<String> input){
		for(int i = 0;i < input.size();i++){
			if (input.get(i).contains(field)){
				float r = getSingleField(input.get(i));
				input.remove(i);
				return r;
			}
		}
		
		return 0;
	}
	
	// locates index desired field is and obtains fields
	// precondition: only works on list of fields needed 
	public static float [] locateFields(String field, ArrayList<String> input){
		for(int i = 0;i < input.size();i++){
			if (input.get(i).contains(field)){
				float [] r = getField(input.get(i));
				input.remove(i);
				return r;
			}
		}
		
		return null;
	}
	
	//ads lighting
	public static float [] ads(ArrayList<Sphere> Spheres, ArrayList<Light> Lights, int intersectOb, float[] N, int k, float [] ambient, Matrix modelIn){
		
		float [] L = {Lights.get(k).pos[0] - Spheres.get(intersectOb).pos[0], Lights.get(k).pos[1] - Spheres.get(intersectOb).pos[1], Lights.get(k).pos[2] - Spheres.get(intersectOb).pos[2]}; 
		normalize(L);

		float lightDotNormal = (L[0]* N[0] + L[1] * N[1] + L[2] * N[2]);
		if(lightDotNormal < 0){
			float [] color = {0, 0, 0};
			return color;
		}
		
		float [] V = {-Spheres.get(intersectOb).pos[0], -Spheres.get(intersectOb).pos[1], -Spheres.get(intersectOb).pos[2]};
		normalize(V);
		
		
		float coeff = 2 * (L[0] * N[0] + L[1] * N[1] + L[2] * N[2]);		
		float [] R = {coeff * N[0] - L[0], coeff * N[1] - L[1], coeff * N[2] - L[2]};
		
		
		float RDotV = R[0]*V[0] + R[1]*V[1] + R[2]*V[2];

		
		float [] color = {Lights.get(k).color[0] * lightDotNormal * Spheres.get(intersectOb).r * Spheres.get(intersectOb).kd + Spheres.get(intersectOb).ks * Lights.get(k).color[0] * (float)Math.pow(RDotV, Spheres.get(intersectOb).n), 
						  Lights.get(k).color[1] * lightDotNormal * Spheres.get(intersectOb).g * Spheres.get(intersectOb).kd + Spheres.get(intersectOb).ks * Lights.get(k).color[1] * (float)Math.pow(RDotV, Spheres.get(intersectOb).n), 
						  Lights.get(k).color[2] * lightDotNormal * Spheres.get(intersectOb).b * Spheres.get(intersectOb).kd + Spheres.get(intersectOb).ks * Lights.get(k).color[2] * (float)Math.pow(RDotV, Spheres.get(intersectOb).n)
						};
		
		return color;
	}
	
}