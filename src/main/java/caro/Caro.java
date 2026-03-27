package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.sound.sampled.*;

public class Caro extends JFrame {
    private static final int SIZE = 15;
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private BanCo banCo = new BanCo(SIZE, SIZE);
    private AIPlayer ai = new AIPlayer(SIZE);
    private char currentPlayer = 'X';
    private JLabel statusLabel, scoreLabel;
    private boolean vsAI, gameOver = false;
    private String nameX, nameO;
    private int scoreX = 0, scoreO = 0;
    private JPanel boardPanel;
    private JLayeredPane layeredPane;

   //tai ham khoi tao
public Caro(boolean vsAI, String nameX, String nameO) {
    this.vsAI = vsAI;
    this.nameX = nameX;
    this.nameO = nameO;
    
    setTitle("Game Caro");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(850, 950);

    layeredPane = new JLayeredPane();
    layeredPane.setPreferredSize(new Dimension(850, 950));
    setContentPane(layeredPane);

    int xPos = (850 - 750) / 2;
    int yPos = (850 - 750) / 2; 

    JPanel bgPnl = new JPanel() { 
        private BufferedImage bgImage;
        {
            try {
                java.io.File file = new java.io.File("src/main/java/res.tiles/banco.png");
                if (file.exists()) bgImage = ImageIO.read(file);
            } catch (Exception e) { e.printStackTrace(); }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) g.drawImage(bgImage, 0, 0, 750, 750, this);
        }
    };
    bgPnl.setBounds(xPos, yPos, 750, 750);
    layeredPane.add(bgPnl, JLayeredPane.DEFAULT_LAYER);

    
    int boardInsideSize = 660; 
    int margin = (750 - boardInsideSize) / 2; 

    JPanel mainBoardPanel = new JPanel(new GridLayout(SIZE, SIZE));
    mainBoardPanel.setBounds(xPos + margin, yPos + margin, boardInsideSize, boardInsideSize); 
    mainBoardPanel.setOpaque(false); 

    for (int i = 0; i < SIZE; i++) {
        for (int j = 0; j < SIZE; j++) {
            buttons[i][j] = new JButton("");
            buttons[i][j].setContentAreaFilled(false);
            buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30)));
            final int r = i, c = j;
            buttons[i][j].addActionListener(e -> xuLyDanh(r, c));
            mainBoardPanel.add(buttons[i][j]);
        }
    }
    layeredPane.add(mainBoardPanel, Integer.valueOf(1));

    JPanel headerPanel = new JPanel(new GridLayout(2, 1));
    headerPanel.setBounds(0, yPos - 90, 850, 80); 
    headerPanel.setOpaque(false);

    statusLabel = new JLabel("Lượt: " + nameX, SwingConstants.CENTER);
    statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
    scoreLabel = new JLabel(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO, SwingConstants.CENTER);
    scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));

    headerPanel.add(statusLabel);
    headerPanel.add(scoreLabel);
    layeredPane.add(headerPanel, JLayeredPane.POPUP_LAYER);

    setLocationRelativeTo(null);
    setVisible(true);
}
    
    private void xuLyDanh(int r, int c) {
        if (gameOver || !buttons[r][c].getText().equals("")) return;
        
        thucHienNuocDi(r, c, currentPlayer);
        
        int[][] winCells = KiemTraThang.layToaDoThang(banCo.getBoard(), r, c, currentPlayer);
        if (winCells != null) {
            xuLyThangCuoc(winCells, currentPlayer == 'X' ? nameX : nameO);
            return;
        }

        if (vsAI) {
            int[] move = ai.getMove(banCo.getBoard());
            if (move != null) {
                thucHienNuocDi(move[0], move[1], 'O');
                int[][] aiWin = KiemTraThang.layToaDoThang(banCo.getBoard(), move[0], move[1], 'O');
                if (aiWin != null) xuLyThangCuoc(aiWin, nameO);
            }
        } else {
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            statusLabel.setText("Lượt: " + (currentPlayer == 'X' ? nameX : nameO));
        }
    }

    private void xuLyThangCuoc(int[][] winCells, String winnerName) {
        gameOver = true;
        for (int[] cell : winCells) {
            buttons[cell[0]][cell[1]].setBackground(new Color(255, 255, 150));
            buttons[cell[0]][cell[1]].setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            buttons[cell[0]][cell[1]].setOpaque(true);
        }
        
        if (winnerName.equals(nameX)) scoreX++; else scoreO++;
        scoreLabel.setText(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO);
        Timer timer = new Timer(2000, e -> showWinMessage(winnerName));
        timer.setRepeats(false);
        timer.start();
        playSound("win.wav");
    }
   private void thucHienNuocDi(int r, int c, char p) {
 
    banCo.getBoard()[r][c] = p;
    buttons[r][c].setText(String.valueOf(p));
    
    buttons[r][c].setFont(new Font("Arial", Font.BOLD, 30));
    if (p == 'X') {
        buttons[r][c].setForeground(new Color(255, 80, 80)); // Đỏ sáng
    } else {
        buttons[r][c].setForeground(new Color(80, 200, 255)); // Xanh Cyan sáng
    }
    playSound("click.wav");
}

   private void showWinMessage(String winnerName) {
    JPanel overlayPanel = new JPanel() {
        private BufferedImage winImage;
        {
            try {
                String imgPath = "src/main/java/res.tiles/winer.png"; 
                java.io.File file = new java.io.File(imgPath);
                
                if (file.exists()) {
                    winImage = javax.imageio.ImageIO.read(file);
                } else
                {
                    java.io.InputStream is = getClass().getResourceAsStream("/res.tiles/winer.png");
                    if (is != null) {
                        winImage = javax.imageio.ImageIO.read(is);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (winImage != null) {
                
                int imgW = 750;
                int imgH = 750;
                int xImg = (getWidth() - imgW) / 2;
                int yImg = (getHeight() - imgH) / 2;
                g2.drawImage(winImage, xImg, yImg, imgW, imgH, this);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Arial", Font.BOLD, 45));
                g2.setColor(new Color(255, 215, 0)); 
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(winnerName)) / 2;
                int textY = yImg + 500; 
                
                g2.setColor(Color.BLACK);
                g2.drawString(winnerName, textX + 2, textY + 2);
                g2.setColor(new Color(255, 215, 0));
                g2.drawString(winnerName, textX, textY);
            } else {
             
                g2.setColor(Color.WHITE);
                g2.drawString("❌ LỖI: Không tìm thấy file winer.png!", 250, 350);
            }
        }
        
    };

    overlayPanel.setLayout(null); 
    overlayPanel.setBounds(0, 0, getWidth(), getHeight());
    overlayPanel.setOpaque(false);

    
    JButton btnNewGame = new JButton();
    int xImg = (getWidth() - 750) / 2;
    int yImg = (getHeight() - 750) / 2;
    //nut choi lai
    btnNewGame.setBounds(xImg + 200, yImg + 510, 350, 80); 
    btnNewGame.setOpaque(false);
    btnNewGame.setContentAreaFilled(false);
    btnNewGame.setBorderPainted(false);
    btnNewGame.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnNewGame.addActionListener(e -> {
        layeredPane.remove(overlayPanel);
        resetGame();
        layeredPane.repaint();
    });
// nut meu
    JButton btnHome = new JButton();
    btnHome.setBounds(xImg + 200, yImg + 620, 350, 80); 
    btnHome.setOpaque(false);
    btnHome.setContentAreaFilled(false);
    btnHome.setBorderPainted(false);
    btnHome.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnHome.addActionListener(e -> {
        this.dispose(); 
        
        new Main().setVisible(true); 
    });

    overlayPanel.add(btnNewGame);
    overlayPanel.add(btnHome);
    layeredPane.add(overlayPanel, JLayeredPane.DRAG_LAYER);
    layeredPane.revalidate();
    layeredPane.repaint();
}
    private void resetGame() {
    for (int i = 0; i < SIZE; i++) {
        for (int j = 0; j < SIZE; j++) {
            buttons[i][j].setText(""); 
            buttons[i][j].setContentAreaFilled(false); 
            buttons[i][j].setOpaque(false);
            buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50)));
            banCo.getBoard()[i][j] = ' ';
        }
    }
    gameOver = false; 
    currentPlayer = 'X'; 
    statusLabel.setText("Lượt: " + nameX);
    layeredPane.repaint();
    }
    private void playSound(String soundFile) {
    try {
        java.net.URL url = getClass().getResource("/res.sounds/" + soundFile);
        if (url != null) {
            javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
    } catch (Exception e) {
        e.printStackTrace();
         }
     }
}    
   