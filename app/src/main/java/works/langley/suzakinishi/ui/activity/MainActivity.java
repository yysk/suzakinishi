package works.langley.suzakinishi.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.vov.vitamio.LibsChecker;
import works.langley.suzakinishi.Constant;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.ui.fragment.LicenseDialogFragment;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar_actionbar)
    Toolbar mToolbarActionbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_HOMEPAGE)));
                break;
            case R.id.action_email:
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(Constant.URL_MAIL)));
                break;
            case R.id.action_license:
                new LicenseDialogFragment().show(getFragmentManager(), "license_dialog_fragment");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
