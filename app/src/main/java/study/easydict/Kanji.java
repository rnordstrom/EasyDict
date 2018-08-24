package study.easydict;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Kanji extends AppCompatActivity {

    private EditText kanjiSearch;
    private TextView kanjiChar;
    private TextView kanjiMeaning;
    private ImageView kanjiImg;
    private ProgressBar progress;

    private String jishoBase;
    private String jishoTail;
    private String jitenonBase;
    private String jitenonTail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji);
        setUp();
    }

    private void setUp() {
        kanjiSearch = (EditText) findViewById(R.id.kanji_search);
        kanjiChar = (TextView) findViewById(R.id.kanji_char);
        kanjiMeaning = (TextView) findViewById(R.id.kanji_meaning);
        kanjiImg = (ImageView) findViewById(R.id.kanji_img);
        progress = (ProgressBar) findViewById(R.id.search_prog);

        jishoBase = getResources().getString(R.string.jisho_base);
        jishoTail = getResources().getString(R.string.jisho_tail);
        jitenonBase = getResources().getString(R.string.jitenon_base);
        jitenonTail = getResources().getString(R.string.jitenon_tail);

        progress.setVisibility(View.INVISIBLE);

        Button searchButton = (Button) findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                new GetDataTask().execute(kanjiSearch.getText().toString());
            }
        });
    }

    private class GetDataTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... strings) {
            ArrayList<String> results = new ArrayList<>();

            try {
                Document jisho = Jsoup.connect(jishoBase + strings[0] + jishoTail).get();
                Element chara = jisho.selectFirst(".character");
                Element meaning = jisho.selectFirst(".kanji-details__main-meanings");

                Document jitenon = Jsoup.connect(jitenonBase + strings[0] + jitenonTail).get();
                Element nextLink = jitenon.selectFirst(".searchtbtd2 a");
                String absUrl = nextLink.absUrl("href");

                Document jitenonKanji = Jsoup.connect(absUrl).get();
                Element img = jitenonKanji.selectFirst("#kanjileft img");
                String imgName = img.absUrl("src");

                results.add(chara.text());
                results.add(meaning.text());
                results.add(imgName);

                return results;
            } catch (Exception e) {
                e.printStackTrace();

                for (int i = 0; i < 2; i++) {
                    results.add("ERROR");
                }

                return results;
            }
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            kanjiChar.setText(strings.get(0));
            kanjiMeaning.setText(strings.get(1));

            new GetImageTask().execute(strings.get(2));

            progress.setVisibility(View.INVISIBLE);
        }
    }

    private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap img = null;

            try {
                InputStream in = new URL(strings[0]).openStream();
                img = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return img;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            kanjiImg.setImageBitmap(bitmap);

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

                return;
            }

            // TODO Enable this when testing is done
            /* MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,
                    UUID.randomUUID().toString(), " "); */
        }
    }
}
