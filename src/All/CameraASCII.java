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
    // frame compartilhado entre threads (visibilidade garantida)
    static volatile BufferedImage frame;
    static volatile BufferedImage grayImage;
    static volatile BufferedImage ASCIIImage;
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
    Image icon = Toolkit.getDefaultToolkit().getImage(Window.class.getResource("/A.png"));
    Window(){
        setTitle("Camera ASCII");
        // setSize(1280, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CameraASCII.running = false;
                if (CameraASCII.webcam != null && CameraASCII.webcam.isOpen()) {
                    CameraASCII.webcam.close(); // fecha a webcam
                }
            }
        });
        setIconImage(icon);
        setBackground(Color.BLACK);
        ImagePanel image = new ImagePanel();
        add(image);
        // reseta a imagem se travar
        new javax.swing.Timer(16, e -> repaint()).start();
        // centraliza a pagina
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
class ImagePanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage img = CameraASCII.ASCIIImage;
        if (img != null) {
            g.drawImage(img, 0, 0, this);
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

    static String littleLighting = " .=10?%#$&@";
    static String mediumLighting = "  .=10?#@";
    static String muchLighting = "   .=10?#";
    @Override
    public void run() {
        while (CameraASCII.running) {
            BufferedImage image = CameraASCII.grayImage;
            if (image != null) {
                CameraASCII.ASCIIImage = convertGrayToASCII(image,190,90);
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
        final String ASCII = mediumLighting; // <------- Caracteres

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