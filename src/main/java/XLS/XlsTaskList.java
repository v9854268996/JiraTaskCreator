package XLS;

import Tools.CheckerProperties;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class XlsTaskList implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(XlsTaskList.class);
    ArrayList<IssueTemplate> issueTemplates;
    ArrayList<Issue> issues = new ArrayList<Issue>();
    ArrayList<String> dependencies = new ArrayList<>();
    Path templatePath;
    String templateName;
    //private static final ArrayList<String> postponedFields = new ArrayList<>(Arrays.asList("customfield_16931"));
    private static final ArrayList<String> postponedFields = new ArrayList<>(Arrays.asList(""));

    public static void main(String[] args) {
        XlsTaskList xlsTaskList = new XlsTaskList(Paths.get("templateGenerator2.0.xlsx"));
        xlsTaskList.setTemplateName("ЦХД BA");
        xlsTaskList.run();
    }


    public XlsTaskList(Path templatePath) {
        this.templatePath = templatePath;

    }

    @Override
    public synchronized void run() {

        issueTemplates = getIssueTemplates(templateName) ;
        cutTasks(issueTemplates);
        setPostponedFields(issueTemplates);
        buildDependencies(issueTemplates);
        logger.info("Нарезка успешно завершена");
        System.out.println("Нарезка успешно завершена");
    }




    public ArrayList<IssueTemplate> getIssueTemplates(String templateName)  {

        if (issueTemplates!= null)
            return issueTemplates;

        issueTemplates = new ArrayList<>();
        Workbook templateWorkbook = null;
        try {
            templateWorkbook = new XSSFWorkbook(new FileInputStream(templatePath.toFile()));
        } catch (IOException e) {
            logger.error("Не удалось прочитать шаблоны из книги excel",e);
            System.out.println("Не удалось прочитать шаблоны из книги excel");
        }
        Sheet templateSheet = templateWorkbook.getSheetAt(0);

        short lastCellNum = templateSheet.getRow(0).getLastCellNum();
        Row fields = templateSheet.getRow(0);

        boolean templateFound = false;
        for (Row row : templateSheet){

            //пропускаем шапку
            if(row.getRowNum()<=1 || (!templateName.trim().equals(XlsUtils.getCellValue(row.getCell(0)).trim()) && !templateFound) )
                continue;

            templateFound = true;
            if(XlsUtils.getCellValue(row.getCell(1)).equals(""))
                break;

            IssueTemplate issueTemplate = new IssueTemplate(row, fields);
            issueTemplates.add(issueTemplate);
//            Issue.FluentCreate fluentCreate = issueTemplate.getFluentCreate();
        }
        try {
            templateWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return issueTemplates;
    }

    public void cutTasks(ArrayList<IssueTemplate> issueTemplates){

        String currentEpic = CheckerProperties.getParameterValue("epic");

        String projectOfEpic = null;
        if(currentEpic!= null)
            projectOfEpic = currentEpic.split("-")[0];
        String project = projectOfEpic.isEmpty() ? projectOfEpic : CheckerProperties.getParameterValue("project");
        String currentBusinessTask = CheckerProperties.getParameterValue("parentBusinessTask");

        for(IssueTemplate issueTemplate : issueTemplates){
            //create epics
            if(issueTemplate.getFieldValues().get("issuetype").equals("Epic")){
                try {
                    Issue issue = issueTemplate.createIssue();
                    currentEpic = issue.getKey();
                    logger.info("Создан эпик " + issue.getKey()+ " " + issue.getSummary());
                    System.out.println("Создан эпик " + issue.getKey()+ " " + issue.getSummary());
                } catch (JiraException e) {
                    logger.error("Не удалось создать задачу " + issueTemplate.getFieldValues().get("summary"), e);
                    System.out.println("Не удалось создать задачу " + issueTemplate.getFieldValues().get("summary"));
                    logAndStop();
                }
            }
            //create business tasks
            else if(issueTemplate.getFieldValues().get("issuetype").equals("Задача")){
                try {
                    if (currentEpic.isEmpty()){
                        logger.error("Не задан эпик для нарезки задач");
                        System.out.println("Не задан эпик для нарезки задач");
                        logAndStop();
                    }

                    issueTemplate.getFluentCreate().field("customfield_14330", currentEpic);
                    Issue issue = issueTemplate.createIssue();
                    currentBusinessTask = issue.getKey();
                    logger.info("Создана бизнес-задача " + issue.getKey()+ " " + issue.getSummary());
                    System.out.println("Создана бизнес-задача " + issue.getKey()+ " " + issue.getSummary());
                } catch (JiraException e) {
                    logger.error("Не удалось создать бизнес-задачу " + issueTemplate.getFieldValues().get("summary"), e);
                    System.out.println("Не удалось создать бизнес-задачу " + issueTemplate.getFieldValues().get("summary"));
                    logAndStop();
                }
            }
            //create subtasks
            else {
                try {
                    if (currentBusinessTask.isEmpty()){
                        logger.error("Не задана бизнес-задача для нарезки задач");
                        System.out.println("Не задана бизнес-задача для нарезки задач");
                        logAndStop();
                    }

                    issueTemplate.getFluentCreate().field(Field.PARENT, currentBusinessTask);
                    Issue issue = issueTemplate.createIssue();
                    logger.info("Создана подзадача " + issue.getKey()+ " " + issue.getSummary());
                    System.out.println("Создана подзадача " + issue.getKey()+ " " + issue.getSummary());
                } catch (JiraException e) {
                    logger.error("Не удалось создать подзадачу " + issueTemplate.getFieldValues().get("summary"), e);
                    System.out.println("Не удалось создать подзадачу " + issueTemplate.getFieldValues().get("summary"));
                    logAndStop();
                }
            }

        }
    }

    private void setPostponedFields(ArrayList<IssueTemplate> issueTemplates) {
        for (IssueTemplate issueTemplate : issueTemplates){
            for (Map.Entry<String,String> fieldEntry : issueTemplate.getFieldValues().entrySet()){

                if (isPostponedField(fieldEntry.getKey())) {
                    try {
                        issueTemplate.issue.update()
                                .field(fieldEntry.getKey().toLowerCase(), "{\"value\": \"" + fieldEntry.getValue() + "\"}")
                                .execute();
                    } catch (JiraException e) {
                        System.out.println("Не проставлено поле " + fieldEntry.getKey() + " в задаче " + issueTemplate.issue.getKey());
                        logger.warn("Не проставлено поле " + fieldEntry.getKey() + " в задаче " + issueTemplate.issue.getKey());
                    }
                }
            }
        }
    }

    protected static boolean isPostponedField(String field){
        Boolean aBoolean = postponedFields.stream()
                .map(v -> v.toLowerCase().equals(field.toLowerCase()))
                .reduce((r1, r2) -> r1 || r2).orElse(false);
        return aBoolean;
    };

    private void buildDependencies(ArrayList<IssueTemplate> issueTemplates) {
        for (IssueTemplate issueTemplate : issueTemplates){
            for (IssueTemplate dependency : issueTemplate.dependencies){
                try {
                    dependency.issue.link(issueTemplate.issue.getKey(), "Зависимые задачи");
                    logger.info("Создана связь " + issueTemplate.issue.getKey() + " и " + dependency.issue.getKey());
                    System.out.println("Создана связь " + issueTemplate.issue.getKey() + " и " + dependency.issue.getKey());
                } catch (JiraException e) {
                    logger.error("Не удалось связать задачи " + issueTemplate.issue.getKey() + " и " + dependency.issue.getKey());
                    System.out.println("Не удалось связать задачи " + issueTemplate.issue.getKey() + " и " + dependency.issue.getKey());
                }
            }
        }
    }

    public static ArrayList<String> getTemplateNames(Path templatesPath) {
        Workbook templateWorkbook = null;
        try {
            templateWorkbook = new XSSFWorkbook(new FileInputStream(templatesPath.toFile()));
        } catch (IOException e) {
            logger.error("Не удалось прочитать шаблоны из книги excel", e);
            System.out.println("Не удалось прочитать шаблоны из книги excel");
        }
        Sheet templateSheet = templateWorkbook.getSheetAt(0);
        ArrayList<String> templateNames = new ArrayList<>();

        for (Row row : templateSheet) {
            String cellValue = XlsUtils.getCellValue(row.getCell(0));
            if(!cellValue.equals("") && !cellValue.trim().toLowerCase().equals("шаблон"))
                templateNames.add(cellValue);
        }

        return templateNames;
    }

    public static void logAndStop(){
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            logger.error("Sleep error", e);
        }
        System.exit(-1);
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }


}
