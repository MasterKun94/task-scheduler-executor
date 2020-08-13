import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanum.pluggable.Executor;
import com.oceanum.pluggable.StateListener;
import org.apache.hadoop.mapreduce.Job;

import java.util.HashMap;
import java.util.Map;

public class MapReduceExecutor implements Executor {
    @Override
    public void run(String[] args, StateListener listener) throws Throwable {
        Map<String, String> config = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
//        mapper.reader().readValue(args[0], Map.class);
        Job job = Job.getInstance();
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
