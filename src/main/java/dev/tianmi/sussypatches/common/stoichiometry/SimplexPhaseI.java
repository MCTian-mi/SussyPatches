package dev.tianmi.sussypatches.common.stoichiometry;

import java.util.*;

/**
 * Custom implementation of Phase I of the Simplex algorithm for feasibility checking.
 * This solver determines whether a system of linear constraints has any feasible solution.
 */
public class SimplexPhaseI {

    private static final double EPSILON = 1e-10;

    public enum ConstraintType {

        LEQ,  // <=
        EQ,   // =
        GEQ;   // >=

        public ConstraintType oppositeType() {
            return switch (this) {
                case LEQ -> GEQ;
                case EQ -> EQ;
                case GEQ -> LEQ;
            };
        }
    }

    /**
     * Represents a linear constraint: sum(coefficients[i] * x[i]) {type} rhs
     */
    public static class LinearConstraint {

        final double[] coefficients;
        final ConstraintType type;
        final double rhs;

        public LinearConstraint(double[] coefficients, ConstraintType type, double rhs) {
            this.coefficients = coefficients.clone();
            this.type = type;
            this.rhs = rhs;
        }
    }

    /**
     * Checks if a system of linear constraints is feasible.
     * All variables are assumed to have non-negativity constraints (x >= 0).
     *
     * @param constraints  List of constraints to check
     * @param numVariables Number of decision variables
     * @return true if the system is feasible, false otherwise
     */
    public static boolean isFeasible(List<LinearConstraint> constraints, int numVariables) {
        if (constraints.isEmpty()) {
            return true;
        }

        // Check if any artificial variables are needed
        boolean needsPhaseI = false;
        for (LinearConstraint c : constraints) {
            ConstraintType effectiveType = c.type;

            // Account for RHS normalization
            if (c.rhs < -EPSILON) {
                if (c.type == ConstraintType.LEQ) {
                    effectiveType = ConstraintType.GEQ;
                } else if (c.type == ConstraintType.GEQ) {
                    effectiveType = ConstraintType.LEQ;
                }
            }

            if (effectiveType == ConstraintType.EQ || effectiveType == ConstraintType.GEQ) {
                needsPhaseI = true;
                break;
            }
        }

        // If only LEQ constraints (after normalization) with non-negative RHS, automatically feasible
        if (!needsPhaseI) {
            return true;
        }

        // Build Phase I problem
        SimplexTableau tableau = buildPhaseITableau(constraints, numVariables);

        // Solve Phase I
        return solvePhaseI(tableau);
    }

    /**
     * Builds the Phase I simplex tableau.
     * Phase I minimizes the sum of artificial variables.
     */
    private static SimplexTableau buildPhaseITableau(List<LinearConstraint> constraints, int numVariables) {
        int numConstraints = constraints.size();

        // Handle empty constraint case
        if (numConstraints == 0) {
            double[][] table = new double[1][numVariables + 1];
            return new SimplexTableau(table, new int[0], numVariables, 0);
        }

        int numSlackVars = 0;
        int numArtificialVars = 0;

        // Count slack and artificial variables needed (after RHS normalization)
        for (LinearConstraint c : constraints) {
            ConstraintType effectiveType = c.type;

            // If RHS is negative, the constraint type will be flipped
            if (c.rhs < -EPSILON) {
                if (c.type == ConstraintType.LEQ) {
                    effectiveType = ConstraintType.GEQ;
                } else if (c.type == ConstraintType.GEQ) {
                    effectiveType = ConstraintType.LEQ;
                }
                // EQ stays EQ
            }

            if (effectiveType == ConstraintType.LEQ) {
                numSlackVars++;
            } else if (effectiveType == ConstraintType.GEQ) {
                numSlackVars++; // surplus variable
                numArtificialVars++;
            } else { // EQ
                numArtificialVars++;
            }
        }

        // Tableau dimensions:
        // Columns: original vars + slack/surplus vars + artificial vars + RHS
        // Rows: constraints + objective function
        int totalVars = numVariables + numSlackVars + numArtificialVars;
        int numRows = numConstraints + 1; // +1 for objective
        int numCols = totalVars + 1; // +1 for RHS

        double[][] table = new double[numRows][numCols];
        int[] basis = new int[numConstraints];

        int currentSlackIndex = numVariables;
        int currentArtificialIndex = numVariables + numSlackVars;

        // Build constraint rows
        for (int i = 0; i < numConstraints; i++) {
            LinearConstraint c = constraints.get(i);

            // Normalize constraint to have non-negative RHS
            double rhsValue = c.rhs;
            double coeffMultiplier = 1.0;
            ConstraintType normalizedType = c.type;

            if (rhsValue < -EPSILON) {
                // Negative RHS: multiply constraint by -1 and flip inequality
                rhsValue = -rhsValue;
                coeffMultiplier = -1.0;
                if (c.type == ConstraintType.LEQ) {
                    normalizedType = ConstraintType.GEQ;
                } else if (c.type == ConstraintType.GEQ) {
                    normalizedType = ConstraintType.LEQ;
                }
                // EQ stays EQ
            }

            // Copy original variable coefficients (with multiplier)
            for (int j = 0; j < numVariables; j++) {
                if (j < c.coefficients.length) {
                    table[i + 1][j] = c.coefficients[j] * coeffMultiplier;
                } else {
                    table[i + 1][j] = 0.0;
                }
            }

            // Add slack/surplus/artificial variables based on normalized constraint type
            if (normalizedType == ConstraintType.LEQ) {
                // Add slack variable
                table[i + 1][currentSlackIndex] = 1.0;
                basis[i] = currentSlackIndex;
                currentSlackIndex++;
            } else if (normalizedType == ConstraintType.GEQ) {
                // Subtract surplus, add artificial
                table[i + 1][currentSlackIndex] = -1.0;
                table[i + 1][currentArtificialIndex] = 1.0;
                basis[i] = currentArtificialIndex;

                // Phase I objective: minimize sum of artificial variables
                table[0][currentArtificialIndex] = 1.0;

                currentSlackIndex++;
                currentArtificialIndex++;
            } else { // EQ
                // Add artificial variable
                table[i + 1][currentArtificialIndex] = 1.0;
                basis[i] = currentArtificialIndex;

                // Phase I objective: minimize sum of artificial variables
                table[0][currentArtificialIndex] = 1.0;

                currentArtificialIndex++;
            }

            // RHS (now guaranteed non-negative)
            table[i + 1][numCols - 1] = rhsValue;
        }

        // Initialize Phase I objective row by eliminating basic variables
        // This ensures the objective row is correct w.r.t. the current basis
        for (int i = 0; i < numConstraints; i++) {
            if (basis[i] >= numVariables + numSlackVars) {
                // This is an artificial variable in the basis
                // Subtract this row from objective to eliminate it
                for (int j = 0; j < numCols; j++) {
                    table[0][j] -= table[i + 1][j];
                }
            }
        }

        return new SimplexTableau(table, basis, numVariables, numConstraints);
    }

    /**
     * Solves Phase I using the simplex algorithm.
     * Returns true if optimal objective value is 0 (feasible), false otherwise.
     */
    private static boolean solvePhaseI(SimplexTableau tableau) {
        int maxIterations = 1000;
        int iteration = 0;

        while (iteration < maxIterations) {
            // Find entering variable (most negative coefficient in objective row)
            int enteringVar = findEnteringVariable(tableau);

            if (enteringVar == -1) {
                // Optimal solution found
                break;
            }

            // Find leaving variable (minimum ratio test)
            int leavingVar = findLeavingVariable(tableau, enteringVar);

            if (leavingVar == -1) {
                // Unbounded (shouldn't happen in Phase I)
                return false;
            }

            // Pivot
            pivot(tableau, leavingVar, enteringVar);

            iteration++;
        }

        if (iteration >= maxIterations) {
            // Failed to converge
            return false;
        }

        // Check if optimal value is 0 (within tolerance)
        double objectiveValue = tableau.table[0][tableau.table[0].length - 1];
        return Math.abs(objectiveValue) < EPSILON;
    }

    /**
     * Finds the entering variable (most negative coefficient in objective row).
     * Returns -1 if no improving variable exists (optimal).
     */
    private static int findEnteringVariable(SimplexTableau tableau) {
        int numCols = tableau.table[0].length - 1; // Exclude RHS
        int entering = -1;
        double minCoeff = -EPSILON; // Only consider significantly negative

        for (int j = 0; j < numCols; j++) {
            if (tableau.table[0][j] < minCoeff) {
                minCoeff = tableau.table[0][j];
                entering = j;
            }
        }

        return entering;
    }

    /**
     * Finds the leaving variable using minimum ratio test.
     * Returns -1 if problem is unbounded.
     */
    private static int findLeavingVariable(SimplexTableau tableau, int enteringVar) {
        int numRows = tableau.table.length;
        int rhsCol = tableau.table[0].length - 1;
        int leaving = -1;
        double minRatio = Double.POSITIVE_INFINITY;

        for (int i = 1; i < numRows; i++) {
            double coeff = tableau.table[i][enteringVar];

            if (coeff > EPSILON) { // Only consider positive coefficients
                double rhs = tableau.table[i][rhsCol];
                double ratio = rhs / coeff;

                if (ratio < minRatio - EPSILON) {
                    minRatio = ratio;
                    leaving = i - 1; // Convert to basis index
                }
            }
        }

        return leaving;
    }

    /**
     * Performs a pivot operation, making enteringVar basic and leavingVar non-basic.
     */
    private static void pivot(SimplexTableau tableau, int leavingRow, int enteringCol) {
        int numRows = tableau.table.length;
        int numCols = tableau.table[0].length;
        int pivotRow = leavingRow + 1; // +1 because row 0 is objective

        // Update basis
        tableau.basis[leavingRow] = enteringCol;

        // Get pivot element
        double pivotElement = tableau.table[pivotRow][enteringCol];

        // Divide pivot row by pivot element
        for (int j = 0; j < numCols; j++) {
            tableau.table[pivotRow][j] /= pivotElement;
        }

        // Eliminate pivot column in all other rows
        for (int i = 0; i < numRows; i++) {
            if (i != pivotRow) {
                double factor = tableau.table[i][enteringCol];
                for (int j = 0; j < numCols; j++) {
                    tableau.table[i][j] -= factor * tableau.table[pivotRow][j];
                }
            }
        }
    }

    /**
     * Internal representation of the simplex tableau.
     */
    private static class SimplexTableau {

        final double[][] table;
        final int[] basis; // Index of basic variable for each constraint
        final int numOriginalVars;
        final int numConstraints;

        SimplexTableau(double[][] table, int[] basis, int numOriginalVars, int numConstraints) {
            this.table = table;
            this.basis = basis;
            this.numOriginalVars = numOriginalVars;
            this.numConstraints = numConstraints;
        }
    }
}
