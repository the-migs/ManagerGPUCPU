package All;
import com.github.sarxos.webcam.Webcam;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class CameraASCII {
    // cria uma fonte monoespaçada
    // todos ocupam exatamente o mesmo espaço horizontal
    static Font font = new Font("Monospaced", Font.PLAIN, 12);
    static Webcam webcam;
    static boolean running = true;
    static Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    // frame compartilhado entre threads (visibilidade garantida)
    static volatile BufferedImage frame;
    static volatile BufferedImage grayImage;
    static volatile BufferedImage ASCIIImage;

    static volatile String characterValue = "  .=10?XY";
    static volatile String almostNoLightingString = " .=10!?XY";
    static volatile String littleLightingString = "  .=10?LYX";
    static volatile String considerableLightingString = "  .=10?XY";
    static volatile String mediumLightingString = "  .=10?X";
    static volatile String muchLightingString = "  .=10?";
    static volatile String extremeLightingString = "   .=10?";

    static void metodSuprem() {
        System.out.println("metodSuprem");

        CameraASCII.running = true; // garante que as threads rodem
        if (CameraASCII.webcam != null && CameraASCII.webcam.isOpen()) {
            CameraASCII.webcam.close(); // fecha qualquer webcam aberta
        }
        // pega a camera padrao do sistema
        webcam = Webcam.getDefault();
        Dimension best = webcam.getViewSizes()[0];
        for (Dimension d : webcam.getViewSizes()) {
            if (d.width * d.height > best.width * best.height) {
                best = d;
            }
        }
        // define as dimensoes
        webcam.setViewSize(best);
        // abre a camera
        webcam.open();
        // inicia threads
        new CaptureThread().start();
        new ConvertGrayThread().start();
        new ConvertImageToASCII().start();
        SwingUtilities.invokeLater(Window::new);
    }
}
class Window extends JFrame {
    static ImagePanel imagePanel;
    static JSplitPane split;
    private Timer repaintTimer;
    Image icon = Toolkit.getDefaultToolkit().getImage(Window.class.getResource("/A.png"));
    Window(){
        setTitle("Camera ASCII");
        // setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CameraASCII.running = false;
                if (repaintTimer != null) {
                    repaintTimer.stop();
                }
                if (CameraASCII.webcam != null && CameraASCII.webcam.isOpen()) {
                    CameraASCII.webcam.close();
                }
            }
        });

        setIconImage(icon);
        setBackground(Color.BLACK);

        imagePanel = new ImagePanel();
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createLineBorder(Color.CYAN, 4));
        JLabel title = new JLabel("> Lighting configuration <");
        title.setForeground(Color.CYAN);

        title.setFont(new Font("SansSerif", Font.BOLD, 60));
        title.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));


        JButton almostNoButton = new JButton("Almost No lighting");
        almostNoButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        almostNoButton.setFocusPainted(false);
        almostNoButton.setPreferredSize(new Dimension(455, 55)); // quadrado 120x120
        almostNoButton.setMinimumSize(new Dimension(455, 55));
        almostNoButton.setMaximumSize(new Dimension(455, 55));

        JButton littleButton = new JButton("Little Lighting");
        littleButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        littleButton.setFocusPainted(false);
        littleButton.setPreferredSize(new Dimension(455, 55));
        littleButton.setMinimumSize(new Dimension(455, 55));
        littleButton.setMaximumSize(new Dimension(455, 55));

        JButton considerableButton = new JButton("Considerable Lighting");
        considerableButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        considerableButton.setFocusable(false);
        considerableButton.setPreferredSize(new Dimension(455, 55));
        considerableButton.setMinimumSize(new Dimension(455, 55));
        considerableButton.setMaximumSize(new Dimension(455, 55));

        JButton mediumButton = new JButton("Medium Lighting");
        mediumButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        mediumButton.setFocusable(false);
        mediumButton.setPreferredSize(new Dimension(455, 55));
        mediumButton.setMinimumSize(new Dimension(455, 55));
        mediumButton.setMaximumSize(new Dimension(455, 55));

        JButton muchButton = new JButton("Much Lighting");
        muchButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        muchButton.setFocusable(false);
        muchButton.setPreferredSize(new Dimension(455, 55));
        muchButton.setMinimumSize(new Dimension(455, 55));
        muchButton.setMaximumSize(new Dimension(455, 55));

        JButton extremeButton = new JButton("Extreme Lighting");
        extremeButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        extremeButton.setFocusable(false);
        extremeButton.setPreferredSize(new Dimension(455, 55));
        extremeButton.setMinimumSize(new Dimension(455, 55));
        extremeButton.setMaximumSize(new Dimension(455, 55));

        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        almostNoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        littleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        considerableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mediumButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        muchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        extremeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        almostNoButton.addActionListener(e -> {
            CameraASCII.characterValue = CameraASCII.almostNoLightingString;
        });

        littleButton.addActionListener(e -> {
            CameraASCII.characterValue = CameraASCII.littleLightingString;
        });

        considerableButton.addActionListener(e -> {
            CameraASCII.characterValue = CameraASCII.considerableLightingString;
        });

        mediumButton.addActionListener(e -> {
           CameraASCII.characterValue = CameraASCII.mediumLightingString;
        });

        muchButton.addActionListener(e -> {
           CameraASCII.characterValue = CameraASCII.muchLightingString;
        });

        extremeButton.addActionListener(e -> {
            CameraASCII.characterValue = CameraASCII.extremeLightingString;
        });

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,imagePanel,controls);

        imagePanel.setBackground(Color.BLACK);

        setLayout(new BorderLayout());
        add(split, BorderLayout.CENTER);
        split.setResizeWeight(0.5); // 50% / 50%
        split.setDividerSize(0);    // sem barra visível
        controls.setBackground(Color.WHITE);

        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(title);
        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(almostNoButton);
        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(littleButton);
        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(considerableButton);
        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(mediumButton);
        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(muchButton);
        controls.add(Box.createRigidArea(new Dimension(0, 30)));
        controls.add(extremeButton);

        // reseta a imagem se travar
        repaintTimer = new Timer(16, e -> imagePanel.repaint());
        repaintTimer.start();
        // define um tamanho pra quando a pessoa tirar da tela cheia
        setSize(1200, 800);
        // faz abrir em tela cheia
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        SwingUtilities.invokeLater(() -> {
            BufferedImage img = CameraASCII.ASCIIImage;
            if (img != null) {
                split.setDividerLocation(img.getWidth());
            }
        });

        setVisible(true);
    }
}
class ImagePanel extends JPanel {
    @Override
    public Dimension getPreferredSize() {
        BufferedImage img = CameraASCII.ASCIIImage;
        if (img != null) {
            return new Dimension(img.getWidth(), img.getHeight());
        }
        return new Dimension(800, 600); // fallback
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage img = CameraASCII.ASCIIImage;
        if (img != null) {
            g.drawImage(img, 0, 0, null);
        }
    }
}
// mantem o frame atualizado (variavel global)
class CaptureThread extends Thread {
    @Override
    public void run() {
        while (CameraASCII.running) {
            // captura um frame atual da camera
            BufferedImage img = CameraASCII.webcam.getImage();

            if (img != null) {
                // atualiza a variavel global dos frames
                CameraASCII.frame = img;
            }
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
// converte o frame para cinza
class ConvertGrayThread extends Thread {
    @Override
    public void run() {
        while (CameraASCII.running) {
            BufferedImage img = CameraASCII.frame;

            if (img != null) {
                // converte o frame para cinza
                CameraASCII.grayImage = convertToGrayscale(img);

                // DEBUG: prova que o pipeline está funcionando
                System.out.println("Frame convertido: " + CameraASCII.grayImage.getWidth() + "x" + CameraASCII.grayImage.getHeight());
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    // Conversão manual RGB -> Grayscale
    static BufferedImage convertToGrayscale(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        BufferedImage gray = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_BYTE_GRAY
        );
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int rgb = source.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int lum = (r + g + b) / 3;

                int grayRgb = (0xFF << 24) | (lum << 16) | (lum << 8) | lum;

                gray.setRGB(x, y, grayRgb);
            }
        }
        return gray;
    }
}
class ConvertImageToASCII extends Thread {
    @Override
    public void run() {
        while (CameraASCII.running) {
            BufferedImage image = CameraASCII.grayImage;
            if (image != null) {
                CameraASCII.ASCIIImage = convertGrayToASCII(image,158,80);
                SwingUtilities.invokeLater(() -> {
                        Window.imagePanel.revalidate();
                    Window.imagePanel.repaint();
                    Window.split.setDividerLocation(CameraASCII.ASCIIImage.getWidth());
                });
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    // metodo responsavel por converter a imagem em ASCII
    public static BufferedImage convertGrayToASCII(BufferedImage grayImage, int cols, int rows) {

        // sequência ASCII do mais escuro para o mais claro
        final String ASCII = CameraASCII.characterValue; // <------- Caracteres

        // largura da imagem de entrada
        int imgW = grayImage.getWidth();
        // altura da imagem de entrada
        int imgH = grayImage.getHeight();
        // criar um Graphics temporário só para medir a fonte
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D gTmp = tmp.createGraphics();
        gTmp.setFont(CameraASCII.font);
        FontMetrics fm = gTmp.getFontMetrics();

        // tamanho real de cada caractere
        int charW = fm.charWidth('A');
        int charH = fm.getHeight();
        int charAscent = fm.getAscent();
        gTmp.dispose();

        // calcular o tamanho REAL da imagem ASCII
        int outW = cols * charW;
        int outH = rows * charH;
        // cria a imagem de saída no tamanho correto
        BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        // fundo preto
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, outW, outH);
        // aplica fonte e cor
        g.setFont(CameraASCII.font);
        g.setColor(Color.WHITE);
        // tamanho do bloco de pixels da imagem original
        int stepX = imgW / cols;
        int stepY = imgH / rows;
        // percorre a grade de caracteres
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // pixel correspondente na imagem original
                int px = x * stepX;
                int py = y * stepY;
                if (px >= imgW || py >= imgH) continue;
                // lê luminosidade
                int rgb = grayImage.getRGB(px, py);
                int lum = rgb & 0xFF;
                // mapeia para caractere ASCII
                int index = (lum * (ASCII.length() - 1)) / 255;
                char c = ASCII.charAt(index);
                // posição do caractere na imagem ASCII
                int drawX = x * charW;
                int drawY = y * charH + charAscent;
                // desenha o caractere
                g.drawString(String.valueOf(c), drawX, drawY);
            }
        }
        // libera recursos
        g.dispose();
        return out;
    }
}