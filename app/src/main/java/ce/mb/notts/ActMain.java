package ce.mb.notts;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ActMain extends AppCompatActivity {


    Button btnPlay;
    Button btnRefrsh;
    ProgressBar prgMain;
    EditText txtInput;
    EditText txtDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alay_main);

        // Vars initializing
        btnPlay = (Button) findViewById(R.id.alay_main_btnPlay);
        btnRefrsh = (Button) findViewById(R.id.alay_main_btnRefresh);
        prgMain = (ProgressBar) findViewById(R.id.alay_main_prgRefresh);
        txtInput = (EditText) findViewById(R.id.alay_main_txtInput);
        txtDuration = (EditText) findViewById(R.id.alay_main_nmrDuration);

        btnPlay.setEnabled(false);
        txtInput.setText("шуьгахь бара уьш");

        //for working with zip audio source
        final File _path = new File(getFilesDir() + "/tts/");
        // Unpacking the sound files
        if (!_path.exists()) {
            try {
                _path.mkdir();
                unpackZip(getAssets().open("tts.zip"), _path.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        NoTTS.init(this, getFilesDir() + "/tts/", Integer.parseInt(txtDuration.getText().toString()), prgMain, btnPlay);
        //  ttsDirPath = new File(Environment.getExternalStorageDirectory().getPath() + "/notts/");

        btnRefrsh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoTTS.loadSounds();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoTTS.PlaySentence(txtInput.getText().toString());
            }

        });
    }


    // Method for unpacking the zip files
    static boolean unpackZip(InputStream zipfile, String path) {
        try {
            path = path + "/";
            ZipInputStream zis;
            String filename;
            zis = new ZipInputStream(new BufferedInputStream(zipfile));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.delete();
                    fmd.mkdirs();
                    continue;
                }


                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Closing the application
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NoTTS.destroy();
    }


}
