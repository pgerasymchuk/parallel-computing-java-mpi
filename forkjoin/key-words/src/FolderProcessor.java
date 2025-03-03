import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.RecursiveTask;

class FolderProcessor extends RecursiveTask<Map<Path, Map<String, Integer>>> {
    private final List<Path> files;
    private final Set<String> keywords;

    public FolderProcessor(List<Path> files, Set<String> keywords) {
        this.files = files;
        this.keywords = keywords;
    }

    @Override
    protected Map<Path, Map<String, Integer>> compute() {
        if (files.size() == 1) {
            try {
                Map<String, Integer> wordCounts = new LineProcessor(
                        Files.readAllLines(files.getFirst()), keywords).compute();

                return wordCounts.isEmpty() ?
                        new HashMap<>() : new HashMap<>(Map.of(files.getFirst(), wordCounts));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            int mid = files.size() / 2;
            FolderProcessor task1 = new FolderProcessor(files.subList(0, mid), keywords);
            FolderProcessor task2 = new FolderProcessor(files.subList(mid, files.size()), keywords);

            invokeAll(task1, task2);
            Map<Path, Map<String, Integer>> leftResult = task1.join();
            Map<Path, Map<String, Integer>> rightResult = task2.join();

            leftResult.putAll(rightResult);
            return leftResult;
        }
    }
}
