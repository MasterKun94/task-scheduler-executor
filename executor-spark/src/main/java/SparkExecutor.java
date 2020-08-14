import com.oceanum.pluggable.Executor;
import com.oceanum.pluggable.StateListener;
import com.oceanum.util.SparkArgs;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SparkExecutor implements Executor {
    private final AtomicReference<SparkAppHandle.State> currentState = new AtomicReference<>();
    private final AtomicReference<SparkAppHandle> handleRef = new AtomicReference<>();
    private final CountDownLatch countDown = new CountDownLatch(1);

    @Override
    public void run(String[] args, StateListener listener) throws Throwable {
        SparkArgs sparkArgs = SparkArgs.fromJson(args[0]);
        SparkLauncher launcher = createLauncher(sparkArgs);
        Map<String, String> info = new ConcurrentHashMap<>();

        SparkAppHandle handler = launcher.startApplication(new SparkAppHandle.Listener(){
                    @Override
                    public void stateChanged(SparkAppHandle handle) {
                        SparkAppHandle.State state = handle.getState();
                        info.put("appId", handle.getAppId());
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
        if (currentState.get() != SparkAppHandle.State.FINISHED) {
            throw new IllegalArgumentException();
        }
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
        env.put("HADOOP_HOME", sparkArgs.hadoopHome());

        SparkLauncher launcher = new SparkLauncher(env);
        sparkArgs.getAppName().ifPresent(launcher::setAppName);
        launcher.setAppResource(sparkArgs.appResource());
        launcher.setMainClass(sparkArgs.mainClass());
        launcher.setSparkHome(sparkArgs.sparkHome());
        launcher.setMaster(sparkArgs.master());
        launcher.addAppArgs(sparkArgs.appArgs());

        sparkArgs.getDeployMode().ifPresent(launcher::setDeployMode);

        for (String jar : sparkArgs.jars()) {
            launcher.addJar(jar);
        }
        for (String file : sparkArgs.files()) {
            launcher.addFile(file);
        }

        for (String pyFile : sparkArgs.pyFiles()) {
            launcher.addPyFile(pyFile);
        }
        sparkArgs.getConf().forEach(launcher::setConf);
        sparkArgs.getPropertiesFile().ifPresent(launcher::setPropertiesFile);
        return launcher;
    }
}