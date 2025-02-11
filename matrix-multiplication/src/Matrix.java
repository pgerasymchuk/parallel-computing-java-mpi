import java.util.Random;

public class Matrix {
    public final int[][] data;

    public Matrix(int rows, int cols) {
        data = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = 0;
            }
        }
    }

    public Matrix(int rows, int cols, int seed) {
        Random rand = new Random(seed);
        data = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = rand.nextInt();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix other = (Matrix) obj;
        if (this.getRows() != other.getRows() ||
            this.getCols() != other.getCols())
        {
            return false;
        }
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                if (data[i][j] != other.data[i][j]) return false;
            }
        }
        return true;
    }

    public int getRows() { return data.length; }
    public int getCols() { return data[0].length; }
}
