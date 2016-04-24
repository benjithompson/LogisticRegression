
import org.apache.commons.cli.*;
import org.apache.commons.math3.util.DoubleArray;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Smaddady on 4/23/2016.
 */
public class Dataset {

    private String sys_cfg_fn;
    private String data_cfg_fn;
    private String train_fn;
    private String dev_fn;

    //sysconfig data
    private int N_TRAIN;
    private int N_DEV;
    private int C;
    private int D;

    //arg data or dataset config
    private double alpha;
    private double b;
    private double lambda;

    //constructor
    public Dataset(Options options, String[] args) {
        getDataFromArgs(options, args);
        setConfigFromFile(data_cfg_fn);
        setConfigFromFile(sys_cfg_fn);

    }

    public String getSys_cfg_fn() {
        return sys_cfg_fn;
    }

    public int getN_TRAIN() {
        return N_TRAIN;
    }

    public int getN_DEV() {
        return N_DEV;
    }

    public int getC() {
        return C;
    }

    public int getD() {
        return D;
    }


    private void setConfigFromFile(String fileName){
        String line;

        try(BufferedReader buf = new BufferedReader(new FileReader(fileName))) {

            while((line = buf.readLine()) != null){

                parseLine(line);

            }

        }catch(Exception e){

            e.getStackTrace();

        }
    }

    private void parseLine(String line){
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter("\\s+");
        String token;
        if(scanner.hasNext()) {
            token = scanner.next();
            switch (token) {
                case "alpha": if(alpha == 0) alpha = Double.parseDouble(scanner.next());
                    return;
                case "b": if(b == 0) b = Double.parseDouble(scanner.next());
                    return;
                case "lambda": if(lambda == 0) lambda = Double.parseDouble(scanner.next());
                    return;
                case "N_TRAIN": N_TRAIN = Integer.parseInt(scanner.next());
                    return;
                case "N_DEV": N_DEV = Integer.parseInt(scanner.next());
                    return;
                case "C": C = Integer.parseInt(scanner.next());
                    return;
                case "D": D = Integer.parseInt(scanner.next());
                    return;
            }
        }
    }



    private void getDataFromArgs(Options options, String[] args){

        CommandLineParser parser = new DefaultParser();

        try {

            CommandLine cmd = parser.parse(options,args);

            if(cmd.hasOption("BT")){

                String[] BT = cmd.getOptionValues("BT");
                this.alpha = Double.parseDouble(BT[0]);
                this.b = Double.parseDouble(BT[1]);

                System.out.println("BT args: " + alpha + " " + b);
            }
            if(cmd.hasOption("L2")){

                this.lambda = Double.parseDouble(cmd.getOptionValue("L2"));

                System.out.println("L2 args: " + lambda);
            }

            String[] inputFilesNames = cmd.getArgs();

            if(inputFilesNames.length != 4){

                System.err.println("Wrong number of input file names");

            }else {

                this.sys_cfg_fn = inputFilesNames[0];
                this.data_cfg_fn = inputFilesNames[1];
                this.train_fn = inputFilesNames[2];
                this.dev_fn = inputFilesNames[3];

                System.out.println("Arguments: " + Arrays.toString(inputFilesNames));
            }

        } catch (ParseException e) {

            System.err.println("ParseException");
            e.printStackTrace();

        }

    }

}
