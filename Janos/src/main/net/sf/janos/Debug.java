/*
 * Created on 04/08/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos;

/**
 * a simple class allowing the developer to switch of debugging messages if they
 * get too annoying.
 * 
 * TODO: replace with apache commons logging, its in the classpath anyway!
 */
public final class Debug {

  private static boolean debugEnabled = true;
  private static boolean infoEnabled = true;
  private static boolean warnEnabled = true;
  private static boolean errorEnabled = true;

  public static final void debug(String message) {
    if (isDebugEnabled()) {
      System.out.println("DEBUG sonosj: " + message);
    }
  }
  
  public static boolean isDebugEnabled() {
    return debugEnabled;
  }

  public static final void info(String message) {
    if (isInfoEnabled()) {
      System.out.println("INFO sonosj: " + message);
    }
  }
  
  public static boolean isInfoEnabled() {
    return infoEnabled;
  }

  public static final void warn(String message) {
    if (isWarnEnabled()) {
      System.out.println("WARN sonosj: " + message);
    }
  }
  
  public static boolean isWarnEnabled() {
    return warnEnabled;
  }

  public static final void error(String message) {
    if (isErrorEnabled()) {
      System.err.println("ERROR sonosj: " + message);
    }
  }

  public static boolean isErrorEnabled() {
    return errorEnabled;
  }
  
  
  
}
