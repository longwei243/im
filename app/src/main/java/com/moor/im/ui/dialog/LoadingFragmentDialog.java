package com.moor.im.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.ui.view.LoadingView;

/**
 * Created by long on 2015/7/6.
 */
public class LoadingFragmentDialog extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_loading, null);
        TextView title = (TextView) view
                .findViewById(R.id.id_dialog_loading_msg);
        title.setText("请稍等...");
        Dialog dialog = new Dialog(getActivity(), R.style.dialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        }catch (Exception e) {}
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        }catch (Exception e) {}

    }
}
