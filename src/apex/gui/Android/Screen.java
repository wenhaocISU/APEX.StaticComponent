package apex.gui.Android;



public class Screen {

	/** Member classes */
	public enum DeviceRotation { landscape, portrait};
	public class Resolution {
		private int x,y;
		public Resolution(int x, int y)	{this.x = x; this.y = y;}
		public int getX() {return x;}
		public int getY() {return y;}
		public String toString() {return x+"*"+y;}
	}
	public class ApexPoint {
		private int x, y;
		public ApexPoint(int x, int y) {this.x = x; this.y = y;}
		public int getX() {return x;}
		public int getY() {return y;}
		public String toString() {return "(" +x +","+y+")";}
	}
	public class ApexRect {
		private ApexPoint topleft, botright;
		public ApexRect(ApexPoint topleft, ApexPoint botright) {
			this.topleft = topleft; this.botright = botright;
		}
		public ApexRect(int x0, int y0, int x1, int y1) {
			this.topleft = new ApexPoint(x0, y0);
			this.botright = new ApexPoint(x1, y1);
		}
		public ApexPoint getTopLeft() {return topleft;}
		public ApexPoint getBotRight() {return botright;}
		public String toString() {return topleft+","+botright;}
	}
	/** Member classes end */

	
	private Resolution portrait_resolution;
	private DeviceRotation rotation = DeviceRotation.portrait;
	private ApexRect application_rect;
	private int dpi;
	
	
	public Screen(int x, int y)
	{
		this.portrait_resolution = new Resolution(x, y);
	}
	
	public void setRotation(DeviceRotation rotation)
	{
		this.rotation = rotation;
	}
	
	public void changeRotation()
	{
		if (this.rotation == DeviceRotation.landscape)
			this.rotation = DeviceRotation.portrait;
		else
			this.rotation = DeviceRotation.landscape;
	}
	
	public void setApplicationRect(int x, int y)
	{
		this.application_rect = new ApexRect(0,0,x,y);
	}
	
	public ApexRect getApplicationRect()
	{
		return this.application_rect;
	}
	
	public void setDPI(int dpi)
	{
		this.dpi = dpi;
	}
	
	public int getDPI()
	{
		return this.dpi;
	}
	
	public void updateRotation(int x, int y)
	{
		if (x == this.portrait_resolution.x && y == this.portrait_resolution.y)
			this.setRotation(DeviceRotation.portrait);
		else if (x == this.portrait_resolution.y && y == this.portrait_resolution.x)
			this.setRotation(DeviceRotation.landscape);
	}
	
	public Resolution getCurrentResolution()
	{
		if (this.rotation == DeviceRotation.portrait)
			return this.portrait_resolution;
		else
			return new Resolution(
					this.portrait_resolution.y, 
					this.portrait_resolution.x);
	}
	
	public void printInfo()
	{
		System.out.println("-Device Info:");
		System.out.println(" portrait resolution: " + this.portrait_resolution);
		System.out.println(" dpi: " + this.dpi);
		System.out.println(" application rect: " + this.application_rect);
		if (this.rotation == DeviceRotation.portrait)
			System.out.println(" current rotation: portrait");
		else
			System.out.println(" current rotation: landscape");
	}
}
