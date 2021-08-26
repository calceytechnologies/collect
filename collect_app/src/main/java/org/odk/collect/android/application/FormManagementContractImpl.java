package org.odk.collect.android.application;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.tasks.InstanceServerUploaderTask;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.annotations.Nullable;

public class FormManagementContractImpl implements FormManagementContract {

    private final FormDownloader formDownloader;
    private final CurrentProjectProvider currentProjectProvider;
    private final SettingsProvider settingsProvider;
    private final FormsRepositoryProvider formsRepositoryProvider;
    private final InstancesRepositoryProvider instancesRepositoryProvider;

    /**
     * Initialise dagger injected constructor.
     *
     * @param currentProjectProvider project provider
     */
    @Inject
    public FormManagementContractImpl(FormDownloader formDownloader,
                                      SettingsProvider settingsProvider,
                                      CurrentProjectProvider currentProjectProvider,
                                      FormsRepositoryProvider formsRepositoryProvider,
                                      InstancesRepositoryProvider instancesRepositoryProvider) {

        this.formDownloader = formDownloader;
        this.settingsProvider = settingsProvider;
        this.currentProjectProvider = currentProjectProvider;
        this.formsRepositoryProvider = formsRepositoryProvider;
        this.instancesRepositoryProvider = instancesRepositoryProvider;
    }

    /**
     * Opens a form based on form id and version.
     *
     * @param context context context
     * @param formId  form id to access
     * @param version form version
     */

    @Override
    public synchronized void openForm(@NotNull Context context, @NotNull String formId, @Nullable String version) {
        if (formId != null) {
            Form form = getForm(formId, version);
            if (form != null) {
                Uri formUri = FormsContract.getUri(currentProjectProvider
                        .getCurrentProject().getUuid(), form.getDbId());
                Intent intent = new Intent(context, FormEntryActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.setData(formUri);
                intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.EDIT_SAVED);
                context.startActivity(intent);
            }
        }
    }

    /**
     * Download forms based on provided list of server details.
     * <note>This could be a single instance or multiple instances</note>
     *
     * @param serverFormDetails list of server details
     * @param listener          listener to detect download execution
     */

    @Override
    public synchronized void downloadForms(@NotNull ArrayList<ServerFormDetails> serverFormDetails,
                                           @Nullable DownloadFormsTaskListener listener) {
        if (serverFormDetails != null && !serverFormDetails.isEmpty() && listener != null) {
            DownloadFormsTask downloadFormsTask = new DownloadFormsTask(formDownloader);
            downloadFormsTask.setDownloaderListener(listener);
            downloadFormsTask.execute(serverFormDetails);
        }
    }

    /**
     * Uploads given formIds to backend server.
     *
     * @param formIds  list of form ids
     * @param listener listener to detect form upload
     */
    @Override
    public void uploadForms(@NonNull @NotNull String[] formIds, @NotNull InstanceUploaderListener listener) {
        if (formIds != null && listener != null) {
            for (String formId : formIds) {
                List<Instance> instances = getUploadForms(formId);
                if (instances != null && !instances.isEmpty()) {
                    InstanceUploaderTask instanceUploaderTask = new InstanceServerUploaderTask();
                    instanceUploaderTask.setUploaderListener(listener);
                    instanceUploaderTask.setRepositories(instancesRepositoryProvider.get(), formsRepositoryProvider.get(), settingsProvider);
                    instanceUploaderTask.execute(getInstanceIds(instances));
                }
            }
        }
    }

    /**
     * Get latest form based on form id and version
     *
     * @param formId  formId
     * @param version version
     * @return
     */
    private Form getForm(String formId, String version) {
        return formsRepositoryProvider.get().getLatestByFormIdAndVersion(formId, version);
    }

    private List<Instance> getUploadForms(String formId) {
        return instancesRepositoryProvider.get().getAllByFormId(formId).stream()
                .filter(instance -> !instance.getStatus().equals(Instance.STATUS_INCOMPLETE)
                        && !instance.getStatus().equals(Instance.STATUS_COMPLETE))
                .collect(Collectors.toList());
    }

    /**
     * Get list of db ids from instances.
     *
     * @param instances instances
     * @return List of ids
     */
    private Long[] getInstanceIds(List<Instance> instances) {
        return instances.stream().map(Instance::getDbId).toArray(Long[]::new);
    }
}
