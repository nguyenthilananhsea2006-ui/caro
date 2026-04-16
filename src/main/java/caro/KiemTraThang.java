package caro;

import java.util.ArrayList;
import java.util.List;

public class KiemTraThang {

   
    public static int[][] layToaDoThang(char[][] board, int row, int col, char p) {
        int SIZE = board.length;
        
        // Nếu là 3x3 thì cần 3 quân, ngược lại (15x15) cần 5 quân
        int winCondition = (SIZE == 3) ? 3 : 5;
        
        // Các hướng kiểm tra: Ngang, Dọc, Chéo xuôi, Chéo ngược
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            List<int[]> winCells = new ArrayList<>();
            winCells.add(new int[]{row, col});

            // Kiểm tra 2 hướng đối lập của mỗi đường 
            for (int side = -1; side <= 1; side += 2) {
                // Chỉ cần quét tối đa (winCondition - 1) ô về mỗi phía
                for (int i = 1; i < winCondition; i++) {
                    int r = row + dir[0] * i * side;
                    int c = col + dir[1] * i * side;
                    
                    if (r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == p) {
                        winCells.add(new int[]{r, c});
                    } else {
                        break;
                    }
                }
            }

            // Nếu số quân liên tiếp đạt đủ điều kiện thắng
            if (winCells.size() >= winCondition) {
                int[][] result = new int[winCells.size()][2];
                for (int i = 0; i < winCells.size(); i++) {
                    result[i] = winCells.get(i);
                }
                return result;
            }
        }
        return null;
    }

   
    public static boolean kiemTraThang(char[][] board, int row, int col, char p) {
        return layToaDoThang(board, row, col, p) != null;
    }
}