import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

class Producer implements Callable<Statistics> {
    private final BlockingQueue<Task> queue;
    private final int taskLimit;
    private final int averageIntervalMs;

    private final Random random = new Random();
    private int rejectedCount = 0;
    private int completedCount = 0;

    public Producer(BlockingQueue<Task> queue, int taskLimit, int averageIntervalMs) {
        this.queue = queue;
        this.taskLimit = taskLimit;
        this.averageIntervalMs = averageIntervalMs;
    }

    @Override
    public Statistics call() {
        for (int i = 0; i < taskLimit; i++) {
            try {
                Thread.sleep(random.nextInt(2 * averageIntervalMs));
                Task task = new Task(i);
                if (queue.offer(task)) {
                    completedCount++;
                }
                else {
                    rejectedCount++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return new Statistics(completedCount, rejectedCount, 0);
    }
}