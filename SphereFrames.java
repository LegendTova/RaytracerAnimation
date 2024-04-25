public class SphereFrames {
	public float[][] pos;
	public float[][] scale;
	public float[] r;
	public float[] g;
	public float[] b;
	public float[] ka;
	public float[] kd;
	public float[] ks;
	public float[] kr;
	public float[] n;
	
	
	public SphereFrames(float[][] pos, float[][] scale, float[] r, float[] g, float[] b, float[] ka, float[] kd, float[] ks, float[] kr, float[] n){
		this.pos = pos;
		this.scale = scale;
		this. r = r;
		this. g = g;
		this.b = b;
		this.ka = ka;
		this.kd = kd;
		this.ks = ks;
		this.kr = kr;
		this.n = n;
	}
	
	public float [] [] getPos(){
		return this.pos;
	}
	
	public float [] [] getScale(){
		return this.scale;
	}
	
	public float [] getR(){
		return this.r;
	}
	
	public float [] getG(){
		return this.g;
	}
	
	public float [] getB(){
		return this.b;
	}
	
	public float [] getKa(){
		return this.ka;
	}
	
	public float [] getKd(){
		return this.kd;
	}
	
	public float [] getKs(){
		return this.ks;
	}
	
	public float [] getKr(){
		return this.kr;
	}
	
	public float [] getN(){
		return this.n;
	}
	
}
	