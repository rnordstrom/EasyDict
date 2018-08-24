package study.easydict;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LookUp extends AppCompatActivity {

    private Button kanjiButton;
    private Button wordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_up);
        setUp();
    }

    private void setUp() {
        kanjiButton = (Button) findViewById(R.id.kanji_button);
        wordButton = (Button) findViewById(R.id.word_button);

        final Intent kanjiIntent = new Intent(this, Kanji.class);
        kanjiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(kanjiIntent);
            }
        });

        final Intent wordIntent = new Intent(this, Word.class);
        wordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(wordIntent);
            }
        });
    }
}
