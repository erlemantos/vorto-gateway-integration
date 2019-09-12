package org.eclipse.vorto.cloudservice;

/**
 * This class just uses system.outs for now. Final form should log to the logging service.
 * @author ERM1SGP
 *
 */
public class Logger {
  
  public static Logger instance() {
    return new Logger();
  }
  
  public void info(String str) {
    System.out.println(str);
  }
  
  public void error(String str) {
    System.out.println(str);
  }
  
  public void error(String str, Throwable e) {
    System.out.println(str);
    e.printStackTrace();
  }
  
}
