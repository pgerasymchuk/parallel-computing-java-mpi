# ⚡ Parallel Computing in Java and MPI

Four projects about making the same work run faster on more cores: matrix multiplication with plain Java threads (striped and Fox block algorithms), divide-and-conquer text processing and matrix multiplication with the **ForkJoin** framework, a multi-channel queueing system built on `java.util.concurrent`, and distributed matrix multiplication in C/C++ with **MPI** using blocking, non-blocking and collective communication. Every parallel version is checked against a sequential baseline and timed against it.

🎓 *Parallel Computing Technologies*, Igor Sikorsky Kyiv Polytechnic Institute, 3rd year, spring 2025.

A parallel Bellman–Ford implementation from the same course lives in its own repository: [pgerasymchuk/BellmanFord](https://github.com/pgerasymchuk/BellmanFord).

## 🗂 Project Structure

```
matrix-multiplication/       # Java threads: sequential vs stripe vs Fox
forkjoin/                    # ForkJoin: word-length, common-words, key-words, matrix
queueing-system/             # multi-channel queueing system with rejections
mpi-matrix-multiplication/   # C/C++ MPI: sequential, blocking, non-blocking, collective + benchmark.sh
```

## 🧮 Matrix multiplication with Java threads

All algorithms implement one `MatrixMultiplier` interface. Matrices are `int[][]` filled from a seeded `Random`, so every run multiplies the same data.

- `SequentialMultiplier` is the plain triple loop used as the baseline.
- `StripeMultiplier` splits the rows of A into horizontal stripes, one per thread. When the row count doesn't divide evenly, the first stripes take one extra row. Each `StripeThread` multiplies its stripe by the whole B into a private block, and the blocks are stitched into C.
- `FoxMultiplier` implements the Fox block algorithm. The threads form a `q × q` grid and the matrices are cut into `q × q` blocks. On iteration `k`, thread `(i, j)` multiplies `A[i][(i + k) mod q]` by `B[(i + k) mod q][j]` and accumulates the result into its block `C[i][j]`.

`Main` runs sizes 128, 512, 1024 and 2048 with 4, 16 and 64 threads, prints the time of each algorithm and the speedup over the sequential version, and fails if any parallel result differs from the sequential one.

## 🌿 ForkJoin framework

| Module | What it does |
|--------|--------------|
| `word-length` | A `RecursiveTask` halves the list of lines of Shakespeare's complete works (~5.6 MB) until a chunk is under 1000 lines, collects word lengths with a regex, and computes mean, min, max, variance and standard deviation. After a warm-up run it times the ForkJoin and sequential passes and checks that both give the same result. |
| `common-words` | `FolderProcessor` recursively splits the list of documents and `LineProcessor` builds a word-frequency map per file. The word sets are intersected on the way back up, which gives the words shared by all documents. |
| `key-words` | The same file/line decomposition with `invokeAll`. It counts occurrences of the keywords from `data/keywords/keywords.txt` in every document and prints the counts per file. |
| `matrix` | `StripeForkJoinTask` (`RecursiveAction`) halves the row range of A until it is small enough, and each leaf writes its rows straight into the shared result. `Main` compares sequential, thread-stripe and ForkJoin multiplication of two 2000 × 2000 matrices. |

## 🚦 Queueing system with rejections

Each system has a bounded `ArrayBlockingQueue` (capacity 100), a `Producer` that generates 1000 tasks with random 0–3 ms gaps and drops a task when `offer()` finds the queue full, and three `Consumer` channels that `take()` tasks and serve them for a Gaussian 10 ± 5 ms. A `QueueMonitor` on a `ScheduledExecutorService` samples the queue length every second.

`Main` runs 20 independent systems in parallel on an `ExecutorService`, collects a `Statistics` object from each one through a `Future` and prints the averages: completed and rejected tasks, rejection probability and mean queue length.

## 🌐 MPI matrix multiplication

All four programs take `NRA NCA NCB` (rows of A, columns of A, columns of B; default 100 × 100 × 100), fill A and B with seeded random `double`s, time the multiplication and print a checksum of C. The shared helpers are in `matrix_utils.c`.

- **sequential**: single-process baseline.
- **blocking**: master/worker with `MPI_Send` / `MPI_Recv`. The master splits the rows of A across `numtasks − 1` workers, spreading the remainder, sends each one its offset, rows and all of B, then gathers the result rows.
- **non-blocking**: the same decomposition with `MPI_Isend` / `MPI_Irecv` and `MPI_Waitall`. Workers compute their own row counts, so the master only sends matrix data.
- **collective**: the master computes too. Rows of A go out with `MPI_Scatterv`, B with `MPI_Bcast`, and the partial results come back with `MPI_Gatherv`.

`benchmark.sh` runs every version for sizes 64 to 2048 with 2, 4 and 8 workers. The point-to-point versions get `workers + 1` processes, the collective one gets `workers`.

## 📊 Results

Each program prints its own timings and speedups. Besides the timings, the runs give these results:

- **Word lengths** in Shakespeare's complete works: mean 4.12, standard deviation 2.11, min 1, max 36. The ForkJoin and sequential passes return identical lists.
- **Queueing system**: arrivals come faster than three channels can serve, so a typical run of 20 systems averages about 593 served and 406 rejected tasks out of 1000, a rejection probability of ≈ 0.40 and a mean queue length of ≈ 17.7.
- **MPI**: the blocking, non-blocking and collective versions produce exactly the same checksum as the sequential program for every process count, including non-square sizes like 97 × 61 × 83 and more workers than rows.

## 🛠️ Tech Stack

- Java 21: `Thread`, `ForkJoinPool`, `RecursiveTask` / `RecursiveAction`, `ExecutorService`, `ScheduledExecutorService`, `ArrayBlockingQueue`, `Callable` / `Future`
- IntelliJ IDEA modules (`.iml`), no external dependencies
- C / C++ with MPI: MS-MPI and Visual Studio 2022 on Windows, Open MPI / MPICH on Linux
- Bash for the MPI benchmark

## 🖥️ Running Locally

```bash
git clone https://github.com/pgerasymchuk/parallel-computing-java-mpi.git
cd parallel-computing-java-mpi
```

**Java (JDK 21+).** Each module is a plain `src/` folder with a `Main` class. Run it from the module folder, since the programs read `text.txt` and `data/` by relative path:

```bash
cd forkjoin/word-length
javac -d out src/*.java && java -cp out Main
```

The same two commands work in `matrix-multiplication/`, `queueing-system/` and the other `forkjoin/` modules. Every module also opens directly in IntelliJ IDEA.

**MPI on Windows.** Install MS-MPI (runtime and SDK), open `mpi-matrix-multiplication/mpi-matrix-multiplication.sln` in Visual Studio 2022 and build the **Debug | x86** configuration. Then run a single version or the whole benchmark from Git Bash:

```bash
cd mpi-matrix-multiplication
mpiexec -n 5 ./Debug/blocking 1024 1024 1024
./benchmark.sh
```

**MPI on Linux / macOS** (Open MPI or MPICH):

```bash
cd mpi-matrix-multiplication
mkdir -p Debug
g++    -O2 -o Debug/sequential   sequential/sequential.cpp
mpicxx -O2 -o Debug/blocking     blocking/blocking.cpp
mpicxx -O2 -o Debug/non-blocking non-blocking/non-blocking.cpp
mpicc  -O2 -o Debug/collective   collective/collective.c

mpiexec -np 5 ./Debug/blocking   512 512 512   # 1 master + 4 workers
mpiexec -np 4 ./Debug/collective 512 512 512   # all ranks compute
./benchmark.sh
```
