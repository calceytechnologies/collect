/*
 * Copyright (C) 2017 University of Washington
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

package org.odk.collect.android.application;

import static org.odk.collect.android.preferences.keys.MetaKeys.KEY_GOOGLE_BUG_154855417_FIXED;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_API_KEY;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_APP_LANGUAGE;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_APP_THEME;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_SERVER_URL;

import android.app.Application;
import android.os.StrictMode;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.configure.qr.AppConfigurationGenerator;
import org.odk.collect.android.externaldata.ExternalDataManager;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectCreator;
import org.odk.collect.android.projects.ProjectImporter;
import org.odk.collect.android.projects.SettingsConnectionMatcher;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.forms.Form;
import org.odk.collect.projects.DaggerProjectsDependencyComponent;
import org.odk.collect.projects.Project;
import org.odk.collect.projects.ProjectsDependencyComponent;
import org.odk.collect.projects.ProjectsDependencyComponentProvider;
import org.odk.collect.projects.ProjectsDependencyModule;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.strings.LocalizedApplication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

public class Collect implements LocalizedApplication, ProjectsDependencyComponentProvider {

    public static final String defaultSysLanguage = "en";
    private static Collect singleton;

    @Inject
    ApplicationInitializer applicationInitializer;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    ProjectImporter projectImporter;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Inject
    ProjectsRepository projectsRepository;

    @Inject
    AppConfigurationGenerator appConfigurationGenerator;

    @Inject
    ProjectCreator projectCreator;

    @NotNull
    private Application application;

    @Nullable
    private FormController formController;
    private ExternalDataManager externalDataManager;
    private AppDependencyComponent applicationComponent;
    private ProjectsDependencyComponent projectsDependencyComponent;

    /**
     * Private constructor restrict following class from initialising.
     */
    private Collect() {
    }

    /**
     * Create a single instance of Collect or retrieve already created single single instance.
     *
     * @return <class>Collect</class>
     */
    public static Collect getInstance() {
        synchronized (Collect.class) {
            if (singleton == null)
                singleton = new Collect();

            return singleton;
        }
    }

    /**
     * Returns application context.
     *
     * @return <class>Application</class>
     */
    public static Application getApplication() {
        return singleton.application;
    }

    /**
     * init collect module from main application
     * Initial module creation will begin from following functions.
     *
     * @param application App application (this will work as shared instance through-out the module)
     * @param langCode    language to translate
     * @param url         server url
     * @param apiKey      server token
     */
    public void init(Application application, String langCode, String url, String apiKey) {
        this.application = application;

        initDaggerModules();
        configureProject(url, apiKey);
        updateLanguageCode(langCode);
        applicationInitializer.initialize();

        if (BuildConfig.DEBUG) {
            //uncomment this for load demo project
            //testProjectConfiguration();
        }


        testStorage();
        fixGoogleBug154855417();
        setupStrictMode();
    }

    /**
     * Initialise dagger modules.
     */
    private void initDaggerModules() {
        applicationComponent = DaggerAppDependencyComponent.builder()
                .appDependencyModule(new AppDependencyModule())
                .application(this.application)
                .build();
        applicationComponent.inject(this);

        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder()
                .projectsDependencyModule(new ProjectsDependencyModule() {
                    @NotNull
                    @Override
                    public ProjectsRepository providesProjectsRepository() {
                        return projectsRepository;
                    }
                })
                .build();
    }


    /**
     * Get form management instance to execute form management functions.
     *
     * @return FormManagementContract
     */
    public FormManagementContract getFormManagementContract() {
        return new FormManagementContractImpl();
    }


    /**
     * Update module language.
     *
     * @param language language
     */
    private void updateLanguageCode(String language) {
        settingsProvider.getGeneralSettings().save(KEY_APP_LANGUAGE, language);
    }

    /**
     * Set server configurations.
     *
     * @param url    server url
     * @param apiKey token to access
     */
    private void configureProject(String url, String apiKey) {
        String settingsJson = appConfigurationGenerator
                .getAppConfigurationAsJsonWithTokenDetails(url, apiKey);

        SettingsConnectionMatcher settingsConnectionMatcher = new SettingsConnectionMatcher(projectsRepository, settingsProvider);
        String UUID = settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson);
        if (UUID == null) {
            projectCreator.createNewProject(settingsJson);
        }

        settingsProvider.getGeneralSettings().save(KEY_SERVER_URL, url);
        settingsProvider.getGeneralSettings().save(KEY_API_KEY, apiKey);
    }

    /**
     * Update application theme.
     *
     * @param theme theme
     */
    private void updateThemePack(String theme) {
        settingsProvider.getGeneralSettings().save(KEY_APP_THEME, theme);
    }

    private void testStorage() {
        // Throw specific error to avoid later ones if the app won't be able to access storage
        try {
            File externalFilesDir = application.getExternalFilesDir(null);
            File testFile = new File(externalFilesDir + File.separator + ".test");
            testFile.createNewFile();
            testFile.delete();
        } catch (IOException e) {
            throw new IllegalStateException("Collect module can't write to storage!");
        }
    }

    /**
     * Enable StrictMode and log violations to the system log.
     * This catches disk and network access on the main thread, as well as leaked SQLite
     * cursors and unclosed resources.
     */
    private void setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .permitDiskReads()  // shared preferences are being read on main thread
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    /**
     * Test function to init demo project.
     */
    private void testProjectConfiguration() {
        projectImporter.importNewProject(Project.Companion.getDEMO_PROJECT());
        currentProjectProvider.setCurrentProject(Project.DEMO_PROJECT_ID);
    }

    @NotNull
    @Override
    public ProjectsDependencyComponent getProjectsDependencyComponent() {
        return projectsDependencyComponent;
    }

    @NotNull
    @Override
    public Locale getLocale() {
        return new Locale(LocaleHelper.getLocaleCode(settingsProvider.getGeneralSettings()));
    }

    /**
     * Gets a unique, privacy-preserving identifier for a form based on its id and version.
     *
     * @param formId      id of a form
     * @param formVersion version of a form
     * @return md5 hash of the form title, a space, the form ID
     */
    public static String getFormIdentifierHash(String formId, String formVersion) {
        Form form = new FormsRepositoryProvider(Collect.getApplication()).get()
                .getLatestByFormIdAndVersion(formId, formVersion);

        String formTitle = form != null ? form.getDisplayName() : "";

        String formIdentifier = formTitle + " " + formId;
        return Md5.getMd5Hash(new ByteArrayInputStream(formIdentifier.getBytes()));
    }

    public AppDependencyComponent getComponent() {
        return applicationComponent;
    }

    public void setComponent(AppDependencyComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
        applicationComponent.inject(this);
    }

    public FormController getFormController() {
        return formController;
    }

    public void setFormController(@Nullable FormController controller) {
        formController = controller;
    }

    public ExternalDataManager getExternalDataManager() {
        return externalDataManager;
    }

    public void setExternalDataManager(ExternalDataManager externalDataManager) {
        this.externalDataManager = externalDataManager;
    }

    // https://issuetracker.google.com/issues/154855417
    private void fixGoogleBug154855417() {
        try {
            Settings metaSharedPreferences = settingsProvider.getMetaSettings();

            boolean hasFixedGoogleBug154855417 = metaSharedPreferences.getBoolean(KEY_GOOGLE_BUG_154855417_FIXED);

            if (!hasFixedGoogleBug154855417) {
                File corruptedZoomTables = new File(application.getFilesDir(), "ZoomTables.data");
                corruptedZoomTables.delete();

                metaSharedPreferences.save(KEY_GOOGLE_BUG_154855417_FIXED, true);
            }
        } catch (Exception exception) {
            Timber.e(exception);
        }
    }
}
