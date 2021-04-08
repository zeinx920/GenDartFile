import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Created by tracy on 4/7/21.
 */
public class GenDartFile extends AnAction {
    private Project project;
    private String psiPath;
    private JDialog jDialog;
    private JTextField nameTextField;
    private JTextArea previewTextArea;
    private ButtonGroup templateGroup;
    private String previewText, type;
    private JRadioButton widgetBtn, modelBtn;

    @Override
    public void actionPerformed(AnActionEvent event) {
        project = event.getProject();
        psiPath = event.getData(PlatformDataKeys.PSI_ELEMENT).toString();
        psiPath = psiPath.substring(psiPath.indexOf(":") + 1);
        initView();
    }

    private void initView() {
        jDialog = new JDialog(new JFrame(), "Generate Formatted Dart File");
        Container container = jDialog.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

        setCodeFile(container);
        setNameAndConfirm(container);
        setJDialog();
    }

    private void setJDialog() {
        jDialog.setModal(true);
        ((JPanel) jDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jDialog.setSize(600, 600);
        jDialog.setLocationRelativeTo(null);
        jDialog.setVisible(true);
    }

    private void setCodeFile(Container container) {
        JPanel template = new JPanel();
        template.setLayout(new GridLayout(1, 2));

        widgetBtn = new JRadioButton("widget");
        setPadding(widgetBtn, 5, 10);
        widgetBtn.setActionCommand("widget");
        widgetBtn.addItemListener(itemListener);

        template.setBorder(BorderFactory.createTitledBorder("选择文件模板"));
        modelBtn = new JRadioButton("model", true);
        setPadding(modelBtn, 5, 10);
        modelBtn.setActionCommand("model");
        modelBtn.addItemListener(itemListener);

        template.add(modelBtn);
        template.add(widgetBtn);
        templateGroup = new ButtonGroup();
        templateGroup.add(modelBtn);
        templateGroup.add(widgetBtn);

        container.add(template);
        setDivision(container);
    }

    //输入框
    private void setNameAndConfirm(Container container) {
        JPanel nameField = new JPanel();
        nameField.setLayout(new FlowLayout());
        nameField.setBorder(BorderFactory.createTitledBorder("文件名   格式:aaa_bbb"));
        nameTextField = new JTextField(30);
        nameTextField.setHorizontalAlignment(JTextField.LEFT);
        nameTextField.addKeyListener(keyListener);
        nameTextField.addFocusListener(focusListener);
        nameField.add(nameTextField);
        container.add(nameField);

        setPreview(container);

        JPanel menu = new JPanel();
        menu.setLayout(new FlowLayout());

        setDivision(container);

        JButton cancel = new JButton("取消");
        cancel.setForeground(JBColor.RED);
        cancel.addActionListener(actionListener);

        JButton ok = new JButton("确定");
        ok.setForeground(JBColor.BLUE);
        ok.addActionListener(actionListener);
        menu.add(cancel);
        menu.add(ok);
        container.add(menu);
    }

    //预览
    private void setPreview(Container container) {
        JPanel file = new JPanel();
        file.setLayout(new GridLayout(1, 2));
        file.setBorder(BorderFactory.createTitledBorder("效果预览"));
        previewTextArea = new JTextArea();
        previewTextArea.setWrapStyleWord(true);
        previewTextArea.setLineWrap(true);
        previewTextArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 0));
        file.add(previewTextArea);
        container.add(file);
    }

    //保存
    private void save() {
        if (nameTextField.getText() == null || "".equals(nameTextField.getText().trim())) {
            Messages.showInfoMessage(project, "文件名不能为空", "提示");
            return;
        }
        dispose();
        updatePreview();
        writeToLocal(psiPath, previewTextArea.getText(), nameTextField.getText() + ".dart");
        project.getBaseDir().refresh(false, true);
    }

    private void writeToLocal(String filePath, String content, String outFileName) {
        try {
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(filePath + "/" + outFileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }


    private final KeyListener keyListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) save();
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    };

    private final ItemListener itemListener = new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
            updatePreview();
            previewTextArea.setText(previewText);
        }
    };

    private final FocusListener focusListener = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {

        }

        //失去焦点更新预览
        @Override
        public void focusLost(FocusEvent e) {
            updatePreview();
            previewTextArea.setText(previewText);
        }
    };

    private void updatePreview() {
        type = templateGroup.getSelection().getActionCommand();
        previewText = parseFile(type + ".dart", psiPath, type + ".dart");
    }

    //读取文件
    private String parseFile(String inputFileName, String filePath, String outFileName) {
        String content = "";
        try {
            InputStream in = this.getClass().getResourceAsStream("/files/" + inputFileName);
            content = new String(readStream(in));
        } catch (Exception e) {
            e.printStackTrace();
        }
        content = content.replaceAll("\\$name", upperTable(nameTextField.getText()));
        return content;
    }

    //首字母和下划线第二个字母大写
    public static String upperTable(String str) {
        StringBuffer sbf = new StringBuffer();
        if (str.contains("_")) {
            String[] split = str.split("_");
            for (String s : split) {
                String upperTable = upperTable(s);
                sbf.append(upperTable);
            }
        } else {
            char[] ch = str.toCharArray();
            if (ch[0] >= 'a' && ch[0] <= 'z') {
                ch[0] = (char) (ch[0] - 32);
            }
            sbf.append(ch);
        }
        return sbf.toString();
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("取消")) {
                dispose();
            } else {
                save();
            }
        }
    };

    private void setPadding(JRadioButton btn, int top, int bottom) {
        btn.setBorder(BorderFactory.createEmptyBorder(top, 10, bottom, 0));
    }

    private void setDivision(Container container) {
        JPanel margin = new JPanel();
        container.add(margin);
    }

    private void dispose() {
        jDialog.dispose();
    }
}