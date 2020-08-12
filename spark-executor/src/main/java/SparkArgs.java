import java.util.Collections;
import java.util.Map;

public class SparkArgs {
    private String appName;
    private String appResource;
    private String mainClass;
    private String[] appArgs = new String[0];
    private String sparkHome;
    private String hadoopHome;
    private String master = "yarn";
    private String deployMode = "cluster";
    private Map<String, String> conf = Collections.emptyMap();
    private String[] jars = new String[0];
    private String[] files = new String[0];
    private String[] pyFiles = new String[0];
    private String propertiesFile;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppResource() {
        return appResource;
    }

    public void setAppResource(String appResource) {
        this.appResource = appResource;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String[] getAppArgs() {
        return appArgs;
    }

    public void setAppArgs(String[] appArgs) {
        this.appArgs = appArgs;
    }

    public String getSparkHome() {
        return sparkHome;
    }

    public void setSparkHome(String sparkHome) {
        this.sparkHome = sparkHome;
    }

    public String getHadoopHome() {
        return hadoopHome;
    }

    public void setHadoopHome(String hadoopHome) {
        this.hadoopHome = hadoopHome;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public Map<String, String> getConf() {
        return conf;
    }

    public void setConf(Map<String, String> conf) {
        this.conf = conf;
    }

    public String[] getJars() {
        return jars;
    }

    public void setJars(String[] jars) {
        this.jars = jars;
    }

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    public String[] getPyFiles() {
        return pyFiles;
    }

    public void setPyFiles(String[] pyFiles) {
        this.pyFiles = pyFiles;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }
}
