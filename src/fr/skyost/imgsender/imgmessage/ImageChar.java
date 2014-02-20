package fr.skyost.imgsender.imgmessage;

/**
 * ImageChar modified by Skyost.
 * 
 * @author bobacadodl.
 */
public enum ImageChar {
	LETTER('A'),
	BLOCK('\u2588'),
	SMALL_BLOCK('\u25A0'),
	TRIGRAM('\u2630'),
	DOTTED_SQUARE('\u2B1A'),
	DIAGONAL('\u259E'),
	DARK_SHADE('\u2593'),
	MEDIUM_SHADE('\u2592'),
	LIGHT_SHADE('\u2591');
	
	private final char c;
	
	ImageChar(final char c) {
		this.c = c;
	}
	
	public char getChar() {
		return c;
	}
}
