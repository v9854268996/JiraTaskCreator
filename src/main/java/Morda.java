//import Appenders.JTextAreaAppender;

//import ch.qos.logback.classic.LoggerContext;
//import org.apache.log4j.Level;
//import org.apache.logging.log4j.core.Appender;
//import org.apache.logging.log4j.core.Filter;
//import org.apache.logging.log4j.core.LoggerContext;
//import org.apache.logging.log4j.core.appender.OutputStreamAppender;
//import org.apache.logging.log4j.core.config.Configuration;
//import org.apache.logging.log4j.core.config.LoggerConfig;
//import org.apache.logging.log4j.core.layout.PatternLayout;
import Tools.CheckerProperties;
import XLS.IssueTemplate;
import XLS.XlsTaskList;
import XLS.XlsUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static XLS.XlsTaskList.getTemplateNames;

public class Morda extends JFrame {
    private JComboBox templateFile;
    private JTextPane textPane1;
    private JPanel rootPanel;
    private JTextArea logArea;
    private JButton initButton;
    private JPanel logPanel;
    private JTextField parentBusinessTask;
    private JTextField taskPostfixName;
    private JTextField sprintDate;
    private JPanel templateContent;
    private JTextArea templateContentArea;
    private JTextField parentEpic;
    private JTextField parentProject;
    private JTextField taskPrefixName;
    private static final Logger logger = LoggerFactory.getLogger(Morda.class);
    private File propertiesPath;
    private static XlsTaskList xlsTaskList;
    private static String selectedValue;

    public Morda(String title) throws HeadlessException {

        super(title);
        setSize(1200, 800);
        this.add(rootPanel);
        //setContentPane(rootPanel);
        //pack();


        templateFile.setEditable(true);
        parentBusinessTask.setText(CheckerProperties.getParameterValue("parentBusinessTask"));
        parentEpic.setText(CheckerProperties.getParameterValue("epic"));
        parentProject.setText(CheckerProperties.getParameterValue("project"));
        taskPostfixName.setText(CheckerProperties.getParameterValue("taskPostfixName"));
        taskPrefixName.setText(CheckerProperties.getParameterValue("taskPrefixName"));
        sprintDate.setText(CheckerProperties.getParameterValue("replacementSprintDate"));

        parentBusinessTask.setEnabled(false);
        parentEpic.setEnabled(false);
        parentProject.setEnabled(false);

//        templateFile.addActionListener(a -> {
//            List<Path> template = Files.walk(Paths.get(System.getProperty("user.dir")))
//                    .filter(Files::isRegularFile)
//                    .filter(f -> f.toFile().getName().contains("template"))
//                    .filter(f -> f.toFile().getName().contains(".xlsx"))
//                    .collect(Collectors.toList());
//            template.stream()
//                    .forEach(t -> morda.templateFile.addItem(t));
//        });

        initButton.addActionListener(e -> {
            initButton.setEnabled(false);
            logArea.setText("");
            CheckerProperties.setProperty("parentBusinessTask", parentBusinessTask.getText().trim().toUpperCase());
            CheckerProperties.setProperty("epic", parentEpic.getText().trim().toUpperCase());

            if (!parentProject.getText().trim().toUpperCase().equals(""))
                CheckerProperties.setProperty("project", parentProject.getText().trim().toUpperCase());
            else if (!parentEpic.getText().trim().toUpperCase().equals(""))
                CheckerProperties.setProperty("project", parentEpic.getText().trim().toUpperCase().split("-")[0]);
            else
                CheckerProperties.setProperty("project", parentBusinessTask.getText().trim().toUpperCase().split("-")[0]);

            CheckerProperties.setProperty("taskPostfixName", taskPostfixName.getText().trim());
            CheckerProperties.setProperty("taskPrefixName", taskPrefixName.getText().trim());
            CheckerProperties.setProperty("replacementSprintDate", sprintDate.getText().trim());
            xlsTaskList = new XlsTaskList(Paths.get("templateGenerator2.0.xlsx"));
            xlsTaskList.setTemplateName(selectedValue);
            new Thread(xlsTaskList).start();
        });
    }

    public static void main(String[] args) throws IOException {
        Path templateFilePath = Paths.get("templateGenerator2.0.xlsx");
        Morda morda = new Morda("Нарезать задачи JIRA");
        morda.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        morda.setVisible(true);

        PrintStream logPrintStream = new PrintStream(new LogOutputStream(morda.logArea), true, "UTF-16");

        System.setOut(logPrintStream);
        System.setErr(logPrintStream);


        ArrayList<String> templateNames = getTemplateNames(templateFilePath);
        templateNames.stream()
                .forEach(t -> morda.templateFile.addItem(t));

        morda.templateFile.addActionListener(a -> {
            selectedValue = ((JComboBox) a.getSource()).getSelectedItem().toString();
            xlsTaskList = new XlsTaskList(templateFilePath);
            xlsTaskList.setTemplateName(selectedValue);
            ArrayList<IssueTemplate> issueTemplates = xlsTaskList.getIssueTemplates(selectedValue);

            String collect = issueTemplates.stream()
                    .map(i -> i.getFieldValues().get("summary") + "(" + i.getFieldValues().get("issuetype") + ")")
                    .collect(Collectors.joining("\n"));
            morda.templateContentArea.setText(collect);

            HashSet<String> taskTypes = (HashSet<String>) issueTemplates.stream().map(i -> i.getFieldValues().get("issuetype")).collect(Collectors.toSet());
            if (taskTypes.contains("Epic")) {
                morda.parentProject.setEnabled(true);
                morda.parentEpic.setEnabled(false);
                morda.parentBusinessTask.setEnabled(false);

            } else if (taskTypes.contains("Задача")) {
                morda.parentProject.setEnabled(false);
                morda.parentEpic.setEnabled(true);
                morda.parentBusinessTask.setEnabled(false);
            } else {
                morda.parentProject.setEnabled(false);
                morda.parentEpic.setEnabled(false);
                morda.parentBusinessTask.setEnabled(true);
            }


        });

        morda.templateFile.setSelectedIndex(0);
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setBorder(BorderFactory.createTitledBorder(null, "Настройки", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(-16777216)));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(800, -1), null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Наименование шаблона");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        templateFile = new JComboBox();
        templateFile.setEditable(true);
        panel1.add(templateFile, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Номер бизнес-задачи, в которой будут нарезаться подзадачи");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        parentBusinessTask = new JTextField();
        panel1.add(parentBusinessTask, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Постфикс названия для всех задач");
        panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        taskPostfixName = new JTextField();
        panel1.add(taskPostfixName, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        initButton = new JButton();
        initButton.setText("Начать нарезку");
        panel1.add(initButton, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Дата спринта (YYYY-MM-DD)");
        panel1.add(label4, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sprintDate = new JTextField();
        panel1.add(sprintDate, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Номер эпика, в котором будут нарезать задачи");
        panel1.add(label5, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        parentEpic = new JTextField();
        panel1.add(parentEpic, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Код проекта, в котором будет нарезаться эпик");
        panel1.add(label6, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        parentProject = new JTextField();
        panel1.add(parentProject, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Префикс названия для всех задач");
        panel1.add(label7, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        taskPrefixName = new JTextField();
        panel1.add(taskPrefixName, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        logPanel = new JPanel();
        logPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(logPanel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logPanel.setBorder(BorderFactory.createTitledBorder("Лог нарезки"));
        final JScrollPane scrollPane1 = new JScrollPane();
        logPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 200), null, null, 0, false));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setText("");
        scrollPane1.setViewportView(logArea);
        templateContent = new JPanel();
        templateContent.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(templateContent, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        templateContent.setBorder(BorderFactory.createTitledBorder("Содержание шаблона"));
        final JScrollPane scrollPane2 = new JScrollPane();
        templateContent.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        templateContentArea = new JTextArea();
        templateContentArea.setEditable(false);
        templateContentArea.setText("");
        scrollPane2.setViewportView(templateContentArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
