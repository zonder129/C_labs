#include <stdlib.h>

#include <unistd.h>

#include <stdio.h>

#include <math.h>

#include <mpi.h>

#include <sys/time.h>

#include <fcntl.h>

#include <unistd.h>

#define DEF_ROW_PROC 4

#define Ia 10.0

#define R 1000.0

#define C 1e-5

int time_interval ;

int num_of_elements ;

int num_of_rows ;

int num_of_process ;

int time_moment = 0 ;

double time_step = R*C/10;

struct timeval tv1,tv2,dtv;

struct timezone tz;

int FindRows(int num_of_elements, int num_of_process) {
    int num_rows ;

    num_rows = (int)sqrt(num_of_elements) ;

    if((num_of_elements % num_rows == 0) && ( num_rows % num_of_process ==0)) {
        return num_rows ;
    }

    while((num_rows > 0)) {
        num_rows -= 1;

        if((num_of_elements % num_rows == 0) && ( num_rows % num_of_process ==0)){
            return num_rows ;
        }
    }
}

int main(int argc, char **argv) {

    double *F_matrix , *f_matrix;

    double *N_matrix , *n_matrix;

    double *p ;

    double *p_n_matrix ,*el_prev, *el_last;;

    int intBuf[4];

    int i , j, row_in_proc , row_elems, time;

    int myrank, total;

    int first, last ;

    FILE *gp = popen("gnuplot -persist", "w");

    MPI_Status *status1;

    MPI_Status *status2;

    MPI_Init (&argc, &argv);

    MPI_Comm_size (MPI_COMM_WORLD, &total);

    MPI_Comm_rank (MPI_COMM_WORLD, &myrank);

    if(!myrank) {

        // initial

        if(argc >= 3) {
            time_interval = atoi(argv[2]);
            num_of_elements = atoi(argv[1]);
        }

        num_of_process = total;

        if((num_of_elements % num_of_process != 0) || (num_of_elements < num_of_process)){
            write(1, "number of processes is not multuple to elems\n",47) ;
            exit(0);
        }

        num_of_rows = FindRows(num_of_elements, num_of_process);

        row_elems = num_of_elements/num_of_rows;

        printf("num_r_el %d \n", row_elems);

        intBuf[0] = row_elems; //количество элементов в строке
        intBuf[1] = num_of_rows / num_of_process; //количество строк на процесс
        intBuf[2] = num_of_process; //количество процессов
        intBuf[3] = time_interval; //время работы

        N_matrix = (double *)malloc(num_of_elements * sizeof(double )); //матрица i+1 момента

        F_matrix = (double *)malloc(num_of_elements * sizeof(double ));//матрица i момента

        for( i = 0; i < num_of_elements; i ++) {
            F_matrix[i] = 0.0 ;
            N_matrix[i] = 0.0 ;
        }

    }

    MPI_Bcast((void *)intBuf, 4, MPI_INT, 0, MPI_COMM_WORLD);

    int n = intBuf[0] ;

    int m = intBuf[1] ;

    int kol = intBuf[2] ;

    int in1, in2, in ;

    time_step = R*C/10 ;

    el_prev = (double *)malloc(n * sizeof(double )); //элементы предыдущей строки

    el_last = (double *)malloc(n * sizeof(double )); //элементы следующей строки

    f_matrix = (double *)malloc(n * m * sizeof(double )); //матрица текущего момента

    n_matrix = (double *)malloc(n * m * sizeof(double )); //матрица следующего момента

    time_interval = intBuf[3] ;

    gettimeofday(&tv1, &tz);

    MPI_Scatter((void *)F_matrix, n*m, MPI_DOUBLE,(void *)f_matrix, n*m, MPI_DOUBLE, 0, MPI_COMM_WORLD);

    for (time = 0; time < time_interval / time_step ; time += 1) {

        status1 = (MPI_Status *)malloc(sizeof(MPI_Status ));

        status2 = (MPI_Status *)malloc(sizeof(MPI_Status ));

        in1 = (myrank == kol - 1) ? kol - 1 : myrank + 1;

        in2 = (!myrank) ? 0 : myrank - 1;

        if (myrank != kol - 1 ) {
            MPI_Send(f_matrix + n * (m - 1 ) , n, MPI_DOUBLE, in1,1, MPI_COMM_WORLD );
        }
        if (myrank) {
            MPI_Send(f_matrix , n, MPI_DOUBLE, in2, 2, MPI_COMM_WORLD );
        }
        if (myrank) {
            MPI_Recv(el_prev, n, MPI_DOUBLE,in2,1, MPI_COMM_WORLD ,status1);
        }
        if (myrank != kol - 1) MPI_Recv(el_last, n, MPI_DOUBLE, in1, 2, MPI_COMM_WORLD , status2);

        for(i = 0 ; i < m ; i ++){
            for(j = 0 ; j < n ; j ++) {
                if ((!myrank && (!i)) || ((myrank == kol - 1) && (i == m-1)) || (!j) || (j == n - 1) ) { //если на границе
                    n_matrix[i*n + j] = (time <= (time_interval / (10*time_step)) ) ? Ia * R : 0.0; 
                } else { 
                    if (i == 0) { //если на границе
                        n_matrix[i*n + j] = (f_matrix[(j - 1) + n * i] - 4*f_matrix[j + n * i] + f_matrix[(j + 1) + n * i] + el_prev[j] + f_matrix[(j) + n * (i + 1)]) * time_step/(R*C) + f_matrix[j + n * i];
                    } else {
                        if(i == m - 1){ //если на границе
                            n_matrix[i*n + j] = (f_matrix[(j - 1) + n * i] - 4*f_matrix[j + n * i] + f_matrix[(j + 1) + n * i] + el_last[j] + f_matrix[(j) + n * (i - 1)]) * time_step/(R*C) + f_matrix[j + n * i];
                        } else { //в ценрте
                            n_matrix[i*n + j] = (f_matrix[(j - 1) + n * i] - 4*f_matrix[j + n * i] + f_matrix[(j + 1) + n * i] + f_matrix[(j) + n * (i + 1)] + f_matrix[(j) + n * (i - 1)]) * time_step/(R*C) + f_matrix[j + n * i];
                        }
                    }
                }
            }
        }

        MPI_Gather((void *)n_matrix, m * n , MPI_DOUBLE, (void *)N_matrix, m * n, MPI_DOUBLE, 0, MPI_COMM_WORLD);

        p = f_matrix ;

        f_matrix = n_matrix ;

        n_matrix = p ;

        if (!myrank) { //вывод графика

            fprintf (gp, "splot [][][0:10000] '-' title 'time = %+.3d)'\n", time );

            for (i=0; i<num_of_elements; i++) {
                fprintf (gp, " %# -15g %# -15g %# -15g\n", (double)(i/row_elems), (double)(i%row_elems),N_matrix[i]);
            }
            fprintf (gp, "e\n"); fflush (gp); usleep (100);

        }

    }

    if(!myrank) {

        gettimeofday(&tv2, &tz);

        dtv.tv_sec= tv2.tv_sec -tv1.tv_sec;

        dtv.tv_usec=tv2.tv_usec-tv1.tv_usec;

        if(dtv.tv_usec<0) { 
            dtv.tv_sec--; dtv.tv_usec+=1000000; 
        }

        printf("Time: %ld .%ld\n",(dtv.tv_sec*1000000+dtv.tv_usec)/1000000 , (dtv.tv_sec*1000000+dtv.tv_usec)%1000000 );

    }

    MPI_Finalize();

    exit(0);

}