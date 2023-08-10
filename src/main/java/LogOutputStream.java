import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class LogOutputStream extends OutputStream {
    JTextArea textArea;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public LogOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        //m.logArea.append(String.valueOf((char) b));
        baos.write(b);
    }

    @Override
    public void flush() throws IOException {
        baos.flush();
        baos.close();
        textArea.append(new String(baos.toByteArray(), "UTF-16"));
        baos.reset();
    }
}