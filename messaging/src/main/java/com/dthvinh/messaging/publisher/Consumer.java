package com.dthvinh.messaging.publisher;

public interface Consumer extends Runnable {
    void init();

    void pollOnce();
}
