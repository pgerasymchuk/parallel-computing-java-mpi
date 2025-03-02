import java.util.List;
import java.util.concurrent.RecursiveTask;

public class ForkJoinWordLengthCounter extends RecursiveTask<List<Integer>> {

    public static final int THRESHOLD = 1000;

    private final List<String> lines;

    public ForkJoinWordLengthCounter(List<String> lines) {
        this.lines = lines;
    }

    @Override
    protected List<Integer> compute() {
        if (lines.size() < THRESHOLD) {
            return SequentialWordLengthCounter.compute(lines);
        } else {
            ForkJoinWordLengthCounter counter1 =
                new ForkJoinWordLengthCounter(lines.subList(0, lines.size() / 2));
            ForkJoinWordLengthCounter counter2 =
                new ForkJoinWordLengthCounter(lines.subList(lines.size() / 2, lines.size()));

            invokeAll(counter1, counter2);

            List<Integer> result = counter1.join();
            result.addAll(counter2.join());
            return result;
        }
    }
}
