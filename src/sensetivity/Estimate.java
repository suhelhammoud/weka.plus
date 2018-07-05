package sensetivity;

import weka.attributeSelection.pas.CuttOffPoint;

import java.util.*;
import java.util.stream.Collectors;


public class Estimate {



    public static void main(String[] args) {
//        double[] ranks = new double[]{.30, .20, .15, .10, .08, .07, .05, .05};
        double[] ranks = new double[]{.125, .125, .125, .125, .125, .125, .125};
//        double[] ranks = new double[]{.30, .30, .30, .10, .00, .00, .00, .00};
        List<Double> rankList = Arrays.stream(ranks)
                .boxed()
                .collect(Collectors.toList());

        double estimate = CuttOffPoint.huffman(rankList);
        double entropy = CuttOffPoint.entropy(rankList);
        System.out.println("huffman = " + estimate);
        System.out.println("entropy =  " + entropy);
    }
}