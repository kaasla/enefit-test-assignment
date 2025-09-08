package com.kaarelkaasla.enefitresourceservice.exceptions;

public class OptimisticLockingException extends RuntimeException {
  public OptimisticLockingException(String message) {
    super(message);
  }
}
