import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {

        int size = 2000;
        Matrix a = new Matrix(size, size, 0);
        Matrix b = new Matrix(size, size, 1);


        long t1 = System.nanoTime();
        Matrix c1 = SequentialMultiplier.multiply(a, b);
        long t2 = System.nanoTime();
        Matrix c3 = StripeMultiplier.multiply(a, b, Runtime.getRuntime().availableProcessors());
        long t3 = System.nanoTime();

        Matrix c2 = new Matrix(a.getRows(), b.getCols());
        ForkJoinPool pool = new ForkJoinPool();
        pool.submit(new StripeForkJoinTask(a, 0, a.getRows(), b, c2,
            a.getRows() / Runtime.getRuntime().availableProcessors())).join();
        pool.shutdown();

        long t4 = System.nanoTime();

        if (!c1.equals(c2) || !c1.equals(c3)) {
            throw new RuntimeException("Matrix multiplication results are not correct");
        }

        System.out.printf("Sequential, s: %.3f \n", (t2 - t1) * 1e-9);
        System.out.printf("StripeMultiplier, s: %.3f \n", (t3 - t2) * 1e-9);
        System.out.printf("ForkJoin, s: %.3f \n", (t4 - t3) * 1e-9);


        System.out.printf("ForkJoin-Sequential Speedup: %.3f \n",
                (double)(t2 - t1) / (t4 - t3));
        System.out.printf("ForkJoin-StripeMultiplier Speedup: %.3f \n",
                (double)(t3 - t2) / (t4 - t3));
    }
}