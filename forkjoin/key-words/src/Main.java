import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        String directoryPath = "data/";
        String keywordsPath = "data/keywords/keywords.txt";

        try (Stream<Path> filesStream = Files.list(Paths.get(directoryPath))) {
            List<Path> files = filesStream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            if (files.isEmpty()) {
                throw new IOException("No files found in " + directoryPath);
            }

            ForkJoinPool pool = new ForkJoinPool();
            FolderProcessor task = new FolderProcessor(files, loadKeywords(keywordsPath));
            Map<Path, Map<String, Integer>> result = pool.invoke(task);
            pool.shutdown();

            if (result.isEmpty()) {
                System.out.println("No matching documents found.");
            } else {
                System.out.println("Each keyword occurrences in each document:");
                result.forEach((path, wordCounts) -> {
                    System.out.println(path.getFileName());
                    wordCounts.forEach((word, count) -> System.out.println("   - " + word + ": " + count));
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> loadKeywords(String keywordsFilePath) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(keywordsFilePath))) {
            return stream.map(String::trim)
                         .filter(word -> !word.isEmpty())
                         .map(String::toLowerCase)
                         .collect(Collectors.toSet());
        }
    }
}
