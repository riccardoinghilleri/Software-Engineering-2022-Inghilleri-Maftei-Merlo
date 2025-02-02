package it.polimi.ingsw.client.gui.controllers;

import it.polimi.ingsw.client.gui.Gui;
import it.polimi.ingsw.enums.CharacterColor;
import it.polimi.ingsw.server.ConnectionMessage.ActionMessage;
import it.polimi.ingsw.server.model.CharacterCard;
import it.polimi.ingsw.server.model.CharacterCardwithStudents;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.Objects;
/**
 * Class CharacterCardController displays the character card on GUI
 */
public class CharacterCardController implements GuiController {

    private Gui gui;
    private Stage stage;
    private ActionMessage message;
    private MainSceneController mainSceneController;
    private boolean alreadyAskedMovements = false;

    @FXML
    private Label infoText, cardName;
    @FXML
    private ImageView cardImg;
    @FXML
    private AnchorPane studentsPane;
    @FXML
    private Button right;
    @FXML
    private TextField movements_textField;

    /**
     * This method sets the parameter alreadyAskedMovements
     * @param alreadyAskedMovements boolean whose value is true if the number
     * of movements has been already asked. (e.g. when using the "PERFORMER")
     *
     */
    public void setAlreadyAskedMovements(boolean alreadyAskedMovements) {
        this.alreadyAskedMovements = alreadyAskedMovements;
    }

    /**
     * This method returns the boolean alreadyAskedMovements
     * @return a boolean
     */
    public boolean isAlreadyAskedMovements() {
        return alreadyAskedMovements;
    }

    /**
     * This method sets the stage
     * @param stage of type Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * This method sets the MainSceneController
     * @param mainSceneController of type MainSceneController
     */
    public void setMainSceneController(MainSceneController mainSceneController) {
        this.mainSceneController = mainSceneController;
    }

    /**
     * This method sets the message to be sent to the server.
     * @param message of type Action message
     */
    public void setMessage(ActionMessage message) {
        this.message = message;
    }

    /**
     * This method removes from the stage the elements that has to be displayed
     * only if the Card is a CharacterCardWithStudents.
     */
    public void clear() {
        for (int i = 1; i <= 12; i++) {
            studentsPane.getChildren().get(i).setVisible(false);
        }
        studentsPane.setVisible(false);
        movements_textField.setVisible(false);
        right.setVisible(false);
        movements_textField.setDisable(false);
        right.setDisable(false);
    }

    /**
     * This method manages the parameters setting for the use of a special card.
     *@param card character card chosen by the player
     */

    public void update(CharacterCard card) {
        message.setCharacterCardName(card.getName().toString().toUpperCase());
        cardName.setText(card.getName().toString().toUpperCase());
        cardImg.setImage(new Image(Objects.requireNonNull(getClass()
                .getResourceAsStream("/graphics/characterCards/" + card.getName().toString().toLowerCase() + ".jpg"))));
        studentsPane.setVisible(false); //cambiamento
        switch (card.getName()) {
            case QUEEN:
            case PRIEST: //isole e colore dello studente
                infoText.setText("CHOOSE THE COLOR OF A STUDENT");
                enableStudentsPane(card);
                break;
            case CLOWN: //colori studenti della carta e studenti colori ingresso scuola
                if (!alreadyAskedMovements) {
                    infoText.setText("HOW MANY STUDENTS DO YOU WANT TO CHANGE?");
                    alreadyAskedMovements = true;
                    movements_textField.clear();
                    movements_textField.setDisable(false);
                    movements_textField.setPromptText("");
                    right.setDisable(false);
                    movements_textField.setVisible(true);
                    right.setVisible(true);
                } else {
                    infoText.setText("CHOOSE THE COLOR OF A STUDENT");
                    enableStudentsPane(card);
                }
                break;
            case LUMBERJACK: //colore a caso disponibile in character color
            case THIEF: //colore casuale
                infoText.setText("CHOOSE A COLOR");
                allColorsStudentsPane();
                break;
            case PERFORMER: //colori della carta e nell?ingresso
                if (!alreadyAskedMovements) {
                    infoText.setText("HOW MANY STUDENTS DO YOU WANT TO CHANGE?");
                    alreadyAskedMovements = true;
                    movements_textField.clear();
                    movements_textField.setDisable(false);
                    movements_textField.setPromptText("");
                    right.setDisable(false);
                    movements_textField.setVisible(true);
                    right.setVisible(true);
                }
                break;
        }
    }

    /**
     * This method manages the mouse Event associated to the choice of a student.
     * (e.g. when using priest, after choosing the student, the player has
     * to choose the island)
     *
     * @param event of type Mouse Event
     */
    public void setColor(MouseEvent event) {
        message.setParameter(((Circle) event.getSource()).getId().toUpperCase());
        switch (message.getCharacterCardName().toUpperCase()) {
            case "PRIEST":
                mainSceneController.setMessage(message);
                mainSceneController.setInfoText("Choose an Island");
                mainSceneController.enableAllIslandsBroke();
                break;
            case "CLOWN":
                mainSceneController.setMessage(message);
                mainSceneController.setInfoText("Choose a Student from your entrance");
                mainSceneController.glowEntrance(true);
                mainSceneController.disableSchoolButtons(true);
                break;
            case "LUMBERJACK":
            case "QUEEN":
            case "THIEF":
                gui.getConnection().send(message);
                break;
            case "PERFORMER":
                break;

        }
        stage.close();
    }

    /**
     * This method parses the input of the movements_textField, showing an error message
     * if the input is not valid.
     * PERFORMER [1-min(2,number of students in the diningroom)] CLOWN [1-3]
     */
    public void setMovements() {
        int movements = -1;
        try {
            movements = Integer.parseInt(movements_textField.getText());
        } catch (NumberFormatException e) {
            movements_textField.clear();
            movements_textField.setPromptText("Invalid Input");
        }
        if (((movements < 1 || movements > 3) && cardName.getText().equalsIgnoreCase("CLOWN"))
                || ((movements < 1 || movements > Math.min(2,mainSceneController.getDiningroomStudents()))
                && cardName.getText().equalsIgnoreCase("PERFORMER"))) {
            movements_textField.clear();
            movements_textField.setPromptText("Invalid Input");
        }
        else {
            movements_textField.setDisable(true);
            right.setDisable(true);
            message.setData(Integer.parseInt(movements_textField.getText()));
            gui.getConnection().send(message);
            movements_textField.clear();
            stage.close();
        }
    }

    /**
     * This method selects the object when the mouse is on it, changing the glow
     * of the selected node.
     * @param event a mouse action on the object.
     */
    public void select(MouseEvent event) {
        Object object = event.getSource();
        ((Node) object).setEffect(new Glow(0.8));
    }

    /**
     * This method deselect the object when the mouse is on it,
     * changing the glow of the deselected node.
     * @param event a mouse action on the object.
     */
    public void unselect(MouseEvent event) {
        Object object = event.getSource();
        ((Node) object).setEffect(null);
    }

    /**
     * This method manages the display of students owned by a CharacterCharacterCards
     * with students.
     * @param card of type CharacterCard
     */
    private void enableStudentsPane(CharacterCard card) {
        studentsPane.setVisible(true); //cambiamento
        for (int i = 1; i <= 6; i++) {
            if (i <= ((CharacterCardwithStudents) card).getStudents().size()) {
                ((ImageView) studentsPane.getChildren().get(i)).setImage(new Image(Objects.requireNonNull(getClass()
                        .getResourceAsStream("/graphics/pieces/student_"
                                + ((CharacterCardwithStudents) card).getStudents().get(i - 1)
                                .getColor().toString().toLowerCase() + ".png"))));
                studentsPane.getChildren().get(i+6).setVisible(true);
                studentsPane.getChildren().get(i).setVisible(true);
                studentsPane.getChildren().get(i+6).setId(((CharacterCardwithStudents) card).getStudents().get(i - 1)
                        .getColor().toString().toLowerCase());
            } else {
                studentsPane.getChildren().get(i).setVisible(false);
                studentsPane.getChildren().get(i + 6).setVisible(false);
            }
        }
    }

    /**
     * This method displays a pane with a student of each color, for the use
     * of the Character Card "THIEF".
     */
    private void allColorsStudentsPane() {
        studentsPane.setVisible(true);
        for (int i = 1; i <= 5; i++) {
            ((ImageView) studentsPane.getChildren().get(i)).setImage(new Image(Objects.requireNonNull(getClass()
                    .getResourceAsStream("/graphics/pieces/student_"
                            + CharacterColor.values()[i - 1].toString().toLowerCase() + ".png"))));
            studentsPane.getChildren().get(i).setVisible(true);
            studentsPane.getChildren().get(i + 6).setVisible(true);
            studentsPane.getChildren().get(i + 6).setId(CharacterColor.values()[i - 1].toString().toLowerCase());
        }
        studentsPane.getChildren().get(6).setVisible(false);
        studentsPane.getChildren().get(12).setVisible(false);
    }

    /**
     * @see GuiController
     * @param gui of type Gui- the main Gui class
     */
    @Override
    public void setGui(Gui gui) {
        this.gui = gui;
    }
}
