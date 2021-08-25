package org.odk.collect.android.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel;
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel;
import org.odk.collect.android.configure.qr.AppConfigurationGenerator;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectCreator;
import org.odk.collect.android.projects.SettingsConnectionMatcher;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.projects.ProjectsRepository;

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

    private SplashScreenViewModel viewModel;

    private CurrentProjectViewModel currentProjectViewModel;



    @BindView(R.id.btn_login)
    Button btnLogin;

    @BindView(R.id.btn_mainmenu)
    Button btnMainMenu;

    @BindView(R.id.btn_get_forms)
    Button btnGetForms;

    @BindView(R.id.btn_open_forms)
    Button btnOpenForms;

    @BindView(R.id.btn_upload_forms)
    Button btnUpload;


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


    @OnClick({R.id.btn_login})
    protected void login(){

        init();
    }

    @OnClick({R.id.btn_mainmenu})
    protected void openMainMenu(){
        if (!viewModel.getShouldFirstLaunchScreenBeDisplayed()){
            ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity.class);
        }else{
            ToastUtils.showLongToast("Please do login");
        }

    }
    @OnClick({R.id.btn_get_forms})
    protected void downloadForms(){


    }
    @OnClick({R.id.btn_open_forms})
    protected void openForm(){


    }
    @OnClick({R.id.btn_upload_forms})
    protected void uploadForm(){

    }

    private void init() {

        if (viewModel.getShouldFirstLaunchScreenBeDisplayed()){
            initializeLogin("https://kf.kobo.dreamsave.net/","asanka", "UwUD#Q8W" );
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
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity.class);
        ToastUtils.showLongToast(getString(R.string.switched_project, currentProjectProvider.getCurrentProject().getName()));
    }

}