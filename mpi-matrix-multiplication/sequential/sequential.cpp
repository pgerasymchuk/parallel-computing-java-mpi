#include <stdio.h> 
#include <stdlib.h>
#include "../matrix_utils.c"
#include <time.h>


int main(int argc, char* argv[]) {
    printf("Matrix Multiplication: Sequential\n");

    int NRA;
    int NCA;
    int NCB;
    if (argc == 4) {
        NRA = atoi(argv[1]);
        NCA = atoi(argv[2]);
        NCB = atoi(argv[3]);
    }
    else {
        NRA = NCA = NCB = 100;
    }

    double* a = (double*)malloc(NRA * NCA * sizeof(double));
    double* b = (double*)malloc(NCA * NCB * sizeof(double));
    double* c = (double*)malloc(NRA * NCB * sizeof(double));

    initialize_matrix(a, NRA, NCA, 0);
    initialize_matrix(b, NCA, NCB, 1);

    clock_t start_time = clock();

    multiply_matrices(a, b, NRA, NCA, NCB, c);

    printf("Execution time: %f seconds\n", (double)(clock() - start_time) / CLOCKS_PER_SEC);
    print_matrix_checksum(c, NRA, NCB);

    free(a);
    free(b);
    free(c);

    return 0;
}