package code.adagedo.proxialertengine.exceptions;

public class HttpClientConnectionException extends RuntimeException{

    public HttpClientConnectionException(String status){
        super(status);
    }
}
