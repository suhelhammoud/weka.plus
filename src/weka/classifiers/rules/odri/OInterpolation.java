package weka.classifiers.rules.odri;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class OInterpolation {
  private List<Double> xc = new ArrayList<>();
  private List<Double> yc = new ArrayList<>();

  double a;
  double b;

  public double getA() {
    return a;
  }

  public double getB() {
    return b;
  }

  public void addPoint(double x, double y) {
    System.out.println("add point x = "+ x +", y = "+ y);
    xc.add(x);
    yc.add(y);
  }

  public IntStream forAll() {
    return IntStream.range(0, xc.size());
  }

  public double numRules(double minOcc) {
    return a * pow(minOcc, b);
  }

  public double minOcc(double numRules) {
    return pow( (double) numRules / a, b);
  }

  public void interpolate() {

    double m = forAll()
            .mapToDouble(i -> pow(yc.get(i), 2))
            .sum();

    double xDash = forAll()
            .mapToDouble(i -> pow(yc.get(i), 2) * log(xc.get(i)))
            .sum() / m;

    IntToDoubleFunction tF = i ->
            pow(yc.get(i), 2) * pow(log(xc.get(i)) - xDash, 2);

    double t = forAll()
            .mapToDouble(i -> tF.applyAsDouble(i))
            .sum();

    IntToDoubleFunction bF = i ->
            pow(yc.get(i), 2) * log(yc.get(i)) * (log(xc.get(i)) - xDash);

    b = forAll()
            .mapToDouble(i -> bF.applyAsDouble(i))
            .sum() / t;

    IntToDoubleFunction aF = i ->
            pow(yc.get(i), 2) * log(yc.get(i));

    double aUp = forAll()
            .mapToDouble(i -> pow(yc.get(i), 2) * log(yc.get(i)))
            .sum();

    double a1 = forAll()
            .mapToDouble(i -> pow(xc.get(i), b) * yc.get(i))
            .sum();
    double a2 = forAll()
            .mapToDouble(i -> pow(xc.get(i), 2 * b))
            .sum();
    a = a1 / a2;
//
////    double a = exp(aSum);
//    double a = exp(aUp / m - b * xDash);
//
////    this.a = a;
////    this.b = b;
//    this.a = 140;
//    this.b = -.9749;
  }

  public void fit2() {
    double b1 = xc.size() * forAll()
            .mapToDouble(i -> log(xc.get(i)) * log(yc.get(i)))
            .sum();

    double b2 = forAll()
            .mapToDouble(i -> log(xc.get(i)))
            .sum() *
            forAll()
                    .mapToDouble(i -> log(yc.get(i)))
                    .sum();

    double b3 = xc.size() * forAll()
            .mapToDouble(i -> pow(log(xc.get(i)), 2))
            .sum();

    double b4 = pow(forAll()
            .mapToDouble(i -> log(xc.get(i)))
            .sum(), 2);

    b = (b1 - b2) / (b3 - b4);

    double a1 = forAll()
            .mapToDouble(i -> log(yc.get(i)))
            .sum();
    double a2 = b * forAll()
            .mapToDouble(i -> log(xc.get(i)))
            .sum();
    a = (a1 - a2)/ xc.size();

    a = exp(a);
  }

  static void test01() {
    OInterpolation oin = new OInterpolation();
//    oin.addPoint(1, 100);
//    oin.addPoint(2, 50);
//    oin.addPoint(3, 33.3333);
//    oin.addPoint(4, 25);
//    oin.addPoint(5, 20);
//    oin.addPoint(6, 16.6666);
//    oin.addPoint(7, 14.28571);
//    oin.addPoint(8, 12.5);
//    oin.addPoint(9, 11.1111);
    oin.addPoint(10, 10);
//    oin.addPoint(11, 9.09090);
//    oin.addPoint(12, 8.333333);
//    oin.addPoint(13, 7.6923);
//    oin.addPoint(14, 7.1428);
//    oin.addPoint(15, 6.666666);
//    oin.addPoint(16, 6.25);
//    oin.addPoint(17, 5.88235);
//    oin.addPoint(18, 5.5555555);
//    oin.addPoint(19, 5.26315);
//    oin.addPoint(20, 5);
//    oin.addPoint(21, 4.76190);
//    oin.addPoint(22, 4.545454);
//    oin.addPoint(23, 4.3478);
    oin.addPoint(24, 4.166666);
//    oin.addPoint(25, 4);
//    oin.addPoint(26, 3.8461);
    oin.addPoint(27, 3.7037);
//    oin.addPoint(28, 3.57142);
//    oin.addPoint(29, 3.44827);
    oin.addPoint(30, 3.333333333);

    oin.fit2();
//    oin.interpolate();
    System.out.println("oin.getA() = " + oin.getA());
    System.out.println("oin.getB() = " + oin.getB());

    double minOcc = 29;
    double numRules = 3.44827;
    System.out.println("oin.numRules(minOcc) = " + oin.numRules(minOcc));
    System.out.println("oin.minOcc(numRules) = " + oin.minOcc(numRules));
  }

  static void test2() {
    OInterpolation oin = new OInterpolation();
    oin.addPoint(1, 120);
    oin.addPoint(48, 15);

    oin.addPoint(3, 96);
    oin.addPoint(200, 2);
    oin.addPoint(100, 7);

//    oin.fit2();
    oin.interpolate();
    System.out.println("oin.getA() = " + oin.getA());
    System.out.println("oin.getB() = " + oin.getB());

//    double minOcc = 30;
    double numRules = 30;
//    System.out.println("oin.numRules(minOcc) = " + oin.numRules(minOcc));
    System.out.println("oin.minOcc(numRules) = " + oin.minOcc(numRules));
  }
  public static void main(String[] args) {
    test2();
  }
}
