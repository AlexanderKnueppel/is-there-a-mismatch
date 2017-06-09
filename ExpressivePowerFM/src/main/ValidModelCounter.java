package main;

/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2015  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
import java.math.BigInteger;

/**
 * Counts possible feature models without dead features.
 * 
 * @author Jens Meinicke
 * @author Alexander Knüppel
 * @author Niklas Lehnfeld
 */

public class ValidModelCounter {
	/**
	 * @param N
	 * @param K
	 * @return numerical value for N over K
	 */
	private static BigInteger binomial(final int N, final int K) {
		BigInteger ret = BigInteger.ONE;
		for (int k = 0; k < K; k++) {
			ret = ret.multiply(BigInteger.valueOf(N - k)).divide(BigInteger.valueOf(k + 1));
		}
		return ret;
	}

	/**
	 * @param x
	 * @param y
	 * @return x^y
	 */
	private static BigInteger pow(BigInteger x, BigInteger y) {
		if (y.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException();
		BigInteger z = x; // z will successively become x^2, x^4, x^8, x^16,
							// x^32...
		BigInteger result = BigInteger.ONE;
		byte[] bytes = y.toByteArray();
		for (int i = bytes.length - 1; i >= 0; i--) {
			byte bits = bytes[i];
			for (int j = 0; j < 8; j++) {
				if ((bits & 1) != 0)
					result = result.multiply(z);
				// short cut out if there are no more bits to handle:
				if ((bits >>= 1) == 0 && i == 0)
					return result;
				z = z.multiply(z);
			}
		}
		return result;
	}

	/**
	 * Routine for analytically calculating the number of theoretical
	 * (non-equivalent) product lines with n features.
	 * 
	 * @param n
	 *            Number of features
	 * @return number of theoretical (non-equivalent) product lines with n
	 *         features
	 */
	public static BigInteger count(final int n) {
		BigInteger result = BigInteger.ZERO;
		for (int k = 0; k <= n; k++) {
			BigInteger pow = BigInteger.valueOf(2);
			pow = pow.pow(n - k);

			BigInteger tmp = ValidModelCounter.pow(BigInteger.valueOf(2), pow);
			tmp = tmp.multiply(ValidModelCounter.binomial(n, k));

			if (k % 2 == 1)
				tmp = tmp.negate();

			result = result.add(tmp);
		}
		return result;
	}

	public static void main(String[] args) {
		int n = 10; // default

		if (args.length > 0) {
			n = Integer.parseInt(args[0]);
		}

		System.out.println("How many non-equivalent product lines with <n> features do exist? (n = " + n + ")");
		for (int i = 1; i <= n; i++) {
			System.out.println(i + ": " + ValidModelCounter.count(i));
		}
	}
}
