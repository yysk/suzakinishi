package works.langley.suzakinishi.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.util.AssetUtil;

public class LicenseDialogFragment extends DialogFragment {

    public LicenseDialogFragment() {
    }

    @Bind(R.id.text_license)
    TextView mTextView;

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_license_dialog, null, false);
        ButterKnife.bind(this, view);
        mTextView.setText(loadLicenseText());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_license)
                .setView(view)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDismiss(dialog);
                    }
                });
        return builder.create();
    }

    private String loadLicenseText() {
        try {
            return AssetUtil.readTextFromAssets(getActivity(), "license.txt");
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
            return null;
        }
    }
}