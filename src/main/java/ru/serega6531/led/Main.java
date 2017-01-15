package ru.serega6531.led;

import org.usb4java.*;

public class Main {

    public static Context libusbContext;


    public static void initLibUsb() throws IllegalStateException {
        libusbContext = new Context();
        if(LibUsb.init(libusbContext) != LibUsb.SUCCESS){
            throw new IllegalStateException("Error initializing libusb");
        }
    }

    public static void freeLibUsb(){
        LibUsb.exit(libusbContext);
    }

}
