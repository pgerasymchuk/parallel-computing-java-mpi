static void initialize_matrix(double* matrix, int rows, int cols, int seed) {
	srand(seed);
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			matrix[i * cols + j] = rand() / (double)(rand() + 1);
		}
	}
}

static void multiply_matrices(double* a, double* b, int row_a, int col_a, int col_b, double* c) {
	for (int i = 0; i < row_a; ++i) {
		for (int j = 0; j < col_b; ++j) {
			c[i * col_b + j] = 0;
			for (int k = 0; k < col_a; ++k) {
				c[i * col_b + j] += a[i * col_a + k] * b[k * col_b + j];
			}
		}
	}
}

static void print_matrix(double* matrix, int rows, int cols) {
	printf("\n********\n");
	for (int i = 0; i < rows; i++) {
		printf("\n");
		for (int j = 0; j < cols; j++) {
			printf("%6.2f ", matrix[i * cols + j]);
		}
	}
	printf("\n********\n");
}

static void print_matrix_checksum(double* matrix, int rows, int cols) {
	long double sum = 0.0;
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			sum = ((i + j) % 2) ? (sum + matrix[i * cols + j]) : (sum - matrix[i * cols + j]);
		}
	}
	printf("Matrix checksum: %Lf\n", sum);
}