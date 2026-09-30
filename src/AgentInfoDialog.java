import javax.swing.*;
import java.awt.*;

public final class AgentInfoDialog {

    /*
     * Класс служебный —
     * объект создавать не требуется.
     */
    private AgentInfoDialog() {
    }

    /*
     * ==========================================
     * ПОКАЗ ИНФОРМАЦИИ
     * ==========================================
     */

    public static void show(Component parent, Agent agent) {

        if (agent == null) {
            return;
        }

        /*
         * Определяем название вида.
         */
        String type = getAgentType(agent);

        /*
         * Формируем текст.
         */
        String message = "<html>" + "<div style='font-size:13px'>"

                + "<b>Тип:</b> " + type

                + "<br><br>"

                + "<b>Emoji:</b> " + agent.getEmoji()

                + "<br>"

                + "<b>Энергия:</b> " + agent.getEnergy() + " / " + agent.getMaxEnergy()

                + "<br>"

                + "<b>Координаты:</b> (" + agent.getX() + ", " + agent.getY() + ")"

                + "</div>" + "</html>";

        /*
         * Показываем информацию.
         */
        JOptionPane.showMessageDialog(parent,

                message,

                "Информация о существе",

                JOptionPane.INFORMATION_MESSAGE);
    }

    /*
     * ==========================================
     * ТИП АГЕНТА
     * ==========================================
     */

    private static String getAgentType(Agent agent) {

        if (agent instanceof Plant) {

            return "Растение";
        }

        if (agent instanceof Herbivore) {

            return "Травоядное";
        }

        if (agent instanceof Predator) {

            return "Хищник";
        }

        return "Неизвестно";
    }

    /*
     * ==========================================
     * ЗАЩИТНЫЙ ВСПОМОГАТЕЛЬНЫЙ МЕТОД
     * ==========================================
     *
     * Может использоваться позже,
     * когда понадобятся более подробные
     * сведения о конкретном классе.
     */

    public static String createText(Agent agent) {

        if (agent == null) {
            return "";
        }

        return "Тип: " + getAgentType(agent)

                + "\n" + "Энергия: " + agent.getEnergy() + "/" + agent.getMaxEnergy()

                + "\n" + "Координаты: (" + agent.getX() + ", " + agent.getY() + ")"

                + "\n" + "Emoji: " + agent.getEmoji();
    }
}