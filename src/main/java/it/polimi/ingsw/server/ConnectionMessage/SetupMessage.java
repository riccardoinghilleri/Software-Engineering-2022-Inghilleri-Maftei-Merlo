package it.polimi.ingsw.server.ConnectionMessage;

public class SetupMessage implements Message{
    private String string;

    public SetupMessage(String string) {
        this.string = string;
    }

    public String getString() {
        return this.string;
    }
}
