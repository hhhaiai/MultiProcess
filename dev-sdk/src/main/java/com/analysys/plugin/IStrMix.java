package com.analysys.plugin;


public interface IStrMix {


    String encrypt(String data, String key);


    String decrypt(String data, String key);


    boolean overflow(String data, String key);

}
