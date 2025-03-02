public class SequentialMultiplier {

    public static Matrix multiply(Matrix a, Matrix b) {
        Matrix result = new Matrix(a.getRows(), b.getCols());
        multiply(a, 0, a.getRows(), b, result);
        return result;
    }

    public static void multiply(Matrix a, int rowStart, int rowFinish, Matrix b, Matrix result) {
        for (int i = rowStart; i < rowFinish; i++) {
            for (int j = 0; j < b.getCols(); j++) {
                int sum = 0;
                for (int k = 0; k < a.getCols(); k++) {
                    sum += a.data[i][k] * b.data[k][j];
                }
                result.data[i][j] = sum;
            }
        }
    }
}