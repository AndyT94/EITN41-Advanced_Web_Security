package MicroMint;

import java.util.LinkedList;
import java.util.Random;

public class MicroMint {
    //The bins
    private int[] bins;
    //The number of bins
    private int nbrBins;
    //Number of collisions in order to create a coin
    private int nbrCollisions;
    //Number of coins to create
    private int coinsToCreate;
    //Confidence intervall width
    private double width;
    private static Random rand = new Random();

    public MicroMint(int u, int k, int c, double width) {
        nbrBins = (int) Math.pow(2, u);
        nbrCollisions = k;
        coinsToCreate = c;
        bins = new int[nbrBins];
        this.width = width;
        simulate();
    }

    public void simulate() {
        int coinsCreated;
        double iterations;
        double currWidth = Integer.MAX_VALUE;
        LinkedList<Double> dataIterations = new LinkedList<Double>();
        //Iterate as long as c.i width is smaller and do at least 2 iterations
        while(width < currWidth || dataIterations.size() < 2) {
            //Reset
            iterations = 0;
            coinsCreated = 0;
            for(int i = 0; i < bins.length; i++) {
                bins[i] = 0;
            }
            //Create the coins
            while (coinsCreated < coinsToCreate) {
                iterations++;
                int thrownBin = rand.nextInt(nbrBins);
                bins[thrownBin]++;
                if(bins[thrownBin] >= nbrCollisions) {
                    coinsCreated++;
                    bins[thrownBin] -= nbrCollisions;
                }
            }
            dataIterations.add(iterations);
            //Calculate current width
            currWidth = getCIWidth(dataIterations, getMean(dataIterations));
        }
        System.out.println("RESULT MEAN: " + Math.round(getMean(dataIterations)));
    }

    private double getCIWidth(LinkedList<Double> dataIterations, double mean) {
        //Calculate variance
        double variance = getVariance(dataIterations, mean);
        //Calculate standard deviation
        double stdDeviation = Math.sqrt(variance);
        //Return width
        return 2 * (3.66 * stdDeviation / Math.sqrt(dataIterations.size()));
    }

    private double getVariance(LinkedList<Double> dataIterations, double mean) {
        double sum = 0.0;
        for(double i: dataIterations) {
            sum += (i-mean)*(i-mean);
        }
        return sum/dataIterations.size();
    }

    private double getMean(LinkedList<Double> dataIterations) {
        double sum = 0.0;
        for(double i: dataIterations) {
            sum += i;
        }
        return sum/dataIterations.size();
    }

    public static void main(String[] args) {
        new MicroMint(20, 7, 10000, 4783);
    }
}
