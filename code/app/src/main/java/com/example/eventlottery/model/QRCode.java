package com.example.eventlottery.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCode {
    public static Bitmap generate(String eventId) {
        Bitmap qrCode = null;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrCode = barcodeEncoder.encodeBitmap(eventId, BarcodeFormat.QR_CODE, 500, 500);
        } catch (WriterException e) {
            Log.e("QRCode", "Failed to generate QR code!");
            Log.e("QRCode", String.valueOf(e));
        }
        return qrCode;
    }
}