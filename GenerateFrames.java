import java.util.ArrayList;
import java.util.*;
import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter; 
import Jama.*;

class GenerateFrames {
	//args[0] = directory
    public static void main(String[] args) {
		ArrayList<ArrayList<String>> frames = new ArrayList<ArrayList<String>>();
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(" ");
		int i = 1;
		
		while(true){
		    String fileName = "./" + args[0] + "/" + i + ".txt";
			temp = getInput(fileName);
			if(temp.isEmpty()){
				break;
			}
			
			frames.add(temp);
			i++;
		}
		
		int resIndex = 0;
		
		for(i = 0;i < frames.get(0).size();i++){
			if(frames.get(0).get(i).contains("RES")){
				resIndex = i;
				break;
			}
		}
		
		int [] res = getFieldInt(frames.get(0).get(resIndex));
		
		int index = -1;
		
		for(i = 0;i < frames.get(0).size();i++){
			if(frames.get(0).get(i).contains("OUTPUT")){
				index = i;
				break;
			}
		}
		
		String outDir = frames.get(0).get(index).substring(7);
		
		for(i = 1;i < frames.size();i++){
			System.out.println(frames.get(i-1) + "\n" + frames.get(i) + "\n");
			interpolateBetweenFrames((ArrayList)frames.get(i-1).clone(), (ArrayList)frames.get(i).clone(), outDir, res);
		}
		

	}
	
	public static void interpolateBetweenFrames(ArrayList<String> start, ArrayList<String> end, String outDir, int [] res){
		ArrayList<Sphere> Spheres = new ArrayList<Sphere>();
		ArrayList<Sphere> SpheresE = new ArrayList<Sphere>();
		
		ArrayList<Light> Lights = new ArrayList<Light>();
		ArrayList<Light> LightsE = new ArrayList<Light>();
		
		float near = locateField("NEAR", start);
		float left = locateField("LEFT", start);
		float right = locateField("RIGHT", start);
		float bottom = locateField("BOTTOM", start);
		float top = locateField("TOP", start);
		float frame = locateField("FRAME", start);
		
		float nearE = locateField("NEAR", end);
		float leftE = locateField("LEFT", end);
		float rightE = locateField("RIGHT", end);
		float bottomE = locateField("BOTTOM", end);
		float topE = locateField("TOP", end);
		float frameE = locateField("FRAME", end);
		
		int frameDiff = (int)(frameE-frame);
		
		sphereComp(start, Spheres);
		sphereComp(end, SpheresE);
		
		lightComp(start, Lights);
		lightComp(end, LightsE);
		
		float [] back = locateFields("BACK", start);
		float [] ambient = locateFields("AMBIENT", start);
		
		float [] backE = locateFields("BACK", end);
		float [] ambientE = locateFields("AMBIENT", end);
		
		ArrayList<SphereFrames> SpheresFrames = new ArrayList<SphereFrames>();
		ArrayList<LightFrames> LightsFrames = new ArrayList<LightFrames>();
		
		for(int i = 0;i < Spheres.size();i++){
			float[][] pos = interpolateMult(Spheres.get(i).getPos(), SpheresE.get(i).getPos(), frameDiff);
			float[][] scale = interpolateMult(Spheres.get(i).getScale(), SpheresE.get(i).getScale(), frameDiff);
			float[] r = interpolateSingle(Spheres.get(i).getR(), SpheresE.get(i).getR(), frameDiff);
			float[] g = interpolateSingle(Spheres.get(i).getG(), SpheresE.get(i).getG(), frameDiff);
			float[] b = interpolateSingle(Spheres.get(i).getB(), SpheresE.get(i).getB(), frameDiff);
			float[] ka = interpolateSingle(Spheres.get(i).getKa(), SpheresE.get(i).getKa(), frameDiff);
			float[] kd = interpolateSingle(Spheres.get(i).getKd(), SpheresE.get(i).getKd(), frameDiff);
			float[] ks = interpolateSingle(Spheres.get(i).getKs(), SpheresE.get(i).getKs(), frameDiff);
			float[] kr = interpolateSingle(Spheres.get(i).getKr(), SpheresE.get(i).getKr(), frameDiff);
			float[] n = interpolateSingle(Spheres.get(i).getN(), SpheresE.get(i).getN(), frameDiff);
			
			SphereFrames tempSp = new SphereFrames(pos, scale, r, g, b, ks, kd, ks, kr, n);
			
			SpheresFrames.add(tempSp);
		}
		
		for(int i = 0;i < Lights.size();i++){
			float[][] pos = interpolateMult(Lights.get(i).getPos(), LightsE.get(i).getPos(), frameDiff);
			float[][] color = interpolateMult(Lights.get(i).getColor(), LightsE.get(i).getColor(), frameDiff);
			
			LightFrames tempSp = new LightFrames(pos, color);
			
			LightsFrames.add(tempSp);
		}
	
		generateInnerFrames(frameDiff, interpolateSingle(near, nearE, frameDiff), interpolateSingle(left, leftE, frameDiff), interpolateSingle(right, rightE, frameDiff), interpolateSingle(bottom, bottomE, frameDiff), interpolateSingle(top, topE, frameDiff), interpolateMult(ambient, ambientE, frameDiff), interpolateMult(back, backE, frameDiff), SpheresFrames, LightsFrames, outDir, res, (int)frame);
	
	}
	
	public static void sphereComp(ArrayList<String> keyframe, ArrayList<Sphere> Spheres){
		for(int i = 0;i < keyframe.size();i++){
			
			if((keyframe.get(i)).contains("SPHERE")){
				String sphereRemoval = keyframe.get(i).substring(7);
				
                String [] temp = sphereRemoval.split("\\s+", 0);
				float [] l = getField(sphereRemoval);
				float [] pos = {l[0], l[1], l[2]};
				float [] scale = {l[3], l[4], l[5]};
				
				sphereRemoval = sphereRemoval.substring(0, 2);
				
				
				Sphere s = new Sphere(temp[0], pos, scale, l[6], l[7], l[8], l[9], l[10], l[11], l[12], l[13]);
				
				Spheres.add(s);
				
			}
		}
	}
	
	public static void lightComp(ArrayList<String> keyframe, ArrayList<Light> Lights){
		for(int i = 0;i < keyframe.size();i++){
			
			if((keyframe.get(i)).contains("LIGHT")){
				String lightRemoval = keyframe.get(i).substring(6);
				
				String [] temp = lightRemoval.split("\\s+", 0);
				float [] l = getField(lightRemoval);
				float [] pos = {l[0], l[1], l[2]};
				float [] color = {l[3], l[4], l[5]};
				
				lightRemoval = lightRemoval.substring(0, 2);
				
				
				Light s = new Light(temp[0], pos, color);
				
				Lights.add(s);
				
			}
		}
	}
	
	public static float [] interpolateSingle(float start, float end, int frameDiff){
		float step = (end - start) / (frameDiff-1);
		
		float [] intepolatedValues = new float [frameDiff];
		
		for(int i = 0;i < intepolatedValues.length;i++){
			intepolatedValues[i] = start + i*step;
		}
		
		return intepolatedValues;
	}
	
	public static float [] [] interpolateMult(float [] start, float [] end, int frameDiff){
		float [] [] intepolatedValues = new float [start.length] [frameDiff];
		
		for(int i = 0;i < start.length;i++){
			intepolatedValues[i] = interpolateSingle(start[i], end[i], frameDiff);
		}
		
		return intepolatedValues;
	}
	
	public static void generateInnerFrames(int frameDiff, float [] near, float [] left, float [] right, float [] bottom, float [] top, float[][] ambient, float[][] back, ArrayList<SphereFrames> SpheresFrames, ArrayList<LightFrames> LightsFrames, String outDir, int [] res, int frameStart){
		for(int i = 0;i < frameDiff;i++){
			String parameters = "NEAR " + near[i] + "\n" +
								"LEFT " + left[i] + "\n" +
								"RIGHT " + right[i] + "\n" +
								"BOTTOM " + bottom[i] + "\n"+
								"TOP " + top[i] + "\n"+
								"RES " + res[0] + " " + res[1] + "\n";
			for(int j = 0;j < SpheresFrames.size();j++){
				parameters += "SPHERE s" + (j+1) + " " + SpheresFrames.get(j).getPos()[0][i] + " " +  SpheresFrames.get(j).pos[1][i] + " " +  SpheresFrames.get(j).pos[2][i] + " " + SpheresFrames.get(j).scale[0][i] + " " +  SpheresFrames.get(j).scale[1][i] + " " +  SpheresFrames.get(j).scale[2][i] + " " +  SpheresFrames.get(j).r[i] + " " +  SpheresFrames.get(j).g[i] + " " + SpheresFrames.get(j).b[i] + " " +  SpheresFrames.get(j).ka[i] + " " + SpheresFrames.get(j).kd[i] + " " + SpheresFrames.get(j).ks[i] + " " + SpheresFrames.get(j).kr[i] + " " + SpheresFrames.get(j).n[i] + "\n";
			}
			
			for(int j = 0;j < LightsFrames.size();j++){
				parameters += "LIGHT l" + (j+1) + " " + LightsFrames.get(j).pos[0][i] + " " + LightsFrames.get(j).pos[1][i] + " " + LightsFrames.get(j).pos[2][i] + " " + LightsFrames.get(j).color[0][i] + " " + LightsFrames.get(j).color[1][i] + " " + LightsFrames.get(j).color[2][i] + "\n"; 
			}
								
			parameters += "BACK " + back[0][i] + " " + back[1][i] + " " + back[2][i] + "\n";  
			parameters += "AMBIENT " + ambient[0][i] + " " + ambient[1][i] + " " + ambient[2][i] + "\n";
			parameters += "OUTPUT " + "./" + outDir + "/" + (i+frameStart+1) + ".ppm\n";
			
			writeFrameTxt(outDir, i + frameStart + 1, parameters);
		}
	}
	
	// writes string to ppm file with output name
	public static void writeFrameTxt(String outDir, int frame, String parameters){

		try {
			File imageC = new File("./" + outDir + "/" + frame + ".txt");
			FileWriter writer = new FileWriter("./" + outDir + "/" + frame + ".txt");			
			writer.write(parameters);
			writer.close();
			
			String [] RayTraceArg = new String[1];
			RayTraceArg[0] = "./" + outDir + "/" + frame + ".txt";
			
			RayTracer r = new RayTracer();
			r.createImage(RayTraceArg);
			
		} catch(IOException e) {

		}
		
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
			System.out.println("Reached end of directory");
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
}