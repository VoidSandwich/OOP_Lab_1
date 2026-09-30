import javax.swing.*;
import java.awt.*;

public final class StatisticsDialog {

    /*
     * Служебный класс.
     */
    private StatisticsDialog() {
    }

    /*
     * ==========================================
     * ПОКАЗ СТАТИСТИКИ
     * ==========================================
     */

    public static void show(Component parent, Environment env) {

        if (env == null) {
            return;
        }

        /*
         * Создаём текст статистики.
         */
        String message = "<html>" + "<div style='font-size:13px'>"

                + "<div style='font-size:16px'>" + "<b>ИТОГОВАЯ СТАТИСТИКА</b>" + "</div>"

                + "<br>"

                + "<b>Продолжительность:</b> " + env.getStep() + " шагов"

                + "<br><br>"

                + "<span style='color:green'>" + "🌿 Растений:" + "</span> " + env.getPlantCount()

                + "<br>"

                + "<span style='color:blue'>" + "🐇 Травоядных:" + "</span> " + env.getHerbivoreCount()

                + "<br>"

                + "<span style='color:red'>" + "🐺 Хищников:" + "</span> " + env.getPredatorCount()

                + "<br><br>"

                + "<b>Всего сущностей:</b> " + env.getAgentCount()

                + "</div>" + "</html>";

        /*
         * Показываем диалог.
         */
        JOptionPane.showMessageDialog(parent,

                message,

                "Статистика",

                JOptionPane.INFORMATION_MESSAGE);
    }

    /*
     * ==========================================
     * ПОЛУЧЕНИЕ ТЕКСТА СТАТИСТИКИ
     * ==========================================
     *
     * Полезно, если потом захотим
     * вынести статистику в отдельное
     * большое окно.
     */

    public static String createText(Environment env) {

        if (env == null) {
            return "";
        }

        return "ИТОГОВАЯ СТАТИСТИКА\n\n"

                + "Продолжительность: " + env.getStep() + " шагов\n"

                + "🌿 Растений: " + env.getPlantCount() + "\n"

                + "🐇 Травоядных: " + env.getHerbivoreCount() + "\n"

                + "🐺 Хищников: " + env.getPredatorCount() + "\n"

                + "Всего сущностей: " + env.getAgentCount();
    }
}