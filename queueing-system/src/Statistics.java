import java.util.List;

public class Statistics {
    public int completedTasks;
    public int rejectedTasks;
    public double averageQueueLength;

    public Statistics(int completedTasks, int rejectedTasks, double averageQueueLength) {
        this.completedTasks = completedTasks;
        this.rejectedTasks = rejectedTasks;
        this.averageQueueLength = averageQueueLength;
    }

    public void print() {
        System.out.println("Completed Tasks: " + completedTasks);
        System.out.println("Rejected Tasks: " + rejectedTasks);
        System.out.println("Probability of rejection: " + (double)rejectedTasks / (completedTasks + rejectedTasks));
        System.out.println("Average Queue Length: " + averageQueueLength);
    }

    public static Statistics calculateAverage(List<Statistics> list){
        Statistics average = new Statistics(0, 0, 0);
        for(Statistics s : list){
            average.completedTasks += s.completedTasks;
            average.rejectedTasks += s.rejectedTasks;
            average.averageQueueLength += s.averageQueueLength;
        }

        average.completedTasks = average.completedTasks / list.size();
        average.rejectedTasks = average.rejectedTasks / list.size();
        average.averageQueueLength = average.averageQueueLength / list.size();

        return average;
    }
}
