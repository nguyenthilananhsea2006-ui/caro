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

                    // 🔥 1. Nếu AI thắng ngay → đánh luôn
                    if (isWinningMove(board, i, j, 'O')) {
                        return new int[]{i, j};
                    }

                    // 🔥 2. Nếu đối thủ sắp thắng → chặn ngay
                    if (isWinningMove(board, i, j, 'X')) {
                        return new int[]{i, j};
                    }

                    // 🔥 3. Chỉ xét ô gần quân cờ (tối ưu + thông minh hơn)
                    if (!hasNeighbor(board, i, j)) continue;

                    int score = tinhDiem(board, i, j);

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                }
            }
        }

        // fallback
        if (bestMove[0] == -1) return new int[]{size / 2, size / 2};

        return bestMove;
    }

    // =========================
    // 🎯 TÍNH ĐIỂM
    // =========================
    private int tinhDiem(char[][] board, int r, int c) {
        int attack = countScore(board, r, c, 'O');
        int defend = countScore(board, r, c, 'X');

        return attack + defend * 2; // ưu tiên chặn
    }

    private int countScore(char[][] board, int row, int col, char player) {
        int totalScore = 0;
        int[] dx = {1, 0, 1, 1};
        int[] dy = {0, 1, 1, -1};

        for (int i = 0; i < 4; i++) {
            int count = 0;
            int block = 0;

            // tiến
            for (int step = 1; step < 5; step++) {
                int nr = row + dx[i] * step;
                int nc = col + dy[i] * step;

                if (nr < 0 || nr >= size || nc < 0 || nc >= size) {
                    block++;
                    break;
                }

                if (board[nr][nc] == player) count++;
                else if (board[nr][nc] != ' ') {
                    block++;
                    break;
                } else break;
            }

            // lùi
            for (int step = 1; step < 5; step++) {
                int nr = row - dx[i] * step;
                int nc = col - dy[i] * step;

                if (nr < 0 || nr >= size || nc < 0 || nc >= size) {
                    block++;
                    break;
                }

                if (board[nr][nc] == player) count++;
                else if (board[nr][nc] != ' ') {
                    block++;
                    break;
                } else break;
            }

            // 🚫 bị chặn 2 đầu → bỏ
            if (block == 2) continue;

            // 🎯 chấm điểm
            if (count >= 4) totalScore += 100000;
            else if (count == 3) totalScore += 10000;
            else if (count == 2) totalScore += 1000;
            else if (count == 1) totalScore += 100;
        }

        return totalScore;
    }

    // =========================
    // 🔥 CHECK THẮNG NGAY
    // =========================
    private boolean isWinningMove(char[][] board, int r, int c, char player) {
        board[r][c] = player;
        boolean win = KiemTraThang.kiemTraThang(board, r, c, player);
        board[r][c] = ' ';
        return win;
    }

    // =========================
    // 🧠 CHỈ XÉT Ô GẦN QUÂN
    // =========================
    private boolean hasNeighbor(char[][] board, int r, int c) {
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int nr = r + i;
                int nc = c + j;

                if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                    if (board[nr][nc] != ' ') return true;
                }
            }
        }
        return false;
    }
}