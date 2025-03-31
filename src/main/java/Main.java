
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.*;
// import java.util.Random;

// This program is Run in Java 8
public class Main {

  // public static final Random rand = new Random();
  public static final ExecutorService exec = Executors.newWorkStealingPool();

  public static class equationF implements Comparable<equationF>, Callable<Integer> {
    protected int a, b;
    protected Integer count = null;
    protected Future<Integer> countAnswer = null;

    public equationF(int a, int b) {
      this.a = a;
      this.b = b;
      this.countAnswer = exec.submit(this);
    }

    public int getA() {
      return a;
    }

    public int getB() {
      return b;
    }

    public int getCount() {
      try {
        return this.count == null ? (this.count = countAnswer.get()).intValue() : this.count.intValue();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        this.countAnswer = exec.submit(this);
        return (this.count = this.getCount()).intValue();
      } finally {
        this.countAnswer = null;
      }
    }

    public int f(int n) {
      return n * n + a * n + b;
    }

    @Override
    public String toString() {
      return String.format("f(n) = n^2 %+4dn %+5d", a, b);
    }

    @Override
    public int compareTo(Main.equationF o) {
      return o == null ? -1 : Integer.compare(this.getCount(), o.getCount());
    }

    @Override
    public Integer call() throws Exception {
      for (int i = 0;; i++) {
        int y = f(i);
        if (y < 2 || !isPrime(y))
          return i;
      }
//			AtomicInteger counter = new AtomicInteger(0);
//			return this.setCount(
//					IntegerStream.generate(counter::getAndIncrement).sequential().takeWhile(i -> isPrime(f(i))).count());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      equationF pair = (equationF) o;
      return this.a == pair.a && this.b == pair.b;
    }

    @Override
    public int hashCode() {
      return Objects.hash(a, b);
    }

  }

  public static boolean isPrime(int n) {
    // Definition 2.1.4: Prime and composite numbers.
//		https://learn.zybooks.com/zybook/LAMISSIONMATH272SargsyanSpring2025/chapter/2/section/1?content_resource_id=108897972
//			An integer n is prime if and only if n>1, and the only positive integers that divide n are 1 and n .

    return (n < 2) ? false: (n == 2)? true:
        !IntStream.rangeClosed(0, Double.valueOf(Math.sqrt(n)/2).intValue()).map(i->i==0?2:i*2+1)
            .anyMatch(n2 -> n % n2 == 0);
  }

  public static void main(String[] args) {
    System.out.printf("Programming Assignment: Quadratic Primes\n");
    System.out.printf("Student: Shun Hoi, Yeung #900494756\n" + "Course: Math 272#14052\n" + "\n");

    System.out.printf("Considering quadratic polynomials of the form\n"
        + "N^2+an+b, where |a|<1000 and |b|≤1000, where |n| is the absolute value of n, an integer.\n" + " \n"
        + "Find the coefficients a, and b, for the quadratic expression that produces the maximum number of primes for consecutive values of n, starting with n=0. \n"
        + "\n\n");

//		equationF e1 = new equationF(1, 41);
//		equationF e2 = new equationF(-79, 1601);
//		System.err.printf("Test 1 (%s): %d\n", e1, e1.getCount() );
//		System.err.printf("Test 2 (%s): %d\n", e2, e2.getCount() );

    System.err.printf("Running...In parallel Mode (professor can remove if condition to show entire process)\n");
    Optional<equationF> max = IntStream.rangeClosed(-(1000 -1), 1000 -1).parallel().boxed()
        .flatMap(a -> IntStream.rangeClosed(-1000, 1000).parallel().boxed().map(b -> new equationF(a, b)))
//				.peek(f->System.err.printf("%s, count: %d \n", f, f.getCount()))
        .peek(f->{
           if (System.currentTimeMillis() % (1000) == 0) // remove the this if condtion to print out the entire process
        // if (f.getA() % 100 == 0 && f.getB() % 100 == 0) 
          System.err.printf("Processing: %s, consecutive primes count: %d \n", f, f.getCount());
        })
//				.forEachOrdered(f->System.out.printf("%s: %d consecutive primes\n", f, f.getCount()));
        .max(equationF::compareTo);

    if (max.isPresent()) {
      equationF temp = max.get();
      System.out.printf("%s, where a = %d and b = %d, which can produced the max consecutive primes: %d, variable n from 0 to %d.\n", temp,
          temp.getA(), temp.getB(), temp.getCount(), temp.getCount() -1 );

      for (int i = 0; i <= temp.getCount(); i++) {
        System.out.printf("f(%d) = %d, isPrime(): %b\n", i, temp.f(i), isPrime(temp.f(i)));
      }
    } else {
      System.out.printf("There is no any function product any consecutive primes!");
    }

    exec.shutdown();
    System.exit(0);
  }
}
