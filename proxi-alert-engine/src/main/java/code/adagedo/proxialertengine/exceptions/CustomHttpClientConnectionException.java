package code.adagedo.proxialertengine.exceptions;

import org.springframework.http.HttpHeaders;

public class CustomHttpClientConnectionException extends RuntimeException{

    public CustomHttpClientConnectionException(String status){
        super(status);
    }
}
