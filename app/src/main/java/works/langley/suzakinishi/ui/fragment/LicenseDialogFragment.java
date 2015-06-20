package works.langley.suzakinishi.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import timber.log.Timber;
import works.langley.suzakinishi.R;
import works.langley.suzakinishi.util.AssetUtil;

public class LicenseDialogFragment extends DialogFragment {

    public LicenseDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_license_dialog, null, false);
        TextView textView = (TextView) view.findViewById(R.id.text_license);
        textView.setText(loadLicenseText());

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