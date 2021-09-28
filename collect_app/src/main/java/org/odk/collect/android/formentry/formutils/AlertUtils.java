package org.odk.collect.android.formentry.formutils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;


public class AlertUtils {

    public static void showMessage(@NonNull Context context,
                                   @StringRes int title,
                                   @StringRes int message,
                                   @StringRes int buttonTitle,
                                   @StringRes int navButtonTitle,
                                   ConfirmDialogListener listener) {
        showMessage(context,
            context.getString(title),
            context.getString(message),
            buttonTitle,
            navButtonTitle,
            listener);
    }

    public static void showMessage(@NonNull Context context,
                                   @StringRes int title,
                                   @StringRes int message,
                                   @StringRes int buttonTitle) {
        showMessage(context, title, context.getString(message), buttonTitle);
    }

    public static void showMessage(@NonNull Context context,
                                   @StringRes int title,
                                   @StringRes int message,
                                   @StringRes int buttonTitle,
                                   ConfirmDialogListener listener) {

        if (!((Activity) context).isFinishing()) {
            MessageDialog dialog = new MessageDialog(
                context,
                context.getString(title),
                context.getString(message),
                listener
            );
            dialog.setConfirmText(context.getString(buttonTitle));
            dialog.setCanceledOnTouchOutside(false);

            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }



    public static void showMessage(@NonNull Context context,
                                   String title,
                                   String message,
                                   @StringRes int buttonTitle,
                                   @StringRes int navButtonTitle,
                                   ConfirmDialogListener listener) {
        if (!((Activity) context).isFinishing()) {
            MessageDialog dialog = new MessageDialog(
                context,
                title,
                message,
                listener
            );
            dialog.setCancelText(context.getString(navButtonTitle));
            dialog.setConfirmText(context.getString(buttonTitle));
            dialog.setCanceledOnTouchOutside(false);

            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    public static void showMessage(@NonNull Activity activity,
                                   @StringRes int title,
                                   String message,
                                   @StringRes int buttonTitle,
                                   ConfirmDialogListener listener) {
        if (!activity.isFinishing()) {
            MessageDialog dialog = new MessageDialog(
                activity,
                activity.getString(title),
                message,
                listener
            );
            dialog.setConfirmText(activity.getString(buttonTitle));
            dialog.setCanceledOnTouchOutside(false);

            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    public static void showMessage(@NonNull Context context,
                                   @StringRes int title,
                                   String message,
                                   @StringRes int buttonTitle,
                                   @StringRes int navButtonTitle,
                                   ConfirmDialogListener listener) {
        showMessage(context, context.getString(title), message, buttonTitle, navButtonTitle, listener);
    }

    public static void showMessage(@NonNull Context context,
                                   @StringRes int title,
                                   String message,
                                   @StringRes int buttonTitle) {
        if (!((Activity) context).isFinishing()) {
            MessageDialog dialog = new MessageDialog(
                context,
                context.getString(title),
                message);
            dialog.setConfirmText(context.getString(buttonTitle));
            dialog.setCanceledOnTouchOutside(false);

            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

}
