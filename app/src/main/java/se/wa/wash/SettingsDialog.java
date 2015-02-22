package se.wa.wash;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Created by peter on 22/02/15.
 *
 *
 */
public class SettingsDialog extends DialogFragment {

    Context mContext;

    public SettingsDialog() {
    }

    public SettingsDialog(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("API Settings Missing or Invalid!")
                .setPositiveButton("Go To Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Start settings activity here
                        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                        startActivity(new Intent(mContext,SettingsActivity.class));
                    }
                })
                .setNegativeButton("Abort", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Cancel and abort everything
                    }
                });
        return builder.create();
    }
}
