import com.oceanum.pluggable.Executor;
import com.oceanum.pluggable.StateListener;

public class FlinkExecutor implements Executor {
    @Override
    public void run(String[] args, StateListener listener) throws Throwable {

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
