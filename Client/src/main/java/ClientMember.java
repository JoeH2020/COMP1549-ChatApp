import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ClientMember extends Member{

//  Declare the instances of the JFrame and relevant elements
    JFrame frame = new JFrame("Chat Client");
    JTextField textBox = new JTextField(60);
    JTextArea messageArea = new JTextArea(20, 60);

    String serverAddress;

    public ClientMember(String UID, String IP, String port, String serverIP, PrintWriter out) {
        super(UID, IP, port);

        this.serverAddress = serverIP;

//      Setup JFrame and related elements
        textBox.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textBox, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

//      Wait for user to press Enter and then clear textBox when pressed
        textBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textBox.getText());
                textBox.setText("");
            }
        });

    }

}
