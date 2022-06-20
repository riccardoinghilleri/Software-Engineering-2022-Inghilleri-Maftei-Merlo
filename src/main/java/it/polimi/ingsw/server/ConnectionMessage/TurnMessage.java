package it.polimi.ingsw.server.ConnectionMessage;

import it.polimi.ingsw.client.Cli;
import it.polimi.ingsw.client.View;

public class TurnMessage implements Message,ServerMessage{

    boolean enable;

    public TurnMessage(boolean enable){
        this.enable=enable;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public void forward(View view) {
        if(view instanceof Cli)
            ((Cli)view).enable(this);
    }
}
