package caro;

import javax.swing.JOptionPane; 

public class BanCo {

    private char[][] board;
    private int rows;
    private int cols;

    public BanCo(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        board = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public char[][] getBoard() {
        return board;
    }
    public boolean datNuocDi(int row, int col, char player) {
        // kiểm tra ô trống 
        if (board[row][col] == ' ') {
            board[row][col] = player;
            //   kiểm tra xem nước đi này có thắng 
            if (KiemTraThang.kiemTraThang(board, row, col, player)) {
                                System.out.println("Player " + player + " thắng tại: " + row + "," + col);
                return true; 
            }
        }
        return false;
    }
    public boolean isFull() {
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (board[i][j] == ' ') return false; // Vẫn còn ô trống
        }
    }
    return true; // Đã hết ô trống
}
    public void reset() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = ' ';
            }
        }
    }
}