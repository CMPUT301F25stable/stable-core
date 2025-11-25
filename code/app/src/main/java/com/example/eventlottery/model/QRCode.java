package com.example.eventlottery.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QRCode {
    private Bitmap qrCode;
    private String eventId;

    /*
    private void generateQRCode() {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrCode = barcodeEncoder.encodeBitmap(eventId, BarcodeFormat.QR_CODE, 500, 500);
        } catch (WriterException e) {
            Log.e("QRCode", "Failed to generate QR code!");
            Log.e("QRCode", String.valueOf(e));
        }
    }

    public QRCode(String eventId) {
        this.eventId = eventId;
        generateQRCode();
    }
    */

    public QRCode(String eventId) {
        this.eventId = eventId;
    }

    public boolean generateQRCode() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Bitmap> bitmapFuture = executor.submit(() -> {
            Bitmap qrCode = null;
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                qrCode = barcodeEncoder.encodeBitmap(eventId, BarcodeFormat.QR_CODE, 500, 500);
            } catch (WriterException e) {
                Log.e("QRCode", "Failed to generate QR code!");
                Log.e("QRCode", String.valueOf(e));
            }
            return qrCode;
        });

        Bitmap qrCode;
        try {
            qrCode = bitmapFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
        executor.close();

        if (qrCode != null) {
            this.qrCode = qrCode;
            return true;
        } else {
            return false;
        }
    }

    public Bitmap getQrCode() {
        return qrCode;
    }

    public String getEventId() {
        return eventId;
    }
}