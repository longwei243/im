package com.m7.imkfsdk.chat;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.moor.im.R;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.OnSubmitOfflineMessageListener;

/**
 * 离线留言对话框
 * Created by longwei
 */
public class OfflineMessageDialog extends DialogFragment {

    EditText id_et_content, id_et_phone, id_et_email;
    Button btn_cancel, btn_submit;
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the layout inflater
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_offline, null);
        id_et_content = (EditText) view.findViewById(R.id.id_et_content);
        id_et_phone = (EditText) view.findViewById(R.id.id_et_phone);
        id_et_email = (EditText) view.findViewById(R.id.id_et_email);

        btn_submit = (Button) view.findViewById(R.id.id_btn_submit);
        btn_cancel = (Button) view.findViewById(R.id.id_btn_cancel);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = id_et_content.getText().toString().trim();
                String phone = id_et_phone.getText().toString().trim();
                String email = id_et_email.getText().toString().trim();
                if(!"".equals(content)) {
                    if(!"".equals(phone) || !"".equals(email)) {
                        IMChatManager.getInstance().submitOfflineMessage(content, phone, email, new OnSubmitOfflineMessageListener() {
                            @Override
                            public void onSuccess() {
                                dismiss();
                                Toast.makeText(getActivity(), "提交留言成功", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailed() {
                                dismiss();
                                Toast.makeText(getActivity(), "提交留言失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else {
                    Toast.makeText(getActivity(), "请输入内容", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }
}
