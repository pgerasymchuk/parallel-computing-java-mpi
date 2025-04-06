#include <stdio.h> 
#include <stdlib.h>
#include "mpi.h" 
#include "../matrix_utils.c"

#define MASTER 0 	     // taskid of master
#define FROM_MASTER 1    // message type 
#define FROM_WORKER 2    // message type 

int main(int argc, char* argv[]) {
    int numtasks;
    int taskid;
    int numworkers;
    int rc = 1;

    double* a;
	double* b;
	double* c;

    int NRA, NCA, NCB;
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
    numworkers = numtasks - 1;

    if (numtasks < 2) {
        printf("Need at least two MPI tasks. Quitting...\n");
        MPI_Abort(MPI_COMM_WORLD, rc);
        exit(1);
    }

    /******** master task *****************/
    if (taskid == MASTER) {
        printf("MPI Matrix Multiplication: Non-Blocking\n");
        //printf("mpi_mm has started with %d tasks.\n", numtasks);

        a = (double*)malloc(NRA * NCA * sizeof(double));
        b = (double*)malloc(NCA * NCB * sizeof(double));
        c = (double*)malloc(NRA * NCB * sizeof(double));

        initialize_matrix(a, NRA, NCA, 0);
        initialize_matrix(b, NCA, NCB, 1);

        double start_time = MPI_Wtime();

        int rows;
        int rows_per_worker = NRA / numworkers;
        int extra = NRA % numworkers;
        int offset = 0;
        int* offsets = (int*)malloc(numworkers * sizeof(int));
        int* rows_sent = (int*)malloc(numworkers * sizeof(int));

        // Send matrix data to worker tasks (non-blocking)
        MPI_Request* send_requests = (MPI_Request*)malloc(numworkers * 2 * sizeof(MPI_Request));
        for (int dest = 1; dest <= numworkers; dest++) {
            rows = (dest <= extra) ? rows_per_worker + 1 : rows_per_worker;
            offsets[dest - 1] = offset;
            rows_sent[dest - 1] = rows;

            //printf("Sending %d rows to task %d offset=%d\n", rows, dest, offset);

            int index = (dest - 1) * 2;
            MPI_Isend(&a[offset * NCA], rows * NCA, MPI_DOUBLE, dest, FROM_MASTER, MPI_COMM_WORLD, &send_requests[index]);
            MPI_Isend(b, NCA * NCB, MPI_DOUBLE, dest, FROM_MASTER, MPI_COMM_WORLD, &send_requests[index + 1]);

            offset += rows;
        }

        MPI_Waitall(numworkers * 2, send_requests, MPI_STATUSES_IGNORE);

        MPI_Request* recv_requests = (MPI_Request*)malloc(numworkers * sizeof(MPI_Request));
        for (int source = 1; source <= numworkers; source++) {
            MPI_Irecv(&c[offsets[source - 1] * NCB], rows_sent[source - 1] * NCB, MPI_DOUBLE, source, FROM_WORKER, MPI_COMM_WORLD, &recv_requests[source - 1]);
        }

        MPI_Waitall(numworkers, recv_requests, MPI_STATUSES_IGNORE);

        printf("Execution time: %f seconds\n", MPI_Wtime() - start_time);
        print_matrix_checksum(c, NRA, NCB);

        free(a);
        free(b);
        free(c);
        free(offsets);
        free(rows_sent);
        free(send_requests);
        free(recv_requests);
    }

    /******** worker task *****************/
    else {
        int rows_per_worker = NRA / numworkers;
        int extra = NRA % numworkers;

        // each worker computes its own share of rows, same as the master does
        int rows = (taskid <= extra) ? rows_per_worker + 1 : rows_per_worker;

        a = (double*)malloc(rows * NCA * sizeof(double));
        b = (double*)malloc(NCA * NCB * sizeof(double));
        c = (double*)malloc(rows * NCB * sizeof(double));

        MPI_Request recv_requests[2];
        MPI_Irecv(a, rows * NCA, MPI_DOUBLE, MASTER, FROM_MASTER, MPI_COMM_WORLD, &recv_requests[0]);
        MPI_Irecv(b, NCA * NCB, MPI_DOUBLE, MASTER, FROM_MASTER, MPI_COMM_WORLD, &recv_requests[1]);

        MPI_Waitall(2, recv_requests, MPI_STATUSES_IGNORE);

        multiply_matrices(a, b, rows, NCA, NCB, c);

        MPI_Request send_requests[1];
        MPI_Isend(c, rows * NCB, MPI_DOUBLE, MASTER, FROM_WORKER, MPI_COMM_WORLD, &send_requests[0]);
        MPI_Waitall(1, send_requests, MPI_STATUSES_IGNORE);

        free(a);
        free(b);
        free(c);
    }

    MPI_Finalize();
    return 0;
}
