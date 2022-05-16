package it.polimi.ingsw.controller.actioncontroller;

import it.polimi.ingsw.server.ConnectionMessage.ActionMessage;
import it.polimi.ingsw.server.model.GameModel;
import it.polimi.ingsw.server.model.Player;
import it.polimi.ingsw.enums.CharacterColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Lumberjack card and contains the specific method of the card.
 */
public class Knight extends ActionController {
    /**
     * The constructor creates a Lumberjack instance
     */
    public Knight(GameModel gameModel) {
        super(gameModel);
        //TODO da finire
    }
    /**
     * This method implements the 'super-power' of the card, overriding the method getInfluence from action controller.
     * Go to "model->boardExpert" to see the complete description.
     * @param actionMessage message with all the necessary parameter.
     * @return the player with the highest influence.
     */
    //metodo che calcola l'influenza aggiungenddo due punti addizionali al currentPlayer
    @Override
    public int getInfluence(ActionMessage actionMessage) {
        int[] influence = new int[getGameModel().getPlayers().size()];
        for(Player player : getGameModel().getPlayers()) {
            influence[player.getClientID()]=0;
        }
        influence = getGameModel().getBoard().getStudentInfluence(actionMessage.getData(),influence, Arrays.asList(CharacterColor.values()));
        influence = getGameModel().getBoard().getTowersInfluence(actionMessage.getData(),influence);
        influence[getGameModel().getCurrentPlayer().getClientID()]+=2;
        return getGameModel().getBoard().getMaxInfluence(influence);
    }
}
