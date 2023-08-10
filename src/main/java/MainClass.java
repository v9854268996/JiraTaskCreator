
import Tools.CheckerProperties;
import Tools.JiraUtils;
import XLS.XlsTaskList;
import net.rcarz.jiraclient.*;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


public class MainClass {
    private static final Logger logger = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) {

        try {

//            JiraClient jiraRestClient = JiraUtils.getJiraRestClient();
//
//            //JSONObject createMeta = Tools.JiraUtils.getCreateMeta("DHREP", "Системный анализ");
//            JSONObject createMetadata = Issue.getCreateMetadata(jiraRestClient.getRestClient(), "DHREP", "Задача");
//            System.out.println(createMetadata);

            //Issue issue = Tools.JiraUtils.getJiraRestClient().getIssue("DHREP-25032");
            //System.out.println(issue);

//            Field.Meta fieldMetadata = Field.getFieldMetadata("customfield_16030", Issue.getCreateMetadata(Tools.JiraUtils.getJiraRestClient().getRestClient(), "DHREP", "Задача"));
//
//            JiraUtils.getJiraRestClient()
//                    .getIssue("DHREP-56014")
//                    .update()
//                    .field("customfield_16931", "{\"value\": \"Преданализ\"}")
//                    .execute();
//            System.out.println("start");


//
//            Issue newIssue = JiraUtils.getJiraRestClient().createIssue("DHREP", "Системный анализ")
//                    .field(Field.SUMMARY, "Bat signal is broken")
//                    .field(Field.DESCRIPTION, "Commissioner Gordon reports the Bat signal is broken.")
//                    .field(Field.ASSIGNEE, "v.d.petrov@rt.ru")
//                    .field(Field.PARENT, "DHREP-56348")
//                    .field("customfield_10937","2019-06-14" )
//                    .field("customfield_10936", "2019-06-14")
//                    .field("customfield_15275", "{\"value\": \"ФЭБ/HR\"}")
//                    .field("customfield_16030", new ArrayList<String>(Arrays.asList("ЦХД")))
//                    .execute();

            XlsTaskList xlsTaskList = new XlsTaskList(Paths.get("templateGenerator2.0.xlsx"));
            xlsTaskList.setTemplateName("Эталон");
            xlsTaskList.run();


            Issue.FluentCreate field = JiraUtils.getJiraRestClient().createIssue("DHREP", "Задача")
                    .field(Field.SUMMARY, "Bat signal is broken")
                    .field(Field.DESCRIPTION, "Commissioner Gordon reports the Bat signal is broken.")
                    .field(Field.ASSIGNEE, "v.d.petrov@rt.ru")
                    .field("customfield_14330", "DHREP-446") //ссылка на эпик
                    .field("customfield_10937", "2019-06-14")
                    .field("customfield_16030", new ArrayList<String>(Arrays.asList("ЦХД")))
                    .field("customfield_10936", "2019-06-14")
                    .field("customfield_16931", "{\"value\": \"1_Преданализ\"}")
                    .field("customfield_15275", "{\"value\": \"ФЭБ/HR\"}");



            field.execute();

            System.out.println("dddf");

//            Issue newEpic = JiraUtils.getJiraRestClient().createIssue("DHREP", "Epic")
//                    .field(Field.SUMMARY, "Bat Epic")
//                    .field("customfield_14332", "Bat Epic 2")
//                    .field(Field.DESCRIPTION, "Epic Epic.")
//                    .field(Field.ASSIGNEE, "v.d.petrov@rt.ru")
//                    .field("customfield_10937","2019-06-14" )
//                    .field("customfield_10936", "2019-06-14")
//                    .field("customfield_15275", "{\"value\": \"ФЭБ/HR\"}")
//                    .execute();





        } catch (JiraException e) {
            e.printStackTrace();
        }



//        TaskList taskList1 = new TaskList(Paths.get("C:\\Users\\V.D.Petrov\\IdeaProjects\\JiraConnector\\JiraConnector\\template_ЭДО.txt"));
//        taskList1.run();


//        try {
//               Issue.FluentCreate issue2;
//               Issue issue = Tools.JiraUtils.getJiraRestClient().getIssue("DWSUP-1010");
//               JSONObject createMeta = Tools.JiraUtils.getCreateMeta("DHREP", "Системный анализ");
//               Files.lines(Paths.get("C:\\Users\\V.D.Petrov\\Documents\\tmpDist\\task.txt")).forEach(e -> downloadJira2(e.trim()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JiraException e) {
//               e.printStackTrace();
//           }
//
//        System.out.println("done");
//
//        try {
//            String baseDir = "C:\\Users\\V.D.Petrov\\Documents\\tmpDist";
//            Issue issue = Tools.JiraUtils.getJiraRestClient().getIssue("DHREP-36299", "attachments");
//
//            try {
//
//                Path downloadDir = Files.createDirectory(Paths.get(baseDir + "/" + issue.getKey()));
//                for (Attachment attachment:issue.getAttachments()){
//                    String name = attachment.getFileName();
//                    Files.write(downloadDir.resolve(name),attachment.download());
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            System.out.println(issue);
//        } catch (JiraException e) {
//            e.printStackTrace();
//        }
//
//        logger.info("Starting programm");
//        Tools.Utils.replaceParameters("sfsdfdsf %%replacementBusinessPartner%% saeerr");
//        //net.sf.json.JSONObject createMeta = Tools.JiraUtils.getCreateMeta("DHREP","Системный анализ");

//        logger.info("Path:" + path.toString());
//        FileWriter fileWriter = new FileWriter(path.toFile(),true);
//        fileWriter.append(Calendar.getInstance().toString() + "directory "+ System.getProperty("user.dir"));
//        fileWriter.flush();
//--------------------------------------
//          TaskList taskList = new TaskList(Paths.get(System.getProperty("user.dir") + "\\" + Tools.CheckerProperties.getParameterValue("templateName")));
//          taskList.run();
//--------------------------------------


//        HashMap<String, ArrayList<String>> h = TaskList.getAttributes("issueType:\"Функциональная разработка\" component:\"07_Разработка, 04.03_Сборка патча модели ODS, 04.02_Доработка модели данных\" assignee:\"nagovitsyna_ma\"");
//
//
//        RestClient jiraRestClient = Tools.JiraUtils.getJiraRestClient().getRestClient();
////        List<Project> projects = Project.get()
//        Project project = Project.get(jiraRestClient,"DHREP");
//        Boolean isValid = Tools.JiraUtils.isValidProject("DHREP");
//
//        //Issue issue = Tools.JiraUtils.getJiraRestClient().getIssue("DWM-6978");
//
////        Issue issue2 = Tools.JiraUtils.getJiraRestClient().getIssue("DWM-6982");
////        issue.update().
//
//        issue.update().field("customfield_11754","2019-09-02").execute();
//        issue.update().field("Бизнес-партнер","ФЭБ/HR").execute();
////
////        //Tools.JiraUtils.getJiraRestClient().
////
////        issue2.link(issue.getKey(),"Зависимые задачи");
////

    }

    public static void downloadJira(String issueKey){
        try {
            String baseDir = "C:\\Users\\V.D.Petrov\\Documents\\tmpDist";
            Issue issue = JiraUtils.getJiraRestClient().getIssue(issueKey);

            if(!issueKey.equals(issue.getKey()))
                System.out.println(issueKey + "__________________________________________");

            try {

                Path downloadDir = Files.createDirectory(Paths.get(baseDir + "/" + issue.getKey()));
                for (Attachment attachment:issue.getAttachments()){
                    String name = attachment.getFileName();
                    Files.write(downloadDir.resolve(name),attachment.download());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


//            System.out.println(issue);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }

    public static void downloadJira2(String issueKey){
        try {
            String baseDir = "C:\\Users\\V.D.Petrov\\Documents\\tmpDist";
            Issue issue = JiraUtils.getJiraRestClient().getIssue(issueKey);

            if(!issueKey.equals(issue.getKey()))
                System.out.println(issueKey + "__________________________________________");

            try {
                Desktop.getDesktop().browse(new URI(CheckerProperties.getParameterValue("jiraurl") + "/secure/attachmentzip/" + issue.getId() + ".zip" ));
//                Path downloadDir = Files.createDirectory(Paths.get(baseDir + "/" + issue.getKey()));
//                for (Attachment attachment:issue.getAttachments()){
//                    String name = attachment.getFileName();
//                    Files.write(downloadDir.resolve(name),attachment.download());
//                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }


//            System.out.println(issue);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }
}
