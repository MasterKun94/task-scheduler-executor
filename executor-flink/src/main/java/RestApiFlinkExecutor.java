import com.nextbreakpoint.flinkclient.api.FlinkApi;
import com.oceanum.pluggable.Executor;
import com.oceanum.pluggable.StateListener;
import com.oceanum.util.FlinkArgs;

import java.io.File;
import java.net.URI;
import java.util.Optional;

public class RestApiFlinkExecutor implements Executor {
    @Override
    public void run(String[] args, StateListener listener) throws Throwable {
        FlinkArgs flinkArgs = FlinkArgs.fromJson(args[0]);
        String clientAddress = flinkArgs.getClientAddress()
                .map(URI::create)
                .map(uri -> {
                    String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
                    String host = uri.getHost() == null ? "localhost" : uri.getHost();
                    int port = uri.getPort() < 0 ? 8081 : uri.getPort();
                    return scheme + "://" + host + ":" + port;
                })
                .orElse("http://localhost:8081");

        FlinkApi api = new FlinkApi();
        api.getApiClient().setBasePath(clientAddress);
        api.uploadJar(new File(flinkArgs.jar()));

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void kill(StateListener stateListener) throws Throwable {

    }

    @Override
    public void close() {

    }
}
