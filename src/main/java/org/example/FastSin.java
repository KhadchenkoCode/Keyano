package org.example;
public final class FastSin {

    private static final double TWO_PI = 2.0 * Math.PI;
    private static final double INV_TWO_PI = 1.0 / TWO_PI;
    private static final double PI = Math.PI;

    public static double sin(double x) {
        // Fast range reduction to [0, 2π)
        x -= (long)(x * INV_TWO_PI) * TWO_PI;

        // Map to [-π, π]
        if (x > PI) {
            x -= TWO_PI;
        }

        // Core approximation
        double y = 1.2732395447351627 * x   // 4/π
                - 0.4052847345693511 * x * Math.abs(x); // 4/π²

        // Extra precision correction (optional but recommended)
      //
          //y = 0.225 * (y * Math.abs(y) - y) + y;

        return y;
    }

    public static double cos(double x) {
        return sin(x + Math.PI / 2);
    }
}
