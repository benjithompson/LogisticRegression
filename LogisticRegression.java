import com.sun.istack.internal.Nullable;
import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Smaddady on 4/23/2016.
 */
public class LogisticRegression {
    Dataset data;
    RealMatrix xHat;
    RealMatrix y;
    RealMatrix betaHat;

    public LogisticRegression(Dataset data) {
        this.data = data;
    }


    public void run(){


        if(data.getC() == 2){
            initBinaryMatrixData();
            //run binary logistic regression

            binaryLogisticRegression();
            //printMatrix(betaHat);

        }else if(data.getC() > 2){
            //run multinomial logistic regression
            //TODO: finish multinomial logistic regression


        }


    }

    private void initBinaryMatrixData(){
        xHat = getXHatFromFile(data.getTrain_fn(), data.getN_TRAIN(), data.getD());
        //printMatrix(xHat);
        y = getYMatrixFromFile(data.getTrain_fn(), data.getN_TRAIN());
        betaHat = new Array2DRowRealMatrix(data.getD()+1, 1);

    }

    private void binaryLogisticRegression(){
        boolean converged = false;
        double oldLL, newLL;
        int badIters = 0;
        int totalIters = 0;
        RealMatrix grad;

        newLL = getLL();
        while(!converged){
            oldLL = newLL;

            grad = getGrad(data.getN_TRAIN());
            betaHat = betaHat.add(grad.scalarMultiply(data.getSTEP_SIZE()));
            newLL = getLL();
            totalIters++;

            if(totalIters > data.getMAX_ITERS()-1) {
                converged = true;
            }

            if(newLL >= oldLL){ // <= for minimization >= for maximization
                badIters++;
                if(badIters > data.getMAX_BAD_COUNT()){
                    converged = true;
                }
            }else{
                badIters = 0;
            }

           printData(totalIters, newLL);
        }

        System.out.println("converged");
    }

    private void printData(int iter, double LL){
        //TODO: finish implementing printouts
        Sigmoid sig = new Sigmoid();
        double bTx = betaHat.transpose().multiply(xHat).getEntry(0,0);
        System.err.printf("Iter %4d: trainAcc=%1.3f testAcc=%1.3f\n", iter, sig.value(bTx), 0.0);
    }

    private double getLL(){
        double LL = 0;
        double bTx;
        double temp = 0;
        assert(betaHat.getColumnDimension() == xHat.getRowDimension());

        Sigmoid sigmoid = new Sigmoid();
        for(int i = 0; i < data.getN_TRAIN(); i++){

            bTx = betaHat.transpose().multiply(xHat.getColumnMatrix(i)).getEntry(0,0);
            LL += (y.getEntry(i, 0) * Math.log(sigmoid.value(bTx)))+(((1-y.getEntry(i, 0)) * Math.log(1-sigmoid.value(bTx))));
        }

        //return negative for minimization
        return -LL;
    }

    private RealMatrix getGrad(int N){
        RealMatrix grad;
        Sigmoid sig = new Sigmoid();

        RealMatrix XbetaHat = xHat.transpose().multiply(betaHat);
        RealMatrix sigXbetaHat = new Array2DRowRealMatrix(N, 1);

        for(int i = 0; i < N; i++){
            sigXbetaHat.setEntry(i, 0, sig.value(XbetaHat.getEntry(i, 0)));
        }

        grad = xHat.multiply(y.subtract(sigXbetaHat));

        return grad;
    }

    private static RealMatrix getXHatFromFile(String fileName, int N, int D) {

        String line;
        String row[];

        RealMatrix rMatrix = new Array2DRowRealMatrix(N, D+1);

        try (BufferedReader buf = new BufferedReader(new FileReader(fileName))) {
            int i,j; //row,column
            i=0;
            while((line = buf.readLine()) != null){
                j = 0;
                row = line.split("\\s+");

                for(String entry : row){

                    try {

                        if(j != 0){
                            rMatrix.setEntry(i, j, Double.parseDouble(entry));
                            j++;
                        }else {
                            rMatrix.setEntry(i, j, 1.0);
                            j++;
                        }

                    } catch (NullPointerException e){
                        e.printStackTrace();
                        System.err.println("Too much data for dimensions specified");
                        System.exit(1);
                    }
                }

                if(j != D+1){
                    System.err.println("Incorrect value points in row " + j);
                    System.exit(1);
                }

                i++;
            }

            if(i != N){
                System.err.println("Incorrect rows of data. Given " + i);
                System.exit(1);
            }

            return rMatrix.transpose();

        } catch (IOException e) {

            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private static RealMatrix getYMatrixFromFile(String fileName, int N) {

        String line;
        String row[];

        RealMatrix rMatrix = new Array2DRowRealMatrix(N, 1);

        try (BufferedReader buf = new BufferedReader(new FileReader(fileName))) {
            int i = 0; //row

            while ((line = buf.readLine()) != null) {

                row = line.split("\\s+");

                try {

                    rMatrix.setEntry(i, 0, Double.parseDouble(row[0]));

                }catch (NullPointerException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                i++;
            }

            if (i != N) {
                System.err.println("Incorrect rows of data. Given " + i + ". Needs " + N);
                System.exit(1);
            }

            return rMatrix;

        } catch (IOException e) {

            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    //print nxm realmatrix
    private static void printMatrix(RealMatrix m){

        for(double[] array: m.getData()){

            for(double e: array){

                System.out.printf("%.3e, ", e);
            }
            System.out.println();
        }
        System.out.println();

    }


    //build matrix betaHat

    //log likelyhood

    //early stopping

    //line search

    //logistic regression

    //multinomial logistic regression



}
