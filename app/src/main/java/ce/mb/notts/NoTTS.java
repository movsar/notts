package ce.mb.notts;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Movsar Bekaev on 12/04/2016.
 */
public class NoTTS {
    private static NoTTS instance;

    public static int mIterator = 0;
    public static int sounds_count = 0;

    // We get string from alpha enum and play audios with corresponding indexes
    public enum alpha {
        а, аь, б, в, г, гӏ, д, е, ё, ж, з, и, й, к, кх, къ, кӏ, л, м, н,
        о, оь, п, пӏ, р, с, т, тӏ, у, уь, ф, х, хь, хӏ, ц, цӏ, ч, чӏ, ш, щ,
        ъ, ы, ь, э, ю, юь, я, яь, ӏ, ччӏ, ггӏ, ккх, ккъ, ккӏ, ппӏ, ттӏ, ххь, ххӏ, ццӏ
    }

    public static SoundPool sounds;
    public static final Map<String, Integer> alphaFiles = new HashMap<>();


    private static Context _ctx;
    private static File _path;
    private static Integer _delay;
    private static ProgressBar _prb;
    private static Button _btn;

    public static void init(Context ctx, String path, Integer delay, final ProgressBar prb, Button btn) {
        init(ctx, path, delay, prb);

        _btn = btn;
        NoTTS.mIterator = 1;
        NoTTS.sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (NoTTS.mIterator == (NoTTS.sounds_count)) {
                    _btn.setEnabled(true);
                    _prb.setProgress(0);
                    NoTTS.mIterator = 1;
                } else {
                    NoTTS.mIterator++;
                    _prb.incrementProgressBy(1);
                }
            }
        });
    }

    public static void init(Context ctx, String path, Integer delay, final ProgressBar prb) {
        _ctx = ctx;
        _path = new File(path);
        _delay = delay;
        _prb = prb;
        if (instance == null) {
            instance = new NoTTS();
        }

        loadSounds();
    }

    public static void loadSounds() {
           /* Reverse method for sounds initializing
            for (alpha a : alpha.values()) {
                Log.d("123", a.name() + " - " + (a.ordinal() + 1));
                //    alphaFiles.put(a.name(), sounds.load(_path.getAbsolutePath() + "/" + (a.ordinal() + 1) + "_1.flac", 1));
            }
            */

        // Creating a SoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            sounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(4)
                    .build();
        } else {
            sounds = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        }


        // Loading the sounds
        new Thread(new Runnable() {
            public void run() {
                File[] directoryListing = _path.listFiles();
                NoTTS.sounds_count = directoryListing.length;

                _prb.setMax(NoTTS.sounds_count - 1);
                _prb.setProgress(0);

                String letter = "";
                String key = "";
                if (directoryListing != null) {
                    for (File child : directoryListing) {
                        letter = String.valueOf(NoTTS.alpha.values()[Integer.parseInt(child.getName().split("_")[0]) - 1]);
                        key = letter + "_" + child.getName().split("_")[1];
                        key = key.substring(0, key.length() - 5);
                        NoTTS.alphaFiles.put(key, NoTTS.sounds.load(_path.getAbsolutePath() + "/" + child.getName(), 1));
                    }
                }
            }
        }).start();
    }

    public static NoTTS getInstance() {
        return instance;
    }


    public static void PlaySentence(String sentence) {

        String _sentence = formAudioString(sentence.replaceAll("\\W", " ").replace("  ", " ").toLowerCase().split(" "));
        int firstIndex = 0;
        int lastIndex = 0;

        for (String word : _sentence.split(" ")) {
            String[] phonemes = word.split("\\|");

            // First phoneme
            firstIndex = 0;
            // Last phoneme
            lastIndex = phonemes.length - 1;

            mIterator = 0;
            for (String phoneme : phonemes) {
                if (!phoneme.equals("")) {
                    if ((mIterator == lastIndex) && (alphaFiles.get(phoneme + "_3") != null))
                        if (mIterator > 0 && phoneme.equals("а") && phonemes[mIterator - 1].equals(phoneme))
                            // When last characters are "аа"
                            sayIt(alphaFiles.get(phoneme + "_4"));
                        else
                            sayIt(alphaFiles.get(phoneme + "_3"));
                    else if ((mIterator == firstIndex) && (alphaFiles.get(phoneme + "_1") != null))
                        // The first phoneme
                        sayIt(alphaFiles.get(phoneme + "_1"));
                    else if (alphaFiles.get(phoneme + "_2") != null)
                        // Middle phoneme when exists
                        sayIt(alphaFiles.get(phoneme + "_2"));
                    else
                        // Middle phoneme of first type
                        sayIt(alphaFiles.get(phoneme + "_1"));
                } else {
                    sleep(190);
                }
                sleep(_delay);

                mIterator++;

            }


        }
    }

    // Digesting the input
    static String purify(final String input) {
        String output = "";
        output = input.replaceAll("1|l|I", "ӏ");
        output = output.replace("щ", "ш");
        return output;
    }

    // Prepare the sentence
    static String formAudioString(final String[] input) {
        String sentence = "";
        for (String w : input) {
            w = purify(w);
            w = w.replaceAll(".", "|$0|").replace("||", "|");

            w = w.replace("ч|ч|ӏ", "ччӏ");
            w = w.replace("г|г|ӏ", "ггӏ");
            w = w.replace("к|к|х", "ккх");
            w = w.replace("к|к|ъ", "ккъ");
            w = w.replace("к|к|ӏ", "ккӏ");
            w = w.replace("п|п|ӏ", "ппӏ");
            w = w.replace("т|т|ӏ", "ттӏ");
            w = w.replace("х|х|ь", "ххь");
            w = w.replace("х|х|ӏ", "ххӏ");
            w = w.replace("ц|ц|ӏ", "ццӏ");

            w = w.replace("а|ь", "аь");
            w = w.replace("г|ӏ", "гӏ");
            w = w.replace("к|х", "кх");
            w = w.replace("к|ъ", "къ");
            w = w.replace("к|ӏ", "кӏ");
            w = w.replace("о|ь", "оь");
            w = w.replace("п|ӏ", "пӏ");
            w = w.replace("т|ӏ", "тӏ");
            w = w.replace("у|ь", "уь");
            w = w.replace("х|ь", "хь");
            w = w.replace("х|ӏ", "хӏ");
            w = w.replace("ц|ӏ", "цӏ");
            w = w.replace("х|ӏ", "хӏ");
            w = w.replace("ч|ӏ", "чӏ");

            Log.d("123", w.substring(w.length() - 2));
            if (w.length() < 6 && w.substring(w.length() - 2).equals("а|")) {
                w = w + "|н";
            }
            sentence = sentence + trim(w, new String[]{"|"}) + " ";
        }

        return sentence;
    }

    // Custom trimming
    static String trim(String s, String[] chars) {
        s = s.trim();
        for (String c : chars) {
            s = s.replaceAll("^" + Pattern.quote(c) + "+(\\w)", "$1");
            s = s.replaceAll("(\\w)" + Pattern.quote(c) + "+$", "$1");
        }
        return s;
    }


    private static float LEFTVOLUME = 1;
    private static float RIGHTVOLUME = 1;
    private static int PRIORITY = 1;
    private static float FREQUENCY = 1;

    // Voice the sound
    private static void sayIt(final Integer afid) {
        if (afid == 0) {
            sleep(130);
        } else {
            sounds.play(afid,
                    LEFTVOLUME,
                    RIGHTVOLUME,
                    PRIORITY,
                    0,
                    FREQUENCY);

        }
    }

    // Thread sleep method
    private static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void destroy(){
        sounds.release();
        sounds = null;
        instance = null;
    }
}
