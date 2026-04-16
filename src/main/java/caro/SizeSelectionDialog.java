package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SizeSelectionDialog extends JDialog {
    private int selectedSize = -1;

    public SizeSelectionDialog(JFrame parent) {
        super(parent, "Chọn kích thước", true);
        setUndecorated(true);
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 45, 30), 0, getHeight(), new Color(40, 25, 15));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Viền vàng đồng
                g2.setColor(new Color(170, 110, 45));
                g2.setStroke(new BasicStroke(5));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 30, 30);
                g2.dispose();
            }
        };
        contentPanel.setLayout(null);
        setContentPane(contentPanel);

        // Tiêu đề hướng dẫn
        JLabel lblTitle = new JLabel("CHỌN KÍCH THƯỚC BÀN CỜ", SwingConstants.CENTER);
        lblTitle.setForeground(new Color(255, 225, 120));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBounds(0, 40, 450, 40);
        contentPanel.add(lblTitle);

        // Nút chọn 3x3
        JButton btn3x3 = createStyledButton("3x3 (TIC-TAC-TOE)", 75, 110, 300, 50);
        btn3x3.addActionListener(e -> {
            selectedSize = 3;
            dispose();
        });
        contentPanel.add(btn3x3);

        // Nút chọn 15x15
        JButton btn15x15 = createStyledButton("15x15 (CARO PRO)", 75, 175, 300, 50);
        btn15x15.addActionListener(e -> {
            selectedSize = 15;
            dispose();
        });
        contentPanel.add(btn15x15);
        
        // Nút đóng (X) nhỏ ở góc
        JButton btnClose = new JButton("X");
        btnClose.setBounds(410, 10, 30, 30);
        btnClose.setForeground(Color.WHITE);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setFont(new Font("Arial", Font.BOLD, 16));
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose);
    }

    private JButton createStyledButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean hovered = getModel().isRollover();
                Color cTop = hovered ? new Color(255, 225, 120) : new Color(170, 110, 45);
                Color cBottom = hovered ? new Color(210, 130, 35) : new Color(110, 65, 15);
                
                g2.setPaint(new GradientPaint(0, 0, cTop, 0, h, cBottom));
                g2.fillRoundRect(0, 0, w, h, 15, 15);
                
                g2.setColor(new Color(50, 25, 5));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, w-1, h-1, 15, 15);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
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

    public int getSelectedSize() {
        return selectedSize;
    }
}
