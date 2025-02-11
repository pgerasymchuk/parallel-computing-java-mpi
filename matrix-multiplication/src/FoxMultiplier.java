public class FoxMultiplier implements MatrixMultiplier {
    private final int nthreads;

    public FoxMultiplier(int nthreads) {
        this.nthreads = nthreads;
    }

    @Override
    public Matrix multiply(Matrix a, Matrix b) {
        if (a.getRows() != a.getCols() ||
            b.getRows() != b.getCols() ||
            a.getCols() != b.getRows())
        {
            throw new IllegalArgumentException("Incorrect sizes of matrices!");
        }

        int q = (int) Math.round(Math.sqrt(nthreads));
        if (q * q != nthreads || a.getRows() % q != 0) {
            throw new IllegalArgumentException(
                    "Fox algorithm needs a square number of threads and a matrix size divisible by sqrt(threads)!");
        }
        int blockSize = a.getRows() / q;

        FoxThread[] threads = new FoxThread[nthreads];
        for (int i = 0; i < nthreads; i++) {
            threads[i] = new FoxThread(a, b, i / q, i % q, blockSize);
            threads[i].start();
        }

        try {
            for (int i = 0; i < nthreads; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            System.out.println("FoxMultiplier interrupted!");
        }

        Matrix result = new Matrix(a.getRows(), a.getCols());
        for (FoxThread thread : threads) {
            // row and col - coords inside each thread`s block
            for (int row = 0; row < blockSize; row++) {
                for (int col = 0; col < blockSize; col++){
                    result.data[thread.i * blockSize + row][thread.j * blockSize + col] =
                            thread.result.data[row][col];
                }
            }
        }
        return result;
    }
}


