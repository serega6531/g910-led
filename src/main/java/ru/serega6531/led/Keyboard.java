package ru.serega6531.led;

import org.usb4java.*;

import java.nio.ByteBuffer;

public class Keyboard {

    private static Device keyboard;

    public Keyboard() {
        findKeyboardDevice();
    }

    public void findKeyboardDevice() throws LibUsbException, IllegalStateException {
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(Main.libusbContext, list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        for (Device device : list) {
            DeviceDescriptor desc = new DeviceDescriptor();
            result = LibUsb.getDeviceDescriptor(device, desc);

            if (result != LibUsb.SUCCESS)
                throw new LibUsbException("Unable to read device descriptor", result);

            if(desc.idVendor() == 0x046d && desc.dump().contains("0xc335")){   //for some reasons libusb tells me 0xc335 = -15563
                keyboard = device;
                LibUsb.freeDeviceList(list, true);

                return;
            }
        }

        LibUsb.freeDeviceList(list, true);
        throw new IllegalStateException("g910 spectrum keyboard not found");
    }

    private int sendToKeyboard(ByteBuffer buf) throws LibUsbException {
        DeviceHandle handle = new DeviceHandle();
        int result = LibUsb.open(keyboard, handle);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException("Unable to open USB device", result);

        try {
            boolean detach = /*LibUsb.hasCapability(LibUsb.CAP_SUPPORTS_DETACH_KERNEL_DRIVER) &&*/ LibUsb.kernelDriverActive(handle, 1) == 1;

            try {
                if (detach) {
                    result = LibUsb.detachKernelDriver(handle, 1);
                    if (result != LibUsb.SUCCESS)
                        throw new LibUsbException("Unable to detach kernel driver", result);
                }

                result = LibUsb.claimInterface(handle, 1);
                if (result != LibUsb.SUCCESS)
                    throw new LibUsbException("Unable to claim interface", result);

                try {
                    return sendData(handle, buf) + sendData(handle, getCommitBuffer());
                } finally {
                    result = LibUsb.releaseInterface(handle, 1);
                    if (result != LibUsb.SUCCESS)
                        throw new LibUsbException("Unable to release interface", result);
                }
            } finally {
                if (detach) {
                    result = LibUsb.attachKernelDriver(handle, 1);
                    if (result != LibUsb.SUCCESS)
                        throw new LibUsbException("Unable to re-attach kernel driver", result);
                }
            }


        } finally {
            LibUsb.close(handle);
        }
    }

    private int sendData(DeviceHandle handle, ByteBuffer buf){
        int transfered = LibUsb.controlTransfer(handle, (byte) 0x21, (byte) 0x09, (short) (buf.limit() > 20 ? 0x0212 : 0x0211), (short) 1, buf, 2000);

        if (transfered < 0)
            throw new LibUsbException("Control transfer failed", transfered);

        return transfered;
    }

    public void setFXColorCycleKeys(byte speed){
        ByteBuffer buf = ByteBuffer.allocateDirect(20);

        populateFXAddressInternal(buf);
        buf.put((byte) 0x00);
        buf.put((byte) 0x03);
        buf.put((byte) 0x00);
        buf.put((byte) 0x00);
        buf.put((byte) 0x00);
        buf.put((byte) 0x00);
        buf.put((byte) 0x00);
        buf.put(speed);
        buf.put((byte) 0x00);
        buf.put((byte) 0x00);
        buf.put((byte) 0x64);

        sendToKeyboard(buf);
    }

    private void populateFXAddressInternal(ByteBuffer buf){
        buf.put((byte) 0x11);
        buf.put((byte) 0xff);
        buf.put((byte) 0x10);
        buf.put((byte) 0x3c);
    }

    private ByteBuffer getCommitBuffer(){
        ByteBuffer buf = ByteBuffer.allocateDirect(20);

        buf.put((byte) 0x11);
        buf.put((byte) 0xff);
        buf.put((byte) 0x0f);
        buf.put((byte) 0x5d);

        return buf;
    }

}
