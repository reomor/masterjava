package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int[][] matrixBTransposed = new int[matrixSize][matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            for (int k = 0; k < matrixSize; k++) {
                matrixBTransposed[k][j] = matrixB[j][k];
            }
        }

        class ThreadResult {
            final int rowIndex;
            final int[] row;

            public ThreadResult(int rowIndex, int[] row) {
                this.rowIndex = rowIndex;
                this.row = row;
            }
        }

        final CompletionService<ThreadResult> completionService = new ExecutorCompletionService<>(executor);

        List<Future<ThreadResult>> submits = new ArrayList<>();

        for (int i = 0; i < matrixSize; i++) {
            final int currentRow = i;
            final int[] row = matrixA[i];

            final Future<ThreadResult> submit = completionService.submit(() -> {
                int[] cRow = new int[matrixSize];

                for (int i1 = 0; i1 < matrixSize; i1++) {
                    cRow[i1] = 0;
                    for (int j = 0; j < matrixSize; j++) {
                        cRow[i1] += row[j] * matrixBTransposed[i1][j];
                    }
                }

                return new ThreadResult(currentRow, cRow);
            });
            submits.add(submit);
        }

        while (!submits.isEmpty()) {

            final Future<ThreadResult> submit = completionService.poll(10, TimeUnit.SECONDS);

            if (submit == null) {
                throw new InterruptedException("submit is null");
            }
            submits.remove(submit);
            final ThreadResult threadResult = submit.get();
            matrixC[threadResult.rowIndex] = threadResult.row;
        }

        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
