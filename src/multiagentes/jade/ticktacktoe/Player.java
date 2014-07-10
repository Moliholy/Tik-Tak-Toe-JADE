/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagentes.jade.ticktacktoe;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import multiagentes.jade.utils.AgentHelper;

/**
 *
 * @author Molina
 */
public class Player extends Agent {

    public static final String SERVICE_NAME = "player";
    public static final String SERVICE_TYPE = "TTTplayer";
    protected HashMap<AID, Board> boards;
    protected int wins;
    protected int draws;
    protected int loses;

    /**
     * Calls the AI to make a movement
     * @param board game status' representation
     * @return the movement to be made expressed as an integer
     * @throws Exception if an invalid movement happens
     */
    protected int play(Board board) throws Exception {
        Movement action = new Movement(board.getMatrix());
        Integer square = action.getBestMovement();
        board.doMovement(square, SquareStatus.FRIENDLY);
        return square;
    }

    /**
     * Revome the existing connection with the finished game
     * @param game the game to be removed
     */
    protected void finishGame(AID game) {
        boards.remove(game);
    }

    /**
     * 
     * @return the number of winned games
     */
    public int getWins() {
        return wins;
    }

    /**
     * 
     * @return the number of drawed games
     */
    public int getDraws() {
        return draws;
    }

    /**
     * 
     * @return the number of lost games
     */
    public int getLoses() {
        return loses;
    }

    /**
     * 
     * @return the total number of games played
     */
    public int getPlayerGames() {
        return wins + loses + draws;
    }

    @Override
    protected void setup() {
        super.setup();
        wins = draws = loses = 0;
        boards = new HashMap<>();
        try {
            AgentHelper.registerYellowPages(this, SERVICE_NAME, SERVICE_TYPE);
        } catch (FIPAException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        AgentHelper.log(this, "Connected and successfully registered in the yellow pages");


        //first behaviour: manage boards' petitions
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = AgentHelper.receiveMessage(myAgent, ACLMessage.PROPOSE);
                //we've got the board's message that wants we to play
                if (message != null && !boards.containsKey(message.getSender())) {
                    //AgentHelper.log(myAgent, "new propose received from " + message.getSender().getName());
                    ACLMessage answer = message.createReply();
                    answer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    //all new petitions will be accepted
                    boards.put(message.getSender(), new Board());
                    myAgent.send(answer);
                }
            }
        });

        //second behaviour: manage movements
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = AgentHelper.receiveMessage(myAgent, ACLMessage.INFORM);
                if (message != null)
                    if (boards.containsKey(message.getSender()))
                        try {
                            AID sender = message.getSender();
                            Board board = boards.get(message.getSender());
                            Player owner = (Player) myAgent;
                            final String content = message.getContent();
                            AgentHelper.log(myAgent, "new movement received from " + sender.getName() + " - " + content);

                            //Check every option in order to send a response or do something
                            if (content == null || content.equals("")) {
                                int action = play(board);
                                sendAction(sender, Integer.toString(action));
                                //board.printBoard();
                            } else if (content.equals(Game.LOSE)) {
                                loses++;
                                finishGame(sender);
                            } else if (content.equals(Game.WIN)) {
                                wins++;
                                finishGame(sender);
                            } else if (content.equals(Game.DRAW)) {
                                draws++;
                                finishGame(sender);
                            } else {
                                int actionReceived = Integer.parseInt(content);
                                board.doMovement(actionReceived, SquareStatus.FOE);
                                int myAction = owner.play(board);
                                owner.sendAction(sender, Integer.toString(myAction));
                                AgentHelper.log(myAgent, "sending the movement " + myAction);
                                //board.printBoard();
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                        }
            }
        });
    }

    /**
     * Sends an action to a game
     * @param game the game as a message's receiver 
     * @param content the message to be sent
     */
    protected void sendAction(AID game, String content) {
        AgentHelper.sendMessage(this, game, ACLMessage.INFORM, content);
    }
    
    
}
