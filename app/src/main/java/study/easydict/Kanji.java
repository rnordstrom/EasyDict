package study.easydict;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
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
        kanjiSearch = findViewById(R.id.kanji_search);
        kanjiChar = findViewById(R.id.kanji_char);
        kanjiMeaning = findViewById(R.id.kanji_meaning);
        kanjiImg = findViewById(R.id.kanji_img);
        progress = findViewById(R.id.search_prog);

        jishoBase = getResources().getString(R.string.jisho_base);
        jishoTail = getResources().getString(R.string.jisho_tail);
        jitenonBase = getResources().getString(R.string.jitenon_base);
        jitenonTail = getResources().getString(R.string.jitenon_tail);

        progress.setVisibility(View.INVISIBLE);

        Button searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                new GetDataTask().execute(kanjiSearch.getText().toString());
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
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
                String imgAddress = img.absUrl("src");

                results.add(chara.text());
                results.add(meaning.text());
                results.add(imgAddress);

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

    @SuppressLint("StaticFieldLeak")
    private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap img = null;
            InputStream in = null;
            String imgAddress = strings[0];
            String imgName = UUID.randomUUID().toString() + ".gif";

            try {
                in = new URL(imgAddress).openStream();
                img = BitmapFactory.decodeStream(in);

                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }

                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri downloadUri = Uri.parse(imgAddress);
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(imgName)
                        .setMimeType("image/gif")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                                File.separator + imgName);

                if (dm != null) {
                    dm.enqueue(request);
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            return img;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            kanjiImg.setImageBitmap(bitmap);
        }
    }
}
