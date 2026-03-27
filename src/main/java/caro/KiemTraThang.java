package caro;

import java.util.ArrayList;
import java.util.List;

public class KiemTraThang {

    //highlight 5 quân thắng
    public static int[][] layToaDoThang(char[][] board, int row, int col, char p) {
        int SIZE = board.length;
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}}; // Ngang, Dọc, Chéo xuôi, Chéo ngược

        for (int[] dir : directions) {
            List<int[]> winCells = new ArrayList<>();
            winCells.add(new int[]{row, col});

            // Kiểm tra 2 hướng của mỗi đường
            for (int side = -1; side <= 1; side += 2) {
                for (int i = 1; i < 5; i++) {
                    int r = row + dir[0] * i * side;
                    int c = col + dir[1] * i * side;
                    if (r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == p) {
                        winCells.add(new int[]{r, c});
                    } else break;
                }
            }

            if (winCells.size() >= 5) {
                int[][] result = new int[winCells.size()][2];
                for (int i = 0; i < winCells.size(); i++) result[i] = winCells.get(i);
                return result;
            }
        }
        return null;
    }

    // Giữ lại hàm cũ để game vẫn hoạt động bình thường
    public static boolean kiemTraThang(char[][] board, int row, int col, char p) {
        return layToaDoThang(board, row, col, p) != null;
    }
}