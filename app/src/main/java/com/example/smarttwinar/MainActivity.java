package com.example.smarttwinar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import static com.google.ar.core.ArCoreApk.InstallStatus.INSTALLED;
import static com.google.ar.core.ArCoreApk.InstallStatus.INSTALL_REQUESTED;

public class MainActivity extends AppCompatActivity {

    private boolean mUserRequestedInstall = true;
    private Session mSession = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 0);
        }

        try{
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success: Safe to create the AR session.

                        mSession = new Session(this);
                        Config config = new Config(mSession);
                        mSession.configure(config);
                        break;
                    case INSTALL_REQUESTED:
                        // When this method returns `INSTALL_REQUESTED`:
                        // 1. This activity will be paused.
                        // 2. The user is prompted to install or update Google Play
                        //    Services for AR (market://details?id=com.google.ar.core).
                        // 3. ARCore downloads the latest device profile data.
                        // 4. This activity is resumed. The next invocation of
                        //    requestInstall() will either return `INSTALLED` or throw an
                        //    exception if the installation or update did not succeed.
                        try{
                            ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(this, true);
                            switch (installStatus) {
                                case INSTALL_REQUESTED:
                                    Log.i("FUCK", "ARCore installation requested.");
                                    return;
                                case INSTALLED:
                                    return;
                            }
                        }catch (UnavailableException e){
                            Log.e("FUCK", "ARCore not installed", e);
                        }
                        mUserRequestedInstall = false;
                        return;
                }
            }
        }catch(UnavailableUserDeclinedInstallationException | UnavailableDeviceNotCompatibleException e) {
                    // Display an appropriate message to the user and return gracefully.
                    Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
                            .show();
                    return;
        } catch (UnavailableArcoreNotInstalledException e) {
            e.printStackTrace();
        } catch (UnavailableSdkTooOldException | UnavailableApkTooOldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSession.close();
    }
}