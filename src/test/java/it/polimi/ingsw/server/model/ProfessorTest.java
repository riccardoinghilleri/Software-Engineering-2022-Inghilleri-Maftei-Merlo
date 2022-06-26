package it.polimi.ingsw.server.model;

import it.polimi.ingsw.constants.Constants;
import it.polimi.ingsw.enums.CharacterColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfessorTest {

    @Test
    void testConstructorProfessor(){
        Professor professor= new Professor(CharacterColor.YELLOW);
        assertEquals(-1, professor.getOwner());
        assertEquals(CharacterColor.YELLOW, professor.getColor());
    }
    @Test
    void testSetOwner(){
        int owner=1;
        Professor professor= new Professor(CharacterColor.PINK);
        assertEquals(CharacterColor.PINK, professor.getColor());
        professor.setOwner(owner);
        assertEquals(owner, professor.getOwner());
    }

    @Test
    public void testToString(){
        Professor professor= new Professor(CharacterColor.RED);
        assertEquals(Constants.getAnsi(CharacterColor.RED)+"▲"+Constants.ANSI_RESET,professor.toString());
    }
}