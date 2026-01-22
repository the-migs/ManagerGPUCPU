package All;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// esse codigo vai ser rico em comentarios pois estou aprendendo a usar o "Swing" e as bibliotecas relacionadas

public class ClassWindow {
    static void metodCenter(){
        // define uma propriedade do sistema Java. Uma configuracao global
        // tira o headless e ativa o headful. Ativa o GUI
        System.setProperty("java.awt.headless", "false");

        //verifica se a GUI nao ta disponivel
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.err.println("Ambiente sem suporte gráfico (headless)");
            return;
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            // cria a janela principal
            new AppWindow();
        });
    }
}
class AppWindow extends JFrame {
    private Timer cpuTimer, gpuTimer;
    AppWindow() {
        //fechar e parar a execucao ao clicar no X so dessa pagina
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //sempre no topo, acima das janelas mesmo se abrir novas
        setAlwaysOnTop(true);
        //tamanho da tela principal
        setSize(500, 100);

        // adiciona o icone da barra da janela do app (no lado esquerdo)
        Image icon = Toolkit.getDefaultToolkit().getImage(AppWindow.class.getResource("/A.png"));
        setIconImage(icon);

        //faz abrir sempre no centro
        // setLocationRelativeTo(null);

        // tera 1 linha e 2 colinas
        // 0 espaço vertical e horizontal
        JPanel container = new JPanel(new GridLayout(1,2,0,0));
        container.setOpaque(false);
        // cor de fundo
        getContentPane().setBackground(Color.DARK_GRAY);
        // tamanho da tela principal
        setSize(500, 100);
        setLayout(new BorderLayout());

        WestPanel west = new WestPanel();
        LestPanel lest = new LestPanel();
        // pega os timers dos painéis
        cpuTimer = west.getTimer();
        gpuTimer = lest.getTimer();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // para os timers quando a janela fecha
                if (cpuTimer != null) cpuTimer.stop();
                if (gpuTimer != null) gpuTimer.stop();
                System.out.println("Timers parados, janela fechada.");
            }
        });
        // puxa os construtores dos dois paineis (direito/esquerdo)
        // adiciona eles no containel
        container.add(west);
        container.add(lest);
        //adiciona o container na tela inicial
        add(container);
        setVisible(true);
    }
}
class WestPanel extends JPanel {
    private Timer timer;
    // configuracao do painel esquerdo
    WestPanel() {
        // torna o painel transparente, passando a responsabilidade
        setOpaque(false);

        // define as dimensoes do painel
        setPreferredSize(new Dimension(250, 60));

        // cria a linha visual (cor e espessura)
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // tipo de Layout
        setLayout(new BorderLayout());

        // configurando o painel do painel, divisao de cima pra parte da CPU
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        // define as dimensoes do painel do topo, onde fica "CPU"
        top.setPreferredSize(new Dimension(120,25));
        // createMatteBorder -> borda ajustavel
        // borda do painel do topo
        // | 1 = borda em cima
        // | 0 = sem borda na esquerda
        // | 0 = sem borda na esquerda
        // | 1 = com borda em baixo (meio)
        // | 0 = sem borda na direita
        // cor preta
        top.setBorder(BorderFactory.createMatteBorder(1,0,1,0,Color.BLACK));

        // criando o texto do topo
        JLabel text = new JLabel("CPU");
        text.setForeground(Color.CYAN);
        text.setBorder(BorderFactory.createLineBorder(new Color(205,0,255)));

        // configura o texto/titulo de dentro do top
        top.add(text, BorderLayout.CENTER);
        top.setOpaque(false);

        // adicionando o bloco do topo que contem a CPU
        add(top, BorderLayout.NORTH);

        // configurando posicao do texto
        text.setHorizontalAlignment(JLabel.CENTER);
        text.setVerticalAlignment(JLabel.CENTER);

        // criando o texto da porcetagem de uso
        JLabel textTwo = new JLabel(">");

        // muda o texto da porcetagem de uso a cyano
        textTwo.setForeground(Color.CYAN);

        // adiciona o layout do texto no meio do painel de baixo
        add(textTwo, BorderLayout.CENTER);

        // centraliza o texto
        textTwo.setVerticalAlignment(JLabel.CENTER);
        textTwo.setHorizontalAlignment(JLabel.CENTER);

        // timer para resetar a % de uso da CPU
        timer = new Timer(650, e -> {
            textTwo.setText(CPUGPU.getCPU());
            revalidate();
            repaint();
        });
        timer.start();
    }
    public Timer getTimer() { return timer; }
}
class LestPanel extends JPanel {
    private Timer timer;
    // configuracao do painel direito
    LestPanel(){
        setOpaque(false);

        setPreferredSize(new Dimension(250, 60));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        setLayout(new BorderLayout());

        JLabel textTwo = new JLabel("<");
        textTwo.setForeground(Color.CYAN);
        textTwo.setHorizontalAlignment(JLabel.CENTER);
        textTwo.setVerticalAlignment(JLabel.CENTER);
        add(textTwo, BorderLayout.CENTER);

        JPanel top = new JPanel();
        top.setOpaque(false);

        top.setPreferredSize(new Dimension(120,25));
        top.setBorder(BorderFactory.createMatteBorder(1,0,1,0,Color.BLACK));
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        JLabel text = new JLabel("GPU");
        text.setForeground(Color.CYAN);
        text.setBorder(BorderFactory.createLineBorder(new Color(205,0,255)));
        text.setHorizontalAlignment(JLabel.CENTER);
        text.setVerticalAlignment(JLabel.CENTER);

        top.add(text, BorderLayout.CENTER);

        timer = new Timer(650, e ->{
            textTwo.setText(CPUGPU.getGPU());
        });
        timer.start();
    }
    public Timer getTimer() { return timer; }
}