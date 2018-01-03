package thresholdScheme;

import java.util.HashMap;
import java.util.Map.Entry;

public class ThresholdScheme {

    public int getDeactivationCode(int k, int n, int[] polyCoeff,
        HashMap<Integer, Integer> collab, int[] givenShares) {
        if (3 > k || k > n || n > 8) {
			throw new IllegalArgumentException("Illegal arguments");
		}
		addf1(collab, polyCoeff, givenShares);

		double sum = 0;
		for (Entry<Integer, Integer> entry : collab.entrySet()) {
			int key = entry.getKey();
			double numerator = 1.0;
			double denominator = 1.0;

			// Multiply numerator with every other point except for its own
			// Multiply denominator with (own point - other point)
			for (Entry<Integer, Integer> e : collab.entrySet()) {
				if (e.getKey() != key) {
					numerator *= -e.getKey();
					denominator *= key - e.getKey();
				}
			}
			sum += numerator / denominator * entry.getValue();
		}
		return (int) Math.round(sum);
	}

	/**
	 * Adds f(1) to the collaboration points
	 *
	 * @param collab
	 *            Collaboration points
	 * @param polyCoeff
	 *            Own secret polynomial
	 * @param givenShares
	 *            Given data shares
	 */
	private void addf1(HashMap<Integer, Integer> collab, int[] polyCoeff, int[] givenShares) {
		int f1 = 0;
		for (int i = 0; i < givenShares.length; i++) {
			f1 += givenShares[i];
		}
		for (int i = 0; i < polyCoeff.length; i++) {
			f1 += polyCoeff[i];
		}
		collab.put(1, f1);
	}

	public static void main(String[] args) {
		ThresholdScheme ts = new ThresholdScheme();
		int k = 5;
		int n = 6;
		int[] polyCoeff = { 15, 19, 13, 18, 20 };
		HashMap<Integer, Integer> collab = new HashMap<Integer, Integer>();
		collab.put(2, 1908);
		collab.put(3, 7677);
		collab.put(5, 50751);
		collab.put(6, 101700);
		int[] givenShares = { 34, 48, 45, 39, 24 };
		System.out.println(ts.getDeactivationCode(k, n, polyCoeff, collab, givenShares));
	}
}
