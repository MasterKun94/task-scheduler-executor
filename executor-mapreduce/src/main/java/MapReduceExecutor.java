import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanum.pluggable.Executor;
import com.oceanum.pluggable.StateListener;
import com.oceanum.util.MapReduceArgs;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.HashMap;
import java.util.Map;

public class MapReduceExecutor implements Executor {
    @Override
    public void run(String[] args, StateListener listener) throws Throwable {
        MapReduceArgs mapReduceArgs = MapReduceArgs.fromJson(args[0]);

        Configuration conf = new Configuration();
        mapReduceArgs.getConf().forEach(conf::set);

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
