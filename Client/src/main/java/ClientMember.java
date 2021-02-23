import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ClientMember extends Member{

    public ClientMember(String UID, String IP, String port) {
        super(UID, IP, port);
    }
}
