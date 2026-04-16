package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Stack;
import javax.sound.sampled.*;

public class Caro extends JFrame {
    private int size; 
    private JButton[][] buttons;
    private BanCo banCo;
    private AIPlayer ai;
    private char currentPlayer = 'X';
    private JLabel statusLabel, scoreLabel, timerLabel;
    private boolean vsAI, gameOver = false;
    private String nameX, nameO;
    private int scoreX = 0, scoreO = 0;
    private JLayeredPane layeredPane;
    
    private JPanel mainBoardPanel; 
    private Stack<int[]> moveHistory = new Stack<>();
    private Timer turnTimer;
    private int timeLeft = 15;
    private final int TOTAL_WIDTH = 850;
    private final int TOTAL_HEIGHT = 820;
    private final int BOARD_IMG_SIZE = 700;
    private final int Y_POS = 60; 

    public Caro(boolean vsAI, String nameX, String nameO, int size) {
        this.size = size;
        this.vsAI = vsAI;
        this.nameX = nameX;
        this.nameO = nameO;

        this.buttons = new JButton[size][size];
        this.banCo = new BanCo(size, size);
        this.ai = new AIPlayer(size);

        setTitle("Game Caro - Chế độ " + size + "x" + size);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(TOTAL_WIDTH, TOTAL_HEIGHT); 
        setResizable(false);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT));
        setContentPane(layeredPane);

        int xPos = (TOTAL_WIDTH - BOARD_IMG_SIZE) / 2; 

        // --- 1. HEADER CONTROL ---
        JPanel controlPanel = new JPanel(null);
        controlPanel.setBounds(0, 0, TOTAL_WIDTH, 60);
        controlPanel.setOpaque(false);

        statusLabel = new JLabel("Lượt: " + nameX, SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        statusLabel.setForeground(new Color(100, 50, 0));
        statusLabel.setBounds(xPos, 5, 200, 30);
        controlPanel.add(statusLabel);

        scoreLabel = new JLabel(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setBounds(0, 30, TOTAL_WIDTH, 30);
        controlPanel.add(scoreLabel);

        JButton menuBtn = createStyledWinButton("MENU", xPos + 220, 5, 80, 30);
        menuBtn.addActionListener(e -> {
            new Main().setVisible(true);
            this.dispose();
        });
        controlPanel.add(menuBtn);

        JButton undoBtn = createStyledWinButton("UNDO", xPos + 310, 5, 80, 30);
        undoBtn.addActionListener(e -> undo());
        controlPanel.add(undoBtn);

        timerLabel = new JLabel("⏱ 15s", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        timerLabel.setForeground(new Color(220, 50, 50));
        timerLabel.setBounds(TOTAL_WIDTH - xPos - 120, 5, 120, 30);
        controlPanel.add(timerLabel);

        layeredPane.add(controlPanel, JLayeredPane.PALETTE_LAYER);

        // --- 2. BOARD BACKGROUND ---
        JPanel bgPnl = new JPanel() {
            private BufferedImage bgImage;
            {
                try {
                    File file = new File("src/main/java/res.tiles/banco.png");
                    if (file.exists()) bgImage = ImageIO.read(file);
                } catch (Exception e) {}
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, BOARD_IMG_SIZE, BOARD_IMG_SIZE, this);
                }
            }
        };
        bgPnl.setBounds(xPos, Y_POS, BOARD_IMG_SIZE, BOARD_IMG_SIZE);
        layeredPane.add(bgPnl, JLayeredPane.DEFAULT_LAYER);

        // --- 3. BUTTON GRID ---
        int boardInsideSize = (int)(BOARD_IMG_SIZE * 0.86); 
        int marginX = (BOARD_IMG_SIZE - boardInsideSize) / 2;
        int offsetY = (int)(BOARD_IMG_SIZE * 0.065); 

        // Gán vào biến toàn cục mainBoardPanel (không khai báo mới)
        mainBoardPanel = new JPanel(new GridLayout(size, size));
        mainBoardPanel.setBounds(xPos + marginX, Y_POS + offsetY, boardInsideSize, boardInsideSize);
        mainBoardPanel.setOpaque(false);

        int fontSize = (size == 3) ? 120 : (size > 15 ? 20 : 30);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setContentAreaFilled(false);
                buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80)));
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, fontSize));
                final int r = i, c = j;
                buttons[i][j].addActionListener(e -> xuLyDanh(r, c));
                mainBoardPanel.add(buttons[i][j]);
            }
        }
        layeredPane.add(mainBoardPanel, JLayeredPane.MODAL_LAYER);

        setLocationRelativeTo(null);
        setVisible(true);
        startCountdown();
    }


    // --- CÁC HÀM XỬ LÝ LOGIC  ---

    private void xuLyDanh(int r, int c) {
        if (gameOver || !buttons[r][c].getText().equals("")) return;
        moveHistory.push(new int[]{r, c});
        thucHienNuocDi(r, c, currentPlayer);
        int[][] winCells = KiemTraThang.layToaDoThang(banCo.getBoard(), r, c, currentPlayer);
        if (winCells != null) {
            xuLyThangCuoc(winCells, currentPlayer == 'X' ? nameX : nameO);
        } else if (banCo.isFull()) {
            gameOver = true;
            showCustomMessage("HÒA CỜ!", false);
        } else {
            if (vsAI) {
                currentPlayer = 'O';
                statusLabel.setText("Máy đang nghĩ...");
                Timer aiDelay = new Timer(500, e -> {
                    int[] aiMove = ai.getMove(banCo.getBoard());
                    if (aiMove[0] != -1) {
                        moveHistory.push(new int[]{aiMove[0], aiMove[1]});
                        thucHienNuocDi(aiMove[0], aiMove[1], 'O');
                        int[][] aiWin = KiemTraThang.layToaDoThang(banCo.getBoard(), aiMove[0], aiMove[1], 'O');
                        if (aiWin != null) xuLyThangCuoc(aiWin, nameO);
                        else {
                            currentPlayer = 'X';
                            statusLabel.setText("Lượt: " + nameX);
                            startCountdown();
                        }
                    }
                });
                aiDelay.setRepeats(false);
                aiDelay.start();
            } else {
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                statusLabel.setText("Lượt: " + (currentPlayer == 'X' ? nameX : nameO));
                startCountdown();
            }
        }
    }

    private void thucHienNuocDi(int r, int c, char p) {
        banCo.getBoard()[r][c] = p;
        buttons[r][c].setText(String.valueOf(p));
        buttons[r][c].setForeground(p == 'X' ? new Color(255, 60, 60) : new Color(60, 130, 255));
        playSound("click.wav");
    }

    private void xuLyThangCuoc(int[][] winCells, String winnerName) {
    gameOver = true;
    if (turnTimer != null) turnTimer.stop();
    
    for (int[] cell : winCells) {
        // Tô màu vàng sáng cho 5 quân thắng
        buttons[cell[0]][cell[1]].setBackground(new Color(255, 255, 0, 200)); 
        buttons[cell[0]][cell[1]].setOpaque(true);
        buttons[cell[0]][cell[1]].setContentAreaFilled(true); 
    }
    
    if (winnerName.equals(nameX)) scoreX++; else scoreO++;
    scoreLabel.setText(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO);
    
    // hiện chậm 5 quân thắng để nhìn rõ hơn
    Timer delayMsg = new Timer(500, e -> showCustomMessage(winnerName, false));
    delayMsg.setRepeats(false);
    delayMsg.start();
}

    private void showCustomMessage(String nameDisplay, boolean isTimeout) {
        int boxW = 500, boxH = 320;
        int boxX = (getWidth() - boxW) / 2;
        int boxY = (getHeight() - boxH) / 2;

        JPanel overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(boxX + 8, boxY + 8, boxW, boxH, 30, 30);
                GradientPaint gp = new GradientPaint(boxX, boxY, new Color(70, 45, 30), boxX, boxY + boxH, new Color(40, 25, 15));
                g2.setPaint(gp);
                g2.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);
                g2.setStroke(new BasicStroke(4f));
                g2.setColor(new Color(170, 110, 45));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, 30, 30);
                String title = isTimeout ? "HẾT GIỜ ĐÁNH!" : (nameDisplay.equals("HÒA CỜ!") ? "KẾT QUẢ" : "CHIẾN THẮNG");
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                g2.setColor(new Color(255, 215, 0)); 
                g2.drawString(title, (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2, boxY + 70);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 50));
                g2.setColor(Color.WHITE); 
                g2.drawString(nameDisplay.toUpperCase(), (getWidth() - g2.getFontMetrics().stringWidth(nameDisplay)) / 2, boxY + 160);
                g2.dispose();
            }
        };
        overlayPanel.setLayout(null);
        overlayPanel.setBounds(0, 0, getWidth(), getHeight());
        overlayPanel.setOpaque(false);

        JButton btnRetry = createStyledWinButton("CHƠI LẠI", boxX + 60, boxY + 230, 160, 45);
        btnRetry.addActionListener(e -> {
            layeredPane.remove(overlayPanel);
            resetGame();
        });
        overlayPanel.add(btnRetry);

        JButton btnMenu = createStyledWinButton("MENU", boxX + boxW - 220, boxY + 230, 160, 45);
        btnMenu.addActionListener(e -> {
            new Main().setVisible(true);
            this.dispose();
        });
        overlayPanel.add(btnMenu);

        layeredPane.add(overlayPanel, JLayeredPane.DRAG_LAYER);
        layeredPane.repaint();
    }

    private JButton createStyledWinButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isHovered = getModel().isRollover();
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(2, 4, w, h, 15, 15);
                Color colorTop = isHovered ? new Color(255, 225, 120) : new Color(170, 110, 45);
                Color colorBottom = isHovered ? new Color(210, 130, 35) : new Color(110, 65, 15);
                g2.setPaint(new GradientPaint(0, 0, colorTop, 0, h, colorBottom));
                g2.fillRoundRect(0, 0, w, h, 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setBounds(x, y, w, h);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void undo() {
        if (moveHistory.isEmpty() || gameOver) return;
        if (vsAI && moveHistory.size() >= 2) {
            resetButtonState(moveHistory.pop());
            resetButtonState(moveHistory.pop());
        } else if (!vsAI) {
            resetButtonState(moveHistory.pop());
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        }
        statusLabel.setText("Lượt: " + (currentPlayer == 'X' ? nameX : nameO));
        startCountdown();
    }

    private void resetButtonState(int[] pos) {
        int r = pos[0], c = pos[1];
        banCo.getBoard()[r][c] = ' ';
        buttons[r][c].setText("");
        buttons[r][c].setOpaque(false);
        buttons[r][c].setBackground(null);
    }

    private void startCountdown() {
        if (turnTimer != null) turnTimer.stop();
        timeLeft = 15;
        timerLabel.setText("⏱ " + timeLeft + "s");
        turnTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("⏱ " + timeLeft + "s");
            if (timeLeft <= 0) {
                turnTimer.stop();
                gameOver = true;
                showCustomMessage(currentPlayer == 'X' ? nameX : nameO, true);
            }
        });
        turnTimer.start();
    }
    private void resetGame() {
    moveHistory.clear();
    banCo.reset();
    
    // Giữ nguyên phần setBounds cho mainBoardPanel như cũ...
    int xPos = (TOTAL_WIDTH - BOARD_IMG_SIZE) / 2;
    int boardInsideSize = (int)(BOARD_IMG_SIZE * 0.86);
    int marginX = (BOARD_IMG_SIZE - boardInsideSize) / 2;
    int offsetY = (int)(BOARD_IMG_SIZE * 0.065);
    if (mainBoardPanel != null) {
        mainBoardPanel.setBounds(xPos + marginX, Y_POS + offsetY, boardInsideSize, boardInsideSize);
    }

    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            buttons[i][j].setText("");
            buttons[i][j].setOpaque(false);
            buttons[i][j].setContentAreaFilled(false); // Tắt màu nền để nhìn xuyên thấu bàn gỗ
            buttons[i][j].setBackground(null);
            buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80)));
        }
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
        } catch (Exception e) {}
    }
}