import org.apache.commons.cli.*;


/**
 * Created by Smaddady on 4/23/2016.
 */
public class Prog2 {
    public static void main(String[] args) {

        //loads parsing options to parse args
        Options options = getOptions();

        //loads system properties from parsed args
        Dataset data = new Dataset(options, args);


        //TODO: open files to useable form. add data to logisticRegression instance

        LogisticRegression logisticRegression = new LogisticRegression(data);


        //TODO: make logReg functions ie, regularization, linesearch, MultinomialRegression, early stopping...
    }

    private static Options getOptions(){
        Options options = new Options();
        Option bt = new Option("BT", "use back-tracing: required args alpha and b");
        bt.hasArgs();
        bt.setArgs(2);
        options.addOption(bt);

        Option l2 = new Option("L2", "use regularization: requires coefficient Lambda");
        bt.hasArgs();
        l2.setArgs(1);
        options.addOption(l2);
        return options;
    }


}
