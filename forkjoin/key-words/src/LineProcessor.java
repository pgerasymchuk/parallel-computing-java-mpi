import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

class LineProcessor extends RecursiveTask<Map<String, Integer>> {
    public static final int THRESHOLD = 10;
    private final List<String> lines;
    private final Set<String> keywords;

    public LineProcessor(List<String> lines, Set<String> keywords) {
        this.lines = lines;
        this.keywords = keywords;
    }

    @Override
    protected Map<String, Integer> compute() {
        if (lines.size() <= THRESHOLD) {
            return processLines(lines);
        }

        int mid = lines.size() / 2;
        LineProcessor task1 = new LineProcessor(lines.subList(0, mid), keywords);
        LineProcessor task2 = new LineProcessor(lines.subList(mid, lines.size()), keywords);

        invokeAll(task1, task2);
        Map<String, Integer> map1 = task1.join();
        Map<String, Integer> map2 = task2.join();
        Map<String, Integer> result = new HashMap<>(map1);
        map2.forEach((k, v) -> result.put(k, result.getOrDefault(k, 0) + v));
        return result;
    }

    private Map<String, Integer> processLines(List<String> lines) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String line : lines) {
            // split on anything that is not a letter or digit, so "Java," and "(thread)" are counted too
            String[] words = line.toLowerCase().split("[^\\p{L}\\p{N}]+");
            for (String word : words) {
                if (keywords.contains(word)) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }
        return wordCount;
    }
}

