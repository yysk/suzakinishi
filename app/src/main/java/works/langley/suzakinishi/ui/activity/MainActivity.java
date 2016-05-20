package works.langley.suzakinishi.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.vov.vitamio.LibsChecker;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.ui.fragment.LicenseDialogFragment;
import works.langley.suzakinishi.util.IntentUtil;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar_actionbar)
    Toolbar mToolbarActionbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupToolbar();
    }

    private void setupToolbar() {
        mToolbarActionbar.setLogo(R.drawable.ic_stat);
        setSupportActionBar(mToolbarActionbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_web:
                startActivity(IntentUtil.createWebIntent());
                break;
            case R.id.action_email:
                startActivity(IntentUtil.createMailIntent());
                break;
            case R.id.action_license:
                new LicenseDialogFragment().show(getSupportFragmentManager(), "license_dialog_fragment");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
