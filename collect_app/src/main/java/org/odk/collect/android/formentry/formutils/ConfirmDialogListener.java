package org.odk.collect.android.formentry.formutils;

public interface ConfirmDialogListener {
    void onConfirmDialogClick();

    default void onCancelDialogClick() {
        // If you want handle cancel dialog then override
    }
}
