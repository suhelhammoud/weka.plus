package weka.attributeSelection.pas;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CutOffPoint {

    public static double entropyValue(double p) {
        if (p < 1e-8 || p > (1 - 1e-8))
            return 0;
        return -p * Math.log(p) / Math.log(2);
    }

    public static double sum(BitSet bs, double[] ranks) {
        return sum(bs, toList(ranks));
    }

    public static double sum(BitSet bs, List<Double> ranks) {
        return bs.stream()
                .mapToDouble(i -> ranks.get(i))
                .sum();
    }

    public static List<Double> toList(double[] arr) {
        return Arrays.stream(arr)
                .boxed()
                .collect(Collectors.toList());
    }

    public static List<Double> normalize(List<Double> ranks) {
        //normalize dataset
        final double sumValue = ranks.stream()
                .mapToDouble(i -> i.doubleValue())
                .sum();

        if (sumValue == 0) throw new NullPointerException("Max Rank Can not be Zero !!");
        return ranks.stream()
                .map(v -> v / sumValue)
                .collect(Collectors.toList());
    }

    public static double huffman(double[] ranks) {
        return huffman(toList(ranks));
    }

    public static double huffman(List<Double> ranks) {
        List<Double> ranksN = normalize(ranks);

        Comparator<BitSet> comp = (o1, o2) -> (int) Math.signum(sum(o1, ranksN) - sum(o2, ranksN));

        List<BitSet> current = IntStream.range(0, ranks.size())
                .mapToObj(i -> {
                    BitSet bitSet = new BitSet(ranksN.size());
                    bitSet.set(i);
                    return bitSet;
                })
                .collect(Collectors.toList());

        double sum = 0.0;
        while (current.size() > 1) {
            BitSet min1 = Collections.min(current, comp);
            current.remove(min1);
            BitSet min2 = Collections.min(current, comp);
            current.remove(min2);

            BitSet merge = new BitSet(ranks.size());
            merge.or(min1);
            merge.or(min2);
            current.add(merge);
            sum += sum(merge, ranksN);
        }

        return Math.pow(2, sum);
    }

    public static double entropy(double[] ranks) {
        return entropy(toList(ranks));
    }

    public static double entropy(List<Double> ranks) {
        List<Double> ranksNormalized = normalize(ranks);

        double hV = ranksNormalized.stream()
                .mapToDouble(CutOffPoint::entropyValue)
                .sum();
        return Math.pow(2, hV);
    }

    public static double threshold(double[] ranks, double threshold) {
        return threshold(toList(ranks), threshold);
    }
        public static double threshold(List<Double> ranks, double threshold){
        List<Double> ranksNormalized = normalize(ranks);

        final double level = 1.0 / ranks.size() * threshold;
//            System.out.println("level = " + level);

        return ranksNormalized.stream()
                .filter( r -> r > level)
                .count();
    }

    public static void main(String[] args) {
//        double[] weights = new double[]{20, 20, 5, 0, 0, 0, 0};

        double[] weights = new double[]{
                30,
                20,
                15,
                10,
                8,
                7,
                5,
                5,
        };
         // 2.75


        double huffman = huffman(weights);
        double entropy = entropy(weights);
        double threshold = threshold(weights, .5);

        System.out.println("entropy = " + entropy);
        System.out.println("huffman = " + huffman);
        System.out.println("threshold = " + threshold);
    }

}
