package it.polimi.ingsw.client;

import it.polimi.ingsw.constants.Constants;
import it.polimi.ingsw.server.ConnectionMessage.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ConnectionSocket class handles the connection between the client and the server.
 *
 * @author
 */
public class ClientConnection implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    private Socket socket;
    private boolean active;

    private ObjectOutputStream os;
    private ObjectInputStream is;

    private final View view;

    public ClientConnection(View view) throws IOException {
        this.serverAddress = Cli.getAddress();
        this.serverPort = Cli.getPort();
        active = true;
        this.view = view;
        socket = new Socket(serverAddress, serverPort);
        is = new ObjectInputStream(socket.getInputStream());
        os = new ObjectOutputStream(socket.getOutputStream());
    }

    public void send(Message message) {
        try {
            os.reset();
            os.writeObject(message);
            os.flush();
        } catch (IOException e) {
            System.err.println("Error during send process.");
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (active) {
                Object message = is.readObject();
                if (message instanceof InfoMessage && ((InfoMessage) message).getString().equalsIgnoreCase("PING")) {
                    send(new InfoMessage("PING"));
                } else manageMessage((Message) message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void manageMessage(Message message) {
        ((ServerMessage) message).forward(view);
    }


}