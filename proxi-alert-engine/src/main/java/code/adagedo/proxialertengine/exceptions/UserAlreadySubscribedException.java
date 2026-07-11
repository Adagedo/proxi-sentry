package code.adagedo.proxialertengine.exceptions;

public class UserAlreadySubscribedException extends RuntimeException{

    public UserAlreadySubscribedException(String message){
        super(message);
    }

}
