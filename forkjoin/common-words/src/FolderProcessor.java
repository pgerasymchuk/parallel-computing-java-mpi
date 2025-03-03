import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import java.nio.file.Path;

class FolderProcessor extends RecursiveTask<Set<String>> {
    private final List<Path> files;

    public FolderProcessor(List<Path> files) {
        this.files = files;
    }

    @Override
    protected Set<String> compute() {
        if (files.size() == 1) {
            try {
                return new HashSet<>(new LineProcessor(Files.readAllLines(files.getFirst())).compute().keySet());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        int mid = files.size() / 2;
        FolderProcessor leftTask = new FolderProcessor(files.subList(0, mid));
        FolderProcessor rightTask = new FolderProcessor(files.subList(mid, files.size()));

        leftTask.fork();
        Set<String> rightResult = rightTask.compute();
        Set<String> leftResult = leftTask.join();

        leftResult.retainAll(rightResult);
        return leftResult;
    }
}