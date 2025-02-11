public class StripeMultiplier implements MatrixMultiplier {
    private final int nthreads;

    public StripeMultiplier(int nthreads) {
        this.nthreads = nthreads;
    }

    @Override
    public Matrix multiply(Matrix a, Matrix b) {
        if (a.getCols() != b.getRows()) {
            throw new IllegalArgumentException("Incorrect sizes of matrices!");
        }
        StripeThread[] threads = new StripeThread[nthreads];
        int rowsInStripe = a.getRows() / nthreads;
        int extraRows = a.getRows() % nthreads;    // first extraRows stripes get one more row

        int rowStart = 0;
        for (int i = 0; i < nthreads; i++) {
            int rowFinish = rowStart + rowsInStripe + (i < extraRows ? 1 : 0);
            threads[i] = new StripeThread(a, rowStart, rowFinish, b);
            threads[i].start();
            rowStart = rowFinish;
        }

        try {
            for (int i = 0; i < nthreads; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("StripeMultiplier interrupted!", e);
        }

        Matrix c = new Matrix(a.getRows(), b.getCols());
        for (StripeThread thread : threads) {
            for (int row = thread.aRowStart; row < thread.aRowFinish; row++) {
                c.data[row] = thread.result.data[row - thread.aRowStart];
            }
        }
        return c;
    }
}

