import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ClientMember {

    private String UID;
    private String port;
    private String IP;

    public ClientMember(String UID, String IP, String port) {
        this.UID = UID;
        this.port = port;
        this.IP = IP;
    }
}
