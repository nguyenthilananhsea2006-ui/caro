/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package caro;

public class AIPlayer {
    private int size;

    public AIPlayer(int size) {
        this.size = size;
    }

    public int[] getMove(char[][] board) {
        int bestScore = -1;
        int[] bestMove = {-1, -1};

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == ' ') {
                    // Tính điểm cho ô trống này
                    int score = tinhDiem(board, i, j);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                }
            }
        }
        
        // Nếu không tìm được (bàn cờ đầy), đánh đại ô giữa (ít xảy ra)
        if (bestMove[0] == -1) return new int[]{size/2, size/2};
        
        return bestMove;
    }

    private int tinhDiem(char[][] board, int r, int c) {
        // AI là quân 'O', Người chơi là quân 'X'
        // Điểm tấn công (cho O) và điểm phòng ngự (chặn X)
        int score = countScore(board, r, c, 'O') + countScore(board, r, c, 'X');
        return score;
    }

    private int countScore(char[][] board, int row, int col, char player) {
        int totalScore = 0;
        int[] dx = {1, 0, 1, 1}; // Các hướng: dọc, ngang, chéo xuôi, chéo ngược
        int[] dy = {0, 1, 1, -1};

        for (int i = 0; i < 4; i++) {
            int count = 0;
            // Đếm hướng tiến
            for (int step = 1; step < 5; step++) {
                int nr = row + dx[i] * step;
                int nc = col + dy[i] * step;
                if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == player) {
                    count++;
                } else break;
            }
            // Đếm hướng lùi
            for (int step = 1; step < 5; step++) {
                int nr = row - dx[i] * step;
                int nc = col - dy[i] * step;
                if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == player) {
                    count++;
                } else break;
            }

            // Gán điểm dựa trên số quân liên tiếp
            if (count == 4) totalScore += 10000; // Sắp thắng/cần chặn gấp
            else if (count == 3) totalScore += 1000;
            else if (count == 2) totalScore += 100;
            else if (count == 1) totalScore += 10;
        }
        return totalScore;
    }
}