package utils.network;

import java.util.Arrays;

public class PadeApproximator {
    private final double[] numeratorCoeffs;
    private final double[] denominatorCoeffs;

    public PadeApproximator(double[] t, double[] y) {
        int maxOrder = Math.min(5, t.length - 1); // безопасность

        int bestM = 1, bestN = 1;
        double bestError = Double.MAX_VALUE;
        double[] bestNum = null, bestDen = null;

        // Перебор всех m + n <= maxOrder
        for (int m = 1; m <= maxOrder; m++) {
            for (int n = 1; n <= maxOrder - m; n++) {
                try {
                    double[][] A = new double[t.length][m + n];
                    double[] B = new double[t.length];

                    for (int i = 0; i < t.length; i++) {
                        double ti = t[i];
                        double yi = y[i];

                        for (int j = 0; j < m; j++) {
                            A[i][j] = Math.pow(ti, j); // numerator part
                        }
                        for (int j = 1; j <= n; j++) {
                            A[i][m + j - 1] = -yi * Math.pow(ti, j); // denominator part
                        }

                        B[i] = yi;
                    }

                    double[] solution = solveLeastSquares(A, B);

                    double[] num = Arrays.copyOfRange(solution, 0, m);
                    double[] den = new double[n + 1];
                    den[0] = 1.0;
                    System.arraycopy(solution, m, den, 1, n);

                    double error = 0.0;
                    for (int i = 0; i < t.length; i++) {
                        double pred = eval(num, den, t[i]);
                        error += Math.pow(pred - y[i], 2);
                    }

                    if (error < bestError) {
                        bestError = error;
                        bestM = m;
                        bestN = n;
                        bestNum = num;
                        bestDen = den;
                    }
                } catch (Exception e) {
                    // Случай вырожденной матрицы — пропускаем
                }
            }
        }

        numeratorCoeffs = bestNum;
        denominatorCoeffs = bestDen;
    }

    private double eval(double[] num, double[] den, double t) {
        double numerator = 0.0;
        for (int i = num.length - 1; i >= 0; i--) {
            numerator = numerator * t + num[i];
        }

        double denominator = 0.0;
        for (int i = den.length - 1; i >= 0; i--) {
            denominator = denominator * t + den[i];
        }

        if (Math.abs(denominator) < 1e-6) denominator = 1e-6;
        return numerator / denominator;
    }

    public double predict(double t) {
        return eval(numeratorCoeffs, denominatorCoeffs, t);
    }

    private double[] solveLeastSquares(double[][] A, double[] B) {
        int rows = A.length;
        int cols = A[0].length;
        double[][] At = new double[cols][rows];

        // Transpose A
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                At[j][i] = A[i][j];

        // Multiply: At * A
        double[][] AtA = new double[cols][cols];
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < cols; j++)
                for (int k = 0; k < rows; k++)
                    AtA[i][j] += At[i][k] * A[k][j];

        // Multiply: At * B
        double[] AtB = new double[cols];
        for (int i = 0; i < cols; i++)
            for (int k = 0; k < rows; k++)
                AtB[i] += At[i][k] * B[k];

        return gaussianElimination(AtA, AtB);
    }

    private double[] gaussianElimination(double[][] A, double[] B) {
        int n = B.length;
        for (int p = 0; p < n; p++) {
            int max = p;
            for (int i = p + 1; i < n; i++)
                if (Math.abs(A[i][p]) > Math.abs(A[max][p]))
                    max = i;

            double[] temp = A[p]; A[p] = A[max]; A[max] = temp;
            double t = B[p]; B[p] = B[max]; B[max] = t;

            for (int i = p + 1; i < n; i++) {
                double alpha = A[i][p] / A[p][p];
                B[i] -= alpha * B[p];
                for (int j = p; j < n; j++)
                    A[i][j] -= alpha * A[p][j];
            }
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = B[i];
            for (int j = i + 1; j < n; j++)
                sum -= A[i][j] * x[j];
            x[i] = sum / A[i][i];
        }

        return x;
    }
}