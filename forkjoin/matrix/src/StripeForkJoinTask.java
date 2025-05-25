import java.util.concurrent.RecursiveAction;

public class StripeForkJoinTask extends RecursiveAction {
    private final Matrix a, b, result;
    private final int rowStart, rowFinish, threshold;

    public StripeForkJoinTask(Matrix a, int rowStart, int rowFinish, Matrix b, Matrix result, int threshold) {
        this.a = a;
        this.b = b;
        this.result = result;
        this.rowStart = rowStart;
        this.rowFinish = rowFinish;
        this.threshold = threshold;
    }

    @Override
    protected void compute() {
        int rowsToProcess = rowFinish - rowStart;

        if (rowsToProcess < threshold || rowsToProcess <= 1) {
            SequentialMultiplier.multiply(a, rowStart, rowFinish, b, result);
        }
        else {
            int rowMiddle = rowStart + rowsToProcess / 2;
            StripeForkJoinTask task1 = new StripeForkJoinTask(a, rowStart, rowMiddle, b, result, threshold);
            StripeForkJoinTask task2 = new StripeForkJoinTask(a, rowMiddle, rowFinish, b, result, threshold);
            invokeAll(task1, task2);
        }
    }
}
