package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Stack;
import javax.sound.sampled.*;

public class Caro extends JFrame {
    private static final int SIZE = 15;
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private BanCo banCo = new BanCo(SIZE, SIZE);
    private AIPlayer ai = new AIPlayer(SIZE);
    private char currentPlayer = 'X';
    private JLabel statusLabel, scoreLabel, timerLabel;
    private boolean vsAI, gameOver = false;
    private String nameX, nameO;
    private int scoreX = 0, scoreO = 0;
    private JLayeredPane layeredPane;
    
    private Stack<int[]> moveHistory = new Stack<>();
    private Timer turnTimer;
    private int timeLeft = 30;

    public Caro(boolean vsAI, String nameX, String nameO) {
        this.vsAI = vsAI;
        this.nameX = nameX;
        this.nameO = nameO;

        setTitle("Game Caro - Pro Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 950);
        setResizable(false);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(850, 950));
        setContentPane(layeredPane);

        // --- TOẠ ĐỘ BÀN CỜ (Đã đưa lên cao) ---
        int xPos = (850 - 750) / 2;
        int yPos = 90; 

        // --- 1. THANH ĐIỀU KHIỂN (CONTROL PANEL) ---
        // Sử dụng Opaque(false) vàsetBounds đúng để không đè lên bàn cờ
        JPanel controlPanel = new JPanel(null);
        controlPanel.setBounds(0, 0, 850, 90);
        controlPanel.setOpaque(false);

        statusLabel = new JLabel("Lượt: " + nameX, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
        statusLabel.setBounds(225, 10, 400, 30);
        controlPanel.add(statusLabel);

        scoreLabel = new JLabel(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setBounds(225, 45, 400, 30);
        controlPanel.add(scoreLabel);

        JButton undoBtn = createStyledWinButton("UNDO", 30, 25, 100, 40);
        undoBtn.addActionListener(e -> undo());
        controlPanel.add(undoBtn);

        timerLabel = new JLabel("⏱ 30s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 30));
        timerLabel.setForeground(new Color(200, 0, 0));
        timerLabel.setBounds(680, 25, 120, 40);
        controlPanel.add(timerLabel);

        layeredPane.add(controlPanel, JLayeredPane.PALETTE_LAYER);

        // --- 2. HÌNH NỀN BÀN CỜ ---
        JPanel bgPnl = new JPanel() {
            private BufferedImage bgImage;
            {
                try {
                    File file = new File("src/main/java/res.tiles/banco.png");
                    if (file.exists()) bgImage = ImageIO.read(file);
                    else {
                        java.io.InputStream is = getClass().getResourceAsStream("/res.tiles/banco.png");
                        if (is != null) bgImage = ImageIO.read(is);
                    }
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

        // --- 3. LƯỚI CÁC Ô BẤM (Cảm ứng chính của game) ---
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
        // Thêm vào lớp Integer(1) để nằm trên Hình nền nhưng dưới ControlPanel
        layeredPane.add(mainBoardPanel, Integer.valueOf(1));

        setLocationRelativeTo(null);
        setVisible(true);
        startCountdown();
    }

    private void startCountdown() {
    if (turnTimer != null) turnTimer.stop();
    timeLeft = 30; 
    timerLabel.setText("⏱ " + timeLeft + "s");
    
    turnTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            timeLeft--;
            timerLabel.setText("⏱ " + timeLeft + "s");
            
            if (timeLeft <= 0) {
                turnTimer.stop();
                gameOver = true;
                // Gọi hàm thông báo đẹp thay vì JOptionPane
                showTimeOutMessage(currentPlayer == 'X' ? nameX : nameO);
            }
        }
    });
    turnTimer.start();
}
private void showTimeOutMessage(String loserName) {
    JPanel timeoutPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Phủ nền tối
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Vẽ khung thông báo chính giữa
            int boxW = 500, boxH = 300;
            int boxX = (getWidth() - boxW) / 2;
            int boxY = (getHeight() - boxH) / 2;
            
            g2.setColor(new Color(60, 20, 20)); // Đỏ gỗ tối
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
            g2.setColor(new Color(255, 50, 50)); // Viền đỏ sáng
            g2.setStroke(new BasicStroke(4));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);

            // Chữ TIME OUT
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            String title = "TIME OUT!";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, boxY + 100);

            // Thông báo thua
            g2.setFont(new Font("Arial", Font.PLAIN, 22));
            g2.setColor(Color.WHITE);
            String msg = loserName + " đã hết thời gian suy nghĩ";
            String msg2 = "Và bị xử thua trận này!";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm2.stringWidth(msg)) / 2, boxY + 160);
            g2.drawString(msg2, (getWidth() - fm2.stringWidth(msg2)) / 2, boxY + 200);
            
            g2.dispose();
        }
    };

    timeoutPanel.setLayout(null);
    timeoutPanel.setBounds(0, 0, 850, 950);
    timeoutPanel.setOpaque(false);

    // Nút "Chơi lại" nằm trong khung thông báo
    JButton btnRetry = createStyledWinButton("THỬ LẠI", 315, 600, 220, 50);
    btnRetry.addActionListener(e -> {
        layeredPane.remove(timeoutPanel);
        resetGame();
        layeredPane.repaint();
    });

    timeoutPanel.add(btnRetry);

    layeredPane.add(timeoutPanel, JLayeredPane.DRAG_LAYER);
    layeredPane.moveToFront(timeoutPanel);
    layeredPane.revalidate();
    layeredPane.repaint();
    
    // Phát âm thanh thua cuộc nếu có
    playSound("lose.wav"); 
}
    private void undo() {
        if (moveHistory.isEmpty() || gameOver) return;
        if (vsAI && moveHistory.size() >= 2) {
            for (int i = 0; i < 2; i++) {
                int[] pos = moveHistory.pop();
                resetButtonState(pos[0], pos[1]);
            }
        } else if (!vsAI) {
            int[] pos = moveHistory.pop();
            resetButtonState(pos[0], pos[1]);
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            statusLabel.setText("Lượt: " + (currentPlayer == 'X' ? nameX : nameO));
        }
        startCountdown();
    }

    private void resetButtonState(int r, int c) {
        banCo.getBoard()[r][c] = ' ';
        buttons[r][c].setText("");
        buttons[r][c].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30)));
        buttons[r][c].setBackground(null);
        buttons[r][c].setOpaque(false);
    }

    private void xuLyDanh(int r, int c) {
        if (gameOver || !buttons[r][c].getText().equals("")) return;

        moveHistory.push(new int[]{r, c});
        thucHienNuocDi(r, c, currentPlayer);

        int[][] winCells = KiemTraThang.layToaDoThang(banCo.getBoard(), r, c, currentPlayer);
        if (winCells != null) {
            xuLyThangCuoc(winCells, currentPlayer == 'X' ? nameX : nameO);
            return;
        }

        if (vsAI) {
            if (turnTimer != null) turnTimer.stop();
            Timer aiThink = new Timer(500, e -> {
                int[] move = ai.getMove(banCo.getBoard());
                if (move != null) {
                    moveHistory.push(new int[]{move[0], move[1]});
                    thucHienNuocDi(move[0], move[1], 'O');
                    int[][] aiWin = KiemTraThang.layToaDoThang(banCo.getBoard(), move[0], move[1], 'O');
                    if (aiWin != null) xuLyThangCuoc(aiWin, nameO);
                    else startCountdown();
                }
            });
            aiThink.setRepeats(false);
            aiThink.start();
        } else {
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            statusLabel.setText("Lượt: " + (currentPlayer == 'X' ? nameX : nameO));
            startCountdown();
        }
    }

    private void thucHienNuocDi(int r, int c, char p) {
        banCo.getBoard()[r][c] = p;
        buttons[r][c].setText(String.valueOf(p));
        buttons[r][c].setFont(new Font("Arial", Font.BOLD, 30));
        buttons[r][c].setForeground(p == 'X' ? new Color(255, 80, 80) : new Color(80, 200, 255));
        buttons[r][c].setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        playSound("click.wav");
    }

    private void xuLyThangCuoc(int[][] winCells, String winnerName) {
        gameOver = true;
        if (turnTimer != null) turnTimer.stop();
        for (int[] cell : winCells) {
            buttons[cell[0]][cell[1]].setBackground(new Color(255, 255, 150));
            buttons[cell[0]][cell[1]].setOpaque(true);
        }
        if (winnerName.equals(nameX)) scoreX++; else scoreO++;
        scoreLabel.setText(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO);
        
        Timer delay = new Timer(1000, e -> showWinMessage(winnerName));
        delay.setRepeats(false);
        delay.start();
        playSound("win.wav");
    }

    private void showWinMessage(String winnerName) {
        JPanel overlayPanel = new JPanel() {
            private BufferedImage winImage;
            {
                try {
                    File file = new File("src/main/java/res.tiles/winer.png");
                    if (file.exists()) winImage = ImageIO.read(file);
                    else {
                        java.io.InputStream is = getClass().getResourceAsStream("/res.tiles/winer.png");
                        if (is != null) winImage = ImageIO.read(is);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (winImage != null) {
                    int imgW = 600, imgH = 600;
                    int xImg = (getWidth() - imgW) / 2;
                    int yImg = (getHeight() - imgH) / 2 - 80;
                    g2.drawImage(winImage, xImg, yImg, imgW, imgH, this);

                    String winText = winnerName.toUpperCase();
                    g2.setFont(new Font("Arial", Font.BOLD, 50));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(winText)) / 2;
                    int textY = yImg + 440;

                    g2.setColor(Color.BLACK);
                    g2.drawString(winText, textX + 3, textY + 3);
                    g2.setColor(Color.YELLOW);
                    g2.drawString(winText, textX, textY);
                }
            }
        };

        overlayPanel.setLayout(null);
        overlayPanel.setBounds(0, 0, 850, 950);
        overlayPanel.setOpaque(false);

        int btnW = 220, btnH = 50;
        int xCenter = (850 - btnW) / 2;
        
        JButton btnNewGame = createStyledWinButton("CHƠI LẠI", xCenter, 600, btnW, btnH);
        btnNewGame.addActionListener(e -> {
            layeredPane.remove(overlayPanel);
            resetGame();
            layeredPane.repaint();
        });

        JButton btnHome = createStyledWinButton("MENU CHÍNH", xCenter, 670, btnW, btnH);
        btnHome.addActionListener(e -> {
            this.dispose();
            new Main().setVisible(true);
        });

        overlayPanel.add(btnNewGame);
        overlayPanel.add(btnHome);

        layeredPane.add(overlayPanel, JLayeredPane.DRAG_LAYER);
        layeredPane.moveToFront(overlayPanel);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private JButton createStyledWinButton(String text, int x, int y, int w, int h) {
    JButton btn = new JButton(text) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Kiểm tra trạng thái nút
            ButtonModel model = getModel();
            Color baseColor = new Color(110, 65, 15); // Màu gỗ mặc định
            
            if (model.isPressed()) {
                g2.setColor(baseColor.darker()); // Nhấn xuống thì tối đi
            } else if (model.isRollover()) {
                g2.setColor(new Color(160, 100, 30)); // Di chuột vào thì sáng lên
            } else {
                g2.setColor(baseColor);
            }

            // Vẽ thân nút
            g2.fillRoundRect(0, 0, w, h, 15, 15);

            // Vẽ viền sáng khi hover
            if (model.isRollover()) {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 15, 15);
            }

            // Vẽ chữ
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            
            g2.dispose();
        }
    };
    
    btn.setBounds(x, y, w, h);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    // Quan trọng: Phải có dòng này để Java Swing biết nút có hiệu ứng hover
    btn.setRolloverEnabled(true); 
    
    return btn;
}

    private void resetGame() {
        moveHistory.clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) resetButtonState(i, j);
        }
        gameOver = false;
        currentPlayer = 'X';
        statusLabel.setText("Lượt: " + nameX);
        startCountdown();
        layeredPane.repaint();
    }

    private void playSound(String soundFile) {
        try {
            File file = new File("amthanh/" + soundFile); 
            if (file.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}