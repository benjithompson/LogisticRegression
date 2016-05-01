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
    private RealMatrix xHatTrain; // d+1 x ntr
    private RealMatrix xHatDev; // d+1 x ndev
    private RealMatrix yTrain; //ntr x 1
    private RealMatrix yDev; // ndev x 1
    private RealMatrix betaHat; // 1 x D+1
    private RealMatrix BHat; // C x D+1
    private RealMatrix predTrain; // ntr x 1
    private RealMatrix predDev; // ndev x 1

    public LogisticRegression(Dataset data) {
        this.data = data;
    }

    public void run(){

        if(data.getC() == 2){

            initMatrixData(data.getC());
            binaryLogisticRegression(data.getSTEP_SIZE(), data.getLamdba());

        }else if(data.getC() > 2){

            initMatrixData(data.getC());
            multinomialLogisticRegression();
        }
    }

    //initialize RealMatrix set, if c > 2 use BHat for multinomial classification
    private void initMatrixData(int c){

        //set initial beta/B matrix to zero
        if(c > 2){
            BHat = new Array2DRowRealMatrix(data.getD()+1, c);
        }else{
            betaHat = new Array2DRowRealMatrix(data.getD()+1, 1);
        }

        xHatTrain = getXHatFromFile(data.getTrain_fn(), data.getN_TRAIN(), data.getD());
        xHatDev = getXHatFromFile(data.getDev_fn(), data.getN_DEV(), data.getD());
        yTrain = getYMatrixFromFile(data.getTrain_fn(), data.getN_TRAIN());
        yDev = getYMatrixFromFile(data.getDev_fn(), data.getN_DEV());

        predTrain = new Array2DRowRealMatrix(1, data.getN_TRAIN());
        predDev = new Array2DRowRealMatrix(1, data.getN_DEV());

    }

    private void binaryLogisticRegression(double STEP_SIZE, double lambda){

        boolean converged = false;
        double oldAcc, newAcc, maxAcc = 0;
        int iter = 0;
        int badIters = 0;
        RealMatrix grad;

        //newLL = getNegativeLogLikelihood(betaHat, xHatTrain, yTrain, data.getN_TRAIN(), data.getD(), data.getLamdba());
        newAcc = getAcc(xHatDev, yDev, predDev, data.getN_DEV());

        while(!converged && iter < data.getMAX_ITERS()){
            iter++;

            oldAcc = newAcc;

            grad = getGrad(data.getD(), lambda);

            betaHat = betaHat.subtract(grad.scalarMultiply(STEP_SIZE));

            newAcc = getAcc(xHatDev, yDev, predDev, data.getN_DEV());

            if(newAcc <= maxAcc){ // <= for minimization >= for maximization

                badIters++;

                if(badIters > data.getMAX_BAD_COUNT()){

                    converged = true;
                }
            }else{
                maxAcc = newAcc;
                badIters = 0;
            }
            
            //TODO: get accuracy, make predictions and test vs real values on training and dev set
            
            printPredictionStats(iter);
        }

        System.out.println("converged");
    }

    private void multinomialLogisticRegression(){






    }

    private RealMatrix getGrad(int D, double lambda){

        RealMatrix grad2;
        double b2 = 0;
        double lambdaB2;

        Sigmoid sig = new Sigmoid();
        RealMatrix XBeta = xHatTrain.transpose().multiply(betaHat);
        RealMatrix sigXBeta = new Array2DRowRealMatrix(XBeta.getRowDimension(), XBeta.getColumnDimension());
        RealMatrix lambdaB2Matrix = new Array2DRowRealMatrix(D+1, 1);

        //set sigmoid(XB)
        for(int i = 0; i < XBeta.getRowDimension(); i++){
            sigXBeta.setEntry(i, 0, sig.value(XBeta.getEntry(i,0)));
        }

        if(lambda > 0) {

            // sum(bk^2)
            for (int i = 0; i < D; i++) {
                b2 = Math.pow(betaHat.getEntry(i, 0), 2.0);
            }

            // lambda*B^2 from index 1 to D instead of 0 to D
            lambdaB2Matrix.setEntry(0, 0, 0);
            lambdaB2 = lambda * b2;
            for (int i = 1; i < D+1; i++) {
                lambdaB2Matrix.setEntry(i, 0, lambdaB2);
            }
            grad2 = xHatTrain.multiply(sigXBeta.subtract(yTrain));
            grad2 = grad2.subtract(lambdaB2Matrix);
            return grad2;

        }else{

            grad2 = xHatTrain.multiply(sigXBeta.subtract(yTrain));
            return grad2;
        }

    }

    private double getAcc(RealMatrix X, RealMatrix y, RealMatrix pred, int N){
        double prob, acc;
        int xi;
        int numCorrectPred = 0;
        Sigmoid sig = new Sigmoid();

        for(int i = 0; i < N; i++){

            //get acc of sig(bTx(i)) for every x(i)
            prob = sig.value(this.betaHat.transpose().multiply(X.getColumnMatrix(i)).getEntry(0,0));
            if(prob > 0.5){
                xi=1;
                pred.setEntry(0, i, xi);

            }else{
                xi=0;
                pred.setEntry(0, i, xi);
            }

            if(xi == y.getEntry(i, 0)){
                numCorrectPred++;
            }
        }
        acc = (double)numCorrectPred/N;
        return acc;


    }

    private void printPredictionStats(int iter){

        double trainAcc;
        double testAcc;

        trainAcc = getAcc(xHatTrain, yTrain, predTrain, data.getN_TRAIN());
        testAcc = getAcc(xHatDev, yDev, predDev, data.getN_DEV());

        System.err.printf("Iter: %04d trainAcc=%.3f testAc=%.3f\n", iter, trainAcc, testAcc);
        //System.out.print("train ");
        //printMatrix(predTrain);
        //System.out.print("dev ");
        //printMatrix(predDev);
    }

    private double getNegativeLogLikelihood(RealMatrix b, RealMatrix x, RealMatrix y, int N, int D, double lambda){

        assert(betaHat.transpose().getColumnDimension() == xHatTrain.getRowDimension());

        double LL = 0;
        double bTx;
        double betaSquared = 0;


        Sigmoid sigmoid = new Sigmoid();
        for(int i = 0; i < data.getN_TRAIN(); i++){
            bTx = betaHat.transpose().multiply(xHatTrain.getColumnMatrix(i)).getEntry(0,0);
            LL += (yTrain.getEntry(i, 0) * Math.log(sigmoid.value(bTx)))+(((1-yTrain.getEntry(i, 0)) * Math.log(1-sigmoid.value(bTx))));
        }

        //return negative for minimization
        if(lambda != 0){

            for(int j = 1; j < D+1; j++){
                betaSquared += Math.pow(betaHat.getEntry(j, 0), 2);
            }
            return (-LL) + (lambda * betaSquared);

        }
        LL = -LL;
        return -LL;
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

                System.out.printf("%d, ", (int)e);
            }
            System.out.println();
        }
    }

}
