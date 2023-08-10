package XLS;

import Tools.CheckerProperties;
import Tools.JiraUtils;
import Tools.Utils;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.TimeTracking;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static XLS.XlsUtils.getCellValue;

public class IssueTemplate {

    Integer key;
    String dependenciesString;
    ArrayList<IssueTemplate> dependencies = new ArrayList<>();
    Map<String,String> fieldValues ;
    private static final Logger logger = LoggerFactory.getLogger(IssueTemplate.class);
    Issue.FluentCreate fluentCreate;
    Issue issue;
    static HashMap<Integer, IssueTemplate> issueTemplateHashMap = new HashMap<>();

    Row row;
    Row fieldsRow;

    public IssueTemplate(Row row, Row fieldsRow) {
        this.row = row;
        this.fieldsRow = fieldsRow;
        this.key = new Double(Double.parseDouble(getCellValue(row.getCell(1)))).intValue();
        issueTemplateHashMap.put(key,this);
    }

    public Map<String,String> getFieldValues(){
        if (fieldValues!= null)
            return fieldValues;

        fieldValues = new HashMap<>();
//        logger.info("min col:" + fieldsRow.getFirstCellNum() + ". max col:" + fieldsRow.getLastCellNum());
        for (Cell cell : row){
            if (cell.getColumnIndex() == 2) {
                dependenciesString = getCellValue(cell);
                if(dependenciesString!="")
                    dependencies = (ArrayList< IssueTemplate>) Arrays.asList(dependenciesString.split("[,.]"))
                        .stream()
//                            .flatMap(e -> Arrays.asList(e.split("\\.")).stream())
                        .map(a -> a.trim())
                        .filter(e -> !"0".equals(e))
                        .map(s -> (Double)Double.parseDouble(s))
                        .map(d -> d.intValue())
                        .map(d -> issueTemplateHashMap.get(d))
                            .peek(i -> {
                                if(i==null) {
                                    System.out.println("!!!");
                                    System.out.println("Ошибка связи. Не найдены задачи с одним из индексов " +  dependenciesString);
                                    System.out.println("!!!");
                                }
                            })
                        .collect(Collectors.toList());
            }

            Short minFieldsColumn = findMinFieldsColumn(fieldsRow);
            Short maxFieldsColumn = findMaxFieldsColumn(fieldsRow);

            if (cell.getColumnIndex() < minFieldsColumn || cell.getColumnIndex() > maxFieldsColumn)
                continue;

            String jiraField = getCellValue(fieldsRow.getCell(cell.getColumnIndex())).toLowerCase();
            String cellValue = Utils.replaceParameters(getCellValue(cell));

            if(!"".equals(cellValue) )
                fieldValues.put(jiraField, cellValue);
        }
        return fieldValues;
    }

    private Short findMinFieldsColumn(Row row) {
        Short minColumn = 0;
        for(Short i = row.getFirstCellNum();i<=row.getLastCellNum();i++){
            if(!XlsUtils.getCellValue(row.getCell(i)).equals(""))
                return i;

        }
        return minColumn;
    }

    private Short findMaxFieldsColumn(Row row) {
        Short maxColumn = row.getLastCellNum();
        for(Short i = maxColumn;i>=row.getFirstCellNum();i--){
            if(!XlsUtils.getCellValue(row.getCell(i)).equals(""))
                return i;
        }
        return maxColumn;
    }

    public Issue.FluentCreate getFluentCreate()  {

        if(fluentCreate != null)
            return fluentCreate;
        try {
        String projectType = CheckerProperties.getParameterValue("project");
        String issueType = getFieldValues().get("issuetype");
        fluentCreate = JiraUtils.getJiraRestClient().createIssue(projectType, issueType);
        JSONObject createMeta = JiraUtils.getCreateMeta(projectType, issueType);

        for (Map.Entry<String, String> entry : getFieldValues().entrySet()){
            String jiraField = entry.getKey();
            String jiraFieldValue = entry.getValue();
            Field.Meta fieldMetadata = Field.getFieldMetadata(jiraField, createMeta);
            if(jiraField.equals("issuetype") || "".equals(jiraFieldValue)|| XlsTaskList.isPostponedField(jiraField))
                continue;

            if (jiraField.equals("customfield_15275")
                    || jiraField.equals("customfield_16931")
                    || jiraField.equals("customfield_19541")
            )
                jiraFieldValue = "{\"value\": \"" + jiraFieldValue + "\"}";

            String prefix = CheckerProperties.getParameterValue("taskPrefixName");
            if(jiraField.equals("summary"))
                jiraFieldValue = (prefix.equals("")?"":(prefix + " "))
                        + jiraFieldValue + " "
                        + CheckerProperties.getParameterValue("taskPostfixName");

            if (fieldMetadata.type.equals("array")) {
                //issue.field(realKey.toLowerCase(),Double.parseDouble(realValue));
                ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(jiraFieldValue.split(",")));
                arrayList = (ArrayList<String>)arrayList.stream().map(a -> a.trim()).collect(Collectors.toList());
                fluentCreate.field(jiraField,arrayList);
            } else if (fieldMetadata.type.equals("timetracking")){
                TimeTracking timeTracking = new TimeTracking();
                timeTracking.setOriginalEstimate(jiraFieldValue);
                timeTracking.setOrignalEstimateSeconds(-1);
                timeTracking.setRemainingEstimateSeconds(-1);
                fluentCreate.field(jiraField,timeTracking);
            } else if (fieldMetadata.type.equals("number")){
                fluentCreate.field(jiraField,Double.parseDouble(jiraFieldValue));
            } else {
                fluentCreate.field(jiraField,jiraFieldValue);
            }
        }
        } catch (JiraException e) {
            logger.error("Не удалось обработать шаблон для задачи " + getFieldValues().get("summary"), e);
            System.out.println("Не удалось обработать шаблон для задачи " + getFieldValues().get("summary"));
            XlsTaskList.logAndStop();
        }

        return fluentCreate;
    }

    public Issue createIssue() throws JiraException {
        issue = getFluentCreate().execute();
        return issue;
    }

    public Integer getKey() {
        return key;
    }

    public String getDependencies() {
        return dependenciesString;
    }
}
