package hairstyle.twod.com.hairstyles;

import android.app.Application;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by srikrishna on 27-09-2016.
 */
public class App extends Application {

    public static TextToSpeech tSpeech;
    @Override
    public void onCreate() {
        super.onCreate();
        tSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }
}
