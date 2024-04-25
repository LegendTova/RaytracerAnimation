public class Light {
	String name = "";
	float[] pos;
	float[] color;

	
	
	public Light(String name, float[] pos, float[] color){
		this.name = name;
		this.pos = pos;
		this.color = color;
	}
	
	public float [] getPos(){
		return this.pos;
	}
	
	public float [] getColor(){
		return this.color;
	}
	
}