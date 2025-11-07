package com.example.eventlottery.view;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.client.android.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Activity that manages scanning of QR codes using the {@link com.journeyapps.barcodescanner.CaptureManager}
 * and a {@link DecoratedBarcodeView}. This class enables a full-screen scanning interface
 * <p>This class is a modified implementation of the default CaptureActivity from the ZXing embedded library.</p>
 * @see CaptureManager
 * @see DecoratedBarcodeView
 */
public class QRScanActivity extends AppCompatActivity {

    /** The CaptureManager instance that handles barcode scanner lifecycle and processing. */
    private CaptureManager capture;

    /** The view used to display and manage the barcode scanner UI. */
    private DecoratedBarcodeView barcodeScannerView;

    /**
     * Initializes the content view for the scanning interface and returns the DecoratedBarcodeView
     * instance embedded within that layout.
     *
     * @return The {@link DecoratedBarcodeView} used for scanning.
     */
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.zxing_capture);
        return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
    }

    /**
     * Called when the activity is first created.
     * initializes the capture manager, and begins decoding barcodes.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        barcodeScannerView = initializeContent();

        ViewCompat.setOnApplyWindowInsetsListener(barcodeScannerView.getStatusView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    /** Called when the activity becomes foregrounded again; resumes the capture manager’s scanning. */
    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    /** Called when the activity is no longer in the foreground; pauses the capture manager’s scanning to conserve resources. */
    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    /** Called when the activity is destroyed; ensures the capture manager releases resources. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    /**
     * Saves the state of this activity (particularly for the scanner) so it can be restored if the system kills it.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    /**
     * Propagates permission request results to the capture manager (such as camera permission).
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Intercepts key down events and passes them to the barcode scanner view for proper handling (e.g., back key, volume).
     *
     * @param keyCode The value in event.getKeyCode().
     * @param event   Description of the key event.
     * @return true if the event was handled by the scanner view; otherwise falls back to default behavior.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
