package sensetivity;

import weka.attributeSelection.pas.PasUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;





public class Estimate {



    public static void main(String[] args) {
        double[] ranks = new double[]{.30, .20, .15, .10, .08, .07, .05, .05};
//        double[] ranks = new double[]{.30, .30, .30, .10, .00, .00, .00, .00};
        List<Double> rankList = Arrays.stream(ranks)
                .boxed()
                .collect(Collectors.toList());

        double estimate = StoryUtils.estimate(rankList);
        double entropy = StoryUtils.entropy(rankList);
        System.out.println("estimate = " + estimate);
        System.out.println("entropy =  " + entropy);
    }
}
