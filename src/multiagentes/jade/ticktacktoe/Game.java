/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagentes.jade.ticktacktoe;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import multiagentes.jade.utils.AgentHelper;

/**
 * Controlador del juego.
 *
 * @author Molina
 */
public class Game extends Agent {

    public static final String LOSE = "lose";
    public static final String WIN = "win";
    public static final String DRAW = "draw";
    protected static final int SIZE = 3;
    protected static final char EMPTY = '-';
    protected static final char PLAYER1 = 'O';
    protected static final char PLAYER2 = 'X';
    protected char[][] board;
    protected GameStatus status;
    protected AID player1;
    protected AID player2;
    protected Behaviour findPlayers;
    protected Behaviour manageGame;
    
    private Window wnd;

    /**
     *  Acceso al tablero de juego.
     * @return  Tablero de juego actual.
     */
    public char[][] getBoard() {
        return board;
    }

    /**
     *  Acceso al estado de juego actual.
     * @return  Estado actual del juego.
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     *
     * @return AID del jugador 1.
     */
    public AID getPlayer1() {
        return player1;
    }

    /**
     *
     * @return AID del jugador 2.
     */
    public AID getPlayer2() {
        return player2;
    }

    /**
     * Envía una invitación a un jugador para que se una al juego.
     * @param player    Jugador al que invitar.
     */
    protected void invitePlayer(AID player) {
        AgentHelper.sendMessage(this, player, ACLMessage.PROPOSE, null);
    }

    /**
     * Envía un mensaje del tipo INFORM a un jugador.
     * @param player    Jugador al que enviar el mensaje.
     * @param content   Contenido del mensaje.
     */
    protected void sendMessageToPlayer(AID player, String content) {
        AgentHelper.sendMessage(this, player, ACLMessage.INFORM, content);
    }
    
     /**
     * Makes the game wait.
     * @param ms    Time in miliseconds.
     */
    private synchronized void makeWait(long ms) {
        try {
            wait(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if there're enough players to begin a new game
     *
     * @return true if there are two players ready to play, or false otherwise
     */
    protected boolean playersReady() {
        return player1 != null && player2 != null;
    }

    /**
     * Prints the current board in the console
     */
    protected void printBoard() {
        System.out.println();
        for (int i = 0; i < SIZE; i++) {
            System.out.println();
            for (int j = 0; j < SIZE; j++)
                System.out.print(board[i][j] + " ");
        }
        System.out.println("\n");
    }

    /**
     * Initializes all class' parameters
     */
    protected void init() {
        status = GameStatus.UNBEGUN;
        player1 = player2 = null;
        board = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = EMPTY;
    }

    /**
     * Finds players to be added in order to be able to begin a new game
     */
    protected void findPlayers() {
        status = GameStatus.UNBEGUN;
        addBehaviour(findPlayers);
        removeBehaviour(manageGame);
    }

    /**
     * Changes the behaviour so that it stops attending game resposes and begins
     * managing the new game
     */
    protected void manageGame() {
        status = GameStatus.TURN_PLAYER1;
        addBehaviour(manageGame);
        removeBehaviour(findPlayers);
    }

    /**
     * Sends messages to all players whose content specifies the obtained result in the game.
     * It also reinitializes game parameters so that a new game can be begun
     */
    protected void finishGame() {
        final GameStatus st = status;
        switch (st) {
            case DRAW:
                sendMessageToPlayer(player1, DRAW);
                sendMessageToPlayer(player2, DRAW);
                break;
            case PLAYER1_WINS:
                sendMessageToPlayer(player1, WIN);
                sendMessageToPlayer(player2, LOSE);
                break;
            case PLAYER2_WINS:
                sendMessageToPlayer(player1, LOSE);
                sendMessageToPlayer(player2, WIN);
                break;
        }
        init();
        findPlayers();
    }

    /**
     * Checks if the current board has at least one square to be filled
     * @return true i the board is not completely filled, or false otherwise
     */
    protected boolean boardFilled() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] == EMPTY)
                    return false;
        return true;
    }

    /**
     * Checks if any player has alredy won
     * @param typeSquare square type that represents the player to be checked
     * @return the game status after the checkout
     */
    protected GameStatus checkWinner(char typeSquare) {
        if (typeSquare == PLAYER1)
            return GameStatus.PLAYER1_WINS;
        else if (typeSquare == PLAYER2)
            return GameStatus.PLAYER2_WINS;
        return null;
    }

    /**
     * Checks the game status after a player's movement
     * @return the new game status
     */
    protected GameStatus checkGameStatus() {
        // it checks the board status
        // 1) Checks rows
        for (int i = 0; i < SIZE; i++) {
            char player = board[i][0];
            if (player != EMPTY) {
                int counter = 1;
                for (int j = 1; j < SIZE; j++)
                    if (board[i][j] == player)
                        counter++;
                if (counter == SIZE)
                    return status = checkWinner(player);
            }
        }

        // 2) Check columns
        for (int i = 0; i < SIZE; i++) {
            char player = board[0][i];
            if (player != EMPTY) {
                int counter = 1;
                for (int j = 1; j < SIZE; j++)
                    if (board[j][i] == player)
                        counter++;
                if (counter == SIZE)
                    return status = checkWinner(player);
            }
        }

        // 3) Checks diagonals
        // diagonal from (0,0) to (2,2)
        char player = board[0][0];
        if (player != EMPTY) {
            int counter = 1;
            for (int i = 1, j = 1; i < SIZE && j < SIZE; i++, j++)
                if (board[i][j] == player)
                    counter++;
            if (counter == SIZE)
                return status = checkWinner(player);
        }
        // diagonal from (0,2) to (2,0)
        player = board[0][SIZE - 1];
        if (player != EMPTY) {
            int counter = 1;
            for (int i = 1, j = SIZE - 2; i < SIZE && j >= 0; i++, j--)
                if (board[i][j] == player)
                    counter++;
            if (counter == SIZE)
                return status = checkWinner(player);
        }

        //Once at this point we know no one has won. Now we check the rest of possibilities
        if (boardFilled())
            return status = GameStatus.DRAW;

        // if it wasn't draw, then we only change the player's turn
        return status = status == GameStatus.TURN_PLAYER1 ? GameStatus.TURN_PLAYER2 : GameStatus.TURN_PLAYER1;
    }

    /**
     * Controls the actions so that it allows players to move and waits for its response
     * @param lastMovement last movement made on the board
     */
    protected void manageActions(int lastMovement) {
        switch (status) {
            case UNBEGUN:
                //do nothing
                break;
            case TURN_PLAYER1:
                sendMessageToPlayer(player1, Integer.toString(lastMovement));
                break;
            case TURN_PLAYER2:
                sendMessageToPlayer(player2, Integer.toString(lastMovement));
                break;
            default:
                finishGame();
        }
    }

    @Override
    protected void setup() {
        super.setup();
        init();
        AgentHelper.log(this, "Connected and looking for players to begin the game");

        //definition of this behaviour in order to find players in the yellow pages
        findPlayers = new CyclicBehaviour() {
            @Override
            public void action() {
                try {
                    //it checks if there're accepted proposal messages in the box
                    ACLMessage received;
                    do {
                        received = AgentHelper.receiveMessage(myAgent, ACLMessage.ACCEPT_PROPOSAL);
                        if (received != null)
                            if (player1 == null)
                                player1 = received.getSender();
                            else if (player2 == null && !player1.equals(received.getSender()))
                                player2 = received.getSender();
                    } while (received != null && !playersReady());
                    if (!playersReady()) {
                        //it selects the first two avaiable agents that finds
                        DFAgentDescription[] agents = AgentHelper.lookForAvailableAgents(myAgent, Player.SERVICE_TYPE);
                        if (agents != null)
                            for (int i = 0; i < agents.length; i++)
                                if (player1 != agents[i].getName())
                                    invitePlayer(agents[i].getName());
                    } else {
                        //we are ready to begin the game
                        wnd.clear();
                        AgentHelper.log(myAgent, "now there are enough players to begin the game");
                        sendMessageToPlayer(player1, "");
                        manageGame();
                        //with this we begin the comunication process
                    }
                } catch (FIPAException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        //definition of the behaviour to manage an existing game
        manageGame = new CyclicBehaviour() {
            @Override
            public void action() {
                //now we've got two players, let's begin the game
                ACLMessage message = AgentHelper.receiveMessage(myAgent, ACLMessage.INFORM);
                AID player = status == GameStatus.TURN_PLAYER1 ? player1 : player2;
                if (message != null)
                    //if the sender is the player that has to move...
                    if (player.equals(message.getSender())) {
                        //then a new movement is done
                        int movement = Integer.parseInt(message.getContent());
                        int row = movement / SIZE, column = movement % SIZE;
                        if (board[row][column] == EMPTY)
                            board[row][column] = player == player1 ? PLAYER1 : PLAYER2;
                        if ( player == player1) {
                            wnd.addMovement(row, column, 1);
                        }else {
                            wnd.addMovement(row, column, 2);
                        }
                        //printBoard();
                        //after the movement we have to check the game status
                        checkGameStatus();
                        makeWait(1500L); //waitting a little in order to see the game correctly
                        manageActions(movement);
                    }
            }
        };

        //first of all we have to find players
        addBehaviour(findPlayers);
        
        // Create and show game window
        wnd = new Window();
        wnd.setVisible(true);
    }
}
