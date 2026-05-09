package domain.utils;

import org.apache.commons.math3.distribution.TDistribution;

public class TTest {

    // Função para calcular o valor t
    public static double calculateTValue(double mean1, double mean2, double sd1, double sd2, int n1, int n2) {
        return (mean1 - mean2) / Math.sqrt((Math.pow(sd1, 2) / n1) + (Math.pow(sd2, 2) / n2));
    }

    // Função para obter o valor p
    public static double calculatePValue(double tValue, int df) {
        TDistribution tDist = new TDistribution(df);
        return 2 * (1 - tDist.cumulativeProbability(Math.abs(tValue)));
    }

    // Função para verificar a significância estatística com α = 0.05
    public static boolean isSignificant(double pValue) {
        return pValue < 0.05;
    }
}
