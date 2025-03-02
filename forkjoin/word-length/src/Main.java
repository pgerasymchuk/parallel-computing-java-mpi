import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        String filePath = "text.txt";

        long t0 = System.nanoTime();
        List<String> lines = FileParser.getFileLines(filePath);
        long loadTime = System.nanoTime() - t0;

        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        // warm-up run, so JIT compilation is not counted in the measured times
        pool.submit(new ForkJoinWordLengthCounter(lines)).join();
        SequentialWordLengthCounter.compute(lines);

        long t1 = System.nanoTime();
        List<Integer> wordCounts;
        wordCounts = pool.submit(new ForkJoinWordLengthCounter(lines)).join();
        long t2 = System.nanoTime();
        List<Integer> sequentialWordCounts = SequentialWordLengthCounter.compute(lines);
        long t3 = System.nanoTime();
        pool.shutdown();

        double mean =  wordCounts.stream().mapToDouble(a -> a).average().orElse(0.0);
        double variance = wordCounts.stream()
                                    .mapToDouble(a -> Math.pow(a - mean, 2))
                                    .average()
                                    .orElse(0.0);
        double standardDeviation = Math.sqrt(variance);
        Integer minLength = wordCounts.stream().min(Integer::compareTo).orElse(null);
        Integer maxLength = wordCounts.stream().max(Integer::compareTo).orElse(null);

        long t4 = System.nanoTime();

        if (!wordCounts.equals(sequentialWordCounts)) {
            throw new RuntimeException("Incorrect result obtained!");
        }

        System.out.println("Mean word length: " + mean);
        System.out.println("Min word length: " + minLength);
        System.out.println("Max word length: " + maxLength);
        System.out.println("Variance: " + variance);
        System.out.println("Standard deviation: " + standardDeviation);

        System.out.println("File loading time, ms: " + 1e-6 * loadTime);
        System.out.println("ForkJoinPool algorithm time, ms: " + 1e-6 * (t2 - t1));
        System.out.println("Sequential algorithm time, ms: " + 1e-6 * (t3 - t2));
        System.out.println("Characteristics calculation time, ms: " + 1e-6 * (t4 - t3));
        System.out.println("Speedup: " + (double)(t3 - t2) / (t2 - t1));
    }
}