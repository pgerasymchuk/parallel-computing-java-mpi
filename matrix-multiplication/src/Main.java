public class Main {
    public static void main(String[] args) {
        int[] sizes = new int[] {128, 512, 1024, 2048};
        int[] numthreads = new int[] {4, 16, 64};

        System.out.println("Size \tThreads \tSeq.(s) \tStripe(s) \tFox(s)");
        for (int size : sizes) {
            for (int nthreads : numthreads) {
                test(size, nthreads);
            }
        }
    }

    public static void test(int size, int nthreads){
        MatrixMultiplier sequential = new SequentialMultiplier();
        MatrixMultiplier stripe = new StripeMultiplier(nthreads);
        MatrixMultiplier fox = new FoxMultiplier(nthreads);

        Matrix a = new Matrix(size, size, 0);
        Matrix b = new Matrix(size, size, 1);

        long t1 = System.nanoTime();
        Matrix c1 = sequential.multiply(a, b);
        long t2 = System.nanoTime();
        Matrix c2 = stripe.multiply(a, b);
        long t3 = System.nanoTime();
        Matrix c3 = fox.multiply(a, b);
        long t4 = System.nanoTime();

        System.out.printf("%d \t%d \t\t\t", size, nthreads);
        System.out.printf("%.3f \t\t", (t2 - t1) * 1e-9);
        System.out.printf("%.3f \t\t", (t3 - t2) * 1e-9);
        System.out.printf("%.3f \t\t\n", (t4 - t3) * 1e-9);

        double stripeSpeedup = ((double)(t2 - t1)) / (t3 - t2);
        System.out.printf("\t\t\t\t\tSpeedup \t%.3f \t\t", stripeSpeedup);

        double foxSpeedup = ((double)(t2 - t1)) / (t4 - t3);
        System.out.printf("%.3f \n\n", foxSpeedup);

        if (!c1.equals(c2) || !c1.equals(c3)) {
            throw new RuntimeException("Matrix multiplication results are not correct");
        }
    }
}