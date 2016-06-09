package blue.made.turremclient2d.util;

/**
 * Created by Sam Sartor on 6/8/2016.
 */
public class Location {
	public final int x;
	public final int y;

	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Location) {
			Location loc = (Location) o;
			return loc.x == x && loc.y == y;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return x * 31 + y;
	}
}
