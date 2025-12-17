/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.tianmi.sussypatches.common.stoichiometry.apachemath.util;

import java.util.*;

import dev.tianmi.sussypatches.common.stoichiometry.apachemath.exception.*;

/**
 * Arrays utilities.
 *
 * @since 3.0
 */
public class MathArrays {

    /**
     * Private constructor.
     */
    private MathArrays() {}

    /**
     * Specification of ordering direction.
     */
    public enum OrderDirection {
        /** Constant for increasing direction. */
        INCREASING,
        /** Constant for decreasing direction. */
        DECREASING
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a     Array.
     * @param b     Array.
     * @param abort Whether to throw an exception if the check fails.
     * @return {@code true} if the arrays have the same length.
     * @throws DimensionMismatchException if the lengths differ and
     *                                    {@code abort} is {@code true}.
     * @since 3.6
     */
    public static boolean checkEqualLength(double[] a,
                                           double[] b,
                                           boolean abort) {
        if (a.length == b.length) {
            return true;
        } else {
            if (abort) {
                throw new DimensionMismatchException(a.length, b.length);
            }
            return false;
        }
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @throws DimensionMismatchException if the lengths differ.
     * @since 3.6
     */
    public static void checkEqualLength(double[] a,
                                        double[] b) {
        checkEqualLength(a, b, true);
    }

    /**
     * Creates a copy of the {@code source} array.
     *
     * @param source Array to be copied.
     * @param len    Number of entries to copy. If smaller then the source
     *               length, the copy will be truncated, if larger it will padded with
     *               zeroes.
     * @return the copied array.
     */
    public static int[] copyOf(int[] source, int len) {
        final int[] output = new int[len];
        System.arraycopy(source, 0, output, 0, FastMath.min(len, source.length));
        return output;
    }

    /**
     * Creates a copy of the {@code source} array.
     *
     * @param source Array to be copied.
     * @param len    Number of entries to copy. If smaller then the source
     *               length, the copy will be truncated, if larger it will padded with
     *               zeroes.
     * @return the copied array.
     */
    public static double[] copyOf(double[] source, int len) {
        final double[] output = new double[len];
        System.arraycopy(source, 0, output, 0, FastMath.min(len, source.length));
        return output;
    }

    /**
     * Compute a linear combination accurately.
     * This method computes the sum of the products
     * <code>a<sub>i</sub> b<sub>i</sub></code> to high accuracy.
     * It does so by using specific multiplication and addition algorithms to
     * preserve accuracy and reduce cancellation effects.
     * <br/>
     * It is based on the 2005 paper
     * <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita, Siegfried M. Rump,
     * and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     *
     * @param a Factors.
     * @param b Factors.
     * @return <code>&Sigma;<sub>i</sub> a<sub>i</sub> b<sub>i</sub></code>.
     * @throws DimensionMismatchException if arrays dimensions don't match
     */
    public static double linearCombination(final double[] a, final double[] b)
                                                                               throws DimensionMismatchException {
        checkEqualLength(a, b);
        final int len = a.length;

        if (len == 1) {
            // Revert to scalar multiplication.
            return a[0] * b[0];
        }

        final double[] prodHigh = new double[len];
        double prodLowSum = 0;

        for (int i = 0; i < len; i++) {
            final double ai = a[i];
            final double aHigh = Double.longBitsToDouble(Double.doubleToRawLongBits(ai) & ((-1L) << 27));
            final double aLow = ai - aHigh;

            final double bi = b[i];
            final double bHigh = Double.longBitsToDouble(Double.doubleToRawLongBits(bi) & ((-1L) << 27));
            final double bLow = bi - bHigh;
            prodHigh[i] = ai * bi;
            final double prodLow = aLow * bLow - (((prodHigh[i] -
                    aHigh * bHigh) -
                    aLow * bHigh) -
                    aHigh * bLow);
            prodLowSum += prodLow;
        }

        final double prodHighCur = prodHigh[0];
        double prodHighNext = prodHigh[1];
        double sHighPrev = prodHighCur + prodHighNext;
        double sPrime = sHighPrev - prodHighNext;
        double sLowSum = (prodHighNext - (sHighPrev - sPrime)) + (prodHighCur - sPrime);

        final int lenMinusOne = len - 1;
        for (int i = 1; i < lenMinusOne; i++) {
            prodHighNext = prodHigh[i + 1];
            final double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += (prodHighNext - (sHighCur - sPrime)) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
        }

        double result = sHighPrev + (prodLowSum + sLowSum);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = 0;
            for (int i = 0; i < len; ++i) {
                result += a[i] * b[i];
            }
        }

        return result;
    }

    /**
     * Compute a linear combination accurately.
     * <p>
     * This method computes a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> to high accuracy. It does
     * so by using specific multiplication and addition algorithms to
     * preserve accuracy and reduce cancellation effects. It is based
     * on the 2005 paper <a
     * href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita,
     * Siegfried M. Rump, and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     * </p>
     * 
     * @param a1 first factor of the first term
     * @param b1 second factor of the first term
     * @param a2 first factor of the second term
     * @param b2 second factor of the second term
     * @return a<sub>1</sub>&times;b<sub>1</sub> +
     *         a<sub>2</sub>&times;b<sub>2</sub>
     * @see #linearCombination(double, double, double, double, double, double)
     * @see #linearCombination(double, double, double, double, double, double, double, double)
     */
    public static double linearCombination(final double a1, final double b1,
                                           final double a2, final double b2) {
        // the code below is split in many additions/subtractions that may
        // appear redundant. However, they should NOT be simplified, as they
        // use IEEE754 floating point arithmetic rounding properties.
        // The variable naming conventions are that xyzHigh contains the most significant
        // bits of xyz and xyzLow contains its least significant bits. So theoretically
        // xyz is the sum xyzHigh + xyzLow, but in many cases below, this sum cannot
        // be represented in only one double precision number so we preserve two numbers
        // to hold it as long as we can, combining the high and low order bits together
        // only at the end, after cancellation may have occurred on high order bits

        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & ((-1L) << 27));
        final double a1Low = a1 - a1High;
        final double b1High = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & ((-1L) << 27));
        final double b1Low = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High = a1 * b1;
        final double prod1Low = a1Low * b1Low - (((prod1High - a1High * b1High) - a1Low * b1High) - a1High * b1Low);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & ((-1L) << 27));
        final double a2Low = a2 - a2High;
        final double b2High = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & ((-1L) << 27));
        final double b2Low = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High = a2 * b2;
        final double prod2Low = a2Low * b2Low - (((prod2High - a2High * b2High) - a2Low * b2High) - a2High * b2Low);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High = prod1High + prod2High;
        final double s12Prime = s12High - prod2High;
        final double s12Low = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // final rounding, s12 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s12High + (prod1Low + prod2Low + s12Low);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = a1 * b1 + a2 * b2;
        }

        return result;
    }

    /**
     * Compute a linear combination accurately.
     * <p>
     * This method computes a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub>
     * to high accuracy. It does so by using specific multiplication and
     * addition algorithms to preserve accuracy and reduce cancellation effects.
     * It is based on the 2005 paper <a
     * href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita,
     * Siegfried M. Rump, and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     * </p>
     * 
     * @param a1 first factor of the first term
     * @param b1 second factor of the first term
     * @param a2 first factor of the second term
     * @param b2 second factor of the second term
     * @param a3 first factor of the third term
     * @param b3 second factor of the third term
     * @return a<sub>1</sub>&times;b<sub>1</sub> +
     *         a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub>
     * @see #linearCombination(double, double, double, double)
     * @see #linearCombination(double, double, double, double, double, double, double, double)
     */
    public static double linearCombination(final double a1, final double b1,
                                           final double a2, final double b2,
                                           final double a3, final double b3) {
        // the code below is split in many additions/subtractions that may
        // appear redundant. However, they should NOT be simplified, as they
        // do use IEEE754 floating point arithmetic rounding properties.
        // The variables naming conventions are that xyzHigh contains the most significant
        // bits of xyz and xyzLow contains its least significant bits. So theoretically
        // xyz is the sum xyzHigh + xyzLow, but in many cases below, this sum cannot
        // be represented in only one double precision number so we preserve two numbers
        // to hold it as long as we can, combining the high and low order bits together
        // only at the end, after cancellation may have occurred on high order bits

        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & ((-1L) << 27));
        final double a1Low = a1 - a1High;
        final double b1High = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & ((-1L) << 27));
        final double b1Low = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High = a1 * b1;
        final double prod1Low = a1Low * b1Low - (((prod1High - a1High * b1High) - a1Low * b1High) - a1High * b1Low);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & ((-1L) << 27));
        final double a2Low = a2 - a2High;
        final double b2High = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & ((-1L) << 27));
        final double b2Low = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High = a2 * b2;
        final double prod2Low = a2Low * b2Low - (((prod2High - a2High * b2High) - a2Low * b2High) - a2High * b2Low);

        // split a3 and b3 as one 26 bits number and one 27 bits number
        final double a3High = Double.longBitsToDouble(Double.doubleToRawLongBits(a3) & ((-1L) << 27));
        final double a3Low = a3 - a3High;
        final double b3High = Double.longBitsToDouble(Double.doubleToRawLongBits(b3) & ((-1L) << 27));
        final double b3Low = b3 - b3High;

        // accurate multiplication a3 * b3
        final double prod3High = a3 * b3;
        final double prod3Low = a3Low * b3Low - (((prod3High - a3High * b3High) - a3Low * b3High) - a3High * b3Low);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High = prod1High + prod2High;
        final double s12Prime = s12High - prod2High;
        final double s12Low = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3
        final double s123High = s12High + prod3High;
        final double s123Prime = s123High - prod3High;
        final double s123Low = (prod3High - (s123High - s123Prime)) + (s12High - s123Prime);

        // final rounding, s123 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s123High + (prod1Low + prod2Low + prod3Low + s12Low + s123Low);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = a1 * b1 + a2 * b2 + a3 * b3;
        }

        return result;
    }

    /**
     * Compute a linear combination accurately.
     * <p>
     * This method computes a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub> +
     * a<sub>4</sub>&times;b<sub>4</sub>
     * to high accuracy. It does so by using specific multiplication and
     * addition algorithms to preserve accuracy and reduce cancellation effects.
     * It is based on the 2005 paper <a
     * href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita,
     * Siegfried M. Rump, and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     * </p>
     * 
     * @param a1 first factor of the first term
     * @param b1 second factor of the first term
     * @param a2 first factor of the second term
     * @param b2 second factor of the second term
     * @param a3 first factor of the third term
     * @param b3 second factor of the third term
     * @param a4 first factor of the third term
     * @param b4 second factor of the third term
     * @return a<sub>1</sub>&times;b<sub>1</sub> +
     *         a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub> +
     *         a<sub>4</sub>&times;b<sub>4</sub>
     * @see #linearCombination(double, double, double, double)
     * @see #linearCombination(double, double, double, double, double, double)
     */
    public static double linearCombination(final double a1, final double b1,
                                           final double a2, final double b2,
                                           final double a3, final double b3,
                                           final double a4, final double b4) {
        // the code below is split in many additions/subtractions that may
        // appear redundant. However, they should NOT be simplified, as they
        // do use IEEE754 floating point arithmetic rounding properties.
        // The variables naming conventions are that xyzHigh contains the most significant
        // bits of xyz and xyzLow contains its least significant bits. So theoretically
        // xyz is the sum xyzHigh + xyzLow, but in many cases below, this sum cannot
        // be represented in only one double precision number so we preserve two numbers
        // to hold it as long as we can, combining the high and low order bits together
        // only at the end, after cancellation may have occurred on high order bits

        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & ((-1L) << 27));
        final double a1Low = a1 - a1High;
        final double b1High = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & ((-1L) << 27));
        final double b1Low = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High = a1 * b1;
        final double prod1Low = a1Low * b1Low - (((prod1High - a1High * b1High) - a1Low * b1High) - a1High * b1Low);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & ((-1L) << 27));
        final double a2Low = a2 - a2High;
        final double b2High = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & ((-1L) << 27));
        final double b2Low = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High = a2 * b2;
        final double prod2Low = a2Low * b2Low - (((prod2High - a2High * b2High) - a2Low * b2High) - a2High * b2Low);

        // split a3 and b3 as one 26 bits number and one 27 bits number
        final double a3High = Double.longBitsToDouble(Double.doubleToRawLongBits(a3) & ((-1L) << 27));
        final double a3Low = a3 - a3High;
        final double b3High = Double.longBitsToDouble(Double.doubleToRawLongBits(b3) & ((-1L) << 27));
        final double b3Low = b3 - b3High;

        // accurate multiplication a3 * b3
        final double prod3High = a3 * b3;
        final double prod3Low = a3Low * b3Low - (((prod3High - a3High * b3High) - a3Low * b3High) - a3High * b3Low);

        // split a4 and b4 as one 26 bits number and one 27 bits number
        final double a4High = Double.longBitsToDouble(Double.doubleToRawLongBits(a4) & ((-1L) << 27));
        final double a4Low = a4 - a4High;
        final double b4High = Double.longBitsToDouble(Double.doubleToRawLongBits(b4) & ((-1L) << 27));
        final double b4Low = b4 - b4High;

        // accurate multiplication a4 * b4
        final double prod4High = a4 * b4;
        final double prod4Low = a4Low * b4Low - (((prod4High - a4High * b4High) - a4Low * b4High) - a4High * b4Low);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High = prod1High + prod2High;
        final double s12Prime = s12High - prod2High;
        final double s12Low = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3
        final double s123High = s12High + prod3High;
        final double s123Prime = s123High - prod3High;
        final double s123Low = (prod3High - (s123High - s123Prime)) + (s12High - s123Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3 + a4 * b4
        final double s1234High = s123High + prod4High;
        final double s1234Prime = s1234High - prod4High;
        final double s1234Low = (prod4High - (s1234High - s1234Prime)) + (s123High - s1234Prime);

        // final rounding, s1234 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s1234High + (prod1Low + prod2Low + prod3Low + prod4Low + s12Low + s123Low + s1234Low);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = a1 * b1 + a2 * b2 + a3 * b3 + a4 * b4;
        }

        return result;
    }

    /**
     * Returns true iff both arguments are null or have same dimensions and all
     * their elements are equal as defined by
     * {@link Precision#equals(float,float)}.
     *
     * @param x first array
     * @param y second array
     * @return true if the values are both null or have same dimension
     *         and equal elements.
     */
    public static boolean equals(float[] x, float[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!Precision.equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} iff both arguments are {@code null} or have same
     * dimensions and all their elements are equal as defined by
     * {@link Precision#equals(double,double)}.
     *
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     *         dimension and equal elements.
     */
    public static boolean equals(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!Precision.equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }
}
