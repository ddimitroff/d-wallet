package com.ddimitroff.projects.dwallet.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A class for custom exception. Thrown where exceptions occur in D-Wallet's
 * response actions
 * 
 * @author Dimitar Dimitrov
 * 
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DWalletResponseException extends Exception {

  /** Serial version UID constant */
  private static final long serialVersionUID = 1L;

  /**
   * Default constructor
   */
  public DWalletResponseException() {
    super();
  }

  /**
   * Parametrized constructor
   * 
   * @param message
   *          - message to set
   */
  public DWalletResponseException(String message) {
    super(message);
  }

  /**
   * Parametrized constructor
   * 
   * @param t
   *          - {@link Throwable} object to set
   */
  public DWalletResponseException(Throwable t) {
    super(t);
  }

  /**
   * Parametrized constructor
   * 
   * @param message
   *          - message to set
   * @param t
   *          - {@link Throwable} object to set
   */
  public DWalletResponseException(String message, Throwable t) {
    super(message, t);
  }

}
