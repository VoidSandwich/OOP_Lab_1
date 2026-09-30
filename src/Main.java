import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        /*
         * Все Swing-компоненты запускаем
         * в Event Dispatch Thread.
         */
        SwingUtilities.invokeLater(
                Main::showSettings
        );
    }

    /*
     * Открытие стартового окна.
     */
    public static void showSettings() {

        SettingsFrame frame =
                new SettingsFrame();

        frame.setVisible(true);
    }
}