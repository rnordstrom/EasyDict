package study.easydict;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Word extends AppCompatActivity {

    private EditText wordSearch;
    private TextView wordJp;
    private TextView wordMeaning;
    private TextView wordPronunc;
    private Button searchButton;
    private ProgressBar progress;

    private final String JISHO_BASE = "https://jisho.org/search/";
    private final String WEBLIO_BASE = "https://www.weblio.jp/content/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        setUp();
    }

    private void setUp() {
        wordSearch = (EditText) findViewById(R.id.word_search);
        wordJp = (TextView) findViewById(R.id.word_jp);
        wordMeaning = (TextView) findViewById(R.id.word_meaning);
        wordPronunc = (TextView) findViewById(R.id.word_pronunc);
        searchButton = (Button) findViewById(R.id.search_button);
        progress = (ProgressBar) findViewById(R.id.search_prog);

        progress.setVisibility(View.INVISIBLE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                new GetDataTask().execute(wordSearch.getText().toString());
            }
        });
    }

    private class GetDataTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... strings) {
            ArrayList<String> results = new ArrayList<>();

            try {
                Document jisho = Jsoup.connect(JISHO_BASE + strings[0]).get();
                Element jp =
                        jisho.selectFirst(".concept_light-representation .text");
                Element meanings =
                        jisho.selectFirst(".meanings-wrapper");

                Element tags = meanings.selectFirst(".meaning-tags");
                StringBuilder sb = new StringBuilder();

                for (Element meaning : meanings.select(".meaning-wrapper")) {
                    Element num = meaning.selectFirst(".meaning-definition-section_divider");

                    if (num == null) {
                        break;
                    }

                    Element text = meaning.selectFirst(".meaning-meaning");

                    sb.append(num.text())
                            .append(" ")
                            .append(text.text())
                            .append("\n");
                }

                Document weblio = Jsoup.connect(WEBLIO_BASE + strings[0]).get();
                Element midashigo = weblio.selectFirst(".midashigo");
                Element pronuncKana =
                        midashigo.selectFirst("b");
                Elements pronuncMora =
                        midashigo.select("span");
                StringBuilder sc = new StringBuilder();

                for (Element span : pronuncMora) {
                    if (!span.text().contains("［")) {
                        continue;
                    }

                    sc.append(span.text());
                }

                results.add(jp.text());
                results.add(tags.text() + "\n" + sb.deleteCharAt(sb.length() - 1).toString());
                results.add(pronuncKana.text() + " " + sc.toString());

                return results;
            } catch (Exception e) {
                e.printStackTrace();

                for (int i = 0; i < 3; i++) {
                    results.add("ERROR");
                }

                return results;
            }
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            wordJp.setText(strings.get(0));
            wordMeaning.setText(strings.get(1));
            wordPronunc.setText(strings.get(2));

            progress.setVisibility(View.INVISIBLE);
        }
    }
}
