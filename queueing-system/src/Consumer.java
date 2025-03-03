import java.util.Random;
import java.util.concurrent.BlockingQueue;

class Consumer implements Runnable {
    private final BlockingQueue<Task> queue;
    private final int id;
    private final int averageIntervalMs;

    private final Random random = new Random();

    public Consumer(BlockingQueue<Task> queue, int id, int averageIntervalMs) {
        this.queue = queue;
        this.id = id;
        this.averageIntervalMs = averageIntervalMs;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Task task = queue.take();
                int id = task.getId();
                long durationMs = Math.abs((long) (random.nextGaussian() * 5 + averageIntervalMs));
                Thread.sleep(durationMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
