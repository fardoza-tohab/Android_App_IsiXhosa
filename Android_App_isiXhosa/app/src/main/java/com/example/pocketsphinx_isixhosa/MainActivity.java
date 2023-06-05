package com.example.pocketsphinx_isixhosa;


import android.annotation.SuppressLint;
import android.os.Bundle;

        import android.Manifest;
        import android.app.Activity;
        import android.content.pm.PackageManager;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;

        import edu.cmu.pocketsphinx.Hypothesis;
        import edu.cmu.pocketsphinx.RecognitionListener;
        import edu.cmu.pocketsphinx.SpeechRecognizer;
        import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

        import java.io.File;
        import java.io.IOException;

public class MainActivity extends Activity implements RecognitionListener {

    /* Keyword we are looking for to activate menu */
    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "hello";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        try {
            setupRecognizer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupRecognizer() throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(getExternalFilesDir(null), "en-us-ptm"))
                .setDictionary(new File(getExternalFilesDir(null), "cmudict-en-us.dict"))
                .setKeywordThreshold(1e-20f)
                .getRecognizer();

        recognizer.addListener(this);
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        recognizer.startListening(KWS_SEARCH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    setupRecognizer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE))
            ((TextView) findViewById(R.id.textView)).setText("I heard the keyword!");
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.textView)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {}
}
