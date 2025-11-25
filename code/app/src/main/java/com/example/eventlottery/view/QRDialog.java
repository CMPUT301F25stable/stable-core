package com.example.eventlottery.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.eventlottery.R;

/** TO BE REPLACED */


public class QRDialog extends DialogFragment {
    private String eventId = "";
    private String eventName = "";
    private Bitmap qrCode;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param qrCode Bitmap of a QR Code.
     * @return A new instance of fragment QRDialog.
     */
    public static QRDialog newInstance(String eventId, String eventName, Bitmap qrCode) {
        QRDialog fragment = new QRDialog();
        Bundle args = new Bundle();
        args.putString("ID", eventId);
        args.putString("Name", eventName);
        args.putParcelable("QRCode", qrCode);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventId = getArguments().getString("ID");
            eventName = getArguments().getString("Name");
            qrCode = getArguments().getParcelable("QRCode");
        }

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_qr, null);
        ImageView imageView = view.findViewById(R.id.imageViewQR);

        imageView.setImageBitmap(qrCode);

        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return builder
                .setView(view)
                .setTitle("QR Code")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Download", null)
                .create();

    }
}