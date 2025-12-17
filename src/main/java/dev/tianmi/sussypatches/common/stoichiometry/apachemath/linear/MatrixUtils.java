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
package dev.tianmi.sussypatches.common.stoichiometry.apachemath.linear;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dev.tianmi.sussypatches.common.stoichiometry.apachemath.exception.*;
import dev.tianmi.sussypatches.common.stoichiometry.apachemath.exception.util.LocalizedFormats;

/**
 * A collection of static methods that operate on or return matrices.
 *
 */
public class MatrixUtils {

    /**
     * Private constructor.
     */
    private MatrixUtils() {
        super();
    }

    /**
     * Returns a {@link RealMatrix} with specified dimensions.
     * <p>
     * The type of matrix returned depends on the dimension. Below
     * 2<sup>12</sup> elements (i.e. 4096 elements or 64&times;64 for a
     * square matrix) which can be stored in a 32kB array, a {@link
     * Array2DRowRealMatrix} instance is built. Above this threshold a {@link
     * BlockRealMatrix} instance is built.
     * </p>
     * <p>
     * The matrix elements are all set to 0.0.
     * </p>
     *
     * @param rows    number of rows of the matrix
     * @param columns number of columns of the matrix
     * @return RealMatrix with specified dimensions
     * @see #createRealMatrix(double[][])
     */
    public static RealMatrix createRealMatrix(final int rows, final int columns) {
        return (rows * columns <= 4096) ?
                new Array2DRowRealMatrix(rows, columns) : new BlockRealMatrix(rows, columns);
    }

    /**
     * Returns a {@link RealMatrix} whose entries are the the values in the
     * the input array.
     * <p>
     * The type of matrix returned depends on the dimension. Below
     * 2<sup>12</sup> elements (i.e. 4096 elements or 64&times;64 for a
     * square matrix) which can be stored in a 32kB array, a {@link
     * Array2DRowRealMatrix} instance is built. Above this threshold a {@link
     * BlockRealMatrix} instance is built.
     * </p>
     * <p>
     * The input array is copied, not referenced.
     * </p>
     *
     * @param data input array
     * @return RealMatrix containing the values of the array
     * @throws DimensionMismatchException if {@code data} is not rectangular (not all rows have the same length).
     * @throws NoDataException            if a row or column is empty.
     * @throws NullArgumentException      if either {@code data} or {@code data[0]}
     *                                    is {@code null}.
     * @throws DimensionMismatchException if {@code data} is not rectangular.
     * @see #createRealMatrix(int, int)
     */
    public static RealMatrix createRealMatrix(double[][] data)
                                                               throws NullArgumentException, DimensionMismatchException,
                                                               NoDataException {
        if (data == null ||
                data[0] == null) {
            throw new NullArgumentException();
        }
        return (data.length * data[0].length <= 4096) ?
                new Array2DRowRealMatrix(data) : new BlockRealMatrix(data);
    }

    /**
     * Returns <code>dimension x dimension</code> identity matrix.
     *
     * @param dimension dimension of identity matrix to generate
     * @return identity matrix
     * @throws IllegalArgumentException if dimension is not positive
     * @since 1.1
     */
    public static RealMatrix createRealIdentityMatrix(int dimension) {
        final RealMatrix m = createRealMatrix(dimension, dimension);
        for (int i = 0; i < dimension; ++i) {
            m.setEntry(i, i, 1.0);
        }
        return m;
    }

    /**
     * Check if matrix indices are valid.
     *
     * @param m      Matrix.
     * @param row    Row index to check.
     * @param column Column index to check.
     * @throws OutOfRangeException if {@code row} or {@code column} is not
     *                             a valid index.
     */
    public static void checkMatrixIndex(final AnyMatrix m,
                                        final int row, final int column)
                                                                         throws OutOfRangeException {
        checkRowIndex(m, row);
        checkColumnIndex(m, column);
    }

    /**
     * Check if a row index is valid.
     *
     * @param m   Matrix.
     * @param row Row index to check.
     * @throws OutOfRangeException if {@code row} is not a valid index.
     */
    public static void checkRowIndex(final AnyMatrix m, final int row)
                                                                       throws OutOfRangeException {
        if (row < 0 ||
                row >= m.getRowDimension()) {
            throw new OutOfRangeException(LocalizedFormats.ROW_INDEX,
                    row, 0, m.getRowDimension() - 1);
        }
    }

    /**
     * Check if a column index is valid.
     *
     * @param m      Matrix.
     * @param column Column index to check.
     * @throws OutOfRangeException if {@code column} is not a valid index.
     */
    public static void checkColumnIndex(final AnyMatrix m, final int column)
                                                                             throws OutOfRangeException {
        if (column < 0 || column >= m.getColumnDimension()) {
            throw new OutOfRangeException(LocalizedFormats.COLUMN_INDEX,
                    column, 0, m.getColumnDimension() - 1);
        }
    }

    /**
     * Check if submatrix ranges indices are valid.
     * Rows and columns are indicated counting from 0 to {@code n - 1}.
     *
     * @param m           Matrix.
     * @param startRow    Initial row index.
     * @param endRow      Final row index.
     * @param startColumn Initial column index.
     * @param endColumn   Final column index.
     * @throws OutOfRangeException       if the indices are invalid.
     * @throws NumberIsTooSmallException if {@code endRow < startRow} or
     *                                   {@code endColumn < startColumn}.
     */
    public static void checkSubMatrixIndex(final AnyMatrix m,
                                           final int startRow, final int endRow,
                                           final int startColumn, final int endColumn)
                                                                                       throws NumberIsTooSmallException,
                                                                                       OutOfRangeException {
        checkRowIndex(m, startRow);
        checkRowIndex(m, endRow);
        if (endRow < startRow) {
            throw new NumberIsTooSmallException(LocalizedFormats.INITIAL_ROW_AFTER_FINAL_ROW,
                    endRow, startRow, false);
        }

        checkColumnIndex(m, startColumn);
        checkColumnIndex(m, endColumn);
        if (endColumn < startColumn) {
            throw new NumberIsTooSmallException(LocalizedFormats.INITIAL_COLUMN_AFTER_FINAL_COLUMN,
                    endColumn, startColumn, false);
        }
    }

    /**
     * Check if submatrix ranges indices are valid.
     * Rows and columns are indicated counting from 0 to n-1.
     *
     * @param m               Matrix.
     * @param selectedRows    Array of row indices.
     * @param selectedColumns Array of column indices.
     * @throws NullArgumentException if {@code selectedRows} or
     *                               {@code selectedColumns} are {@code null}.
     * @throws NoDataException       if the row or column selections are empty (zero
     *                               length).
     * @throws OutOfRangeException   if row or column selections are not valid.
     */
    public static void checkSubMatrixIndex(final AnyMatrix m,
                                           final int[] selectedRows,
                                           final int[] selectedColumns)
                                                                        throws NoDataException, NullArgumentException,
                                                                        OutOfRangeException {
        if (selectedRows == null) {
            throw new NullArgumentException();
        }
        if (selectedColumns == null) {
            throw new NullArgumentException();
        }
        if (selectedRows.length == 0) {
            throw new NoDataException(LocalizedFormats.EMPTY_SELECTED_ROW_INDEX_ARRAY);
        }
        if (selectedColumns.length == 0) {
            throw new NoDataException(LocalizedFormats.EMPTY_SELECTED_COLUMN_INDEX_ARRAY);
        }

        for (final int row : selectedRows) {
            checkRowIndex(m, row);
        }
        for (final int column : selectedColumns) {
            checkColumnIndex(m, column);
        }
    }

    /**
     * Check if matrices are addition compatible.
     *
     * @param left  Left hand side matrix.
     * @param right Right hand side matrix.
     * @throws MatrixDimensionMismatchException if the matrices are not addition
     *                                          compatible.
     */
    public static void checkAdditionCompatible(final AnyMatrix left, final AnyMatrix right)
                                                                                            throws MatrixDimensionMismatchException {
        if ((left.getRowDimension() != right.getRowDimension()) ||
                (left.getColumnDimension() != right.getColumnDimension())) {
            throw new MatrixDimensionMismatchException(left.getRowDimension(), left.getColumnDimension(),
                    right.getRowDimension(), right.getColumnDimension());
        }
    }

    /**
     * Check if matrices are subtraction compatible
     *
     * @param left  Left hand side matrix.
     * @param right Right hand side matrix.
     * @throws MatrixDimensionMismatchException if the matrices are not addition
     *                                          compatible.
     */
    public static void checkSubtractionCompatible(final AnyMatrix left, final AnyMatrix right)
                                                                                               throws MatrixDimensionMismatchException {
        if ((left.getRowDimension() != right.getRowDimension()) ||
                (left.getColumnDimension() != right.getColumnDimension())) {
            throw new MatrixDimensionMismatchException(left.getRowDimension(), left.getColumnDimension(),
                    right.getRowDimension(), right.getColumnDimension());
        }
    }

    /**
     * Check if matrices are multiplication compatible
     *
     * @param left  Left hand side matrix.
     * @param right Right hand side matrix.
     * @throws DimensionMismatchException if matrices are not multiplication
     *                                    compatible.
     */
    public static void checkMultiplicationCompatible(final AnyMatrix left, final AnyMatrix right)
                                                                                                  throws DimensionMismatchException {
        if (left.getColumnDimension() != right.getRowDimension()) {
            throw new DimensionMismatchException(left.getColumnDimension(),
                    right.getRowDimension());
        }
    }

    /**
     * Serialize a {@link RealVector}.
     * <p>
     * This method is intended to be called from within a private
     * <code>writeObject</code> method (after a call to
     * <code>oos.defaultWriteObject()</code>) in a class that has a
     * {@link RealVector} field, which should be declared <code>transient</code>.
     * This way, the default handling does not serialize the vector (the {@link
     * RealVector} interface is not serializable by default) but this method does
     * serialize it specifically.
     * </p>
     * <p>
     * The following example shows how a simple class with a name and a real vector
     * should be written:
     *
     * <pre>
     * <code>
     * public class NamedVector implements Serializable {
     *
     *     private final String name;
     *     private final transient RealVector coefficients;
     *
     *     // omitted constructors, getters ...
     *
     *     private void writeObject(ObjectOutputStream oos) throws IOException {
     *         oos.defaultWriteObject();  // takes care of name field
     *         MatrixUtils.serializeRealVector(coefficients, oos);
     *     }
     *
     *     private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
     *         ois.defaultReadObject();  // takes care of name field
     *         MatrixUtils.deserializeRealVector(this, "coefficients", ois);
     *     }
     *
     * }
     * </code>
     * </pre>
     * </p>
     *
     * @param vector real vector to serialize
     * @param oos    stream where the real vector should be written
     * @throws IOException if object cannot be written to stream
     * @see #deserializeRealVector(Object, String, ObjectInputStream)
     */
    public static void serializeRealVector(final RealVector vector,
                                           final ObjectOutputStream oos)
                                                                         throws IOException {
        final int n = vector.getDimension();
        oos.writeInt(n);
        for (int i = 0; i < n; ++i) {
            oos.writeDouble(vector.getEntry(i));
        }
    }

    /**
     * Deserialize a {@link RealVector} field in a class.
     * <p>
     * This method is intended to be called from within a private
     * <code>readObject</code> method (after a call to
     * <code>ois.defaultReadObject()</code>) in a class that has a
     * {@link RealVector} field, which should be declared <code>transient</code>.
     * This way, the default handling does not deserialize the vector (the {@link
     * RealVector} interface is not serializable by default) but this method does
     * deserialize it specifically.
     * </p>
     *
     * @param instance  instance in which the field must be set up
     * @param fieldName name of the field within the class (may be private and final)
     * @param ois       stream from which the real vector should be read
     * @throws ClassNotFoundException if a class in the stream cannot be found
     * @throws IOException            if object cannot be read from the stream
     * @see #serializeRealVector(RealVector, ObjectOutputStream)
     */
    public static void deserializeRealVector(final Object instance,
                                             final String fieldName,
                                             final ObjectInputStream ois)
                                                                          throws ClassNotFoundException, IOException {
        try {

            // read the vector data
            final int n = ois.readInt();
            final double[] data = new double[n];
            for (int i = 0; i < n; ++i) {
                data[i] = ois.readDouble();
            }

            // create the instance
            final RealVector vector = new ArrayRealVector(data, false);

            // set up the field
            final java.lang.reflect.Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, vector);

        } catch (NoSuchFieldException nsfe) {
            IOException ioe = new IOException();
            ioe.initCause(nsfe);
            throw ioe;
        } catch (IllegalAccessException iae) {
            IOException ioe = new IOException();
            ioe.initCause(iae);
            throw ioe;
        }
    }

    /**
     * Serialize a {@link RealMatrix}.
     * <p>
     * This method is intended to be called from within a private
     * <code>writeObject</code> method (after a call to
     * <code>oos.defaultWriteObject()</code>) in a class that has a
     * {@link RealMatrix} field, which should be declared <code>transient</code>.
     * This way, the default handling does not serialize the matrix (the {@link
     * RealMatrix} interface is not serializable by default) but this method does
     * serialize it specifically.
     * </p>
     * <p>
     * The following example shows how a simple class with a name and a real matrix
     * should be written:
     *
     * <pre>
     * <code>
     * public class NamedMatrix implements Serializable {
     *
     *     private final String name;
     *     private final transient RealMatrix coefficients;
     *
     *     // omitted constructors, getters ...
     *
     *     private void writeObject(ObjectOutputStream oos) throws IOException {
     *         oos.defaultWriteObject();  // takes care of name field
     *         MatrixUtils.serializeRealMatrix(coefficients, oos);
     *     }
     *
     *     private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
     *         ois.defaultReadObject();  // takes care of name field
     *         MatrixUtils.deserializeRealMatrix(this, "coefficients", ois);
     *     }
     *
     * }
     * </code>
     * </pre>
     * </p>
     *
     * @param matrix real matrix to serialize
     * @param oos    stream where the real matrix should be written
     * @throws IOException if object cannot be written to stream
     * @see #deserializeRealMatrix(Object, String, ObjectInputStream)
     */
    public static void serializeRealMatrix(final RealMatrix matrix,
                                           final ObjectOutputStream oos)
                                                                         throws IOException {
        final int n = matrix.getRowDimension();
        final int m = matrix.getColumnDimension();
        oos.writeInt(n);
        oos.writeInt(m);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {
                oos.writeDouble(matrix.getEntry(i, j));
            }
        }
    }

    /**
     * Deserialize a {@link RealMatrix} field in a class.
     * <p>
     * This method is intended to be called from within a private
     * <code>readObject</code> method (after a call to
     * <code>ois.defaultReadObject()</code>) in a class that has a
     * {@link RealMatrix} field, which should be declared <code>transient</code>.
     * This way, the default handling does not deserialize the matrix (the {@link
     * RealMatrix} interface is not serializable by default) but this method does
     * deserialize it specifically.
     * </p>
     *
     * @param instance  instance in which the field must be set up
     * @param fieldName name of the field within the class (may be private and final)
     * @param ois       stream from which the real matrix should be read
     * @throws ClassNotFoundException if a class in the stream cannot be found
     * @throws IOException            if object cannot be read from the stream
     * @see #serializeRealMatrix(RealMatrix, ObjectOutputStream)
     */
    public static void deserializeRealMatrix(final Object instance,
                                             final String fieldName,
                                             final ObjectInputStream ois)
                                                                          throws ClassNotFoundException, IOException {
        try {

            // read the matrix data
            final int n = ois.readInt();
            final int m = ois.readInt();
            final double[][] data = new double[n][m];
            for (int i = 0; i < n; ++i) {
                final double[] dataI = data[i];
                for (int j = 0; j < m; ++j) {
                    dataI[j] = ois.readDouble();
                }
            }

            // create the instance
            final RealMatrix matrix = new Array2DRowRealMatrix(data, false);

            // set up the field
            final java.lang.reflect.Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, matrix);

        } catch (NoSuchFieldException nsfe) {
            IOException ioe = new IOException();
            ioe.initCause(nsfe);
            throw ioe;
        } catch (IllegalAccessException iae) {
            IOException ioe = new IOException();
            ioe.initCause(iae);
            throw ioe;
        }
    }
}
