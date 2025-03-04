import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        final int NUM_SYSTEMS = 20;
        final int QUEUE_CAPACITY = 100;
        final int NUM_CONSUMERS = 3;
        final int TASK_LIMIT = 1000;
        final int AVERAGE_PRODUCER_INTERVAL_MS = 2;
        final int AVERAGE_CONSUMER_INTERVAL_MS = 10;
        final int QUEUE_MONITOR_INTERVAL_MS = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(NUM_SYSTEMS);

        List<Future<Statistics>> systemsResults = new ArrayList<>();
        for (int systemId = 0; systemId < NUM_SYSTEMS; systemId++) {
            systemsResults.add(executor.submit(() -> {
                BlockingQueue<Task> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

                ExecutorService consumerService = Executors.newFixedThreadPool(NUM_CONSUMERS);
                for (int i = 0; i < NUM_CONSUMERS; i++) {
                    consumerService.submit(new Consumer(queue, i, AVERAGE_CONSUMER_INTERVAL_MS));
                    Thread.sleep(1);
                }

                ExecutorService producerService = Executors.newSingleThreadExecutor();
                Future<Statistics> stats = producerService.submit(
                        new Producer(queue, TASK_LIMIT, AVERAGE_PRODUCER_INTERVAL_MS));

                ScheduledExecutorService monitorService = Executors.newSingleThreadScheduledExecutor();
                QueueMonitor queueMonitor = new QueueMonitor(queue);
                monitorService.scheduleAtFixedRate(queueMonitor, 0, QUEUE_MONITOR_INTERVAL_MS, TimeUnit.MILLISECONDS);

                try {
                    producerService.shutdown();
                    Thread.sleep(5000);
                    consumerService.shutdownNow();
                    monitorService.shutdownNow();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new Statistics(stats.get().completedTasks, stats.get().rejectedTasks, queueMonitor.getAverageQueueLength());
            }));

        }
        executor.shutdown();

        List<Statistics> systemStatistics = new ArrayList<>();
        try {
            for (Future<Statistics> future : systemsResults) {
                systemStatistics.add(future.get());
            }
        } catch (ExecutionException | InterruptedException e) { throw new RuntimeException(e); }

        Statistics averageStatistics = Statistics.calculateAverage(systemStatistics);
        System.out.println("Average statistics for all queueing systems:");
        averageStatistics.print();
    }
}