#include <stdio.h> 
#include <stdlib.h>
#include "mpi.h" 
#include "../matrix_utils.c"

#define MASTER 0 	    // taskid of master
#define FROM_MASTER 1   // message type 
#define FROM_WORKER 2   // message type 

int main(int argc, char* argv[]) {
    int numtasks;
    int taskid;
    int numworkers;
    int rows; 		    // rows of matrix A sent to each worker
    int rows_per_worker;
    int extra;          // extra rows for some workers
    int offset;         // used to determine rows sent to each worker
    int rc = 1;         // error code

    double* a;
	double* b;
	double* c;

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

    MPI_Status status;
    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numtasks);
    MPI_Comm_rank(MPI_COMM_WORLD, &taskid);

    if (numtasks < 2) {
        printf("Need at least two MPI tasks. Quitting...\n");
        MPI_Abort(MPI_COMM_WORLD, rc);
        exit(1);
    }
    numworkers = numtasks - 1;

    /******** master task *****************/
    if (taskid == MASTER) {
        printf("MPI Matrix Multiplication: Blocking\n");
        //printf("mpi_mm has started with %d tasks.\n", numtasks);

        a = (double*)malloc(NRA * NCA * sizeof(double));
        b = (double*)malloc(NCA * NCB * sizeof(double));
        c = (double*)malloc(NRA * NCB * sizeof(double));

        initialize_matrix(a, NRA, NCA, 0);
        initialize_matrix(b, NCA, NCB, 1);

        double start_time = MPI_Wtime();

        rows_per_worker = NRA / numworkers;
        extra = NRA % numworkers;
        offset = 0;

        for (int dest = 1; dest <= numworkers; dest++) {
            rows = (dest <= extra) ? rows_per_worker + 1 : rows_per_worker;
            //printf("Sending %d rows to task %d offset=%d\n", rows, dest, offset);

            MPI_Send(&offset, 1, MPI_INT, dest, FROM_MASTER, MPI_COMM_WORLD);
            MPI_Send(&rows, 1, MPI_INT, dest, FROM_MASTER, MPI_COMM_WORLD);
            MPI_Send(&a[offset * NCA], rows * NCA, MPI_DOUBLE, dest, FROM_MASTER, MPI_COMM_WORLD);
            MPI_Send(b, NCA * NCB, MPI_DOUBLE, dest, FROM_MASTER, MPI_COMM_WORLD);

            offset = offset + rows;
        }

        for (int source = 1; source <= numworkers; source++) {
            MPI_Recv(&offset, 1, MPI_INT, source, FROM_WORKER, MPI_COMM_WORLD, &status);
            MPI_Recv(&rows, 1, MPI_INT, source, FROM_WORKER, MPI_COMM_WORLD, &status);
            MPI_Recv(&c[offset * NCB], rows * NCB, MPI_DOUBLE, source, FROM_WORKER, MPI_COMM_WORLD, &status);
            //printf("Received results from task %d\n", source);
        }

        printf("Execution time: %f seconds\n", MPI_Wtime() - start_time);
        print_matrix_checksum(c, NRA, NCB);
        
        free(a);
        free(b);
        free(c);
    }

    /******** worker task *****************/
    else {
        MPI_Recv(&offset, 1, MPI_INT, MASTER, FROM_MASTER, MPI_COMM_WORLD, &status);
        MPI_Recv(&rows, 1, MPI_INT, MASTER, FROM_MASTER, MPI_COMM_WORLD, &status);

        a = (double*)malloc(rows * NCA * sizeof(double));
        b = (double*)malloc(NCA * NCB * sizeof(double));
        c = (double*)malloc(rows * NCB * sizeof(double));

        MPI_Recv(a, rows * NCA, MPI_DOUBLE, MASTER, FROM_MASTER, MPI_COMM_WORLD, &status);
        MPI_Recv(b, NCA * NCB, MPI_DOUBLE, MASTER, FROM_MASTER, MPI_COMM_WORLD, &status);

        multiply_matrices(a, b, rows, NCA, NCB, c);

        MPI_Send(&offset, 1, MPI_INT, MASTER, FROM_WORKER, MPI_COMM_WORLD);
        MPI_Send(&rows, 1, MPI_INT, MASTER, FROM_WORKER, MPI_COMM_WORLD);
        MPI_Send(c, rows * NCB, MPI_DOUBLE, MASTER, FROM_WORKER, MPI_COMM_WORLD);

        free(a);
        free(b);
        free(c);
    }

    MPI_Finalize();
    return 0;
}