package multiagentes.jade.ticktacktoe;

/**
 *
 * @author Molina This class represents the model. Every object of this class
 * owns to one Player, so that every player has its own copy of the game status.
 */
public class Board {

    private static final int _SIZE = 3;
    private SquareStatus[][] _matrix;

    public Board() {
        this._matrix = new SquareStatus[_SIZE][_SIZE];
        for (int i = 0; i < _SIZE; i++)
            for (int j = 0; j < _SIZE; j++)
                _matrix[i][j] = SquareStatus.EMPTY;
    }

    /**
     *  Imprime por consola el tablero. Se usa para tareas de debug.
     */
    public void printBoard() {
        System.out.println();
        for (int i = 0; i < _SIZE; i++) {
            System.out.println();
            for (int j = 0; j < _SIZE; j++)
                System.out.print(_matrix[i][j].toString() + " ");
        }
        System.out.println();
    }

    /**
     * Modifica el estado de una de las casillas del tablero.
     * @param movement  Posición de la casilla.
     * @param status    Nuevo estado de la casilla.
     * @throws Exception    Si la casilla no es válida.
     */
    public void doMovement(int movement, SquareStatus status) throws Exception {
        int row = movement / _SIZE;
        int column = movement % _SIZE;
        if (_matrix[row][column] == SquareStatus.EMPTY
                && movement >= 0 && movement < _SIZE * _SIZE)
            _matrix[row][column] = status;
        else
            throw new Exception("Invalid square");
    }

    /**
     * Acceso a la matriz que compone el tablero.
     * @return  Tablero actual.
     */
    public SquareStatus[][] getMatrix() {
        return _matrix;
    }
}
