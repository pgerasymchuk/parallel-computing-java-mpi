import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

class QueueMonitor implements Runnable {
    private final BlockingQueue<Task> queue;
    private final List<Integer> queueSizes = new ArrayList<>();

    public QueueMonitor(BlockingQueue<Task> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        int currentQueueSize = queue.size();
        queueSizes.add(currentQueueSize);
        System.out.println("Current queue size: " + currentQueueSize);
    }

    public Double getAverageQueueLength(){
        return queueSizes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
}
