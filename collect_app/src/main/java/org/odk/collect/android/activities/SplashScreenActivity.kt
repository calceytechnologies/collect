/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.configure.qr.AppConfigurationGenerator
import org.odk.collect.android.databinding.SplashScreenBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.*
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.ToastUtils
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class SplashScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var splashScreenViewModelFactoryFactory: SplashScreenViewModel.Factory

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var appConfigurationGenerator: AppConfigurationGenerator

    lateinit var viewModel: SplashScreenViewModel

    private lateinit var binding: SplashScreenBinding

    lateinit var settingsConnectionMatcher: SettingsConnectionMatcher

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DaggerUtils.getComponent(this).inject(this)
        viewModel = ViewModelProvider(this, splashScreenViewModelFactoryFactory)[SplashScreenViewModel::class.java]

        DaggerUtils.getComponent(applicationContext).inject(this)
        settingsConnectionMatcher = SettingsConnectionMatcher(projectsRepository, settingsProvider)

       // init()
        ActivityUtils.startActivityAndCloseAllOthers(this, CollectLauncherActivity::class.java)

    }

    private fun init() {
        when {
            viewModel.shouldFirstLaunchScreenBeDisplayed -> {

                //set login details
                initializeLogin("https://kc.kobo.dreamsave.net/","ganidu", "UdjS%W5T" )
                ActivityUtils.startActivityAndCloseAllOthers(this, FirstLaunchActivity::class.java)
            }
            viewModel.shouldDisplaySplashScreen -> startSplashScreen()
            else -> endSplashScreen()
        }
    }

    private fun endSplashScreen() {
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
    }

    private fun startSplashScreen() {
        if (viewModel.doesLogoFileExist) {
            binding.splashDefault.visibility = View.GONE
            binding.splash.setImageBitmap(viewModel.scaledSplashScreenLogoBitmap)
            binding.splash.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            delay(2000)
            endSplashScreen()
        }
    }

    fun initializeLogin(url: String, user: String , pwd: String){
        val settingsJson = appConfigurationGenerator.getAppConfigurationAsJsonWithServerDetails(
            url,
            user,
            pwd
        )

        settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->

                ToastUtils.showLongToast("Project alredy setup with " + uuid)

        } ?: run {

            createProject(settingsJson)
            Analytics.log(AnalyticsEvents.MANUAL_CREATE_PROJECT)
        }

    }

    fun createProject(settingsJson: String) {
        projectCreator.createNewProject(settingsJson)
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
        ToastUtils.showLongToast(getString(R.string.switched_project, currentProjectProvider.getCurrentProject().name))
    }
}
