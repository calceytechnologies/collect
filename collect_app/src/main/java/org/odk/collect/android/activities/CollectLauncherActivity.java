package org.odk.collect.android.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.R2;
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel;
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel;
import org.odk.collect.android.configure.qr.AppConfigurationGenerator;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectCreator;
import org.odk.collect.android.projects.SettingsConnectionMatcher;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.projects.ProjectsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CollectLauncherActivity extends AppCompatActivity {

    @Inject
    ProjectCreator projectCreator;

    @Inject
    ProjectsRepository projectsRepository;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    AppConfigurationGenerator appConfigurationGenerator;

    SettingsConnectionMatcher settingsConnectionMatcher;

    @Inject
    SplashScreenViewModel.Factory splashScreenViewModelFactoryFactory;

    @Inject
    CurrentProjectViewModel.Factory currentProjectViewModelFactory;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    private SplashScreenViewModel viewModel;

    private CurrentProjectViewModel currentProjectViewModel;



    @BindView(R2.id.btn_login)
    Button btnLogin;

    @BindView(R2.id.btn_mainmenu)
    Button btnMainMenu;

    @BindView(R2.id.btn_get_forms)
    Button btnGetForms;

    @BindView(R2.id.btn_open_forms)
    Button btnOpenForms;

    @BindView(R2.id.btn_upload_forms)
    Button btnUpload;



    public String test_URL = "https://kc.kobo.dreamsave.net/";
    public String test_user = "ganidu";
    public String test_pwd = "UdjS%W5T";
    //public String test_user = "asanka";
    //public String test_pwd = "UwUD#Q8W";

    public String test_form = "achbHHiVrNKvuhwqqfSNEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        setContentView(R.layout.activity_odklauncher);
        ButterKnife.bind(this);

        viewModel =  new ViewModelProvider(this, splashScreenViewModelFactoryFactory).get(SplashScreenViewModel.class);


        DaggerUtils.getComponent(getApplicationContext()).inject(this);
        settingsConnectionMatcher = new SettingsConnectionMatcher(projectsRepository, settingsProvider);
        ToastUtils.showLongToast("Initiate with Launcher activity ");


    }


    @OnClick({R2.id.btn_login})
    protected void login(){

        init();
    }

    @OnClick({R2.id.btn_mainmenu})
    protected void openMainMenu(){
        if (!viewModel.getShouldFirstLaunchScreenBeDisplayed()){
            ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity.class);
        }else{
            ToastUtils.showLongToast("Please do login");
        }

    }
    @OnClick({R2.id.btn_get_forms})
    protected void downloadForms(){

        String formId = test_form;
        Intent i = new Intent(getApplicationContext(),
                FormDownloadListActivity.class);
        i.putExtra(ApplicationConstants.BundleKeys.FORM_IDS, new String[]{formId});
        //i.putExtra(ApplicationConstants.BundleKeys.URL,"https://kc.kobo.dreamsave.net/");
        startActivity(i);
    }

    @OnClick({R2.id.btn_open_forms})
    protected void openForm(){
//
        long formdid = getFormId(test_form);
        if (formdid != 0){
            Uri formUri = FormsContract.getUri(currentProjectProvider.getCurrentProject().getUuid(), formdid );
            Intent intent = new Intent(this, FormEntryActivity.class);
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(formUri);
            intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
            startActivity(intent);
        }else{
            ToastUtils.showLongToast("Form not found");
        }


    }

    @OnClick({R2.id.btn_upload_forms})
    protected void uploadForm(){

        long instanceID = getInstanceId(test_form);

        if (instanceID != 0){
            long[] instanceIds = new long[]{instanceID};

            Intent i = new Intent(this, InstanceUploaderActivity.class);
            i.putExtra(FormEntryActivity.KEY_INSTANCES, instanceIds);
            startActivity(i);
        }else{
            ToastUtils.showLongToast("Complete form not found for upload");
        }

    }

    //Login
    private void init() {

        if (viewModel.getShouldFirstLaunchScreenBeDisplayed()){
            initializeLogin(test_URL,test_user, test_pwd );
        }else{

            currentProjectViewModel = new ViewModelProvider(this, currentProjectViewModelFactory).get(CurrentProjectViewModel.class);
            currentProjectViewModel.getCurrentProject().observe(this, project -> {

                ToastUtils.showLongToast("Already login to  " + project.getName());
                //endSplashScreen();
            });

        }
    }


    void initializeLogin( String url ,  String user ,  String pwd){
        String settingsJson = appConfigurationGenerator.getAppConfigurationAsJsonWithServerDetails(
                url,
                user,
                pwd
        );

        String UUID =  settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson);
        if(UUID != null){
            ToastUtils.showLongToast("Project already setup with " + UUID);

        }else{
            createProject(settingsJson);
        }

    }

    void createProject( String settingsJson) {
        projectCreator.createNewProject(settingsJson);
        // ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity.class);
        ToastUtils.showLongToast(getString(R.string.switched_project, currentProjectProvider.getCurrentProject().getName()));
    }


    //Get form id
    private long getFormId(String id){
        List<Form> formsFromDB = formsRepositoryProvider.get().getAll();
        if(formsFromDB != null ){
            for (Form form : formsFromDB){
                if(form.getFormId().equals(id)){
                    return form.getDbId();
                }
            }
        }
        return 0;
    }

    //Get completed form id from instance table
    private long getInstanceId(String id){
        List<Instance> instances = instancesRepositoryProvider.get().getAll();
        if(instances != null ){
            for (Instance instance : instances){
                if(instance.getFormId().equals(id)){
                    return instance.getDbId();
                }
            }
        }
        return 0;
    }

}