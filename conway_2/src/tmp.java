public class tmp {
	private static final int COLOR_RED = 0xff000000 | (255 << 16) | (0 << 8)
			| 0;
	private static final int COLOR_BLUE = 0xff000000 | (0 << 16) | (0 << 8)
			| 255;// 0x0000ff;

	public static void main(String[] args) {
		System.out.println("red: " + COLOR_RED + " blue: " + COLOR_BLUE);
		System.out.println(0xFF000000 | 255);
	}
}
