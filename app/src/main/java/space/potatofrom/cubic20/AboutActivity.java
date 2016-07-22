package space.potatofrom.cubic20;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Fix links in the textviews not being able to be clicked
        ((TextView) findViewById(R.id.about_text_copyright))
                .setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.about_text_source_code))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

}
