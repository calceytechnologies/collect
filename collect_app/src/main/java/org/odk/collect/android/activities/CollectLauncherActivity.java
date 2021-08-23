package org.odk.collect.android.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel;
import org.odk.collect.android.configure.qr.AppConfigurationGenerator;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectCreator;
import org.odk.collect.android.projects.SettingsConnectionMatcher;
import org.odk.collect.projects.ProjectsRepository;

import javax.inject.Inject;

public class CollectLauncherActivity extends AppCompatActivity {

/*    @Inject
    ProjectCreator projectCreator;


    @Inject
    ProjectsRepository projectsRepository;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    AppConfigurationGenerator appConfigurationGenerator;

    @Inject
    SettingsConnectionMatcher settingsConnectionMatcher;

    @Inject
    SplashScreenViewModel.Factory splashScreenViewModelFactoryFactory;


    SplashScreenViewModel viewModel;

    @Inject
    MainMenuViewModel.Factory viewModelFactory;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // DaggerUtils.getComponent(this).inject(this);
        setContentView(R.layout.activity_odklauncher);
       // viewModel = new ViewModelProvider(this, splashScreenViewModelFactoryFactory).get(ODKLauncher.class);
       // settingsConnectionMatcher = new SettingsConnectionMatcher(projectsRepository, settingsProvider)

        init();
    }

    private void init() {
//        when {
//            viewModel.shouldFirstLaunchScreenBeDisplayed -> {
//                initializeLogin("https://kc.kobo.dreamsave.net/","ganidu", "UdjS%W5T" )
//                //ActivityUtils.startActivityAndCloseAllOthers(this, FirstLaunchActivity::class.java)
//            }
//            viewModel.shouldDisplaySplashScreen -> startSplashScreen()
//            else -> endSplashScreen()
//        }
    }

    private void endSplashScreen() {
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity.class);
    }


}