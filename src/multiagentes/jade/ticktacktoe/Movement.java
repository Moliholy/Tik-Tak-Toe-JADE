package multiagentes.jade.ticktacktoe;

import java.util.Random;

public class Movement {

    protected final int SIZE;
    protected SquareStatus[][] matrix;
    protected int depth;
    protected Movement[] children;
    protected double score;
    protected Integer movement;
    protected final int MAX_DEPTH = 9;

    /**
     * Class constructor. It initializes all its parameters
     *
     * @param _matrix current board representation
     */
    public Movement(SquareStatus[][] _matrix) {
        SIZE = _matrix.length;
        //MAX_DEPTH = SIZE * SIZE;
        movement = null;
        matrix = _matrix;
        depth = 0;
        generateChildren();
        score = calculateScore();
        //printScoreTree();
    }

    protected void printScoreTree() {
        String tab = "";
        for (int i = 0; i < depth; i++)
            tab += '\t';
        System.out.println(tab + score);
        if (children != null)
            for (int i = 0; i < children.length; i++)
                children[i].printScoreTree();
    }

    /**
     * Special constructor used only to generate its children
     *
     * @param _matrix the board's representation
     * @param _depth depth analysis usend in tree's exploration
     * @param _movement movement made to reach this board's status
     */
    protected Movement(SquareStatus[][] _matrix, int _depth, int _movement) {
        SIZE = _matrix.length;
        //MAX_DEPTH = SIZE * SIZE;
        movement = _movement;
        matrix = _matrix;
        depth = _depth;
        generateChildren();
        score = calculateScore();
    }

    /**
     * Gets the movement used to reach this status
     *
     * @return the movement used to reach this board's status
     */
    public Integer getMovement() {
        return movement;
    }

    /**
     * Gets the punctuation of this movement
     *
     * @return punctuation assigned to this board's status
     */
    public double getPunctuation() {
        if (depth % 2 == 0)
            return score;
        return -score;
    }

    /**
     * Gets the best movement starting from the given status as an integer value
     *
     * @return the best movement found as an square position
     */
    public Integer getBestMovement() {
        return getBestAction().movement;
    }

    /**
     * Gets the best movement starting from the given status
     *
     * @return the best movement found
     */
    public Movement getBestAction() {
        Movement bestAction = null;
        if (children != null) {
            Random random = new Random();
            bestAction = children[0];
            double punct = children[0].getPunctuation();
            for (int i = 1; i < children.length; i++)
                if (children[i].getPunctuation() > punct
                        || (children[i].getPunctuation() == punct
                        && random.nextBoolean())) {
                    punct = children[i].getPunctuation();
                    bestAction = children[i];
                }
        }
        return bestAction;
    }

    /**
     * Gets the number of empty squares
     *
     * @return the total number of squares marked as empty
     */
    protected int getFreeSquares() {
        int counter = 0;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (matrix[i][j] == SquareStatus.EMPTY)
                    counter++;
        return counter;
    }

    /**
     * Generates all children starting from the current status
     */
    private void generateChildren() {
        if (depth < MAX_DEPTH && !leafNode()) {
            int size = getFreeSquares();
            if (size > 0) {
                children = new Movement[size];
                int pos = 0;
                for (int i = 0; i < SIZE * SIZE; i++) {
                    int row = i / SIZE;
                    int column = i % SIZE;
                    if (matrix[row][column] == SquareStatus.EMPTY)
                        children[pos++] = generateChildren(i);
                }
            }
        } else // this is a leaf node
            children = null;
    }

    /**
     * Generates all children starting from a given position
     *
     * @param pos position to start a new movement
     * @return the generated child
     */
    protected Movement generateChildren(int pos) {
        SquareStatus[][] newMatrix = new SquareStatus[SIZE][SIZE];
        // let's copy the matrix
        for (int i = 0; i < newMatrix.length; i++)
            for (int j = 0; j < newMatrix.length; j++)
                switch (matrix[i][j]) {
                    case EMPTY:
                        newMatrix[i][j] = SquareStatus.EMPTY;
                        break;
                    case FRIENDLY:
                        newMatrix[i][j] = SquareStatus.FOE;
                        break;
                    case FOE:
                        newMatrix[i][j] = SquareStatus.FRIENDLY;
                        break;
                }
        // and modify one data
        int row = pos / SIZE;
        int column = pos % SIZE;
        newMatrix[row][column] = SquareStatus.FOE;
        return new Movement(newMatrix, depth + 1, pos);
    }

    /**
     * Calculates the status' score
     *
     * @return the status' score as a number
     */
    private double calculateScore() {
        // if the movement has children, the score will be better or worst than
        // its children's scores, depending if we are analizing a
        // odd or peer depth according to minimax criterion
        // IF IT DOES NOT HAVE CHILDREN STANDARD LEAF NODE ALGORITHM WILL BE APPLY
        if (children == null)
            return evaluatePosition();
        else
            return maximumValue();
        /*
         else if (depth % 2 == 0)
         return maximumValue();
         else
         return minimumValue();
         * */
    }

    /**
     * Gets the children with the worst punctuation of its brothers
     *
     * @return the worst child in the current status
     */
    protected double minimumValue() {
        double minimumPunctuation = children[0].getPunctuation();
        for (int i = 1; i < children.length; i++)
            if (children[i].getPunctuation() < minimumPunctuation)
                minimumPunctuation = children[i].getPunctuation();
        return minimumPunctuation;
    }

    /**
     * Gets the children with the best punctuation of its brothers
     *
     * @return the best child in the current status
     */
    protected double maximumValue() {
        double maximumPunctuation = children[0].getPunctuation();
        for (int i = 1; i < children.length; i++)
            if (children[i].getPunctuation() > maximumPunctuation)
                maximumPunctuation = children[i].getPunctuation();

        return maximumPunctuation;
    }

    /**
     * Checks if a node has no children
     *
     * @return true if a tree's node has children of false otherwise
     */
    protected boolean leafNode() {
        int counter1 = 0;
        int counter2 = 0;


        // ROWS
        for (int i = 0; i < SIZE; i++) {
            counter1 = 0;
            counter2 = 0;
            for (int j = 0; j < SIZE; j++)
                if (matrix[i][j] == SquareStatus.FOE)
                    counter1++;
                else if (matrix[i][j] == SquareStatus.FRIENDLY)
                    counter2++;
        }
        if (counter2 == SIZE
                || counter1 == SIZE)
            return true;


        // COLUMNS
        for (int i = 0; i < SIZE; i++) {
            counter1 = 0;
            counter2 = 0;
            for (int j = 0; j < SIZE; j++)
                if (matrix[i][j] == SquareStatus.FOE)
                    counter1++;
                else if (matrix[i][j] == SquareStatus.FRIENDLY)
                    counter2++;
            if (counter2 == SIZE
                    || counter1 == SIZE)
                return true;
        }


        // MAIN DIAGONAL
        counter1 = 0;
        counter2 = 0;
        for (int i = 0; i < SIZE; i++)
            if (matrix[i][i] == SquareStatus.FOE)
                counter1++;
            else if (matrix[i][i] == SquareStatus.FRIENDLY)
                counter2++;
        if (counter2 == SIZE
                || counter1 == SIZE)
            return true;


        // SECONDARY DIAGONAL
        counter1 = 0;
        counter2 = 0;
        for (int i = 0, j = SIZE - 1;
                i < SIZE && j >= 0; i++, j--)
            if (matrix[i][j] == SquareStatus.FOE)
                counter1++;
            else if (matrix[i][j] == SquareStatus.FRIENDLY)
                counter2++;
        if (counter2 == SIZE
                || counter1 == SIZE)
            return true;


        if (getFreeSquares() == 0)
            return true;

        return false;
    }

    /**
     * Calculates the associated position's value
     *
     * @return the score of the current position
     */
    protected double evaluatePosition() {
        double punct = 0;
        int counterFOE;
        int counterFRIENDLY;
        // ROWS
        for (int i = 0; i < SIZE; i++) {
            counterFOE = 0;
            counterFRIENDLY = 0;
            for (int j = 0; j < SIZE; j++)
                if (matrix[i][j] == SquareStatus.FOE)
                    counterFOE++;
                else if (matrix[i][j] == SquareStatus.FRIENDLY)
                    counterFRIENDLY++;
            if (counterFRIENDLY == SIZE)
                return best();
            else if (counterFOE == SIZE)
                return worst();
            else if (counterFOE == 0)
                punct++;
        }

        // COLUMNS
        for (int i = 0; i < SIZE; i++) {
            counterFOE = 0;
            counterFRIENDLY = 0;
            for (int j = 0; j < SIZE; j++)
                if (matrix[j][i] == SquareStatus.FOE)
                    counterFOE++;
                else if (matrix[j][i] == SquareStatus.FRIENDLY)
                    counterFRIENDLY++;
            if (counterFRIENDLY == SIZE)
                return best();
            else if (counterFOE == SIZE)
                return worst();
            else if (counterFOE == 0)
                punct++;
        }

        // MAIN DIAGONAL
        counterFOE = 0;
        counterFRIENDLY = 0;
        for (int i = 0; i < SIZE; i++)
            if (matrix[i][i] == SquareStatus.FOE)
                counterFOE++;
            else if (matrix[i][i] == SquareStatus.FRIENDLY)
                counterFRIENDLY++;
        if (counterFRIENDLY == SIZE)
            return best();
        else if (counterFOE == SIZE)
            return worst();
        else if (counterFOE == 0)
            punct++;

        // SECONDARY DIAGONAL
        counterFOE = 0;
        counterFRIENDLY = 0;
        for (int i = 0, j = SIZE - 1; i < SIZE && j >= 0; i++, j--)
            if (matrix[i][j] == SquareStatus.FOE)
                counterFOE++;
            else if (matrix[i][j] == SquareStatus.FRIENDLY)
                counterFRIENDLY++;
        if (counterFRIENDLY == SIZE)
            return best();
        else if (counterFOE == SIZE)
            return worst();
        else if (counterFOE == 0)
            punct++;
        return punct;
    }

    private int best() {
        return SIZE * 100 - depth;
    }

    private int worst() {
        return -(SIZE * 100 + depth);
    }
}
