package org.odk.collect.android.openrosa;

public class HttpCredentials implements HttpCredentialsInterface {

    private final String username;
    private final String password;


    private String apiKey;
    private String appVersion;

    public HttpCredentials(String username, String password) {
        this.username = (username == null) ? "" : username;
        this.password = (password == null) ? "" : password;
    }

    public HttpCredentials(String username, String password, String apiKey, String appVersion) {
        this(username, password);
        this.apiKey = apiKey;
        this.appVersion = appVersion;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (super.equals(obj)) {
            return true;
        }

        return ((HttpCredentials) obj).getUsername().equals(getUsername()) &&
                ((HttpCredentials) obj).getPassword().equals(getPassword());
    }

    @Override
    public int hashCode() {
        return (getUsername() + getPassword()).hashCode();
    }
}
