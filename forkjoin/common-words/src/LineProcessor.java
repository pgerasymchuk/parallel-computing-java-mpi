import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.List;
import java.util.HashMap;

class LineProcessor extends RecursiveTask<Map<String, Integer>> {
    private final List<String> lines;
    private final int THRESHOLD = 10;

    public LineProcessor(List<String> lines) {
        this.lines = lines;
    }

    @Override
    protected Map<String, Integer> compute() {
        if (lines.size() < THRESHOLD) {
            return processLines(lines);
        }

        int mid = lines.size() / 2;
        LineProcessor leftTask = new LineProcessor(lines.subList(0, mid));
        LineProcessor rightTask = new LineProcessor(lines.subList(mid, lines.size()));

        leftTask.fork();
        Map<String, Integer> rightResult = rightTask.compute();
        Map<String, Integer> leftResult = leftTask.join();

        return mergeWordCounts(leftResult, rightResult);
    }

    private Map<String, Integer> processLines(List<String> lines) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String line : lines) {
            String[] words = line.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }
        return wordCount;
    }

    private static Map<String, Integer> mergeWordCounts(Map<String, Integer> map1, Map<String, Integer> map2) {
        map2.forEach((word, count) -> map1.merge(word, count, Integer::sum));
        return map1;
    }
}