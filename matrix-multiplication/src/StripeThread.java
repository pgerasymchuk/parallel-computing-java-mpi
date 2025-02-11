public class StripeThread extends Thread{
    private final Matrix a;
    public final int aRowStart;
    public final int aRowFinish;
    private final Matrix b;
    public final Matrix result;

    public StripeThread(Matrix a, int aRowStart, int aRowFinish, Matrix b) {
        this.a = a;
        this.aRowStart = aRowStart;
        this.aRowFinish = aRowFinish;
        if (a.getCols() != b.getRows()) {
            throw new IllegalArgumentException("Incorrect sizes of matrices!");
        }
        this.b = b;
        this.result = new Matrix(aRowFinish - aRowStart, b.getCols());
    }

    @Override
    public void run(){
        for(int row = aRowStart; row < aRowFinish; row++){
            for (int i = 0; i < a.getCols(); i++) {         // s
                for (int j = 0; j < b.getCols(); j++) {    //  m
                    result.data[row - aRowStart][j] += a.data[row][i] * b.data[i][j];
                }
            }
        }
    }
}
