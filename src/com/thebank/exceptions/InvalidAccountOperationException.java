package com.thebank.exceptions;

public class InvalidAccountOperationException extends Exception{
    public InvalidAccountOperationException (String message){
        super(message);
    }
}
