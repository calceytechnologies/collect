package org.odk.collect.android.formentry.formutils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MessageDialog extends Dialog {

    @BindView(R2.id.txt_title)
    TextView tvTitle;

    @BindView(R2.id.txt_desc)
    TextView tvDesc;

    @BindView(R2.id.btn_cancel)
    Button btnCancel;

    @BindView(R2.id.btn_ok)
    Button btnOk;

    private final String CHARACTER_NEXT_LINE = "\n";
    private final String CHARACTER_NEXT_LINE_HTML = "<br>";

    private String title;
    private String description;
    private String cancelText;
    private String confirmText;
    private ConfirmDialogListener listener;

    public MessageDialog(@NonNull Context context,
                         String title,
                         String description,
                         ConfirmDialogListener listener) {
        super(context);
        this.title = title;
        this.description = description;
        this.listener = listener;
    }

    public MessageDialog(@NonNull Context context, String title, String description) {
        super(context);
        this.title = title;
        this.description = description;
    }

    public void updateDescription(String description) {
        if (tvDesc != null) {
            tvDesc.setText(description);
        }
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout for dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirm);
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ButterKnife.bind(this);
        tvTitle.setText(title);

        // fix line break issue when show as html text.
        if (description !=null){
            description =  description.replace(CHARACTER_NEXT_LINE,CHARACTER_NEXT_LINE_HTML);
        }

        tvDesc.setText(Html.fromHtml(description));
        if(title!=null && !title.isEmpty()){
            tvTitle.setVisibility(View.VISIBLE);
        }else{
            tvTitle.setVisibility(View.GONE);
        }


        // Set Button is cancel/no
        if (cancelText != null && !cancelText.isEmpty()){
            btnCancel.setText(cancelText);
            btnCancel.setVisibility(View.VISIBLE);
        }else{
            btnCancel.setVisibility(View.GONE);
        }


        // Set content button
        if (confirmText != null && !confirmText.isEmpty()){
            btnOk.setText(confirmText);
        }

    }

    @OnClick(R2.id.btn_ok)
    public void confirm() {
        if (listener != null) {
            listener.onConfirmDialogClick();
        }
        dismiss();
    }

    @OnClick(R2.id.btn_cancel)
    public void close() {
        if (listener != null) {
            listener.onCancelDialogClick();
        }
        dismiss();
    }
}
