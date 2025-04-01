package src;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BattleshipGame extends JFrame {

    private static final int GRID_SIZE = 10;
    private JButton[][] boardButtons = new JButton[GRID_SIZE][GRID_SIZE];
    private char[][] gameBoard = new char[GRID_SIZE][GRID_SIZE]; // Hidden board
    private int[] shipLengths = {1, 3, 3, 4, 5}; // Updated ship lengths
    private Map<Integer, Integer> shipHealth = new HashMap<>(); // Length -> Remaining hits
    private int missCount = 0;
    private int strikeCount = 0;
    private int totalMissCount = 0;
    private int totalHitCount = 0;
    private int shipsSunk = 0;

    private JLabel missLabel;
    private JTextField missTextField;
    private JLabel strikeLabel;
    private JTextField strikeTextField;
    private JLabel totalMissLabel;
    private JTextField totalMissTextField;
    private JLabel totalHitLabel;
    private JTextField totalHitTextField;

    private JButton playAgainButton;
    private JButton quitButton;

    private Random random = new Random();

    private ImageIcon blankIcon;
    private ImageIcon missIcon;
    private ImageIcon hitIcon;

    public BattleshipGame() {
        // **Main Frame Setup**
        setTitle("Single Player Battleship");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle closing ourselves
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleQuit();
            }
        });
        setLayout(new BorderLayout());

        // **Load Icons**
        try {
            blankIcon = new ImageIcon(getClass().getResource("/blank.png"));
            missIcon = new ImageIcon(getClass().getResource("/miss.png"));
            hitIcon = new ImageIcon(getClass().getResource("/hit.png"));
            int size = 30;
            blankIcon = new ImageIcon(blankIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            missIcon = new ImageIcon(missIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            hitIcon = new ImageIcon(hitIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
            blankIcon = null;
            missIcon = null;
            hitIcon = null;
        }

        // **Create Game Board Panel**
        JPanel gameBoardPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        gameBoardPanel.setBorder(new TitledBorder(new EtchedBorder(), "Game Board"));
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                boardButtons[row][col] = new JButton();
                if (blankIcon != null) {
                    boardButtons[row][col].setIcon(blankIcon);
                } else {
                    boardButtons[row][col].setText("~");
                }
                boardButtons[row][col].addActionListener(new BoardButtonListener(row, col));
                gameBoardPanel.add(boardButtons[row][col]);
            }
        }
        add(gameBoardPanel, BorderLayout.CENTER);

        // **Create Status Panel**
        JPanel statusPanel = new JPanel(new GridLayout(4, 2));
        statusPanel.setBorder(new TitledBorder(new EtchedBorder(), "Status"));

        missLabel = new JLabel("MISS counter [1-5]:");
        missTextField = new JTextField("0");
        missTextField.setEditable(false);
        statusPanel.add(missLabel);
        statusPanel.add(missTextField);

        strikeLabel = new JLabel("STRIKE counter [1-3]:");
        strikeTextField = new JTextField("0");
        strikeTextField.setEditable(false);
        statusPanel.add(strikeLabel);
        statusPanel.add(strikeTextField);

        totalMissLabel = new JLabel("TOTAL MISS counter [1-83]:");
        totalMissTextField = new JTextField("0");
        totalMissTextField.setEditable(false);
        statusPanel.add(totalMissLabel);
        statusPanel.add(totalMissTextField);

        totalHitLabel = new JLabel("TOTAL HIT counter [1-17]:");
        totalHitTextField = new JTextField("0");
        totalHitTextField.setEditable(false);
        statusPanel.add(totalHitLabel);
        statusPanel.add(totalHitTextField);

        add(statusPanel, BorderLayout.NORTH);

        // **Create Control Panel**
        JPanel controlPanel = new JPanel(new FlowLayout());
        playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePlayAgain();
            }
        });
        quitButton = new JButton("Quit");
        quitButton.addActionListener((ActionEvent ae) -> handleQuit());
        controlPanel.add(playAgainButton);
        controlPanel.add(quitButton);
        add(controlPanel, BorderLayout.SOUTH);

        // **Initialize Game**
        resetGame();
        setVisible(true);
    }

    private void placeShips() {
        // Initialize the hidden game board and ship health
        for (int row = 0; row < GRID_SIZE; row++) {
            Arrays.fill(gameBoard[row], ' ');
        }
        shipHealth.clear();

        for (int length : shipLengths) {
            boolean placed = false;
            while (!placed) {
                int row = random.nextInt(GRID_SIZE);
                int col = random.nextInt(GRID_SIZE);
                boolean horizontal = random.nextBoolean();

                if (canPlaceShip(row, col, length, horizontal)) {
                    placeShipOnBoard(row, col, length, horizontal);
                    shipHealth.put(length, shipHealth.getOrDefault(length, 0) + length);
                    placed = true;
                }
            }
        }
        shipsSunk = 0;
        // For debugging: print the hidden board
        /*
        System.out.println("Hidden Board:");
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(gameBoard[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Ship Health: " + shipHealth);
        */
    }

    private boolean canPlaceShip(int row, int col, int length, boolean horizontal) {
        if (horizontal) {
            if (col + length > GRID_SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (gameBoard[row][col + i] == 'S') return false;
            }
        } else { // Vertical
            if (row + length > GRID_SIZE) return false;
            for (int i = 0; i < length; i++) {
                if (gameBoard[row + i][col] == 'S') return false;
            }
        }
        return true;
    }

    private void placeShipOnBoard(int row, int col, int length, boolean horizontal) {
        for (int i = 0; i < length; i++) {
            if (horizontal) {
                gameBoard[row][col + i] = 'S';
            } else { // Vertical
                gameBoard[row + i][col] = 'S';
            }
        }
    }

    private void handleCellClick(int row, int col) {
        if (boardButtons[row][col].isEnabled()) {
            boardButtons[row][col].setEnabled(false); // Disable the button

            if (gameBoard[row][col] == 'S') {
                // **HIT**
                if (hitIcon != null) {
                    boardButtons[row][col].setIcon(hitIcon);
                } else {
                    boardButtons[row][col].setText("X");
                }
                totalHitCount++;
                missCount = 0;
                totalHitTextField.setText(String.valueOf(totalHitCount));
                missTextField.setText("0");

                // Check if a ship is sunk
                int sunkShipLength = checkIfShipSunk(row, col);
                if (sunkShipLength > 0) {
                    shipsSunk++;
                    JOptionPane.showMessageDialog(this, "You sunk a battleship (size " + sunkShipLength + ")!", "Ship Sunk", JOptionPane.INFORMATION_MESSAGE);
                    if (shipsSunk == shipLengths.length) {
                        JOptionPane.showMessageDialog(this, "Congratulations! You sunk all the battleships!", "Game Over - You Win!", JOptionPane.INFORMATION_MESSAGE);
                        disableAllButtons();
                        promptForReplay();
                    }
                }

            } else if (gameBoard[row][col] == ' ') {
                // **MISS**
                if (missIcon != null) {
                    boardButtons[row][col].setIcon(missIcon);
                } else {
                    boardButtons[row][col].setText("M");
                }
                totalMissCount++;
                missCount++;
                totalMissTextField.setText(String.valueOf(totalMissCount));
                missTextField.setText(String.valueOf(missCount));

                if (missCount >= 5) {
                    strikeCount++;
                    missCount = 0;
                    strikeTextField.setText(String.valueOf(strikeCount));
                    missTextField.setText("0");
                    if (strikeCount >= 3) {
                        JOptionPane.showMessageDialog(this, "Game Over! You reached 3 strikes.", "Game Over - You Lose!", JOptionPane.INFORMATION_MESSAGE);
                        revealShips();
                        disableAllButtons();
                        promptForReplay();
                    }
                }
            }
            gameBoard[row][col] = 'H'; // Mark as hit or missed on hidden board
        }
    }

    private int checkIfShipSunk(int hitRow, int hitCol) {
        for (int length : shipLengths) {
            int sunkParts = 0;
            int totalParts = 0;
            for (int r = 0; r < GRID_SIZE; r++) {
                for (int c = 0; c < GRID_SIZE; c++) {
                    boolean isPartOfShip = false;
                    // Check horizontal
                    if (c <= hitCol && hitCol < c + length && r == hitRow) {
                        boolean potentialShip = true;
                        for (int i = 0; i < length; i++) {
                            if (c + i >= GRID_SIZE || gameBoard[r][c + i] != 'S') {
                                potentialShip = false;
                                break;
                            }
                        }
                        if (potentialShip) isPartOfShip = true;
                    }
                    // Check vertical if not horizontal
                    if (!isPartOfShip && r <= hitRow && hitRow < r + length && c == hitCol) {
                        boolean potentialShip = true;
                        for (int i = 0; i < length; i++) {
                            if (r + i >= GRID_SIZE || gameBoard[r + i][c] != 'S') {
                                potentialShip = false;
                                break;
                            }
                        }
                        if (potentialShip) isPartOfShip = true;
                    }

                    if (isPartOfShip) {
                        totalParts++;
                        if (gameBoard[r][c] == 'H') {
                            sunkParts++;
                        }
                    }
                }
            }
            if (totalParts > 0 && sunkParts == totalParts) {
                return length;
            }
        }
        return 0;
    }


    private void revealShips() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (gameBoard[row][col] == 'S') {
                    if (hitIcon != null) {
                        boardButtons[row][col].setIcon(hitIcon); // Show where ships were (using hit icon for visibility)
                    } else {
                        boardButtons[row][col].setText("S");
                    }
                }
            }
        }
    }

    private void disableAllButtons() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                boardButtons[row][col].setEnabled(false);
            }
        }
    }

    private void resetGame() {
        // Reset counters
        missCount = 0;
        strikeCount = 0;
        totalMissCount = 0;
        totalHitCount = 0;
        missTextField.setText("0");
        strikeTextField.setText("0");
        totalMissTextField.setText("0");
        totalHitTextField.setText("0");

        // Reset game board GUI
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                boardButtons[row][col].setEnabled(true);
                if (blankIcon != null) {
                    boardButtons[row][col].setIcon(blankIcon);
                } else {
                    boardButtons[row][col].setText("~");
                }
            }
        }

        // Place ships again
        placeShips();
    }

    private void handlePlayAgain() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to start a new game?", "Play Again", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        }
    }

    private void handleQuit() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void promptForReplay() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BattleshipGame::new);
    }

    private class BoardButtonListener implements ActionListener {
        private int row;
        private int col;

        public BoardButtonListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            handleCellClick(row, col);
        }
    }
}