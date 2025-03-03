import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        String directoryPath = "data/";

        try (Stream<Path> filesStream = Files.list(Paths.get(directoryPath))) {
            Set<String> commonWords;
            List<Path> files = filesStream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            if (files.isEmpty()) {
                throw new IOException("No files found in " + directoryPath);
            }

            ForkJoinPool pool = new ForkJoinPool();
            FolderProcessor task = new FolderProcessor(files);
            commonWords = pool.invoke(task);
            pool.shutdown();

            System.out.println("Common words across all documents: " + commonWords);
        } catch (IOException e) {
            System.out.println("Error occurred while reading files!");
            e.printStackTrace();
        }
    }
}

