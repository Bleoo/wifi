package com.leo.wifi;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/5/18.
 */
public class InputDialog extends Dialog {

    private TextView tv_title;
    private EditText et_context;
    private DoneListener mDoneListener;

    public InputDialog(Context context) {
        this(context, R.style.custom_dialog);
    }

    public InputDialog(Context context, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.dialog_input);
        setCanceledOnTouchOutside(true);

        // 设置为80%宽度
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        lp.width = (int) (metrics.widthPixels * 0.8);
        window.setAttributes(lp);

        tv_title = (TextView) findViewById(R.id.tv_title);
        et_context = (EditText) findViewById(R.id.et_context);

        et_context.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (mDoneListener != null) {
                        mDoneListener.done(et_context.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    public void setTitle(String title) {
        tv_title.setText(title);
    }

    public void setDoneListener(DoneListener doneListener) {
        mDoneListener = doneListener;
    }

    @Override
    public void show() {
        et_context.setText("");
        super.show();
    }

    public interface DoneListener {
        void done(String context);
    }
}
