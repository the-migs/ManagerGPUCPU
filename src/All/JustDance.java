package All;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JustDance{

    public static void metodJustDance() {

        javax.swing.SwingUtilities.invokeLater(() -> {
            URL URLGif = JustDance.class.getResource("/skeleton.gif");
            ImageIcon GIF = new ImageIcon(URLGif);

            JFrame frame = new JFrame("Just Dance");
            frame.setSize(600, 600);

            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().setBackground(Color.BLACK);
            JLabel label = new JLabel(GIF);
            label.setOpaque(false);

            frame.add(label);
            Music player = new Music();
            player.playMusic();
            frame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    player.stopMusic();
                }
            });

            frame.setVisible(true);
        });
    }
}
class Music {
    private Clip clip;

    public void playMusic() {
        try {
            URL URLSound = JustDance.class.getResource("/sound.wav");
            if (URLSound == null) {
                return;
            }
            AudioInputStream sound = AudioSystem.getAudioInputStream(URLSound);
            clip = AudioSystem.getClip();
            clip.open(sound);
            clip.loop(clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void stopMusic() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
