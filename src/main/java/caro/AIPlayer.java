package caro;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {
    private int size;
    private Random random = new Random(); 

    public AIPlayer(int size) {
        this.size = size;
    }

    public int[] getMove(char[][] board) {
        int bestScore = -1;
        List<int[]> bestMoves = new ArrayList<>(); 

        // 🧠 Kiểm tra xem bàn cờ có trống không (Lượt đầu của AI)
        boolean isFirstMove = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != ' ') {
                    isFirstMove = false;
                    break;
                }
            }
        }

        
        if (isFirstMove) {
            int center = size / 2;
            return new int[]{center + random.nextInt(3) - 1, center + random.nextInt(3) - 1};
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == ' ') {

                    //  1. Nếu AI thắng ngay -> đánh 
                    if (isWinningMove(board, i, j, 'O')) {
                        return new int[]{i, j};
                    }

                    //  2. Nếu đối thủ sắp thắng -> chặn 
                    if (isWinningMove(board, i, j, 'X')) {
                        return new int[]{i, j};
                    }

                    //  3. Chỉ xét ô gần quân cờ
                    if (!hasNeighbor(board, i, j)) continue;

                    int score = tinhDiem(board, i, j);

                    // Thuật toán chọn nước đi tốt nhất có yếu tố ngẫu nhiên
                    if (score > bestScore) {
                        bestScore = score;
                        bestMoves.clear(); // Xóa các nước cũ vì đã tìm thấy điểm cao hơn
                        bestMoves.add(new int[]{i, j});
                    } else if (score == bestScore && score != -1) {
                        bestMoves.add(new int[]{i, j}); // Gom các nước có điểm bằng nhau
                    }
                }
            }
        }

        
        if (bestMoves.isEmpty()) {
            return new int[]{size / 2, size / 2};
        }

        // Bốc ngẫu nhiên 1 trong các nước tốt nhất
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private int tinhDiem(char[][] board, int r, int c) {
        int attack = countScore(board, r, c, 'O');
        int defend = countScore(board, r, c, 'X');
        
        // Thêm một chút điểm ưu tiên các ô gần tâm hơn để AI đánh tập trung
        int centerBonus = (size / 2) - Math.abs(r - size / 2) - Math.abs(c - size / 2);

        return attack + defend * 2 + centerBonus; // Ưu tiên chặn + ưu tiên tâm
    }

    private int countScore(char[][] board, int row, int col, char player) {
        int totalScore = 0;
        int[] dx = {1, 0, 1, 1};
        int[] dy = {0, 1, 1, -1};

        for (int i = 0; i < 4; i++) {
            int count = 0;
            int block = 0;

            for (int step = 1; step < 5; step++) {
                int nr = row + dx[i] * step;
                int nc = col + dy[i] * step;
                if (nr < 0 || nr >= size || nc < 0 || nc >= size) { block++; break; }
                if (board[nr][nc] == player) count++;
                else if (board[nr][nc] != ' ') { block++; break; }
                else break;
            }

            for (int step = 1; step < 5; step++) {
                int nr = row - dx[i] * step;
                int nc = col - dy[i] * step;
                if (nr < 0 || nr >= size || nc < 0 || nc >= size) { block++; break; }
                if (board[nr][nc] == player) count++;
                else if (board[nr][nc] != ' ') { block++; break; }
                else break;
            }

            if (block == 2) continue;

            // Bảng điểm giúp AI biết ưu tiên thế cờ
            if (count >= 4) totalScore += 100000;
            else if (count == 3) totalScore += (block == 0 ? 10000 : 2000); // 3 quân thoáng sẽ mạnh hơn 3 quân bị chặn 1 đầu
            else if (count == 2) totalScore += (block == 0 ? 1000 : 200);
            else if (count == 1) totalScore += 10;
        }
        return totalScore;
    }

    private boolean isWinningMove(char[][] board, int r, int c, char player) {
        board[r][c] = player;
        boolean win = KiemTraThang.kiemTraThang(board, r, c, player);
        board[r][c] = ' ';
        return win;
    }

    private boolean hasNeighbor(char[][] board, int r, int c) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nr = r + i, nc = c + j;
                if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                    if (board[nr][nc] != ' ') return true;
                }
            }
        }
        return false;
    }
}