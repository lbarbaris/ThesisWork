package utils.network;

public class PolynomialFit {
    private final int degree;
    private final double[] coefficients;

    public PolynomialFit(double[] x, double[] y, int degree) {
        this.degree = degree;
        this.coefficients = fitPolynomial(x, y, degree);
    }

    public double predict(double x) {
        double result = 0.0;
        for (int i = 0; i <= degree; i++) {
            result += coefficients[i] * Math.pow(x, i);
        }
        return result;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    private double[] fitPolynomial(double[] x, double[] y, int degree) {
        int n = degree + 1;
        double[][] A = new double[n][n];
        double[] B = new double[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = 0.0;
                for (double v : x) {
                    A[i][j] += Math.pow(v, i + j);
                }
            }

            B[i] = 0.0;
            for (int k = 0; k < x.length; k++) {
                B[i] += y[k] * Math.pow(x[k], i);
            }
        }

        return gaussianElimination(A, B);
    }

    private double[] gaussianElimination(double[][] A, double[] B) {
        int n = B.length;
        for (int p = 0; p < n; p++) {
            // Find pivot
            int max = p;
            for (int i = p + 1; i < n; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }

            // Swap rows
            double[] temp = A[p];
            A[p] = A[max];
            A[max] = temp;

            double t = B[p];
            B[p] = B[max];
            B[max] = t;

            // Eliminate
            for (int i = p + 1; i < n; i++) {
                double alpha = A[i][p] / A[p][p];
                B[i] -= alpha * B[p];
                for (int j = p; j < n; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }

        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = B[i];
            for (int j = i + 1; j < n; j++) {
                sum -= A[i][j] * x[j];
            }
            x[i] = sum / A[i][i];
        }

        return x;
    }
}