import com.nextbreakpoint.flinkclient.api.ApiClient;
import com.nextbreakpoint.flinkclient.api.FlinkApi;
import com.nextbreakpoint.flinkclient.model.DashboardConfiguration;
import com.nextbreakpoint.flinkclient.model.JarListInfo;
import com.nextbreakpoint.flinkclient.model.JarRunResponseBody;
import com.nextbreakpoint.flinkclient.model.JarUploadResponseBody;

import java.io.File;

/**
 * @author chenmingkun
 * @date 2020/7/21
 */
public class FlinkClient {
    public static void main(String[] args) throws Exception {
        FlinkApi api = new FlinkApi();
        ApiClient client = api.getApiClient();
        client.setBasePath("http://localhost:8081");
        DashboardConfiguration config = api.showConfig();
        System.out.println(config);
        JarListInfo jars = api.listJars();
        System.out.println(jars);
        JarUploadResponseBody result = api.uploadJar(new File("C:\\Users\\chenmingkun\\work\\idea\\flink-test\\target\\flink-test-1.0-SNAPSHOT.jar"));
        System.out.println(result);
        Thread.sleep(2000);
        JarRunResponseBody response = api.runJar(new File(result.getFilename()).getName(), true, null, null, "a, b, c, d, e, f, g", "org.example.StreamingJob", null);
        System.out.println(response);
    }
}
