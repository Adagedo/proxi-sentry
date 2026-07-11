package code.adagedo.proxialertengine.exceptions;

public class UserAlreadyRegisteredException extends RuntimeException{

    public UserAlreadyRegisteredException(String message){
        super(message);
    }

}
