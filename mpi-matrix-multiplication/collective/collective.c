#include <stdio.h> 
#include <stdlib.h>
#include "mpi.h" 
#include "../matrix_utils.c"

#define MASTER 0        

int main(int argc, char* argv[]) {
    int numtasks;
    int taskid;
    int rc = 1;
    int NRA;
    int NCA; 
    int NCB; 

    double* a = NULL;  
    double* b = NULL;   
    double* c = NULL;   

    double* local_a = NULL;
    double* local_c = NULL;

    int* sendcounts = NULL;  
    int* displs = NULL;      

    if (argc == 4) {
        NRA = atoi(argv[1]);
        NCA = atoi(argv[2]);
        NCB = atoi(argv[3]);
    }
    else {
        NRA = NCA = NCB = 100;
    }

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numtasks);
    MPI_Comm_rank(MPI_COMM_WORLD, &taskid);

    if (numtasks < 2) {
        if (taskid == MASTER) {
            printf("Need at least two MPI tasks. Quitting...\n");
        }
        MPI_Abort(MPI_COMM_WORLD, rc);
        exit(1);
    }

    sendcounts = (int*)malloc(numtasks * sizeof(int));
    displs = (int*)malloc(numtasks * sizeof(int));

    int rows_per_task = NRA / numtasks;
    int extra = NRA % numtasks;
    int offset = 0;
    
    for (int i = 0; i < numtasks; i++) {
        int rows = (i < extra) ? rows_per_task + 1 : rows_per_task;
        sendcounts[i] = rows * NCA;
        displs[i] = offset * NCA;
        offset += rows;
    }

    int local_rows = (taskid < extra) ? rows_per_task + 1 : rows_per_task;

    local_a = (double*)malloc(local_rows * NCA * sizeof(double));
    local_c = (double*)malloc(local_rows * NCB * sizeof(double));

    if (taskid == MASTER) {
        printf("MPI Matrix Multiplication: Using Collective Operations\n");
		printf("Started with %d tasks.\n", numtasks);

        a = (double*)malloc(NRA * NCA * sizeof(double));
        b = (double*)malloc(NCA * NCB * sizeof(double));
        c = (double*)malloc(NRA * NCB * sizeof(double));

        initialize_matrix(a, NRA, NCA, 0);
        initialize_matrix(b, NCA, NCB, 1);
    }

    double start_time = MPI_Wtime();

    MPI_Scatterv(a, sendcounts, displs, MPI_DOUBLE,
        local_a, local_rows * NCA, MPI_DOUBLE,
        MASTER, MPI_COMM_WORLD);

    b = (taskid == MASTER) ? b : (double*)malloc(NCA * NCB * sizeof(double));
    MPI_Bcast(b, NCA * NCB, MPI_DOUBLE, MASTER, MPI_COMM_WORLD);

    multiply_matrices(local_a, b, local_rows, NCA, NCB, local_c);

    for (int i = 0; i < numtasks; i++) {
        sendcounts[i] = sendcounts[i] / NCA * NCB;
        displs[i] = displs[i] / NCA * NCB;
    }

    MPI_Gatherv(local_c, local_rows * NCB, MPI_DOUBLE,
        c, sendcounts, displs, MPI_DOUBLE,
        MASTER, MPI_COMM_WORLD);

    if (taskid == MASTER) {
        printf("Execution time: %f seconds\n", MPI_Wtime() - start_time);
        print_matrix_checksum(c, NRA, NCB);
    }

    if (taskid == MASTER) {
        free(a);
        free(c);
    }
    free(b);
    free(local_a);
    free(local_c);
    free(sendcounts);
    free(displs);

    MPI_Finalize();
    return 0;
}