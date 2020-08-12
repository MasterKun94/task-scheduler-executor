import com.alibaba.fastjson.JSON;
import com.oceanum.pluggable.Executor;
import com.oceanum.pluggable.StateListener;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Launcher implements Executor {
    private final AtomicReference<SparkAppHandle.State> currentState = new AtomicReference<>();
    private final AtomicReference<SparkAppHandle> handleRef = new AtomicReference<>();
    private final CountDownLatch countDown = new CountDownLatch(1);

    @Override
    public void run(String[] args, StateListener listener) throws Throwable {
        SparkArgs sparkArgs = JSON.parseObject(args[0], SparkArgs.class);
        SparkLauncher launcher = createLauncher(sparkArgs);
        Map<String, String> info = new ConcurrentHashMap<>();
        String resourceManager = getResourceManagerUrl(sparkArgs);
        info.put("resourceManager", resourceManager);

        SparkAppHandle handler = launcher.startApplication(new SparkAppHandle.Listener(){
                    @Override
                    public void stateChanged(SparkAppHandle handle) {
                        SparkAppHandle.State state = handle.getState();
                        info.put("appId", handle.getAppId());
                        info.put("appUrl", resourceManager + "/" + handle.getAppId());
                        info.put("appStatus", state.toString());
                        listener.updateState(info);
                        currentState.set(state);
                        if (state.isFinal()) {
                            countDown.countDown();
                        }
                    }

                    @Override
                    public void infoChanged(SparkAppHandle handle) {
                        stateChanged(handle);
                    }
                });
        handleRef.set(handler);
        countDown.await();
        handler.disconnect();
    }

    @Override
    public boolean isRunning() {
        SparkAppHandle.State state = currentState.get();
        return state == null || !state.isFinal();
    }

    @Override
    public void kill(StateListener stateListener) throws Throwable {
        handleRef.get().stop();
        if (!countDown.await(120, TimeUnit.SECONDS)) {
            handleRef.get().kill();
        }
    }

    @Override
    public void close() {
        handleRef.get().disconnect();
    }

    private SparkLauncher createLauncher(SparkArgs sparkArgs) {
        Map<String, String> env = new HashMap<>();
        env.put("HADOOP_HOME", sparkArgs.getHadoopHome());

        SparkLauncher launcher = new SparkLauncher(env);

        if (nonEmpty(sparkArgs.getAppName())) {
            launcher.setAppName(sparkArgs.getAppName());
        }

        launcher.setAppResource(sparkArgs.getAppResource());
        launcher.setMainClass(sparkArgs.getMainClass());
        launcher.setSparkHome(sparkArgs.getSparkHome());
        launcher.setMaster(sparkArgs.getMaster());

        if (sparkArgs.getAppArgs() != null) {
            launcher.addAppArgs(sparkArgs.getAppArgs());
        }

        if (nonEmpty(sparkArgs.getDeployMode())) {
            launcher.setDeployMode(sparkArgs.getDeployMode());
        }

        if (sparkArgs.getJars() != null) {
            for (String jar : sparkArgs.getJars()) {
                launcher.addJar(jar);
            }
        }
        if (sparkArgs.getFiles() != null) {
            for (String file : sparkArgs.getFiles()) {
                launcher.addFile(file);
            }
        }
        if (sparkArgs.getPyFiles() != null) {
            for (String pyFile : sparkArgs.getPyFiles()) {
                launcher.addPyFile(pyFile);
            }
        }
        if (sparkArgs.getConf() != null) {
            sparkArgs.getConf().forEach(launcher::setConf);
        }
        if (nonEmpty(sparkArgs.getPropertiesFile())) {
            launcher.setPropertiesFile(sparkArgs.getPropertiesFile());
        }
        return launcher;
    }

    private String getResourceManagerUrl(SparkArgs sparkArgs) {
        return "";
    }

    private boolean nonEmpty(String string) {
        return string != null && !"".equals(string.trim());
    }
}