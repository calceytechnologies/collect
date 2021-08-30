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
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.tasks.InstanceServerUploaderTask;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormListItem;
import org.odk.collect.forms.instances.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.reactivex.annotations.Nullable;

public class FormManagementContractImpl implements FormManagementContract {

    @Inject
    FormDownloader formDownloader;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    /**
     * Initialise dagger injected constructor.
     */
    public FormManagementContractImpl() {
        Collect.getInstance().getComponent().inject(this);
    }


    @Override
    public synchronized void openForm(@NotNull Context context, @NotNull String formId,
                                      @Nullable String version) {
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

    @Override
    public synchronized void downloadFormDetails(@NotNull List<FormListItem> formListItems,
                                                 @NotNull DownloadFormsTaskListener listener) {
        if (formListItems != null && listener != null) {
            serverFormsDetailsFetcher.setFormList(formListItems);
            DownloadFormListTask formListTask = new DownloadFormListTask(serverFormsDetailsFetcher);
            formListTask.setDownloaderListener((formList, exception) -> {
                if (exception == null & formList != null) {
                    // form meta data download completes
                    downloadForms(formList, listener);
                } else {
                    // form meta data download failed
                    listener.formsDownloadingCancelled();
                }
            });
            formListTask.execute();
        }
    }

    @Override
    public void uploadForms(@NonNull @NotNull String[] formIds, @NotNull InstanceUploaderListener listener) {
        if (formIds != null && listener != null) {
            for (String formId : formIds) {
                List<Instance> instances = getUploadForms(formId);
                if (instances != null && !instances.isEmpty()) {
                    InstanceUploaderTask instanceUploaderTask = new InstanceServerUploaderTask();
                    instanceUploaderTask.setUploaderListener(listener);
                    instanceUploaderTask.setRepositories(instancesRepositoryProvider.get(),
                            formsRepositoryProvider.get(), settingsProvider);
                    instanceUploaderTask.execute(getInstanceIds(instances));
                } else {
                    listener.uploadCanceled();
                }
            }
        }
    }

    @Override
    public void removeUploadedForms(@NotNull Set<String> instanceIds) {
        // filters stream to upload
        Stream<Instance> instancesToDelete = instanceIds.stream()
                .map(id -> new InstancesRepositoryProvider(Collect.getApplication()).get().get(Long.parseLong(id)))
                .filter(instance -> instance.getStatus().equals(Instance.STATUS_SUBMITTED));

        if(instancesToDelete != null && instanceIds.size() != 0){
            // remove instances
            DeleteInstancesTask dit = new DeleteInstancesTask(instancesRepositoryProvider.get(), formsRepositoryProvider.get());
            dit.execute(instancesToDelete.map(Instance::getDbId).toArray(Long[]::new));
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

    /**
     * Get forms to upload.
     *
     * @param formId form id
     * @return List of instance
     */
    private List<Instance> getUploadForms(String formId) {
        return instancesRepositoryProvider.get().getAllByFormId(formId).stream()
                .filter(instance -> !instance.getStatus().equals(Instance.STATUS_INCOMPLETE)
                        && !instance.getStatus().equals(Instance.STATUS_SUBMITTED))
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

    /**
     * Download meta data cached forms from backend.
     *
     * @param formList forms
     * @param listener listener
     */
    private void downloadForms(List<ServerFormDetails> formList, DownloadFormsTaskListener listener) {
        DownloadFormsTask downloadFormsTask = new DownloadFormsTask(formDownloader);
        downloadFormsTask.setDownloaderListener(listener);
        downloadFormsTask.execute((ArrayList<ServerFormDetails>) formList);
    }
}
