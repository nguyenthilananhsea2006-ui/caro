package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.File;

public class Main extends JFrame {
    private BufferedImage backgroundImage;

    public Main() {
        initFrame();
        loadResource();
        setupContent();
    }

    private void initFrame() {
        setTitle("Cờ Caro - Menu");
        setSize(750, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void setupContent() {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground(g);
            }
        };
        backgroundPanel.setLayout(null);
        setContentPane(backgroundPanel);
        addMenuButtons(backgroundPanel);
    }

    private void addMenuButtons(JPanel panel) {
        int bWidth = 280, bHeight = 60;
        int startX = (750 - bWidth) / 2;

        panel.add(createCustomButton("PLAYER VS PLAYER", startX, 350, bWidth, bHeight, false));
        panel.add(createCustomButton("PLAYER VS AI", startX, 425, bWidth, bHeight, true));
        panel.add(createCustomButton("EXIT GAME", startX, 500, bWidth, bHeight, false));
    }

    private JButton createCustomButton(String text, int x, int y, int w, int h, boolean vsAI) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                boolean isHovered = getModel().isRollover();
                int yOffset = isHovered ? -2 : 0; // Nhích lên nhẹ khi hover

                // 1. Đổ bóng nút (Shadow)
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRoundRect(3, 7, w, h, 12, 12);

                // 2. Màu sắc Gradient cho Nút (Theo mã màu bạn yêu cầu)
                Color colorTop = isHovered ? new Color(255, 225, 120) : new Color(170, 110, 45);
                Color colorBottom = isHovered ? new Color(210, 130, 35) : new Color(110, 65, 15);
                
                GradientPaint gp = new GradientPaint(0, yOffset, colorTop, 0, h + yOffset, colorBottom);
                g2.setPaint(gp);
                g2.fillRoundRect(0, yOffset, w, h, 12, 12);

                // 3. Viền nút sắc nét
                g2.setStroke(new BasicStroke(2.5f));
                g2.setColor(new Color(50, 25, 5));
                g2.drawRoundRect(0, yOffset, w, h, 12, 12);

                // 4. Vẽ chữ (In đậm, Trắng)
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (w - fm.stringWidth(getText())) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2 + yOffset;
                g2.drawString(getText(), textX, textY);
                
                g2.dispose();
            }
        };

        btn.setBounds(x, y, w + 10, h + 10); // Thêm khoảng trống cho bóng đổ
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Sự kiện click
        btn.addActionListener(e -> {
            if (text.equals("EXIT GAME")) System.exit(0);
            else startNewGame(vsAI);
        });

        return btn;
    }

   private void startNewGame(boolean vsAI) {
    String title = vsAI ? "CHẾ ĐỘ VS MÁY" : "CHẾ ĐỘ 2 NGƯỜI";
    
    // Tạo dialog tùy chỉnh cho Player 1
    NameInputDialog dialog1 = new NameInputDialog(this, title, "Nhập tên Người chơi 1 (X):");
    dialog1.setVisible(true);
    String p1 = dialog1.getInputName();
    if (p1 == null || p1.trim().isEmpty()) return;

    String p2 = "AI Máy";
    if (!vsAI) {
        // Tạo dialog tùy chỉnh cho Player 2
        NameInputDialog dialog2 = new NameInputDialog(this, title, "Nhập tên Người chơi 2 (O):");
        dialog2.setVisible(true);
        p2 = dialog2.getInputName();
        if (p2 == null || p2.trim().isEmpty()) return;
    }

    new Caro(vsAI, p1, p2).setVisible(true);
    this.dispose();
}

    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, 750, 750, this);
        } else {
            g2d.setPaint(new GradientPaint(0, 0, new Color(45, 30, 20), 0, 750, new Color(15, 10, 5)));
            g2d.fillRect(0, 0, 750, 750);
        }
    }

    private void loadResource() {
        try {
            InputStream is = getClass().getResourceAsStream("/res.tiles/menu.png");
            if (is != null) backgroundImage = ImageIO.read(is);
            else {
                File file = new File("src/main/java/res.tiles/menu.png");
                if (file.exists()) backgroundImage = ImageIO.read(file);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}