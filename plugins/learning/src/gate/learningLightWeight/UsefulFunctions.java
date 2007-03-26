/*
 *  UsefulFunctions.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: UsefulFunctions.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learningLightWeight;
/**
 * Some useful functions.
 *
 */
public class UsefulFunctions {
  /** The sigmoid fucntion. */
  public static double sigmoid(double x) {
    return 1.0 / (1 + Math.exp(-2.0 * x));
  }
}
