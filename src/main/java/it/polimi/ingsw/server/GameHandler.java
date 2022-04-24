package it.polimi.ingsw.server;

import it.polimi.ingsw.controller.Action;
import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.server.ConnectionMessage.*;
import it.polimi.ingsw.server.model.GameModel;
import it.polimi.ingsw.server.model.Player;
import it.polimi.ingsw.server.model.enums.PlayerColor;
import it.polimi.ingsw.server.model.enums.Wizard;

import java.util.ArrayList;

import java.util.List;

public class GameHandler {
    private int gameId;
    private int playersNumber;
    private boolean expertMode;
    private int currentClientConnection;
    private GameModel gameModel;

    private int turnNumber;
    private int playerTurnNumber;

    private GameHandlerPhase phase;
    private List<ClientConnection> clients;
    private Server server;
    private Controller controller;
    boolean alreadySettedClouds;
    int alreadySettedAssistantCards;

    public GameHandler(int gameId, boolean expertMode, List<ClientConnection> clients, Server server) {
        this.gameId = gameId;
        this.playersNumber = clients.size();
        this.currentClientConnection = 0;
        this.expertMode = expertMode;
        this.clients = new ArrayList<>(clients);
        this.server = server;
        this.phase = GameHandlerPhase.SETUP_NICKAME;
        this.gameModel = new GameModel(expertMode);
        this.controller = new Controller(this.gameModel);
        this.turnNumber = 0;
        PlayerColor.reset(playersNumber);
        Wizard.reset();
        alreadySettedClouds = false;
        alreadySettedAssistantCards = 0;
        //TODO il server dopo aver creato il gamehandler chiama un metodo gamehandler.setupGame()
    }

    //Gestisce i messaggi ricevuti dalla client connection
    public void manageMessage(ClientConnection client, Message message) {
        if (currentClientConnection != client.getClientId()) {
            client.sendMessage(new InfoMessage("It is not your turn! Please wait."));
        } else {
            if (phase == GameHandlerPhase.SETUP_NICKAME) {
                setupNickname((SetupMessage) message);
            } else if (phase == GameHandlerPhase.SETUP_COLOR) {
                gameModel.getPlayerById(client.getClientId()).setColor(((SetupMessage) message).getString());
                phase = GameHandlerPhase.SETUP_WIZARD;
                setupGame();
            } else if (phase == GameHandlerPhase.SETUP_WIZARD) {
                gameModel.getPlayerById(client.getClientId()).getDeck().setWizard(((SetupMessage) message).getString());
                currentClientConnection = (currentClientConnection + 1) % (playersNumber - 1);
                if (currentClientConnection == 0) {
                    turnNumber = 1;
                    phase = GameHandlerPhase.PIANIFICATION;
                    gameModel.createBoard();
                    pianificationTurn();
                    //TODO forse si deve fare il display della board
                } else {
                    phase = GameHandlerPhase.SETUP_NICKAME;
                    setupGame();
                }
            } else {
                if (phase == GameHandlerPhase.PIANIFICATION) {
                    if (!controller.setAssistantCard((ActionMessage) message)) {
                        clients.get(currentClientConnection)
                                .sendMessage(new MultipleChoiceMessage("You can not choose this Assistant Card! " +
                                        "Please choose another Assistant Card: ",
                                        gameModel.getCurrentPlayer().getDeck().getAssistantCards()));
                    }
                    if (controller.getPhase() == Action.CHOOSE_ASSISTANT_CARD)
                        pianificationTurn();
                    else if (controller.getPhase() == Action.DEFAULT_MOVEMENTS) {
                        phase = GameHandlerPhase.ACTION;
                        actionTurn();
                    }
                } else if (phase == GameHandlerPhase.ACTION) {
                    //TODO in game
                    // nel primo turno l'ordine è quello di collegamento, poi è quello determinato dal gameModel
                    String error = controller.nextAction((ActionMessage) message);
                    if (error != null) {
                        clients.get(currentClientConnection).sendMessage(new InfoMessage(error));
                    }
                    if (controller.getPhase() == Action.SETUP_CLOUD && turnNumber < 10) {
                        turnNumber++;
                        phase = GameHandlerPhase.PIANIFICATION;
                        pianificationTurn();
                    } else if (controller.getPhase() == Action.SETUP_CLOUD && turnNumber == 10) {
                        //TODO checkEndGame
                    } else actionTurn();
                }
            }
        }
    }

    public void setupGame() {
        if (phase == GameHandlerPhase.SETUP_NICKAME) {
            clients.get(currentClientConnection).sendMessage(new NicknameMessage("Please choose your Nickname: "));
        } else if (phase == GameHandlerPhase.SETUP_COLOR) {
            if (PlayerColor.notChosen().size() > 1)
                clients.get(currentClientConnection)
                        .sendMessage(new MultipleChoiceMessage("Please choose your Color: ", PlayerColor.notChosen()));
            else {
                clients.get(currentClientConnection)
                        .sendMessage(new InfoMessage("The Game has chosen the color for you.\n" +
                                "Your color is " + PlayerColor.notChosen().get(0)));
                gameModel.getPlayerById(currentClientConnection).setColor(PlayerColor.notChosen().get(0).toString());
                phase = GameHandlerPhase.SETUP_WIZARD;
                setupGame();
            }
        } else if (phase == GameHandlerPhase.SETUP_WIZARD) {
            clients.get(currentClientConnection).sendMessage(new MultipleChoiceMessage("Please choose your Wizard: ", Wizard.notChosen()));
        }
    }

    public void pianificationTurn() {
        if (!alreadySettedClouds) {
            controller.setClouds();
            alreadySettedClouds = true; //ricordarsi di rimetterlo a false alla fine del turno
        }
        currentClientConnection = gameModel.getCurrentPlayer().getClientID();
        clients.get(currentClientConnection)
                .sendMessage(new MultipleChoiceMessage("Please choose an Assistant Card: ",
                        gameModel.getCurrentPlayer().getDeck().getAssistantCards()));
        //alreadySettedAssistantCards++;
    }

    public void actionTurn() {
        currentClientConnection = gameModel.getCurrentPlayer().getClientID();
        clients.get(currentClientConnection)
                .sendMessage(new AskActionMessage("Questa è la tua prossima azione: ",
                        controller.getPhase()));
    }

    public void setupNickname(SetupMessage message) {
        for (Player p : gameModel.getPlayers()) {
            if (p.getNickname().equals(message.getString())) {
                clients.get(currentClientConnection)
                        .sendMessage(new NicknameMessage("The nickname in not available. Please choose another Nickname: "));
                return;
            }
        }
        gameModel.createPlayer(message.getString(), currentClientConnection);
        phase = GameHandlerPhase.SETUP_COLOR;
        setupGame();
    }

    public void endGame(int winner) {
        //TODO stampare vincitore mandando messaggio a tutti
        while (!clients.isEmpty()) {
            clients.get(currentClientConnection).closeConnection();
        }
    }

    public void resetGame() {

    }

    public void sendAll(Message message) {

    }

    public void sendAllExcept(int clientId, Message message) {

    }

    public void send(int clientId, Message message) {

    }
}
