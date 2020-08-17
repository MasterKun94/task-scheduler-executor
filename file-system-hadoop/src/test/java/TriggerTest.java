import com.oceanum.common.Environment;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSInotifyEventInputStream;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.hadoop.hdfs.inotify.Event;
import org.apache.hadoop.hdfs.inotify.EventBatch;
import org.apache.hadoop.hdfs.inotify.MissingEventsException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

//监控hdfs系统的文件状态
public class TriggerTest extends Thread {

    public static void getFileStatus() throws IOException, InterruptedException, MissingEventsException {
        HdfsAdmin admin = new HdfsAdmin(URI.create("hdfs://192.168.10.130:8022"), new Configuration());
        DFSInotifyEventInputStream eventStream = admin.getInotifyEventStream();
        while( true ) {
            EventBatch events = eventStream.take();
            for (Event event : events.getEvents()) {
                System.out.println("=======================================================");
                System.out.println( "event type = " + event.getEventType() );
                switch( event.getEventType() ) {
                    case CREATE:
                        Event.CreateEvent createEvent = (Event.CreateEvent) event;
                        System.out.println("create  path = " + createEvent.getPath());
                        System.out.println(createEvent.getCtime());
                        System.out.println(createEvent.getOwnerName());
                        break;
                    case CLOSE:
                        Event.CloseEvent closeEvent = (Event.CloseEvent) event;
                        System.out.println("close  path = " + closeEvent.getPath());
                        break;
                    case APPEND:
                        Event.AppendEvent appendEvent = (Event.AppendEvent) event;
                        System.out.println("append  path = " + appendEvent.getPath());
                        break;
                    case RENAME:
                        Event.RenameEvent renameEvent = (Event.RenameEvent) event;
                        System.out.println("rename  srcPath = " + renameEvent.getSrcPath());
                        System.out.println("rename  dstPath = " + renameEvent.getDstPath());
                        break;
                    case METADATA:
                        Event.MetadataUpdateEvent metadataUpdateEvent = (Event.MetadataUpdateEvent) event;
                        System.out.println("metadata  path = " + metadataUpdateEvent.getPath());
                        break;
                    case UNLINK:
                        Event.UnlinkEvent unlinkEvent = (Event.UnlinkEvent) event;
                        System.out.println("unlink  path = " + unlinkEvent.getPath());
                        break;
                    default:
                        break;
                }
            }
            System.out.println("=======================================================");
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("hadoop.home.dir", "E:\\hadoop");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        getFileStatus();
    }
}