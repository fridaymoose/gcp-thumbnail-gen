package ovh.kg4.pkravchenko.exception;

public class FunctionException extends RuntimeException {

  public FunctionException(String message) {
    super(message);
  }

  public FunctionException(String message, Throwable cause) {
    super(message, cause);
  }
}
