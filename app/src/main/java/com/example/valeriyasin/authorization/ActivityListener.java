package com.example.valeriyasin.authorization;

/**
 * Created by valeriyasin on 11/15/16.
 */
public interface ActivityListener {
//    void onAuthStarted();
//
    void onComplete(TokenObject tokenObject);

    void onError(String error);
}
