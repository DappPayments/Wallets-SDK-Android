package mx.dapp.sdk.wallet.gui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import mx.dapp.sdk.wallet.R;
import mx.dapp.sdk.wallet.tool.DappWallet;

public class DappReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public static final String QR_STR_KEY = "str_code";

    private ZXingScannerView mScannerView;

    private static final int REQUEST_ACCESS_CAMERA = 225;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);

        mScannerView = new ZXingScannerView(this);
        mScannerView.setFormats(formats);
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkCameraPermission();
    }

    @Override
    public void onPause() {
        stopCamera();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_CAMERA) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                starCamera();
            } else {
                stopCamera();
                setResult(DappWallet.RESULT_PERMISSION_REJECTED, getIntent());
                finish();
            }
        }
    }


    private void checkCameraPermission() {
        int permissionCheck = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_ACCESS_CAMERA);
            }
        } else {
            starCamera();
        }
    }


    private void starCamera() {
        if (mScannerView != null) {
            mScannerView.setResultHandler(this);
            mScannerView.setAspectTolerance(0.5f);
            mScannerView.startCamera();
        }
    }

    private void stopCamera() {
        if (mScannerView != null) {
            mScannerView.stopCamera();
        }
    }


    @Override
    public void handleResult(Result result) {
        String code = result.getText();
        Intent intent = getIntent();
        intent.putExtra(QR_STR_KEY, code);
        setResult(Activity.RESULT_OK, intent);
        stopCamera();
        finish();
    }
}
