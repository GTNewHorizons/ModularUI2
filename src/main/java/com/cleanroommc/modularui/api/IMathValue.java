package com.cleanroommc.modularui.api;

/**
 * Math value interface
 * <p>
 * This interface provides only one method which is used by all
 * mathematically related classes. The point of this interface is to
 * provide a generalized abstract method for computing/fetching some values
 * from different mathematical classes.
 */
public interface IMathValue {

    IMathValue get();

    boolean isNumber();

    void set(double value);

    void set(String value);

    double doubleValue();

    boolean booleanValue();

    String stringValue();

    class EvaluateException extends RuntimeException {

        public EvaluateException(String message) {
            super(message);
        }
    }
}
