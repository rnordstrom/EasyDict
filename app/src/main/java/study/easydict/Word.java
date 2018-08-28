package study.easydict;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Locale;

import study.easydict.data.WordDefinition;

public class Word extends AppCompatActivity {

    private EditText wordSearch;
    private TextView wordJp;
    private TextView wordMeaning;
    private TextView wordPronunc;
    private TextView numForms;
    private ProgressBar progress;

    private String jishoBase;
    private String weblioBase;
    private String currentWord;
    private int nextCount;
    private int formCount;

    private ArrayList<String> tagFilter;
    private ArrayList<WordDefinition> currOtherForms;

    private String errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        setUp();
    }

    private void setUp() {
        wordSearch = findViewById(R.id.word_search);
        wordJp = findViewById(R.id.word_jp);
        wordMeaning = findViewById(R.id.word_meaning);
        wordPronunc = findViewById(R.id.word_pronunc);
        numForms = findViewById(R.id.num_forms);
        progress = findViewById(R.id.search_prog);
        Button searchButton = findViewById(R.id.search_button);
        Button prevButton = findViewById(R.id.prev_button);
        Button nextButton = findViewById(R.id.next_button);
        Button formMinusButton = findViewById(R.id.form_minus_button);
        Button formPlusButton = findViewById(R.id.form_plus_button);

        jishoBase = getResources().getString(R.string.jisho_base);
        weblioBase = getResources().getString(R.string.weblio_base);

        tagFilter = new ArrayList<>();
        tagFilter.add("Wikipedia definition");
        tagFilter.add("Other forms");
        tagFilter.add("Place");

        currOtherForms = new ArrayList<>();

        progress.setVisibility(View.INVISIBLE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                currentWord = wordSearch.getText().toString();
                nextCount = 0;
                formCount = 0;
                currOtherForms.clear();
                errorMsg = getResources().getString(R.string.error_msg);

                new GetDataTask().execute(currentWord);

                wordSearch.setText("");
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);

                formCount = 0;
                currOtherForms.clear();

                if (nextCount > 0) {
                    nextCount--;
                }

                errorMsg = getResources().getString(R.string.error_msg);

                new GetDataTask().execute(currentWord);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);

                formCount = 0;
                currOtherForms.clear();

                nextCount++;
                errorMsg = getResources().getString(R.string.limit_msg);

                new GetDataTask().execute(currentWord);
            }
        });

        formMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);

                formCount--;

                if (formCount < 0) {
                    formCount = currOtherForms.size() - 1;
                }

                WordDefinition def = currOtherForms.get(formCount);

                if (!def.isLookedUp()) {
                    new GetOtherFormsTask().execute(def);
                } else {
                    progress.setVisibility(View.INVISIBLE);

                    wordJp.setText(def.getWordJp());
                    wordMeaning.setText(def.getEngDef());
                    wordPronunc.setText(def.getFurigana());
                }
            }
        });

        formPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);

                formCount++;

                if (formCount >= currOtherForms.size()) {
                    formCount = 0;
                }

                WordDefinition def = currOtherForms.get(formCount);

                if (!def.isLookedUp()) {
                    new GetOtherFormsTask().execute(def);
                } else {
                    progress.setVisibility(View.INVISIBLE);

                    wordJp.setText(def.getWordJp());
                    wordMeaning.setText(def.getEngDef());
                    wordPronunc.setText(def.getFurigana());
                }
            }
        });
    }

    public WordDefinition getFieldData(int n, String... strings) {
        WordDefinition result = new WordDefinition("", "", "", 0);

        try {
            Document jisho = Jsoup.connect(jishoBase + strings[0]).get();
            Element jp =
                    jisho.select(".concept_light-representation .text").get(n);
            Element meanings =
                    jisho.select(".meanings-wrapper").get(n);

            StringBuilder sb = new StringBuilder();

                for (Element item : meanings.children()) {

                if ((item.hasClass("meaning-tags")
                        && tagFilter.contains(item.text()))) {
                    break;
                }

                if (item.hasClass("meaning-wrapper")) {
                    Element num = item.selectFirst(".meaning-definition-section_divider");
                    Element text = item.selectFirst(".meaning-meaning");
                    Element supp = item.selectFirst(".sense-tag");

                    sb.append(num.text())
                            .append(" ")
                            .append(text.text());

                    if (supp != null && supp.text().contains("Usually written using kana alone")) {
                        sb.append(" ")
                                .append("(")
                                .append(supp.text())
                                .append(")");
                    }

                    sb.append("\n");
                }
                else if (item.hasClass("meaning-tags")) {
                    if (item.elementSiblingIndex() > 0) {
                        sb.append("\n");
                    }

                    sb.append(item.text())
                            .append("\n");
                }
            }

            String engDef = sb.deleteCharAt(sb.length() - 1).toString();
            Elements otherForms = meanings.select(".break-unit");

            for (Element e : otherForms) {
                String text = e.text();
                String w;
                String f;

                if (!text.contains(" ")) {
                    w = text;
                    f = w;
                } else {
                    w = text.substring(0, text.indexOf(" "));
                    f = text.substring(text.indexOf("【") + 1, text.indexOf("】"));
                }

                currOtherForms.add(new WordDefinition(w, engDef, f, 0));
            }

            Element furigana = jisho.select(".concept_light-representation .furigana").get(n);
            String wordJpText = jp.text();
            String hiragana = flattenHiragana(furigana.children(), wordJpText);
            boolean hasRepeated = false;
            String wordJpTextC = "";

            if (wordJpText.contains("々")) {
                hasRepeated = true;

                int index = wordJpText.indexOf("々");
                StringBuilder repeatedChars = new StringBuilder(wordJpText);

                repeatedChars.replace(index, index + 1,
                        wordJpText.substring(index - 1, index));

                wordJpTextC = wordJpText;
                wordJpText = repeatedChars.toString();
            }

            if (hasRepeated) {
                wordJpText = wordJpTextC;
            }

            result = new WordDefinition(wordJpText, engDef, weblioLookUp(wordJpText, hiragana),
                    otherForms.size());
            result.setLookedUp(true);
            currOtherForms.add(0, result);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setHasException(true);

            return result;
        }
    }

    private String weblioLookUp(String word, String hiragana) {
        try {
            Document weblio = Jsoup.connect(weblioBase + word).get();
            Elements midashigos = weblio.select(".midashigo");
            Element midashigo = null;

            for (Element m : midashigos) {
                String midashiT = m.text();
                Element midashiB = m.selectFirst("b");
                String kanji = "";

                if (midashiT == null || midashiB == null) {
                    break;
                }

                try {
                    kanji = midashiT.substring(midashiT.indexOf("【") + 1, midashiT.indexOf("】"));
                } catch (StringIndexOutOfBoundsException s) {
                    s.printStackTrace();
                }

                String cleanKanji = kanji.replaceAll("・", "")
                        .replaceAll("\\s+", "")
                        .replaceAll("▽", "")
                        .replaceAll("▼", "");

                if (cleanKanji.isEmpty()
                        && word.equals(midashiB.text()
                        .replaceAll("\\s+", "")
                        .replaceAll("・", ""))) {
                    midashigo = m;
                    break;
                } else if (cleanKanji.contains(word)
                        && hiragana.equals(midashiB.text()
                        .replaceAll("\\s+", "")
                        .replaceAll("・", ""))) {
                    midashigo = m;
                    break;
                }
            }

            String pronuncKanaText;
            String pronuncMoraText;

            if (midashigo == null) {
                pronuncKanaText = hiragana;
                pronuncMoraText = "";
            } else {
                pronuncKanaText = midashigo.selectFirst("b").text();

                Elements pronuncMora =
                        midashigo.select("span");
                StringBuilder sc = new StringBuilder();

                for (Element span : pronuncMora) {
                    if (!span.text().contains("［")) {
                        continue;
                    }

                    sc.append(span.text());
                }

                pronuncMoraText = sc.toString();
            }

            return pronuncKanaText + " " + pronuncMoraText;
        } catch (Exception e) {
            e.printStackTrace();

            return "";
        }
    }

    private String flattenHiragana(Elements furigana, String word) {
        StringBuilder sb = new StringBuilder();
        Element e;

        for (int i = 0; i < word.length(); i++) {
            e = furigana.get(i);
            char wordSym = word.charAt(i);

            if (e.text().isEmpty() && (int) wordSym > 0x3040 && (int) wordSym < 0x30FF) {
                sb.append(word.charAt(i));
            } else {
                sb.append(e.text());
            }
        }

        return sb.toString();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetDataTask extends AsyncTask<String, Void, WordDefinition> {
        @Override
        protected WordDefinition doInBackground(String... strings) {
            return getFieldData(nextCount, strings);
        }

        @Override
        protected void onPostExecute(WordDefinition def) {
            if (def.isHasException()) {
                Snackbar s = Snackbar.make(findViewById(R.id.clayout), errorMsg, Snackbar.LENGTH_SHORT);
                s.show();
            } else {
                wordJp.setText(def.getWordJp());
                wordMeaning.setText(def.getEngDef());
                wordPronunc.setText(def.getFurigana());
                numForms.setText(String.format(Integer.toString(def.getNumForms()), Locale.getDefault()));
            }

            progress.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetOtherFormsTask extends AsyncTask<WordDefinition, Void, WordDefinition> {
        @Override
        protected WordDefinition doInBackground(WordDefinition... wordDefinitions) {
            WordDefinition def = wordDefinitions[0];
            String p = weblioLookUp(def.getWordJp(), def.getFurigana());

            def.setFurigana(p);
            def.setLookedUp(true);

            return def;
        }

        @Override
        protected void onPostExecute(WordDefinition def) {
            if (def.isHasException()) {
                Snackbar s = Snackbar.make(findViewById(R.id.clayout),
                        getResources().getString(R.string.forms_fail_msg), Snackbar.LENGTH_SHORT);
                s.show();
            } else {
                wordJp.setText(def.getWordJp());
                wordMeaning.setText(def.getEngDef());
                wordPronunc.setText(def.getFurigana());
            }

            progress.setVisibility(View.INVISIBLE);
        }
    }
}
