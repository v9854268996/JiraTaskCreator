import java.nio.file.Paths;

public class RunTask {
    public static void main(String[] args) {
        TaskList taskList = new TaskList(Paths.get("C:\\Users\\V.D.Petrov\\Documents\\JiraConnector\\templateBA.txt"));
        taskList.run();


    }
}
