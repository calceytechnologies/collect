package org.odk.collect.android.application;

import android.app.Activity;
import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.forms.FormListItem;

import java.util.List;
import java.util.Set;

import io.reactivex.annotations.Nullable;

/**
 * interface to communicate between ODK module and Application module.
 * implementation is responsible to execute following tasks.
 * <p>
 * - Authenticate ODK module with backend server.
 * - Fetch forms from backend server (This could come as a single URL or list of URLs to download)
 * - Upload completed forms to backend server.
 * - Open downloaded forms based on given form id.
 */

public interface FormManagementContract {

    /**
     * Opens locally saved ODK form.
     *
     * @param activity activity
     * @param formId  form id to access
     * @param requestCode request code to notify returned actions
     */
    void openForm(@NotNull Activity activity, @NotNull String formId,@NotNull int requestCode);


    /**
     * Download forms based on provided list of server details.
     * <note>This could be a single instance or multiple instances</note>
     *
     * @param formListItems list of server details
     * @param listener      listener to detect download execution
     */
    void downloadFormDetails(@NotNull List<FormListItem> formListItems,
                             @NotNull DownloadFormsTaskListener listener);

    /**
     * Uploads given formIds to backend server.
     *
     * @param formIds  list of form ids
     * @param listener listener to detect form upload
     */
    void uploadForms(@NotNull String[] formIds, @NotNull InstanceUploaderListener listener);


    /**
     * Remove uploaded forms.
     *
     * @param instanceIds instance ids
     */
    void removeUploadedForms(@NotNull Set<String> instanceIds);
}
